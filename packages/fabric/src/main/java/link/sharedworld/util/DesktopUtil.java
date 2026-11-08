package link.sharedworld.util;

import net.minecraft.client.Minecraft;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

public final class DesktopUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger("peerworld-desktop");

    private DesktopUtil() {
    }

    public static void openFolder(Path path) {
        if (path == null) {
            return;
        }
        try {
            path = path.toAbsolutePath().normalize();
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }

            // Always copy the full path to clipboard as a convenient fallback
            try {
                if (Minecraft.getInstance() != null && Minecraft.getInstance().keyboardHandler != null) {
                    Minecraft.getInstance().keyboardHandler.setClipboard(path.toString());
                }
            } catch (Throwable ignored) {
            }

            String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
            if (os.contains("win")) {
                new ProcessBuilder("explorer.exe", path.toString()).start();
                return;
            } else if (os.contains("mac")) {
                new ProcessBuilder("open", path.toString()).start();
                return;
            } else {
                // Linux: prioritize graphical file managers over terminal emulators (like kitty-open)
                String[] guiManagers = {"dolphin", "nautilus", "nemo", "thunar", "pcmanfm"};
                for (String manager : guiManagers) {
                    try {
                        Process check = new ProcessBuilder("which", manager).start();
                        if (check.waitFor() == 0) {
                            new ProcessBuilder(manager, path.toString()).start();
                            return;
                        }
                    } catch (Throwable ignored) {
                    }
                }
                // Fallback to xdg-open
                try {
                    new ProcessBuilder("xdg-open", path.toString()).start();
                    return;
                } catch (Throwable t) {
                    LOGGER.warn("xdg-open failed to launch: {}", t.getMessage());
                }
            }

            if (java.awt.Desktop.isDesktopSupported() && java.awt.Desktop.getDesktop().isSupported(java.awt.Desktop.Action.OPEN)) {
                java.awt.Desktop.getDesktop().open(path.toFile());
                return;
            }

            try {
                Class<?> blaze3D = Class.forName("com.mojang.blaze3d.Blaze3D");
                blaze3D.getMethod("openPath", Path.class).invoke(null, path);
                return;
            } catch (Throwable ignored) {
            }

            try {
                Class<?> utilClass = Class.forName("net.minecraft.Util");
                Object platform = utilClass.getMethod("getPlatform").invoke(null);
                platform.getClass().getMethod("openPath", Path.class).invoke(platform, path);
                return;
            } catch (Throwable ignored) {
            }
        } catch (Throwable throwable) {
            LOGGER.warn("Failed to open folder {}", path, throwable);
        }
    }
}
