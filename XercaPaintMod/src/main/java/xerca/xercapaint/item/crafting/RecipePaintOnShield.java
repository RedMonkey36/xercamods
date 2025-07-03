package xerca.xercapaint.item.crafting;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.level.Level;
import xerca.xercapaint.CanvasType;
import xerca.xercapaint.item.ItemCanvas;
import xerca.xercapaint.item.Items;

public class RecipePaintOnShield extends CustomRecipe {
    public RecipePaintOnShield(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput inv, Level world) {
        boolean foundCanvas = false, foundShield = false;
        for (int i = 0; i < inv.size(); i++) {
            ItemStack s = inv.getItem(i);
            if (s.isEmpty()) continue;
            if (!foundCanvas && s.getItem() instanceof ItemCanvas &&
                ((ItemCanvas)s.getItem()).getCanvasType() == CanvasType.TALL) {
                foundCanvas = true;
            } else if (!foundShield && s.is(net.minecraft.world.item.Items.SHIELD)) {
                foundShield = true;
            } else {
                return false;
            }
        }
        return foundCanvas && foundShield;
    }

    @Override
    public ItemStack assemble(CraftingInput inv, HolderLookup.Provider _p) {
        ItemStack canvas = ItemStack.EMPTY;
        for (int i = 0; i < inv.size(); i++) {
            ItemStack s = inv.getItem(i);
            if (s.getItem() instanceof ItemCanvas &&
                ((ItemCanvas)s.getItem()).getCanvasType() == CanvasType.TALL) {
                canvas = s;
                break;
            }
        }
        if (canvas.isEmpty()) return ItemStack.EMPTY;

        ItemStack out = new ItemStack(net.minecraft.world.item.Items.SHIELD);
        out.set(Items.CANVAS_PIXELS,  canvas.get(Items.CANVAS_PIXELS));
        out.set(Items.CANVAS_ID,      canvas.get(Items.CANVAS_ID));
        out.set(Items.CANVAS_VERSION, canvas.getOrDefault(Items.CANVAS_VERSION, 1));
        if (canvas.get(Items.CANVAS_TITLE) != null) {
            out.set(Items.CANVAS_TITLE,  canvas.get(Items.CANVAS_TITLE));
            out.set(Items.CANVAS_AUTHOR, canvas.get(Items.CANVAS_AUTHOR));
        }
        return out;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput inv) {
        return NonNullList.withSize(inv.size(), ItemStack.EMPTY);
    }

    @Override
    public RecipeSerializer<? extends CustomRecipe> getSerializer() {
        return Items.CRAFTING_SPECIAL_PAINT_ON_SHIELD;
    }
}
