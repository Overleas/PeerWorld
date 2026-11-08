package link.sharedworld.screen;

import java.util.List;
import link.sharedworld.SharedWorldClient;
import link.sharedworld.versioned.ClientCompat;
import link.sharedworld.versioned.VersionedScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.FormattedCharSequence;

/**
 * Native Minecraft confirmation screen for transferring world host authority
 * to an online connected friend.
 */
public final class ConfirmTransferHostScreen extends VersionedScreen {
    private final Screen parent;
    private final ServerPlayer targetPlayer;
    private final String targetName;

    public ConfirmTransferHostScreen(Screen parent, ServerPlayer targetPlayer) {
        super(Component.translatable("screen.sharedworld.transfer_host_confirm_title"));
        this.parent = parent;
        this.targetPlayer = targetPlayer;
        this.targetName = ClientCompat.profileName(targetPlayer.getGameProfile());
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int buttonY = Math.min(this.height - 40, this.height / 2 + 35);

        this.addRenderableWidget(Button.builder(
                Component.translatable("screen.sharedworld.transfer_host_action").withStyle(ChatFormatting.GOLD),
                button -> executeTransfer()
        ).bounds(centerX - 155, buttonY, 150, 20).build());

        this.addRenderableWidget(Button.builder(
                Component.translatable("gui.cancel"),
                button -> this.onClose()
        ).bounds(centerX + 5, buttonY, 150, 20).build());
    }

    private void executeTransfer() {
        if (this.targetPlayer == null) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        ClientCompat.setScreen(mc, null);
        SharedWorldClient.hostingManager().transferHostTo(this.targetPlayer.getUUID(), this.targetName);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            ClientCompat.setScreen(this.minecraft, this.parent);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

        int centerX = this.width / 2;
        int titleY = Math.max(30, this.height / 2 - 50);

        graphics.drawCenteredString(
                this.font,
                this.getTitle().copy().withStyle(ChatFormatting.GOLD),
                centerX,
                titleY,
                0xFFFFAA00
        );

        List<FormattedCharSequence> lines = this.font.split(
                Component.translatable("screen.sharedworld.transfer_host_confirm_message", this.targetName),
                Math.min(this.width - 60, 360)
        );
        int y = titleY + 22;
        for (FormattedCharSequence line : lines) {
            graphics.drawCenteredString(this.font, line, centerX, y, 0xFFFFFFFF);
            y += 12;
        }

        List<FormattedCharSequence> detailLines = this.font.split(
                Component.translatable("screen.sharedworld.transfer_host_confirm_detail"),
                Math.min(this.width - 60, 360)
        );
        y += 4;
        for (FormattedCharSequence line : detailLines) {
            graphics.drawCenteredString(this.font, line, centerX, y, 0xFFAAAAAA);
            y += 12;
        }
    }
}
