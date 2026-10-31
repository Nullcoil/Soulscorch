package net.nullcoil.soulscorch.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.nullcoil.soulscorch.Soulscorch;

@Environment(EnvType.CLIENT)
public class SoulBrewingStandScreen extends AbstractContainerScreen<SoulBrewingStandMenu> {
    private static final Identifier FUEL_LENGTH_SPRITE = Soulscorch.id("container/brewing_stand/soul_fuel_length");
    private static final Identifier BREW_PROGRESS_SPRITE = Soulscorch.id("container/brewing_stand/brew_progress");
    private static final Identifier BUBBLES_SPRITE = Soulscorch.id("container/brewing_stand/bubbles");
    private static final Identifier BREWING_STAND_LOCATION = Soulscorch.id("textures/gui/container/soul_brewing_stand.png");
    private static final int[] BUBBLELENGTHS = new int[]{29, 24, 20, 16, 11, 6, 0};

    public SoulBrewingStandScreen(final SoulBrewingStandMenu menu, final Inventory inventory, final Component title) {
        super(menu, inventory, title);
    }

    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
    }

    public void extractBackground(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY, final float a) {
        super.extractBackground(graphics, mouseX, mouseY, a);
        int xo = (this.width - this.imageWidth) / 2;
        int yo = (this.height - this.imageHeight) / 2;
        graphics.blit(RenderPipelines.GUI_TEXTURED, BREWING_STAND_LOCATION, xo, yo, 0f, 0f, this.imageWidth, this.imageHeight, 256, 256);
        int fuel = ((SoulBrewingStandMenu) this.menu).getFuel();
        int fuelLength = Mth.clamp((18 * fuel + 20 - 1) / 20, 0, 18);
        if (fuelLength > 0) {
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, FUEL_LENGTH_SPRITE, 18, 4, 0, 0, xo + 60, yo + 44, fuelLength, 4);
        }
        int tickCount = ((SoulBrewingStandMenu) this.menu).getBrewingTicks();
        if (tickCount > 0) {
            int length = (int) (28f * (1f - (float) tickCount / 200f));
            if (length > 0) {
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, BREW_PROGRESS_SPRITE, 9, 28, 0, 0, xo+97, yo + 16, 9, length);
            }

            length = BUBBLELENGTHS[tickCount / 2 % 7];
            if (length > 0) {
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, BUBBLES_SPRITE, 12, 29, 0, 29-length, xo+63, yo+14+29-length, 12, length);
            }
        }
    }
}