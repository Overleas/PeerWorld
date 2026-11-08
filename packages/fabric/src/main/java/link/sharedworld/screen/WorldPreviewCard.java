package link.sharedworld.screen;

import link.sharedworld.SharedWorldText;
import link.sharedworld.versioned.GuiBlit;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.FaviconTexture;

/**
 * The world-icon well plus the server-list card preview shared by the create
 * and edit screens. Provides a unified, centered layout model where all form
 * controls, avatar/icon pickers, preview cards, and action buttons align
 * cohesively without visual fragmentation or text overlaps.
 */
final class WorldPreviewCard {
    static final int ICON_SIZE = 60;
    static final int ICON_GAP = 8;
    static final int ROW_WIDTH = SharedWorldServerList.ROW_WIDTH; // 305
    static final int ROW_HEIGHT = SharedWorldServerList.ROW_HEIGHT; // 36

    private static final String EDIT_ICON_SPRITE = "sharedworld:edit_icon";
    private static final String EDIT_ICON_HIGHLIGHTED_SPRITE = "sharedworld:edit_icon_highlighted";
    private static final String DELETE_ICON_HIGHLIGHTED_SPRITE = "sharedworld:delete_icon_highlighted";
    private static final String PING_5_SPRITE = "minecraft:server_list/ping_5";

    private WorldPreviewCard() {
    }

    static int formWidth(ScreenRectangle contentArea) {
        return Math.min(ROW_WIDTH, Math.max(260, contentArea.width() - 24));
    }

    static int formLeft(ScreenRectangle contentArea) {
        return contentArea.left() + (contentArea.width() - formWidth(contentArea)) / 2;
    }

    static int iconX(ScreenRectangle contentArea) {
        return formLeft(contentArea);
    }

    static int fieldsX(ScreenRectangle contentArea) {
        return formLeft(contentArea) + ICON_SIZE + ICON_GAP;
    }

    static int fieldsWidth(ScreenRectangle contentArea) {
        return formWidth(contentArea) - ICON_SIZE - ICON_GAP;
    }

    static int cardX(ScreenRectangle contentArea) {
        return formLeft(contentArea);
    }

    static int startY(ScreenRectangle contentArea) {
        return contentArea.top() + Math.max(6, (contentArea.height() - 162) / 2);
    }

    static int nameBoxY(ScreenRectangle contentArea) {
        return startY(contentArea) + 14;
    }

    static int motdBoxY(ScreenRectangle contentArea) {
        return startY(contentArea) + 54;
    }

    static int cardY(ScreenRectangle contentArea) {
        return startY(contentArea) + 100;
    }

    static int extraButtonY(ScreenRectangle contentArea) {
        return startY(contentArea) + 142;
    }

    static boolean iconHovered(int iconX, int iconY, int mouseX, int mouseY) {
        return mouseX >= iconX && mouseX <= iconX + ICON_SIZE && mouseY >= iconY && mouseY <= iconY + ICON_SIZE;
    }

    /**
     * The icon well: framed world icon, an always-visible pencil badge when it is
     * editable, and a dim overlay with action sprite when hovered.
     */
    static void renderIconWell(GuiGraphics guiGraphics, FaviconTexture texture, int iconX, int iconY, boolean editable, boolean hovered, boolean clickDeletes) {
        guiGraphics.fill(iconX - 1, iconY - 1, iconX + ICON_SIZE + 1, iconY + ICON_SIZE + 1, 0xFF4A4A4A);
        GuiBlit.favicon(guiGraphics, texture, iconX, iconY, ICON_SIZE);
        if (!editable) {
            return;
        }
        // The dark chip keeps the pencil readable over any world screenshot.
        guiGraphics.fill(iconX + ICON_SIZE - 16, iconY + ICON_SIZE - 16, iconX + ICON_SIZE, iconY + ICON_SIZE, 0xB0000000);
        GuiBlit.sprite(guiGraphics, EDIT_ICON_SPRITE, iconX + ICON_SIZE - 14, iconY + ICON_SIZE - 14, 12, 12);
        if (hovered) {
            guiGraphics.fill(iconX, iconY, iconX + ICON_SIZE, iconY + ICON_SIZE, 0x80000000);
            GuiBlit.sprite(guiGraphics, clickDeletes ? DELETE_ICON_HIGHLIGHTED_SPRITE : EDIT_ICON_HIGHLIGHTED_SPRITE, iconX + (ICON_SIZE - 24) / 2, iconY + (ICON_SIZE - 24) / 2, 24, 24);
        }
    }

    /** The card exactly as the world list would draw it, selected, with a full ping. */
    static void renderCard(GuiGraphics guiGraphics, Font font, FaviconTexture texture, int rowX, int rowY, String worldName, String motd) {
        int contentX = rowX + SharedWorldServerList.CONTENT_PADDING;
        int contentY = rowY + SharedWorldServerList.CONTENT_PADDING;
        SharedWorldServerList.renderSelectedOutline(guiGraphics, rowX, rowY, true);
        GuiBlit.favicon(guiGraphics, texture, contentX, contentY, 32);
        int max = SharedWorldMetadataFormat.extractMaxPlayers(motd);
        SharedWorldServerList.renderRowContents(guiGraphics, font, rowX, rowY, worldName, SharedWorldMetadataFormat.cleanMotd(motd), SharedWorldText.playerCount(0, max), PING_5_SPRITE);
    }
}
