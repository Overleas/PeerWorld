package link.sharedworld.screen;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

public final class MaxPlayersSlider extends AbstractSliderButton {
    private final Consumer<Integer> onValueChange;
    private int currentPlayers;

    public MaxPlayersSlider(int x, int y, int width, int height, int initialPlayers, Consumer<Integer> onValueChange) {
        super(x, y, width, height, Component.empty(), toProgress(initialPlayers));
        this.onValueChange = onValueChange;
        this.currentPlayers = snapToStep(initialPlayers);
        this.updateMessage();
    }

    private static double toProgress(int players) {
        return (snapToStep(players) - 2) / 62.0;
    }

    private static int snapToStep(int players) {
        int clamped = Math.max(2, Math.min(64, players));
        int stepIndex = Math.round((clamped - 2) / 2.0f);
        return 2 + stepIndex * 2;
    }

    public void setPlayers(int players) {
        this.currentPlayers = snapToStep(players);
        this.value = toProgress(this.currentPlayers);
        this.updateMessage();
    }

    public int getPlayers() {
        return this.currentPlayers;
    }

    @Override
    protected void updateMessage() {
        int step = (int) Math.round(this.value * 31.0);
        this.currentPlayers = 2 + step * 2;
        if (this.currentPlayers > 16) {
            this.setMessage(Component.translatable("screen.sharedworld.settings_max_players_warn", this.currentPlayers));
            this.setTooltip(Tooltip.create(Component.translatable("screen.sharedworld.settings.max_players_warn_tooltip")));
        } else {
            this.setMessage(Component.translatable("screen.sharedworld.settings_max_players", this.currentPlayers));
            this.setTooltip(Tooltip.create(Component.translatable("screen.sharedworld.settings.max_players_tooltip")));
        }
    }

    @Override
    protected void applyValue() {
        int step = (int) Math.round(this.value * 31.0);
        this.currentPlayers = 2 + step * 2;
        if (this.onValueChange != null) {
            this.onValueChange.accept(this.currentPlayers);
        }
    }
}
