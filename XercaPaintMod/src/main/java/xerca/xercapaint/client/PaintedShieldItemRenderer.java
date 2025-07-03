package xerca.xercapaint.client;

import com.mojang.serialization.MapCodec;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import xerca.xercapaint.Mod;
import xerca.xercapaint.item.Items;

import java.util.List;
import java.util.UUID;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@net.fabricmc.api.Environment(net.fabricmc.api.EnvType.CLIENT)
public class PaintedShieldItemRenderer implements SpecialModelRenderer<ItemStack> {

    // NEW: separate width & height
    private static final int CANVAS_WIDTH  = 16;
    private static final int CANVAS_HEIGHT = 32;

    private static final Map<UUID, DynamicTexture> CACHE = new ConcurrentHashMap<>();

    @Override
    public void render(ItemStack stack,
                       ItemDisplayContext context,
                       PoseStack ms,
                       MultiBufferSource buffers,
                       int light,
                       int overlay,
                       boolean leftHanded) {
        Minecraft mc = Minecraft.getInstance();
        Level world = mc.level;

        // Draw the vanilla shield (wood, rim, straps, blocking pose…)
        mc.getItemRenderer().renderStatic(
            stack, context, light, overlay, ms, buffers, world, /*seed*/0
        );

        // If there's a painting component, overlay it
        List<Integer> pixelList = stack.get(Items.CANVAS_PIXELS);

        if (pixelList != null) {
            // Convert to primitive and compute a stable UUID
            int[] pixels = pixelList.stream().mapToInt(Integer::intValue).toArray();
            String idStr = stack.get(Items.CANVAS_ID);
            UUID uuid = UUID.nameUUIDFromBytes(idStr.getBytes());

            // Build-or-fetch the DynamicTexture at 16×32
            DynamicTexture tex = CACHE.computeIfAbsent(uuid, u ->
                new DynamicTexture(CANVAS_WIDTH, CANVAS_HEIGHT, false)
            );
            NativeImage img = tex.getPixels();
            for (int i = 0; i < pixels.length && i < CANVAS_WIDTH * CANVAS_HEIGHT; i++) {
                img.setPixel(i % CANVAS_WIDTH, i / CANVAS_WIDTH, pixels[i]);
            }
            tex.upload();

            // Register & bind our paint texture
            ResourceLocation texRL = Mod.id("shield/" + uuid);
            TextureManager tm = mc.getTextureManager();
            tm.register(texRL, tex);
            RenderSystem.setShaderTexture(0, texRL);

            // Draw a flat 16×32 quad on the front face of the shield
            ms.pushPose();
            ms.translate(0, 0, -0.5);
            PoseStack.Pose pose = ms.last();
            Matrix4f mat = pose.pose();
            // Matrix3f norm = pose.normal();
            VertexConsumer vb = buffers.getBuffer(RenderType.entityCutoutNoCull(texRL));

            // NEW: compute separate scales for width & height
            float sW      = 1.0f / CANVAS_WIDTH;
            float sH      = 1.0f / CANVAS_HEIGHT;
            float halfW   = (CANVAS_WIDTH  * sW) / 2f;  // = 8 * sW
            float halfH   = (CANVAS_HEIGHT * sH) / 2f;  // = 16 * sH
            float[] xs    = {-halfW, -halfW,  halfW,  halfW};
            float[] ys    = {-halfH,  halfH,  halfH, -halfH};
            float[] us    = {    0f,      0f,      1f,      1f};
            float[] vs    = {    1f,      0f,      0f,      1f};

            for (int i = 0; i < 4; i++) {
                vb.addVertex(mat, xs[i], ys[i], 0f)
                  .setColor(255, 255, 255, 255)
                  .setUv(us[i], vs[i])
                  .setOverlay(OverlayTexture.NO_OVERLAY)
                  .setLight(light)
                  .setNormal(pose, 0f, 0f, 1f);
            }
            ms.popPose();
        }
    }

    @Override
    public ItemStack extractArgument(ItemStack stack) {
        return stack;
    }

    public static class Unbaked implements SpecialModelRenderer.Unbaked {
        public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(Unbaked::new);

        @Override
        public SpecialModelRenderer<ItemStack> bake(
                net.minecraft.client.model.geom.EntityModelSet entityModels) {
            return new PaintedShieldItemRenderer();
        }

        @Override
        public MapCodec<? extends SpecialModelRenderer.Unbaked> type() {
            return MAP_CODEC;
        }
    }
}
