package link.sharedworld.versioned;

import net.minecraft.client.Minecraft;

/**
 * Version-specific client entry points for Minecraft 1.21.6-1.21.8: the session service
 * still hangs directly off Minecraft, leaving a world goes through the saving-screen
 * disconnect, and subtitles are not deferred through screens at all.
 */
public final class ClientCompat {
    private ClientCompat() {
    }

    public static void disconnectFromWorld(Minecraft minecraft) {
        // The 1.20.1 disconnect mixin only fires for pause-screen quits; programmatic
        // disconnects must fire the hook here so the release pass-through is consumed,
        // matching the newer versions where the wrapper method always runs the hook.
        if (!(minecraft.screen instanceof net.minecraft.client.gui.screens.PauseScreen)) {
            link.sharedworld.SharedWorldDisconnectHook.onDisconnect(minecraft);
        }
        minecraft.clearLevel();
    }

    public static void drawDeferredSubtitles(Minecraft minecraft) {
    }

    /**
     * The Mojang-signed profile keypair (chat-signing certificate) backing
     * SharedWorld certificate auth, or empty when the account has none
     * (offline profile, certificate-blocking mods).
     */
    public static java.util.Optional<link.sharedworld.api.ProfileCertificateData> profileCertificate(Minecraft minecraft)
            throws InterruptedException, java.util.concurrent.ExecutionException, java.util.concurrent.TimeoutException {
        return minecraft.getProfileKeyPairManager().prepareKeyPair()
                .get(10, java.util.concurrent.TimeUnit.SECONDS)
                .map(pair -> new link.sharedworld.api.ProfileCertificateData(
                        pair.privateKey(),
                        pair.publicKey().data().key().getEncoded(),
                        pair.publicKey().data().expiresAt().toEpochMilli(),
                        pair.publicKey().data().keySignature()));
    }

    public static java.util.UUID profileId(com.mojang.authlib.GameProfile profile) {
        return profile.getId();
    }

    public static String profileName(com.mojang.authlib.GameProfile profile) {
        return profile.getName();
    }

    /** The running client's world data version, or a permissive maximum when undetectable. */
    public static int currentDataVersion() {
        try {
            net.minecraft.WorldVersion version = net.minecraft.SharedConstants.getCurrentVersion();
            return version == null ? Integer.MAX_VALUE : version.getDataVersion().getVersion();
        } catch (RuntimeException | LinkageError exception) {
            // Headless/undetected version: never block on an unknowable comparison.
            return Integer.MAX_VALUE;
        }
    }

    /** The running client's Minecraft version name (for example "1.21.1"), or null. */
    public static String currentMinecraftVersion() {
        try {
            net.minecraft.WorldVersion version = net.minecraft.SharedConstants.getCurrentVersion();
            return version == null ? null : version.getName();
        } catch (RuntimeException | LinkageError exception) {
            return null;
        }
    }

    /** Show a screen; screen management moved off Minecraft in newer versions. */
    public static void setScreen(Minecraft minecraft, net.minecraft.client.gui.screens.Screen screen) {
        minecraft.setScreen(screen);
    }

    /** The currently displayed screen, or null when none is open. */
    public static net.minecraft.client.gui.screens.Screen currentScreen(Minecraft minecraft) {
        return minecraft.screen;
    }

    /**
     * Whether the screen belongs to the vanilla world-entry flow (world selection/creation,
     * connecting, level loading). SharedWorld lifecycle screens must never be forced over these.
     */
    public static boolean isWorldEntryScreen(net.minecraft.client.gui.screens.Screen screen) {
        return screen instanceof net.minecraft.client.gui.screens.worldselection.SelectWorldScreen
            || screen instanceof net.minecraft.client.gui.screens.worldselection.CreateWorldScreen
            || screen instanceof net.minecraft.client.gui.screens.ConnectScreen
            || screen instanceof net.minecraft.client.gui.screens.LevelLoadingScreen
            || screen instanceof net.minecraft.client.gui.screens.ProgressScreen
            || screen instanceof net.minecraft.client.gui.screens.GenericDirtMessageScreen
            || screen instanceof net.minecraft.client.gui.screens.ReceivingLevelScreen;
    }

    /** Show a system chat line on the local client (autosave health warnings). */
    public static void showChatMessage(Minecraft minecraft, net.minecraft.network.chat.Component message) {
        if (minecraft.player != null) {
            minecraft.player.displayClientMessage(message, false);
        }
    }

    /** Show a system toast on the local client (migration, backup, player join notifications). */
    public static void showToast(Minecraft minecraft, net.minecraft.network.chat.Component title, net.minecraft.network.chat.Component message) {
        if (minecraft != null && minecraft.getToasts() != null) {
            net.minecraft.client.gui.components.toasts.SystemToast.add(
                    minecraft.getToasts(),
                    net.minecraft.client.gui.components.toasts.SystemToast.SystemToastIds.PERIODIC_NOTIFICATION,
                    title,
                    message
            );
        }
    }

    /** Show an actionbar overlay message above the hotbar. */
    public static void showOverlayMessage(Minecraft minecraft, net.minecraft.network.chat.Component message) {
        if (minecraft != null && minecraft.player != null) {
            minecraft.player.displayClientMessage(message, true);
        }
    }

    @FunctionalInterface
    public interface NewWorldCreatedCallback {
        void onWorldCreated(String worldName, java.nio.file.Path worldDirectory);
    }

    public static void openCreateWorldScreen(
            Minecraft minecraft,
            Runnable onCancel,
            NewWorldCreatedCallback callback
    ) {
        net.minecraft.client.gui.screens.worldselection.CreateWorldScreen.openFresh(minecraft, currentScreen(minecraft));
    }

    public static void sendResourcePack(net.minecraft.server.level.ServerPlayer player, java.util.UUID packId, String url, String hash, boolean required, net.minecraft.network.chat.Component prompt) {
        if (player != null && player.connection != null) {
            player.connection.send(new net.minecraft.network.protocol.game.ClientboundResourcePackPacket(
                    url,
                    hash != null ? hash : "",
                    required,
                    prompt
            ));
        }
    }

    public static int playerLatency(net.minecraft.server.level.ServerPlayer player) {
        return player != null ? player.latency : -1;
    }
}
