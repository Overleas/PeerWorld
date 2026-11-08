package link.sharedworld.screen;

import link.sharedworld.sync.ManagedWorldStore;
import link.sharedworld.util.DesktopUtil;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.network.chat.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.stream.Stream;

final class EditWorldPacksForm {
    private final ManagedWorldStore worldStore;
    private final String worldId;
    private final BooleanSupplier editableByPlayer;
    private final Runnable onChanged;

    private String loadedUrl = "";
    private boolean loadedRequired;
    private boolean currentRequired;

    private final List<DatapackInfo> installedDatapacks = new ArrayList<>();
    private LocalResourcePackInfo localResourcePack;

    private Button openDatapacksButton;
    private Button refreshDatapacksButton;
    private Button openWorldFolderButton;
    private EditBox resourcePackUrlBox;
    private Button toggleRequiredButton;

    private ScreenRectangle lastArea;

    record DatapackInfo(String name, boolean enabled, Path path, long sizeBytes) {}
    record LocalResourcePackInfo(boolean exists, String fileName, long sizeBytes) {}

    EditWorldPacksForm(ManagedWorldStore worldStore, String worldId, BooleanSupplier editableByPlayer, Runnable onChanged) {
        this.worldStore = worldStore;
        this.worldId = worldId;
        this.editableByPlayer = editableByPlayer;
        this.onChanged = onChanged;
    }

    void createWidgets(Font font) {
        this.openDatapacksButton = Button.builder(
                Component.translatable("screen.sharedworld.packs_datapacks_open"),
                ignored -> openDatapacksFolder()
        ).width(180).build();

        this.refreshDatapacksButton = Button.builder(
                Component.translatable("screen.sharedworld.packs_datapacks_refresh"),
                ignored -> refreshPacks()
        ).width(180).build();

        this.openWorldFolderButton = Button.builder(
                Component.translatable("screen.sharedworld.packs_resourcepack_open_world"),
                ignored -> openWorldFolder()
        ).width(180).build();

        this.resourcePackUrlBox = new EditBox(font, 0, 0, 180, 20, Component.translatable("screen.sharedworld.packs_resourcepack_url_label"));
        this.resourcePackUrlBox.setMaxLength(256);
        this.resourcePackUrlBox.setValue(this.loadedUrl);
        this.resourcePackUrlBox.setResponder(text -> this.onChanged.run());

        this.toggleRequiredButton = Button.builder(
                this.requiredButtonMessage(),
                ignored -> {
                    this.currentRequired = !this.currentRequired;
                    this.toggleRequiredButton.setMessage(this.requiredButtonMessage());
                    this.onChanged.run();
                }
        ).width(180).build();

        refreshPacks();
    }

    void visitWidgets(Consumer<AbstractWidget> consumer) {
        consumer.accept(this.openDatapacksButton);
        consumer.accept(this.refreshDatapacksButton);
        consumer.accept(this.openWorldFolderButton);
        consumer.accept(this.resourcePackUrlBox);
        consumer.accept(this.toggleRequiredButton);
    }

    private static final int COLUMN_WIDTH = 190;
    private static final int COLUMN_GAP = 16;

    void layout(ScreenRectangle area) {
        this.lastArea = area;
        int columnWidth = Math.min(COLUMN_WIDTH, (area.width() - COLUMN_GAP - 20) / 2);
        int totalWidth = columnWidth * 2 + COLUMN_GAP;
        int leftColumn = area.left() + (area.width() - totalWidth) / 2;
        int rightColumn = leftColumn + columnWidth + COLUMN_GAP;
        int top = area.top() + Math.max(12, (area.height() - (5 * 24)) / 2);

        // Left Column: Datapacks
        this.openDatapacksButton.setWidth(columnWidth);
        this.openDatapacksButton.setPosition(leftColumn, top + 72);

        this.refreshDatapacksButton.setWidth(columnWidth);
        this.refreshDatapacksButton.setPosition(leftColumn, top + 96);

        // Right Column: Resource Pack
        this.openWorldFolderButton.setWidth(columnWidth);
        this.openWorldFolderButton.setPosition(rightColumn, top + 24);

        this.resourcePackUrlBox.setWidth(columnWidth);
        this.resourcePackUrlBox.setPosition(rightColumn, top + 64);

        this.toggleRequiredButton.setWidth(columnWidth);
        this.toggleRequiredButton.setPosition(rightColumn, top + 88);
    }

