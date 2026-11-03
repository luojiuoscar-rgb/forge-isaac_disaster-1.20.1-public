// Blockbench model for Minecraft Forge 1.20.1 (Mojang mappings).

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class BobbyBombModel<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("isaac_disaster", "bobby_bomb"), "main");
	private final ModelPart bomb_root;
	private final ModelPart winding_key;

	public BobbyBombModel(ModelPart root) {
		this.bomb_root = root.getChild("bomb_root");
		this.winding_key = this.bomb_root.getChild("winding_key");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition bomb_root = partdefinition.addOrReplaceChild("bomb_root", CubeListBuilder.create().texOffs(0, 0).addBox(-7.0F, -15.0F, -7.0F, 14.0F, 14.0F, 14.0F, new CubeDeformation(0.0F))
		.texOffs(0, 29).addBox(-7.0F, -14.0F, -8.0F, 14.0F, 12.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(32, 29).addBox(-7.0F, -14.0F, 7.0F, 14.0F, 12.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(60, 0).addBox(7.0F, -14.0F, -7.0F, 1.0F, 12.0F, 14.0F, new CubeDeformation(0.0F))
		.texOffs(92, 0).addBox(-8.0F, -14.0F, -7.0F, 1.0F, 12.0F, 14.0F, new CubeDeformation(0.0F))
		.texOffs(0, 45).addBox(-6.0F, -16.0F, -6.0F, 12.0F, 1.0F, 12.0F, new CubeDeformation(0.0F))
		.texOffs(50, 45).addBox(-6.0F, -1.0F, -6.0F, 12.0F, 1.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition fuse_tip_r1 = bomb_root.addOrReplaceChild("fuse_tip_r1", CubeListBuilder.create().texOffs(110, 42).addBox(-0.5F, -1.75F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.8F, -17.3F, 0.0F, 0.0F, 0.0F, 1.309F));

		PartDefinition fuse_lower_r1 = bomb_root.addOrReplaceChild("fuse_lower_r1", CubeListBuilder.create().texOffs(102, 42).addBox(-0.5F, -1.75F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -15.85F, 0.0F, 0.0F, 0.0F, 0.5236F));

		PartDefinition winding_key = bomb_root.addOrReplaceChild("winding_key", CubeListBuilder.create().texOffs(102, 28).addBox(-1.0F, -1.0F, -2.75F, 2.0F, 2.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(80, 28).addBox(3.0F, -1.5F, 0.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(86, 28).addBox(-4.0F, -1.5F, 0.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(92, 28).addBox(-1.0F, -1.0F, 0.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(64, 35).addBox(1.0F, -2.5F, 0.0F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(74, 35).addBox(-4.0F, -2.5F, 0.0F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(84, 35).addBox(1.0F, 1.5F, 0.0F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(94, 35).addBox(-4.0F, 1.5F, 0.0F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(102, 35).addBox(1.0F, -1.5F, 0.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(108, 35).addBox(-2.0F, -1.5F, 0.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -8.0F, 10.5F));

		return LayerDefinition.create(meshdefinition, 128, 64);
	}

	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		bomb_root.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}