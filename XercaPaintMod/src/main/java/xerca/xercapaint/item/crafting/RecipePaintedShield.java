package xerca.xercapaint.item.crafting;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import xerca.xercapaint.CanvasType;
import xerca.xercapaint.item.ItemCanvas;
import xerca.xercapaint.item.Items;

import java.util.List;

@MethodsReturnNonnullByDefault
public class RecipePaintedShield extends CustomRecipe {
    public RecipePaintedShield(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput inv, @NotNull Level world) {
        ItemStack canvas = ItemStack.EMPTY;
        ItemStack shield = ItemStack.EMPTY;

        for (int i = 0; i < inv.size(); i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty()) {
                if (stack.getItem() instanceof ItemCanvas
                        && ((ItemCanvas) stack.getItem()).getCanvasType() == CanvasType.TALL) {
                    if (!canvas.isEmpty()) return false;
                    canvas = stack;
                } else if (stack.getItem() instanceof ShieldItem) {
                    if (!shield.isEmpty()) return false;
                    shield = stack;
                } else {
                    return false;
                }
            }
        }

        return !canvas.isEmpty() && !shield.isEmpty();
    }

    @Override
    public ItemStack assemble(CraftingInput inv, @NotNull HolderLookup.Provider lookup) {
        ItemStack canvas = ItemStack.EMPTY;
        for (int i = 0; i < inv.size(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.getItem() instanceof ItemCanvas
                    && ((ItemCanvas) stack.getItem()).getCanvasType() == CanvasType.TALL) {
                canvas = stack;
                break;
            }
        }
        if (canvas.isEmpty()) {
            return ItemStack.EMPTY;
        }

        // Create the painted shield and copy all painting NBT
        ItemStack result = new ItemStack(Items.ITEM_PAINTED_SHIELD);
        List<Integer> pixels = canvas.get(Items.CANVAS_PIXELS);
        String canvasId   = canvas.get(Items.CANVAS_ID);
        int version       = canvas.getOrDefault(Items.CANVAS_VERSION, 1);

        result.set(Items.CANVAS_PIXELS, pixels);
        result.set(Items.CANVAS_ID,    canvasId);
        result.set(Items.CANVAS_VERSION, version);

        String title  = canvas.get(Items.CANVAS_TITLE);
        String author = canvas.get(Items.CANVAS_AUTHOR);
        if (title != null && author != null) {
            result.set(Items.CANVAS_TITLE,  title);
            result.set(Items.CANVAS_AUTHOR, author);
        }

        // Copy CustomModelData so the shield uses the correct baked texture
        result.set(DataComponents.CUSTOM_MODEL_DATA,
                   canvas.get(DataComponents.CUSTOM_MODEL_DATA));

        return result;
    }

    @Override
    public @NotNull NonNullList<ItemStack> getRemainingItems(CraftingInput inv) {
        // both inputs are consumed
        return NonNullList.withSize(inv.size(), ItemStack.EMPTY);
    }

    @Override
    public @NotNull RecipeSerializer<? extends CustomRecipe> getSerializer() {
        return Items.CRAFTING_SPECIAL_PAINTED_SHIELD;
    }
}