    void renderDecorations(GuiGraphics guiGraphics, Font font) {
        if (this.lastArea == null) {
            return;
        }
        int columnWidth = Math.min(COLUMN_WIDTH, (this.lastArea.width() - COLUMN_GAP - 20) / 2);
        int totalWidth = columnWidth * 2 + COLUMN_GAP;
        int leftColumn = this.lastArea.left() + (this.lastArea.width() - totalWidth) / 2;
        int rightColumn = leftColumn + columnWidth + COLUMN_GAP;
        int top = this.lastArea.top() + Math.max(12, (this.lastArea.height() - (5 * 24)) / 2);

        // Left: Datapacks Title
        guiGraphics.drawString(font, Component.translatable("screen.sharedworld.packs_datapacks_title"), leftColumn, top, 0xFFFFFFFF);
        if (this.installedDatapacks.isEmpty()) {
            guiGraphics.drawString(font, Component.translatable("screen.sharedworld.packs_datapacks_none"), leftColumn, top + 14, 0xFFA0A0A0);
        } else {
            Component countText = Component.translatable("screen.sharedworld.packs_datapacks_count", this.installedDatapacks.size());
            guiGraphics.drawString(font, countText, leftColumn, top + 14, 0xFF55FF55);
            int listY = top + 28;
            for (int i = 0; i < Math.min(3, this.installedDatapacks.size()); i++) {
                DatapackInfo dp = this.installedDatapacks.get(i);
                String prefix = dp.enabled ? "§a✔ " : "§7✗ ";
                String truncated = font.plainSubstrByWidth(prefix + dp.name, columnWidth);
                guiGraphics.drawString(font, Component.literal(truncated), leftColumn, listY, 0xFFE0E0E0);
                listY += 12;
            }
            if (this.installedDatapacks.size() > 3) {
                guiGraphics.drawString(font, Component.translatable("screen.sharedworld.packs_datapacks_more", (this.installedDatapacks.size() - 3)), leftColumn, listY, 0xFF808080);
            }
        }

        // Right: Resource Pack Title
        guiGraphics.drawString(font, Component.translatable("screen.sharedworld.packs_resourcepack_title"), rightColumn, top, 0xFFFFFFFF);
        if (this.localResourcePack != null && this.localResourcePack.exists()) {
            Component localText = Component.translatable("screen.sharedworld.packs_resourcepack_local_found",
                    this.localResourcePack.fileName(), formatSize(this.localResourcePack.sizeBytes()));
            guiGraphics.drawString(font, localText, rightColumn, top + 12, 0xFF55FF55);
        } else {
            guiGraphics.drawString(font, Component.translatable("screen.sharedworld.packs_resourcepack_local_none"), rightColumn, top + 12, 0xFFA0A0A0);
        }

        // URL label
        guiGraphics.drawString(font, Component.translatable("screen.sharedworld.packs_resourcepack_url_label"), rightColumn, top + 52, 0xFFA0A0A0);

        // Hint below required button
        WrappedText.draw(guiGraphics, font, Component.translatable("screen.sharedworld.packs_resourcepack_hint"), rightColumn, top + 114, columnWidth, 0xFF808080);
    }

    void populate(String url, boolean required) {
        this.loadedUrl = url == null ? "" : url.trim();
        this.loadedRequired = required;
        this.currentRequired = required;
        if (this.resourcePackUrlBox != null) {
            this.resourcePackUrlBox.setValue(this.loadedUrl);
        }
        if (this.toggleRequiredButton != null) {
            this.toggleRequiredButton.setMessage(this.requiredButtonMessage());
        }
        refreshPacks();
    }

