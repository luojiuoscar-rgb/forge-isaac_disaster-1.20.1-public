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

public class BrimstoneBombModel<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("isaac_disaster", "brimstone_bomb"), "main");
	private final ModelPart bomb_root;
	private final ModelPart horn_left;
	private final ModelPart horn_right;

	public BrimstoneBombModel(ModelPart root) {
		this.bomb_root = root.getChild("bomb_root");
		this.horn_left = this.bomb_root.getChild("horn_left");
		this.horn_right = this.bomb_root.getChild("horn_right");
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

		PartDefinition horn_left = bomb_root.addOrReplaceChild("horn_left", CubeListBuilder.create(), PartPose.offset(8.0F, -14.5F, -1.0F));

		PartDefinition horn_left_tip_r1 = horn_left.addOrReplaceChild("horn_left_tip_r1", CubeListBuilder.create().texOffs(100, 58).mirror().addBox(-0.5F, -2.0F, -0.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(5.0F, -1.775F, 0.0F, 0.0F, 0.0F, -0.637F));

		PartDefinition horn_left_curve_r1 = horn_left.addOrReplaceChild("horn_left_curve_r1", CubeListBuilder.create().texOffs(116, 48).mirror().addBox(-1.5F, -2.0F, -1.5F, 3.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(4.475F, 1.375F, 0.0F, 0.0F, 0.0F, 1.0638F));

		PartDefinition horn_left_root_r1 = horn_left.addOrReplaceChild("horn_left_root_r1", CubeListBuilder.create().texOffs(100, 48).mirror().addBox(-2.0F, -3.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.575F, 0.4F, 0.0F, 0.0F, 0.0F, -0.7295F));

		PartDefinition horn_right = bomb_root.addOrReplaceChild("horn_right", CubeListBuilder.create(), PartPose.offset(-8.0F, -14.5F, -1.0F));

		PartDefinition horn_right_tip_r1 = horn_right.addOrReplaceChild("horn_right_tip_r1", CubeListBuilder.create().texOffs(100, 58).addBox(-0.5F, -2.0F, -0.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.0F, -1.775F, 0.0F, 0.0F, 0.0F, 0.637F));

		PartDefinition horn_right_curve_r1 = horn_right.addOrReplaceChild("horn_right_curve_r1", CubeListBuilder.create().texOffs(116, 48).addBox(-1.5F, -2.0F, -1.5F, 3.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.475F, 1.375F, 0.0F, 0.0F, 0.0F, -1.0638F));

		PartDefinition horn_right_root_r1 = horn_right.addOrReplaceChild("horn_right_root_r1", CubeListBuilder.create().texOffs(100, 48).addBox(-2.0F, -3.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.575F, 0.4F, 0.0F, 0.0F, 0.0F, 0.7295F));

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