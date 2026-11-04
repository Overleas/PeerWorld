package link.sharedworld.mixin;

import net.minecraft.network.chat.Component;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerChatSessionValidationMixin {
    private static final Logger LOGGER = LoggerFactory.getLogger("sharedworld/chat-session");

    @Redirect(
            method = "handleChatSessionUpdate",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;disconnect(Lnet/minecraft/network/chat/Component;)V"
            ),
            require = 0
    )
    private void sharedworld$suppressChatSessionValidationDisconnect(ServerGamePacketListenerImpl listener, Component reason) {
        LOGGER.warn("SharedWorld suppressed chat session validation disconnect: {}", reason.getString());
    }
}
