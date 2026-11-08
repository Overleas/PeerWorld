package link.sharedworld.versioned;

import net.minecraft.client.Minecraft;

/** Version-specific client entry points whose location moved across Minecraft versions. */
public final class ClientCompat {
    private ClientCompat() {
    }

    public static void disconnectFromWorld(Minecraft minecraft) {
        minecraft.disconnectFromWorld(null);
    }

    public static void drawDeferredSubtitles(Minecraft minecraft) {
        minecraft.gui.extractDeferredSubtitles();
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
        return profile.id();
    }

    public static String profileName(com.mojang.authlib.GameProfile profile) {
        return profile.name();
    }

    /** The running client's world data version, or a permissive maximum when undetectable. */
    public static int currentDataVersion() {
        try {
            net.minecraft.WorldVersion version = net.minecraft.SharedConstants.getCurrentVersion();
            return version == null ? Integer.MAX_VALUE : version.dataVersion().version();
        } catch (RuntimeException | LinkageError exception) {
            // Headless/undetected version: never block on an unknowable comparison.
            return Integer.MAX_VALUE;
        }
    }

    /** The running client's Minecraft version name (for example "1.21.11"), or null. */
    public static String currentMinecraftVersion() {
        try {
            net.minecraft.WorldVersion version = net.minecraft.SharedConstants.getCurrentVersion();
            return version == null ? null : version.name();
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
            || screen instanceof net.minecraft.client.gui.screens.GenericMessageScreen;
    }

    /** Show a system chat line on the local client (autosave health warnings). */
    public static void showChatMessage(Minecraft minecraft, net.minecraft.network.chat.Component message) {
        if (minecraft.gui != null) {
            minecraft.gui.getChat().addClientSystemMessage(message);
        }
    }

    /** Show a system toast on the local client (migration, backup, player join notifications). */
    public static void showToast(Minecraft minecraft, net.minecraft.network.chat.Component title, net.minecraft.network.chat.Component message) {
        if (minecraft != null && minecraft.getToastManager() != null) {
            net.minecraft.client.gui.components.toasts.SystemToast.add(
                    minecraft.getToastManager(),
                    net.minecraft.client.gui.components.toasts.SystemToast.SystemToastId.PERIODIC_NOTIFICATION,
                    title,
                    message
            );
        }
    }

    /** Show an actionbar overlay message above the hotbar. */
    public static void showOverlayMessage(Minecraft minecraft, net.minecraft.network.chat.Component message) {
        if (minecraft != null && minecraft.player != null) {
            minecraft.player.sendOverlayMessage(message);
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
        net.minecraft.client.gui.screens.worldselection.CreateWorldScreen.openFresh(
                minecraft,
                onCancel,
                (screen, registries, settings, gamerules, tempDataPackDir) -> {
                    try {
                        String targetFolder = screen.getUiState().getTargetFolder();
                        net.minecraft.world.level.storage.LevelStorageSource levelSource = minecraft.getLevelSource();
                        net.minecraft.world.level.storage.LevelStorageSource.LevelStorageAccess access =
                                levelSource.createAccess(targetFolder);
                        if (tempDataPackDir != null && java.nio.file.Files.exists(tempDataPackDir)) {
                            java.nio.file.Path dataPackDir = access.getLevelPath(net.minecraft.world.level.storage.LevelResource.DATAPACK_DIR);
                            java.nio.file.Files.createDirectories(dataPackDir);
                            try (java.util.stream.Stream<java.nio.file.Path> stream = java.nio.file.Files.walk(tempDataPackDir)) {
                                stream.filter(p -> !p.equals(tempDataPackDir)).forEach(src -> {
                                    java.nio.file.Path dest = dataPackDir.resolve(tempDataPackDir.relativize(src));
                                    try {
                                        if (java.nio.file.Files.isDirectory(src)) {
                                            java.nio.file.Files.createDirectories(dest);
                                        } else {
                                            java.nio.file.Files.copy(src, dest, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                                        }
                                    } catch (Exception ignored) {
                                    }
                                });
                            }
                        }
                        access.saveDataTag(settings.data());
                        java.nio.file.Path worldDir = access.getLevelPath(net.minecraft.world.level.storage.LevelResource.ROOT);

                        // Save data/minecraft/world_gen_settings.dat (required for world loading in 26.x)
                        try {
                            net.minecraft.resources.RegistryOps<net.minecraft.nbt.Tag> registryOps =
                                    registries.compositeAccess().createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE);
                            net.minecraft.nbt.Tag genTag = net.minecraft.world.level.levelgen.WorldGenSettings.CODEC
                                    .encodeStart(registryOps, settings.genSettings())
                                    .getOrThrow();
                            net.minecraft.nbt.CompoundTag rootTag = new net.minecraft.nbt.CompoundTag();
                            rootTag.put("data", genTag);
                            net.minecraft.nbt.NbtUtils.addCurrentDataVersion(rootTag);
                            java.nio.file.Path dataDir = worldDir.resolve("data").resolve("minecraft");
                            java.nio.file.Files.createDirectories(dataDir);
                            net.minecraft.nbt.NbtIo.writeCompressed(rootTag, dataDir.resolve("world_gen_settings.dat"));
                        } catch (Exception e) {
                            org.slf4j.LoggerFactory.getLogger("peerworld").warn("Failed to encode world_gen_settings.dat", e);
                        }

                        // Save data/minecraft/game_rules.dat if gamerules are present
                        if (gamerules != null && gamerules.isPresent()) {
                            try {
                                net.minecraft.world.flag.FeatureFlagSet flags = settings.data().getDataConfiguration().enabledFeatures();
                                net.minecraft.nbt.Tag rulesTag = net.minecraft.world.level.gamerules.GameRules.codec(flags)
                                        .encodeStart(net.minecraft.nbt.NbtOps.INSTANCE, gamerules.get())
                                        .getOrThrow();
                                net.minecraft.nbt.CompoundTag rootTag = new net.minecraft.nbt.CompoundTag();
                                rootTag.put("data", rulesTag);
                                net.minecraft.nbt.NbtUtils.addCurrentDataVersion(rootTag);
                                java.nio.file.Path dataDir = worldDir.resolve("data").resolve("minecraft");
                                java.nio.file.Files.createDirectories(dataDir);
                                net.minecraft.nbt.NbtIo.writeCompressed(rootTag, dataDir.resolve("game_rules.dat"));
                            } catch (Exception e) {
                                org.slf4j.LoggerFactory.getLogger("peerworld").warn("Failed to encode game_rules.dat", e);
                            }
                        }

                        access.close();
                        String name = screen.getUiState().getName().trim();
                        if (name.isEmpty()) {
                            name = targetFolder;
                        }
                        callback.onWorldCreated(name, worldDir);
                        return true;
                    } catch (Exception exception) {
                        org.slf4j.LoggerFactory.getLogger("peerworld").error("Failed to initialize new world for PeerWorld", exception);
                        return false;
                    }
                }
        );
    }

    public static void sendResourcePack(net.minecraft.server.level.ServerPlayer player, java.util.UUID packId, String url, String hash, boolean required, net.minecraft.network.chat.Component prompt) {
        if (player != null && player.connection != null) {
            player.connection.send(new net.minecraft.network.protocol.common.ClientboundResourcePackPushPacket(
                    packId != null ? packId : java.util.UUID.randomUUID(),
                    url,
                    hash != null ? hash : "",
                    required,
                    java.util.Optional.ofNullable(prompt)
            ));
        }
    }

    public static int playerLatency(net.minecraft.server.level.ServerPlayer player) {
        return player != null && player.connection != null ? player.connection.latency() : -1;
    }
}
