package twilightforest.client.model.entity;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import twilightforest.entity.monster.Troll;

public class TrollModel extends HumanoidModel<Troll> {

	public TrollModel(ModelPart root) {
		super(root);
	}

	public static LayerDefinition create() {
		MeshDefinition mesh = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
		PartDefinition definition = mesh.getRoot();

		var head = definition.addOrReplaceChild("head", CubeListBuilder.create()
				.texOffs(0, 0)
				.addBox(-5.0F, -8.0F, -3.0F, 10.0F, 10.0F, 10.0F),
			PartPose.offset(0.0F, -9.0F, -6.0F));

		definition.addOrReplaceChild("hat", CubeListBuilder.create(),
			PartPose.ZERO);

		head.addOrReplaceChild("nose", CubeListBuilder.create()
				.texOffs(0, 21)
				.addBox(-2.0F, -2.0F, -2.0F, 4.0F, 8.0F, 4.0F),
			PartPose.offset(0.0F, -2.0F, -4.0F));

		definition.addOrReplaceChild("body", CubeListBuilder.create()
				.texOffs(40, 0)
				.addBox(-8.0F, 0.0F, -5.0F, 16.0F, 26.0F, 10.0F),
			PartPose.offset(0.0F, -14.0F, 0.0F));

		definition.addOrReplaceChild("right_arm", CubeListBuilder.create()
				.texOffs(32, 36)
				.addBox(-5.0F, -2.0F, -3.0F, 6.0F, 22.0F, 6.0F),
			PartPose.offset(-9.0F, -9.0F, 0.0F));

		definition.addOrReplaceChild("left_arm", CubeListBuilder.create().mirror()
				.texOffs(32, 36)
				.addBox(-1.0F, -2.0F, -3.0F, 6.0F, 22.0F, 6.0F),
			PartPose.offset(9.0F, -9.0F, 0.0F));

		definition.addOrReplaceChild("right_leg", CubeListBuilder.create()
				.texOffs(0, 44)
				.addBox(-3.0F, 0.0F, -4.0F, 6.0F, 12.0F, 8.0F),
			PartPose.offset(-5.0F, 12.0F, 0.0F));

		definition.addOrReplaceChild("left_leg", CubeListBuilder.create().mirror()
				.texOffs(0, 44)
				.addBox(-3.0F, 0.0F, -4.0F, 6.0F, 12.0F, 8.0F),
			PartPose.offset(5.0F, 12.0F, 0.0F));

		return LayerDefinition.create(mesh, 128, 64);
	}

	@Override
	public void setupAnim(Troll entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		this.head.yRot = netHeadYaw / (180F / (float) Math.PI);
		this.head.xRot = headPitch / (180F / (float) Math.PI);
		this.hat.yRot = this.head.yRot;
		this.hat.xRot = this.head.xRot;
		this.rightLeg.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
		this.leftLeg.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount;
		this.rightLeg.yRot = 0.0F;
		this.leftLeg.yRot = 0.0F;

		if (entity.isVehicle()) {
			// arms up!
			this.rightArm.xRot = Mth.PI;
			this.leftArm.xRot = Mth.PI;
		} else {
			this.rightArm.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 2.0F * limbSwingAmount * 0.5F;
			this.leftArm.xRot = Mth.cos(limbSwing * 0.6662F) * 2.0F * limbSwingAmount * 0.5F;
		}
		this.rightArm.zRot = 0.0F;
		this.leftArm.zRot = 0.0F;

		if (this.leftArmPose != ArmPose.EMPTY) {
			this.rightArm.xRot += Mth.PI;
		}
		if (this.rightArmPose != ArmPose.EMPTY) {
			this.leftArm.xRot += Mth.PI;
		}

		if (this.attackTime > 0F) {
			float swing = 1.0F - this.attackTime;

			this.rightArm.xRot -= (Mth.PI * swing);
			this.leftArm.xRot -= (Mth.PI * swing);
		}

		this.rightArm.yRot = 0.0F;
		this.leftArm.yRot = 0.0F;

		if (!entity.isVehicle()) {
			this.rightArm.zRot += Mth.cos(ageInTicks * 0.09F) * 0.05F + 0.05F;
			this.leftArm.zRot -= Mth.cos(ageInTicks * 0.09F) * 0.05F + 0.05F;
			this.rightArm.xRot += Mth.sin(ageInTicks * 0.067F) * 0.05F;
			this.leftArm.xRot -= Mth.sin(ageInTicks * 0.067F) * 0.05F;
		}
	}

	@Override
	public void prepareMobModel(Troll entity, float limbSwing, float limbSwingAmount, float partialTicks) {
		if (entity.getTarget() != null) {
			this.rightArm.xRot += Mth.PI;
			this.leftArm.xRot += Mth.PI;
		}
	}
}