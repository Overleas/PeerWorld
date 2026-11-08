package link.sharedworld.screen;

import java.util.ArrayList;
import java.util.List;
import link.sharedworld.versioned.ClientCompat;
import link.sharedworld.versioned.VersionedScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

/**
 * In-game pause menu dialog allowing the hosting player to transfer
 * world hosting authority to an online connected friend with one click.
 */
public final class TransferHostScreen extends VersionedScreen {
    private final Screen parent;
    private final List<ServerPlayer> guests = new ArrayList<>();

    public TransferHostScreen(Screen parent) {
        super(Component.translatable("screen.sharedworld.transfer_host_title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.clearWidgets();
        this.guests.clear();

        Minecraft mc = Minecraft.getInstance();
        MinecraftServer server = mc.getSingleplayerServer();
        if (server != null) {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                if (mc.player == null || !player.getUUID().equals(mc.player.getUUID())) {
                    this.guests.add(player);
                }
            }
        }

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        int startY = 60;
        for (int i = 0; i < this.guests.size(); i++) {
            ServerPlayer guest = this.guests.get(i);
            int y = startY + i * 26;
            if (y > this.height - 60) {
                break; // Screen overflow guard
            }
            this.addRenderableWidget(Button.builder(
                    Component.translatable("screen.sharedworld.transfer_host_action"),
                    button -> ClientCompat.setScreen(this.minecraft, new ConfirmTransferHostScreen(this, guest))
            ).bounds(centerX + 60, y, 90, 20).build());
        }

        this.addRenderableWidget(Button.builder(
                Component.translatable("gui.back"),
                button -> ClientCompat.setScreen(this.minecraft, this.parent)
        ).bounds(centerX - 100, this.height - 32, 200, 20).build());
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }

    @Override
    public void onClose() {
        ClientCompat.setScreen(this.minecraft, this.parent);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // Main screen header
        graphics.drawCenteredString(this.font, this.getTitle(), centerX, 20, 0xFFFFFFFF);
        graphics.drawCenteredString(
                this.font,
                Component.translatable("screen.sharedworld.transfer_host_subtitle"),
                centerX,
                35,
                0xFFAAAAAA
        );

        if (this.guests.isEmpty()) {
            graphics.drawCenteredString(
                    this.font,
                    Component.translatable("screen.sharedworld.transfer_host_no_guests_tooltip"),
                    centerX,
                    centerY,
                    0xFF888888
            );
            return;
        }

        int startY = 60;
        for (int i = 0; i < this.guests.size(); i++) {
            ServerPlayer guest = this.guests.get(i);
            int y = startY + i * 26;
            if (y > this.height - 60) {
                break;
            }

            int latency = ClientCompat.playerLatency(guest);
            String latencyStr = latency >= 0 ? latency + " ms" : "? ms";
            int pingColor = latency < 50 ? 0xFF55FF55 : latency < 150 ? 0xFFFFFF55 : 0xFFFF5555;

            String guestName = ClientCompat.profileName(guest.getGameProfile());
            graphics.drawString(this.font, "• " + guestName, centerX - 140, y + 6, 0xFFFFFFFF);
            graphics.drawString(this.font, latencyStr, centerX - 10, y + 6, pingColor);
        }
    }
}
