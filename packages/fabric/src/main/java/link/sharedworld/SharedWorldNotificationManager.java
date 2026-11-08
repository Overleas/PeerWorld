package link.sharedworld;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import link.sharedworld.versioned.ClientCompat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;

/**
 * Manages in-game notifications and toasts for PeerWorld events:
 * - Player join notifications (with 2.5s stabilization window on world load)
 * - Cloud auto-backup completion toast
 * - Host migration toast (self became host or new host appointed)
 */
public final class SharedWorldNotificationManager {
    private static final Set<UUID> knownPlayerUuids = new HashSet<>();
    private static long joinedWorldAt = 0L;
    private static String lastConnectedWorldId = null;

    private SharedWorldNotificationManager() {
    }

    public static void tick(Minecraft client) {
        if (client == null || client.level == null || client.getConnection() == null) {
            reset();
            return;
        }

        // Only active when playing in a SharedWorld (either as host or guest)
        boolean isHosting = SharedWorldDevSessionBridge.isHostingSharedWorld();
        SharedWorldPlaySessionTracker.ActiveWorldSession session = SharedWorldClient.playSessionTracker().currentSession();
        boolean isGuest = session != null && session.role() == SharedWorldPlaySessionTracker.SessionRole.GUEST;

        if (!isHosting && !isGuest) {
            reset();
            return;
        }

        String currentWorldId = isHosting ? "host" : session.worldId();
        if (!Objects.equals(currentWorldId, lastConnectedWorldId)) {
            reset();
            lastConnectedWorldId = currentWorldId;
            joinedWorldAt = System.currentTimeMillis();
        }

        long now = System.currentTimeMillis();
        boolean initialStabilization = (now - joinedWorldAt) < 2500L;

        UUID localUuid = client.player != null ? client.player.getUUID() : null;
        Set<UUID> currentlyOnline = new HashSet<>();

        for (PlayerInfo info : client.getConnection().getOnlinePlayers()) {
            if (info == null || info.getProfile() == null) {
                continue;
            }
            UUID uuid = ClientCompat.profileId(info.getProfile());
            if (uuid == null) {
                continue;
            }
            currentlyOnline.add(uuid);

            if (!knownPlayerUuids.contains(uuid)) {
                knownPlayerUuids.add(uuid);
                // Do not notify for self, and do not notify during the initial 2.5s join burst
                if (!initialStabilization && localUuid != null && !uuid.equals(localUuid)) {
                    String name = ClientCompat.profileName(info.getProfile());
                    ClientCompat.showToast(
                            client,
                            Component.translatable("toast.peerworld.player_joined_title"),
                            Component.translatable("toast.peerworld.player_joined_description", name)
                    );
                }
            }
        }

        // Remove departed players so they trigger toast if they rejoin
        knownPlayerUuids.retainAll(currentlyOnline);
    }

    public static void notifyHostChanged(String newHostName, boolean isSelf) {
        Minecraft client = Minecraft.getInstance();
        if (client == null) {
            return;
        }
        if (isSelf) {
            ClientCompat.showToast(
                    client,
                    Component.translatable("toast.peerworld.host_title"),
                    Component.translatable("toast.peerworld.host_became_self")
            );
        } else {
            ClientCompat.showToast(
                    client,
                    Component.translatable("toast.peerworld.host_title"),
                    Component.translatable("toast.peerworld.host_assigned", newHostName != null ? newHostName : "Player")
            );
        }
    }

    public static void notifyBackupSaved() {
        notifyBackupSaved(null);
    }

    public static void notifyBackupSaved(String timeStr) {
        Minecraft client = Minecraft.getInstance();
        if (client == null) {
            return;
        }
        Component desc = (timeStr == null || timeStr.isBlank())
                ? Component.translatable("toast.peerworld.backup_description")
                : Component.translatable("toast.peerworld.backup_description_time", timeStr);
        ClientCompat.showToast(
                client,
                Component.translatable("toast.peerworld.backup_title"),
                desc
        );
    }

    public static void notifyAutoSaveSaved() {
        Minecraft client = Minecraft.getInstance();
        if (client == null) {
            return;
        }
        ClientCompat.showOverlayMessage(
                client,
                Component.translatable("peerworld.autosave.saved_actionbar")
        );
    }

    public static void reset() {
        knownPlayerUuids.clear();
        joinedWorldAt = 0L;
        lastConnectedWorldId = null;
    }
}
