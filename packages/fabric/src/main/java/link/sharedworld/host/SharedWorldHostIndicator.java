package link.sharedworld.host;

import java.util.UUID;
import link.sharedworld.SharedWorldDevSessionBridge;
import net.minecraft.client.Minecraft;

/**
 * Tracks the current active host UUID and name for HUD / Tab-list crown indicators
 * on both the hosting player and connected guest clients.
 */
public final class SharedWorldHostIndicator {
    private static volatile UUID currentHostUuid;
    private static volatile String currentHostName;

    private SharedWorldHostIndicator() {
    }

    public static void setCurrentHost(UUID uuid, String name) {
        currentHostUuid = uuid;
        currentHostName = name;
    }

    public static void setCurrentHost(String rawUuid, String name) {
        UUID parsed = null;
        if (rawUuid != null && !rawUuid.isBlank()) {
            try {
                if (rawUuid.contains("-")) {
                    parsed = UUID.fromString(rawUuid);
                } else if (rawUuid.length() == 32) {
                    parsed = UUID.fromString(
                            rawUuid.replaceFirst(
                                    "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{12})",
                                    "$1-$2-$3-$4-$5"
                            )
                    );
                }
            } catch (Exception ignored) {
            }
        }
        setCurrentHost(parsed, name);
    }

    public static UUID currentHostUuid() {
        if (SharedWorldDevSessionBridge.isHostingSharedWorld()) {
            Minecraft mc = Minecraft.getInstance();
            if (mc != null) {
                if (mc.player != null) {
                    return mc.player.getUUID();
                }
                if (mc.getUser() != null) {
                    return mc.getUser().getProfileId();
                }
            }
        }
        return currentHostUuid;
    }

    public static String currentHostName() {
        if (SharedWorldDevSessionBridge.isHostingSharedWorld()) {
            Minecraft mc = Minecraft.getInstance();
            if (mc != null) {
                if (mc.player != null) {
                    return link.sharedworld.versioned.ClientCompat.profileName(mc.player.getGameProfile());
                }
                if (mc.getUser() != null) {
                    return mc.getUser().getName();
                }
            }
        }
        return currentHostName;
    }

    public static boolean isCurrentHost(UUID playerUuid, String playerName) {
        if (playerUuid == null && (playerName == null || playerName.isBlank())) {
            return false;
        }

        // If local client is hosting, check if the queried player is the local host
        if (SharedWorldDevSessionBridge.isHostingSharedWorld()) {
            Minecraft mc = Minecraft.getInstance();
            if (mc != null) {
                if (mc.player != null) {
                    if (playerUuid != null && playerUuid.equals(mc.player.getUUID())) {
                        return true;
                    }
                    if (playerName != null && playerName.equalsIgnoreCase(link.sharedworld.versioned.ClientCompat.profileName(mc.player.getGameProfile()))) {
                        return true;
                    }
                }
                if (mc.getUser() != null) {
                    if (playerUuid != null && playerUuid.equals(mc.getUser().getProfileId())) {
                        return true;
                    }
                    if (playerName != null && playerName.equalsIgnoreCase(mc.getUser().getName())) {
                        return true;
                    }
                }
            }
            return false;
        }

        // Client is guest connected to a SharedWorld
        UUID hostUuid = currentHostUuid;
        if (hostUuid != null && playerUuid != null && hostUuid.equals(playerUuid)) {
            return true;
        }
        String hostName = currentHostName;
        if (hostName != null && playerName != null && hostName.equalsIgnoreCase(playerName)) {
            return true;
        }

        return false;
    }

    public static void clear() {
        currentHostUuid = null;
        currentHostName = null;
    }
}
