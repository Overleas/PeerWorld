package link.sharedworld.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

/**
 * Confirmation dialog when a player with automatic hosting disabled attempts
 * to play an idle (offline) world that requires someone to host it.
 */
public final class ConfirmOfflineHostScreen extends link.sharedworld.versioned.VersionedScreen {
    private final Screen parent;
    private final String worldName;
    private final Runnable onConfirm;

    public ConfirmOfflineHostScreen(Screen parent, String worldName, Runnable onConfirm) {
        super(Component.translatable("screen.sharedworld.offline_host_title"));
        this.parent = parent;
        this.worldName = worldName;
        this.onConfirm = onConfirm;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int buttonY = Math.min(this.height - 40, this.height / 2 + 35);
        this.addRenderableWidget(Button.builder(Component.translatable("screen.sharedworld.offline_host_start"), button -> {
            link.sharedworld.versioned.ClientCompat.setScreen(this.minecraft, this.parent);
            this.onConfirm.run();
        }).bounds(centerX - 105, buttonY, 100, 20).build());

        this.addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), button -> {
            this.onClose();
        }).bounds(centerX + 5, buttonY, 100, 20).build());
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            link.sharedworld.versioned.ClientCompat.setScreen(this.minecraft, this.parent);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        int titleY = Math.max(30, this.height / 2 - 50);
        graphics.drawCenteredString(this.font, this.getTitle(), this.width / 2, titleY, 0xFFFFFFFF);
        List<FormattedCharSequence> lines = this.font.split(
                Component.translatable("screen.sharedworld.offline_host_body", this.worldName == null ? "?" : this.worldName),
                Math.min(this.width - 60, 380)
        );
        int y = titleY + 22;
        for (FormattedCharSequence line : lines) {
            graphics.drawCenteredString(this.font, line, this.width / 2, y, 0xFFC0C0C0);
            y += 12;
        }
    }
}
