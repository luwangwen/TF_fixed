package twilightforest.client.model.entity;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import twilightforest.entity.monster.Wraith;

public class WraithModel extends HumanoidModel<Wraith> {
	private final ModelPart dress;

	public WraithModel(ModelPart root) {
		super(root, RenderType::entityTranslucent);

		this.dress = root.getChild("dress");
	}

	public static LayerDefinition create() {
		MeshDefinition mesh = HumanoidModel.createMesh(CubeDeformation.NONE, 0);
		PartDefinition definition = mesh.getRoot();

		definition.addOrReplaceChild("right_arm", CubeListBuilder.create()
				.texOffs(0, 16)
				.addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F),
			PartPose.offset(-5.0F, 2.0F, 0.0F));

		definition.addOrReplaceChild("left_arm", CubeListBuilder.create().mirror()
				.texOffs(0, 16)
				.addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F),
			PartPose.offset(5.0F, 2.0F, 0.0F));

		definition.addOrReplaceChild("dress", CubeListBuilder.create()
				.texOffs(40, 16)
				.addBox(-4F, 12.0F, -2F, 8, 12, 4),
			PartPose.ZERO);

		return LayerDefinition.create(mesh, 64, 32);
	}

	@Override
	protected Iterable<ModelPart> headParts() {
		return ImmutableList.of(this.head, this.hat);
	}

	@Override
	protected Iterable<ModelPart> bodyParts() {
		return ImmutableList.of(
			this.body,
			this.rightArm,
			this.leftArm,
			this.dress
		);
	}

	@Override
	public void renderToBuffer(PoseStack p_102034_, VertexConsumer p_102035_, int p_102036_, int p_102037_, int color) {
		super.renderToBuffer(p_102034_, p_102035_, p_102036_, p_102037_, FastColor.ARGB32.color((int) (FastColor.ARGB32.alpha(color) * 0.6F), FastColor.ARGB32.red(color), FastColor.ARGB32.green(color), FastColor.ARGB32.blue(color)));
	}

	@Override
	public void setupAnim(Wraith entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

		float var8 = Mth.sin(this.attackTime * Mth.PI);
		float var9 = Mth.sin((1.0F - (1.0F - this.attackTime) * (1.0F - this.attackTime)) * Mth.PI);
		this.rightArm.zRot = 0.0F;
		this.leftArm.zRot = 0.0F;
		this.rightArm.yRot = -(0.1F - var8 * 0.6F);
		this.leftArm.yRot = 0.1F - var8 * 0.6F;
		this.rightArm.xRot = -Mth.HALF_PI;
		this.leftArm.xRot = -Mth.HALF_PI;
		this.rightArm.xRot -= var8 * 1.2F - var9 * 0.4F;
		this.leftArm.xRot -= var8 * 1.2F - var9 * 0.4F;
		this.rightArm.zRot += Mth.cos(ageInTicks * 0.09F) * 0.05F + 0.05F;
		this.leftArm.zRot -= Mth.cos(ageInTicks * 0.09F) * 0.05F + 0.05F;
		this.rightArm.xRot += Mth.sin(ageInTicks * 0.067F) * 0.05F;
		this.leftArm.xRot -= Mth.sin(ageInTicks * 0.067F) * 0.05F;
	}
}
