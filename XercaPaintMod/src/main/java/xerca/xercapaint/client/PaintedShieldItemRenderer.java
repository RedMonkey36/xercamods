package xerca.xercapaint.client;

import com.mojang.serialization.MapCodec;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.Minecraft;

public class PaintedShieldItemRenderer implements SpecialModelRenderer<ItemStack> {
    @Override
    public void render(ItemStack stack,
                       ItemDisplayContext displayContext,
                       PoseStack matrices,
                       MultiBufferSource buffers,
                       int light,
                       int overlay,
                       boolean someFlag) {
        // For now: just render exactly like the vanilla shield
        Minecraft mc = Minecraft.getInstance();
        
        // TODO
    }

    @Override
    public ItemStack extractArgument(ItemStack itemStack) {
        return itemStack;
    }

    public static class Unbaked implements SpecialModelRenderer.Unbaked {
        public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(Unbaked::new);

        @Override
        public SpecialModelRenderer<ItemStack> bake(EntityModelSet entityModels) {
            return new PaintedShieldItemRenderer();
        }

        @Override
        public MapCodec<? extends SpecialModelRenderer.Unbaked> type() {
            return MAP_CODEC;
        }
    }
}
