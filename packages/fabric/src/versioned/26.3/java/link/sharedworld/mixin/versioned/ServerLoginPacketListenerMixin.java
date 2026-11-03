package link.sharedworld.mixin.versioned;

import link.sharedworld.RuntimePlayerIdentity;
import link.sharedworld.SharedWorldDevHelperPolicy;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.login.ServerboundHelloPacket;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLoginPacketListenerImpl.class)
public abstract class ServerLoginPacketListenerMixin {
    @Shadow
    @Final
    Connection connection;

    @Shadow
    abstract void startClientVerification(com.mojang.authlib.GameProfile gameProfile);

    @Inject(
            method = "handleHello",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/MinecraftServer;usesAuthentication()Z"
            ),
            cancellable = true
    )
    private void sharedworld$allowOfflineLogin(ServerboundHelloPacket packet, CallbackInfo callbackInfo) {
        if (!SharedWorldDevHelperPolicy.shouldAllowOfflineGuestLogin(this.connection.getRemoteAddress())) {
            return;
        }

        this.startClientVerification(RuntimePlayerIdentity.insecureDialtoneProfile(packet));
        callbackInfo.cancel();
    }
}