    void markSaved(String url, boolean required) {
        this.loadedUrl = url == null ? "" : url.trim();
        this.loadedRequired = required;
        this.currentRequired = required;
    }

    static String normalizeUrl(String raw) {
        if (raw == null) {
            return null;
        }
        String trimmed = raw.trim();
        if (trimmed.isEmpty()) {
            return "";
        }
        if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")) {
            return "https://" + trimmed;
        }
        return trimmed;
    }

    boolean dirty() {
        if (this.resourcePackUrlBox == null) {
            return false;
        }
        String currentUrl = normalizeUrl(this.resourcePackUrlBox.getValue());
        String loaded = normalizeUrl(this.loadedUrl);
        return !Objects.equals(currentUrl, loaded) || this.currentRequired != this.loadedRequired;
    }

    String currentUrl() {
        return normalizeUrl(this.resourcePackUrlBox == null ? this.loadedUrl : this.resourcePackUrlBox.getValue());
    }

    boolean currentRequired() {
        return this.currentRequired;
    }

    void updateWidgets(boolean editable) {
        this.openDatapacksButton.active = true;
        this.refreshDatapacksButton.active = true;
        this.openWorldFolderButton.active = true;
        this.resourcePackUrlBox.setEditable(editable);
        this.toggleRequiredButton.active = editable;
    }

    private Component requiredButtonMessage() {
        return Component.translatable(this.currentRequired
                ? "screen.sharedworld.packs_resourcepack_required_on"
                : "screen.sharedworld.packs_resourcepack_required_off");
    }

    private void openDatapacksFolder() {
        Path worldDir = this.worldStore.workingCopy(this.worldId);
        Path datapacksDir = worldDir.resolve("datapacks");
        DesktopUtil.openFolder(datapacksDir);
        refreshPacks();
    }

    private void openWorldFolder() {
        Path worldDir = this.worldStore.workingCopy(this.worldId);
        DesktopUtil.openFolder(worldDir);
        refreshPacks();
    }

    void refreshPacks() {
        this.installedDatapacks.clear();
        Path worldDir = this.worldStore.workingCopy(this.worldId);
        Path datapacksDir = worldDir.resolve("datapacks");

        if (Files.isDirectory(datapacksDir)) {
            try (Stream<Path> stream = Files.list(datapacksDir)) {
                stream.forEach(path -> {
                    String name = path.getFileName().toString();
                    if (name.startsWith(".")) {
                        return;
                    }
                    boolean enabled = !name.endsWith(".disabled");
                    long size = 0L;
                    try {
                        if (Files.isRegularFile(path)) {
                            size = Files.size(path);
                        }
                    } catch (IOException ignored) {
                    }
                    this.installedDatapacks.add(new DatapackInfo(name, enabled, path, size));
                });
            } catch (IOException ignored) {
            }
        }

        // Check local resources.zip
        Path rootZip = worldDir.resolve("resources.zip");
        Path nestedZip = worldDir.resolve("resourcepacks").resolve("resources.zip");
        if (Files.isRegularFile(rootZip)) {
            try {
                this.localResourcePack = new LocalResourcePackInfo(true, "resources.zip", Files.size(rootZip));
            } catch (IOException ignored) {
                this.localResourcePack = new LocalResourcePackInfo(true, "resources.zip", 0L);
            }
        } else if (Files.isRegularFile(nestedZip)) {
            try {
                this.localResourcePack = new LocalResourcePackInfo(true, "resourcepacks/resources.zip", Files.size(nestedZip));
            } catch (IOException ignored) {
                this.localResourcePack = new LocalResourcePackInfo(true, "resourcepacks/resources.zip", 0L);
            }
        } else {
            this.localResourcePack = new LocalResourcePackInfo(false, null, 0L);
        }
    }

    private static String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format(Locale.ROOT, "%.1f KB", bytes / 1024.0);
        return String.format(Locale.ROOT, "%.1f MB", bytes / (1024.0 * 1024.0));
    }
}
