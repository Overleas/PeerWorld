package link.sharedworld.mixin;

import com.mojang.authlib.GameProfile;
import java.util.UUID;
import link.sharedworld.host.SharedWorldHostIndicator;
import link.sharedworld.versioned.ClientCompat;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Appends a golden crown 👑 after the active host's name in the Tab player list overlay.
 */
@Mixin(PlayerTabOverlay.class)
public class PlayerTabOverlayMixin {
    @Inject(method = "getNameForDisplay", at = @At("RETURN"), cancellable = true)
    private void peerworld$appendHostCrown(PlayerInfo playerInfo, CallbackInfoReturnable<Component> cir) {
        if (playerInfo == null) {
            return;
        }
        GameProfile profile = playerInfo.getProfile();
        if (profile == null) {
            return;
        }
        UUID uuid = ClientCompat.profileId(profile);
        String name = ClientCompat.profileName(profile);

        if (SharedWorldHostIndicator.isCurrentHost(uuid, name)) {
            Component original = cir.getReturnValue();
            if (original != null) {
                MutableComponent modified = original.copy();
                modified.append(Component.literal(" 👑").withStyle(ChatFormatting.GOLD));
                cir.setReturnValue(modified);
            }
        }
    }
}
