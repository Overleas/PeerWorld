package link.sharedworld.screen;

import link.sharedworld.SharedWorldText;

/**
 * Formatting helpers for world metadata inputs, shared by the create wizard
 * and the edit screen (extracted from a long-dead form screen).
 */
public final class SharedWorldMetadataFormat {
    private SharedWorldMetadataFormat() {
    }

    public static String cleanMotd(String motd) {
        if (motd == null) {
            return "";
        }
        return motd.replaceAll("\\s*\u200B\\[pw:[^\\]]+\\]", "").trim();
    }

    public static String attachMetadata(String motd, int maxPlayers, String resourcePackUrl, boolean resourcePackRequired) {
        String clean = cleanMotd(motd);
        int clamped = Math.max(2, Math.min(64, (maxPlayers / 2) * 2));
        StringBuilder sb = new StringBuilder(clean);
        sb.append(" \u200B[pw:mp=").append(clamped).append("]");
        if (resourcePackUrl != null && !resourcePackUrl.isBlank()) {
            sb.append("\u200B[pw:rpu=").append(resourcePackUrl.trim()).append("]");
            if (resourcePackRequired) {
                sb.append("\u200B[pw:rpr=1]");
            }
        }
        return sb.toString();
    }

    public static String attachMaxPlayers(String motd, int maxPlayers) {
        String url = extractResourcePackUrl(motd);
        boolean req = extractResourcePackRequired(motd);
        return attachMetadata(motd, maxPlayers, url, req);
    }

    public static String attachResourcePack(String motd, String resourcePackUrl, boolean resourcePackRequired) {
        int maxPlayers = extractMaxPlayers(motd);
        return attachMetadata(motd, maxPlayers, resourcePackUrl, resourcePackRequired);
    }

    public static String extractResourcePackUrl(String motd) {
        if (motd != null) {
            java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("\u200B\\[pw:rpu=([^\\]]+)\\]").matcher(motd);
            if (matcher.find()) {
                return matcher.group(1).trim();
            }
        }
        return null;
    }

    public static boolean extractResourcePackRequired(String motd) {
        return motd != null && motd.contains("\u200B[pw:rpr=1]");
    }

    public static int extractMaxPlayers(String motd) {
        if (motd != null) {
            java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("\u200B\\[pw:mp=(\\d+)\\]").matcher(motd);
            if (matcher.find()) {
                try {
                    int parsed = Integer.parseInt(matcher.group(1));
                    return Math.max(2, Math.min(64, parsed));
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return 8;
    }

    public static String decodeMotdInput(String value) {
        if (value == null) {
            return null;
        }
        return value.replace("\\u00A7", "§").replace("\\u00a7", "§").replace("\\n", "\n").trim();
    }

    public static String effectiveMotd(String input) {
        String decoded = decodeMotdInput(input);
        return decoded == null || decoded.isBlank() ? defaultMotd() : cleanMotd(decoded);
    }

    public static String encodeMotdInput(String motd) {
        String clean = cleanMotd(motd);
        if (clean == null || clean.isBlank()) {
            return defaultMotd();
        }
        return clean.replace("§", "\\u00a7").replace("\n", "\\n");
    }

    public static String defaultMotd() {
        return SharedWorldText.defaultMotd();
    }

    public static String friendlyMessage(Throwable throwable) {
        if (throwable == null || throwable.getMessage() == null || throwable.getMessage().isBlank()) {
            return SharedWorldText.string("screen.sharedworld.error_generic");
        }
        return throwable.getMessage();
    }
}
