package net.luojiuoscar.isaac_disaster.client.renderer.familiar.depression;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.client.model.familiar.depression.DepressionModel;
import net.luojiuoscar.isaac_disaster.entity.familiar.impl.depression.DepressionEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public final class DepressionRenderer extends EntityRenderer<DepressionEntity> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            IsaacDisaster.MOD_ID, "textures/entity/familiar/depression.png");
    private final DepressionModel<DepressionEntity> model = new DepressionModel<>();

    public DepressionRenderer(EntityRendererProvider.Context context) { super(context); }

    @Override
    public void render(@NotNull DepressionEntity entity, float yaw, float partialTick,
                       @NotNull PoseStack poses, @NotNull MultiBufferSource buffers, int packedLight) {
        poses.pushPose();
        float scale = DepressionEntity.BASE_SCALE * entity.getVisualScale();
        poses.scale(scale, scale, scale);
        poses.mulPose(Axis.YP.rotationDegrees(180 - entity.getRenderYaw(partialTick)));
        model.renderToBuffer(poses, buffers.getBuffer(model.renderType(TEXTURE)), packedLight,
                OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);
        poses.popPose();
        super.render(entity, yaw, partialTick, poses, buffers, packedLight);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull DepressionEntity entity) { return TEXTURE; }
}
