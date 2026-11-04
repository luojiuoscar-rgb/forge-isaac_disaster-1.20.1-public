package net.luojiuoscar.isaac_disaster.client.renderer.familiar.headless_baby;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.client.model.familiar.headless_baby.HeadlessBabyModel;
import net.luojiuoscar.isaac_disaster.entity.familiar.impl.headless_baby.HeadlessBabyEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public final class HeadlessBabyRenderer extends EntityRenderer<HeadlessBabyEntity> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            IsaacDisaster.MOD_ID, "textures/entity/familiar/headless_baby.png");
    private final HeadlessBabyModel<HeadlessBabyEntity> model;

    public HeadlessBabyRenderer(EntityRendererProvider.Context context) {
        super(context);
        model = new HeadlessBabyModel<>(context.bakeLayer(HeadlessBabyModel.LAYER_LOCATION));
    }

    @Override
    public void render(@NotNull HeadlessBabyEntity entity, float yaw, float partialTick,
                       @NotNull PoseStack poses, @NotNull MultiBufferSource buffers, int packedLight) {
        poses.pushPose();
        float scale = HeadlessBabyEntity.BASE_SCALE * entity.getVisualScale();
        poses.scale(scale, scale, scale);
        poses.mulPose(Axis.YP.rotationDegrees(180 - entity.getRenderYaw(partialTick)));
        // Keep Bobby's body alignment; removing the head must not lift the remaining rig.
        poses.translate(0, 14.5F / 16, 0);
        poses.scale(-1, -1, 1);
        model.renderToBuffer(poses, buffers.getBuffer(model.renderType(TEXTURE)), packedLight,
                OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);
        poses.popPose();
        super.render(entity, yaw, partialTick, poses, buffers, packedLight);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull HeadlessBabyEntity entity) { return TEXTURE; }
}
