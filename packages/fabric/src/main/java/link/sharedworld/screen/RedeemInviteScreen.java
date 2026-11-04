package link.sharedworld.screen;

import link.sharedworld.SharedWorldClient;
import link.sharedworld.SharedWorldText;
import link.sharedworld.api.SharedWorldApiClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.concurrent.CompletableFuture;

public final class RedeemInviteScreen extends link.sharedworld.versioned.VersionedScreen {
    private final SharedWorldScreen parent;
    private EditBox codeBox;
    private Button doneButton;
    private boolean redeemInFlight;
    private String statusMessage = "";

    public RedeemInviteScreen(SharedWorldScreen parent) {
        super(Component.translatable("screen.sharedworld.redeem_title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        this.codeBox = new EditBox(this.font, centerX - 100, 106, 200, 20, Component.translatable("screen.sharedworld.code_hint"));
        this.codeBox.setMaxLength(32);
        this.codeBox.setHint(SharedWorldText.hint("screen.sharedworld.code_hint"));
        this.addRenderableWidget(this.codeBox);
        this.setInitialFocus(this.codeBox);

        this.doneButton = this.addRenderableWidget(Button.builder(Component.translatable("screen.sharedworld.done"), button -> this.submit())
                .bounds(centerX - 100, this.height / 4 + 96, 200, 20)
                .build());
        this.doneButton.active = !this.redeemInFlight;
        this.addRenderableWidget(Button.builder(Component.translatable("screen.sharedworld.cancel"), button -> {
                    this.parent.clearTransientFocus();
                    link.sharedworld.versioned.ClientCompat.setScreen(this.minecraft, this.parent);
                })
                .bounds(centerX - 100, this.height / 4 + 120, 200, 20)
                .build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 32, 0xFFFFFFFF);
        guiGraphics.drawString(
                this.font,
                Component.translatable("screen.sharedworld.invite_code"),
                this.width / 2 - 100,
                92,
                0xFFA0A0A0
        );
        // Keep the status line clear of the height-relative buttons on small
        // scaled windows (fixed 158 can land on the Done button there).
        int statusY = Math.min(158, this.height / 4 + 96 - 12);
        guiGraphics.drawString(this.font, this.statusMessage, this.width / 2 - 100, statusY, 0xFFA0A0A0);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    private void submit() {
        if (this.redeemInFlight) {
            return;
        }
        String raw = this.codeBox.getValue().trim();
        if (raw.isEmpty()) {
            this.statusMessage = SharedWorldText.string("screen.sharedworld.invite_code_required");
            return;
        }
        String code = raw;
        if (raw.contains("@")) {
            String[] parts = raw.split("@", 2);
            code = parts[0].trim().toUpperCase();
            String customUrl = parts[1].trim();
            if (!customUrl.isEmpty()) {
                if (!customUrl.startsWith("http://") && !customUrl.startsWith("https://")) {
                    customUrl = "https://" + customUrl;
                }
                link.sharedworld.SharedWorldClientConfigStore.shared().setConfiguredBackendBaseUrl(customUrl);
                SharedWorldClient.updateBackendBaseUrl(customUrl);
            }
        } else {
            code = code.toUpperCase();
        }
        final String finalCode = code;

        this.redeemInFlight = true;
        this.doneButton.active = false;
        this.statusMessage = SharedWorldText.string("screen.sharedworld.redeem_adding_world");
        CompletableFuture
                .supplyAsync(() -> {
                    try {
                        return SharedWorldClient.apiClient().redeemInvite(finalCode);
                    } catch (Exception exception) {
                        throw new RuntimeException(exception);
                    }
                }, SharedWorldClient.ioExecutor())
                .whenComplete((result, error) -> ScreenGuards.runIfCurrent(this, () -> {
                    this.redeemInFlight = false;
                    if (this.doneButton != null) {
                        this.doneButton.active = true;
                    }
                    if (error != null) {
                        this.parent.clearTransientFocus();
                        link.sharedworld.versioned.ClientCompat.setScreen(this.minecraft, new SharedWorldErrorScreen(
                                this.parent,
                                Component.translatable("screen.sharedworld.error_title"),
                                inviteRedeemErrorMessage(error)
                        ));
                    } else {
                        this.parent.onChildOperationFinished(result.id());
                        this.parent.clearTransientFocus();
                        link.sharedworld.versioned.ClientCompat.setScreen(this.minecraft, this.parent);
                    }
                }));
    }

    private static Component inviteRedeemErrorMessage(Throwable error) {
        String code = SharedWorldApiClient.errorCode(error);
        if ("invite_not_found".equals(code)) {
            return Component.translatable("screen.sharedworld.invite_error_not_found");
        }
        if ("invite_inactive".equals(code)) {
            return Component.translatable("screen.sharedworld.invite_error_inactive");
        }
        if ("invite_expired".equals(code)) {
            return Component.translatable("screen.sharedworld.invite_error_expired");
        }
        String message = SharedWorldApiClient.friendlyErrorMessage(error);
        return Component.literal(SharedWorldText.errorMessageOrDefault(message));
    }
}
