package net.luojiuoscar.isaac_disaster.client.renderer.familiar.item_giving;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.luojiuoscar.isaac_disaster.entity.familiar.base.item_giving.AbstractItemGivingFamiliarEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class ItemGivingFamiliarRenderer<T extends AbstractItemGivingFamiliarEntity> extends EntityRenderer<T> {
    private final ResourceLocation texture;
    private final EntityModel<? super T> model;

    public ItemGivingFamiliarRenderer(EntityRendererProvider.Context context, ResourceLocation texture,
                                     EntityModel<? super T> model) {
        super(context);
        this.texture = texture;
        this.model = model;
    }

    @Override
    public void render(@NotNull T entity, float entityYaw, float partialTick,
                       @NotNull PoseStack poses, @NotNull MultiBufferSource buffers, int packedLight) {
        poses.pushPose();
        float scale = entity.getVisualScale();
        poses.scale(scale, scale, scale);
        poses.mulPose(Axis.YP.rotationDegrees(180.0F - entity.getRenderYaw(partialTick)));
        poses.scale(-1, -1, 1);
        model.setupAnim(entity, 0, 0, entity.tickCount + partialTick, 0, 0);
        model.renderToBuffer(poses, buffers.getBuffer(model.renderType(texture)), packedLight,
                OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);
        poses.popPose();
        super.render(entity, entityYaw, partialTick, poses, buffers, packedLight);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull T entity) {
        return texture;
    }
}
