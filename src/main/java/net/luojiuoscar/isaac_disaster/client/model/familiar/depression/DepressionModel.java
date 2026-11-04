package net.luojiuoscar.isaac_disaster.client.model.familiar.depression;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

public final class DepressionModel<T extends Entity> extends EntityModel<T> {
    public DepressionModel() { super(RenderType::entityCutout); }

    @Override
    public void setupAnim(@NotNull T entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {}

    @Override
    public void renderToBuffer(@NotNull PoseStack poses, @NotNull VertexConsumer buffer,
                               int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        var pose = poses.last();
        for (float[] quad : DepressionMesh.QUADS) {
            for (int vertex = 0; vertex < 4; vertex++) {
                int offset = vertex * 8;
                buffer.vertex(pose.pose(), quad[offset], quad[offset + 1], quad[offset + 2])
                        .color(red, green, blue, alpha).uv(quad[offset + 3], quad[offset + 4])
                        .overlayCoords(packedOverlay).uv2(packedLight)
                        .normal(pose.normal(), quad[offset + 5], quad[offset + 6], quad[offset + 7]).endVertex();
            }
        }
    }
}
