package link.sharedworld.host;

import link.sharedworld.SharedWorldBackendConstants;
import link.sharedworld.SharedWorldClientConfigStore;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.util.concurrent.CompletableFuture;

/**
 * Evaluates host suitability based on hardware (RAM, CPU cores) and network latency.
 * Computed once at client startup without impacting game frame rates.
 */
public final class HostHardwareEvaluator {

    public enum RatingTier {
        EXCELLENT,
        GOOD,
        BASIC
    }

    public record HardwareEvaluation(
            int score,
            long totalRamBytes,
            int ramGb,
            int cpuCores,
            int pingMs,
            RatingTier tier
    ) {
        public String specsSummary() {
            String pingText = pingMs > 0 ? (pingMs + " ms") : "—";
            return ramGb + " GB RAM • " + cpuCores + " CPU • " + pingText;
        }
    }

    private static volatile HardwareEvaluation cachedEvaluation;

    private HostHardwareEvaluator() {
    }

    public static HardwareEvaluation get() {
        HardwareEvaluation current = cachedEvaluation;
        if (current == null) {
            current = evaluateInitialSync();
            cachedEvaluation = current;
        }
        return current;
    }

    /**
     * Trigger asynchronous ping check on startup to refine initial hardware evaluation.
     */
    public static void initializeAsync() {
        get(); // ensure initial sync evaluation exists
        CompletableFuture.runAsync(() -> {
            int latency = measureNetworkLatency();
            if (latency > 0) {
                updateWithPing(latency);
            }
        });
    }

    private static int measureNetworkLatency() {
        String baseUrl = SharedWorldClientConfigStore.shared().resolvedBackendBaseUrl();
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = SharedWorldBackendConstants.DEFAULT_BASE_URL;
        }

        // 1. Measure pure TCP round-trip latency to the coordinator host
        try {
            String host = URI.create(baseUrl).getHost();
            if (host != null && !host.isBlank()) {
                InetAddress addr = InetAddress.getByName(host);
                long start = System.nanoTime();
                try (Socket s = new Socket()) {
                    s.connect(new InetSocketAddress(addr, 443), 2000);
                }
                long ms = (System.nanoTime() - start) / 1_000_000L;
                if (ms > 0 && ms < 3000) {
                    return (int) ms;
                }
            }
        } catch (Throwable ignored) {
        }

        // 2. Fallback to Anycast DNS (1.1.1.1:53) to determine baseline internet latency
        try {
            long start = System.nanoTime();
            try (Socket s = new Socket()) {
                s.connect(new InetSocketAddress("1.1.1.1", 53), 2000);
            }
            long ms = (System.nanoTime() - start) / 1_000_000L;
            if (ms > 0 && ms < 3000) {
                return (int) ms;
            }
        } catch (Throwable ignored) {
        }

        return -1;
    }

    private static HardwareEvaluation evaluateInitialSync() {
        long totalRam = resolveTotalRamBytes();
        int cpus = Runtime.getRuntime().availableProcessors();
        return computeEvaluation(totalRam, cpus, -1);
    }

    private static synchronized void updateWithPing(int pingMs) {
        HardwareEvaluation current = get();
        cachedEvaluation = computeEvaluation(current.totalRamBytes(), current.cpuCores(), pingMs);
    }

    public static int normalizeToStandardRamGb(double gib) {
        int[] standardTiers = {4, 6, 8, 12, 16, 24, 32, 48, 64, 96, 128, 256};
        for (int tier : standardTiers) {
            // Systems report slightly less RAM due to hardware reservations (VRAM, ACPI, PCIe)
            if (gib >= (tier - 1.8) && gib <= (tier + 0.6)) {
                return tier;
            }
        }
        return (int) Math.max(1, Math.round(gib));
    }

    private static HardwareEvaluation computeEvaluation(long totalRamBytes, int cpuCores, int pingMs) {
        double gib = totalRamBytes / (1024.0 * 1024.0 * 1024.0);
        int ramGb = normalizeToStandardRamGb(gib);

        // RAM score (Weight 40%)
        int ramScore;
        if (ramGb < 6) {
            ramScore = 20;
        } else if (ramGb < 8) {
            ramScore = 40;
        } else if (ramGb < 12) {
            ramScore = 60;
        } else if (ramGb < 16) {
            ramScore = 80;
        } else if (ramGb < 24) {
            ramScore = 90;
        } else {
            ramScore = 100;
        }

        // CPU score (Weight 35%)
        int cpuScore;
        if (cpuCores <= 2) {
            cpuScore = 25;
        } else if (cpuCores <= 4) {
            cpuScore = 50;
        } else if (cpuCores <= 6) {
            cpuScore = 75;
        } else if (cpuCores <= 8) {
            cpuScore = 90;
        } else {
            cpuScore = 100;
        }

        // Network latency / Ping score (Weight 25%)
        int pingScore;
        if (pingMs <= 0) {
            pingScore = 70; // neutral default when offline/not measured
        } else if (pingMs < 45) {
            pingScore = 100;
        } else if (pingMs < 90) {
            pingScore = 80;
        } else if (pingMs < 160) {
            pingScore = 50;
        } else {
            pingScore = 20;
        }

        int totalScore = (int) Math.round((ramScore * 0.40) + (cpuScore * 0.35) + (pingScore * 0.25));
        totalScore = Math.max(1, Math.min(100, totalScore));

        RatingTier tier;
        if (totalScore >= 80) {
            tier = RatingTier.EXCELLENT;
        } else if (totalScore >= 60) {
            tier = RatingTier.GOOD;
        } else {
            tier = RatingTier.BASIC;
        }

        return new HardwareEvaluation(totalScore, totalRamBytes, ramGb, cpuCores, pingMs, tier);
    }

    private static long resolveTotalRamBytes() {
        long totalRam = -1;
        try {
            java.lang.management.OperatingSystemMXBean osBean = java.lang.management.ManagementFactory.getOperatingSystemMXBean();
            if (osBean instanceof com.sun.management.OperatingSystemMXBean sunBean) {
                totalRam = sunBean.getTotalMemorySize();
            }
        } catch (Throwable ignored) {
        }

        if (totalRam <= 0) {
            try {
                oshi.SystemInfo si = new oshi.SystemInfo();
                totalRam = si.getHardware().getMemory().getTotal();
            } catch (Throwable ignored) {
            }
        }

        if (totalRam <= 0) {
            totalRam = Runtime.getRuntime().maxMemory() * 2;
        }
        return Math.max(1024L * 1024L * 1024L, totalRam);
    }
}
