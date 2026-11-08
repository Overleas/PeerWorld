package link.sharedworld.mixin;

import link.sharedworld.SharedWorldDevSessionBridge;
import link.sharedworld.screen.TransferHostScreen;
import link.sharedworld.versioned.ClientCompat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Injects a [ 👑 Transfer Host... ] button into the Pause (Esc) menu
 * whenever hosting a managed SharedWorld / PeerWorld session.
 */
@Mixin(PauseScreen.class)
public abstract class PauseScreenMixin extends Screen {
    protected PauseScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void peerworld$addTransferHostButton(CallbackInfo ci) {
        if (!SharedWorldDevSessionBridge.isHostingSharedWorld()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        MinecraftServer server = mc.getSingleplayerServer();
        if (server == null) {
            return;
        }

        int playerCount = server.getPlayerCount();
        boolean hasGuests = playerCount > 1;

        int btnWidth = 140;
        int btnHeight = 20;
        int x = this.width - btnWidth - 10;
        int y = 8;

        Button transferBtn = Button.builder(
                Component.translatable("screen.sharedworld.transfer_host_button"),
                btn -> ClientCompat.setScreen(mc, new TransferHostScreen(this))
        ).bounds(x, y, btnWidth, btnHeight).build();

        transferBtn.active = hasGuests;
        if (!hasGuests) {
            transferBtn.setTooltip(Tooltip.create(Component.translatable("screen.sharedworld.transfer_host_no_guests_tooltip")));
        } else {
            transferBtn.setTooltip(Tooltip.create(Component.translatable("screen.sharedworld.transfer_host_tooltip")));
        }

        this.addRenderableWidget(transferBtn);
    }
}
