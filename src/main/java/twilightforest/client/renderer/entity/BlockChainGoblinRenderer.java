package twilightforest.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import twilightforest.TwilightForestMod;
import twilightforest.client.model.TFModelLayers;
import twilightforest.client.model.entity.BlockChainGoblinModel;
import twilightforest.client.model.entity.ChainModel;
import twilightforest.client.model.entity.SpikeBlockModel;
import twilightforest.entity.monster.BlockChainGoblin;

public class BlockChainGoblinRenderer<T extends BlockChainGoblin, M extends BlockChainGoblinModel<T>> extends HumanoidMobRenderer<T, M> {

	private static final ResourceLocation GOBLIN_TEXTURE = TwilightForestMod.getModelTexture("blockgoblin.png");
	private static final ResourceLocation BLOCK_AND_CHAIN_TEXTURE = TwilightForestMod.getModelTexture("block_and_chain.png");

	private final Model model;
	private final Model chainModel;

	public BlockChainGoblinRenderer(EntityRendererProvider.Context manager, M goblinModel, float shadowSize) {
		super(manager, goblinModel, shadowSize);
		this.model = new SpikeBlockModel(manager.bakeLayer(TFModelLayers.CHAIN_BLOCK));
		this.chainModel = new ChainModel(manager.bakeLayer(TFModelLayers.CHAIN));
	}

	@Override
	public void render(T goblin, float yaw, float partialTicks, PoseStack stack, MultiBufferSource buffer, int light) {
		super.render(goblin, yaw, partialTicks, stack, buffer, light);

		stack.pushPose();

		double blockInX = (goblin.block.getX() - goblin.getX());
		double blockInY = (goblin.block.getY() - goblin.getY());
		double blockInZ = (goblin.block.getZ() - goblin.getZ());

		VertexConsumer consumer = buffer.getBuffer(this.model.renderType(BLOCK_AND_CHAIN_TEXTURE));
		stack.translate(blockInX, blockInY, blockInZ);

		float pitch = goblin.xRotO + (goblin.getXRot() - goblin.xRotO) * partialTicks;
		stack.mulPose(Axis.YP.rotationDegrees(180 - Mth.wrapDegrees(yaw)));
		stack.mulPose(Axis.XP.rotationDegrees(pitch));

		stack.scale(-1.0F, -1.0F, 1.0F);

		this.model.renderToBuffer(stack, consumer, light, OverlayTexture.NO_OVERLAY);
		stack.popPose();

		stack.pushPose();
		stack.translate(0.0D, goblin.getEyeHeight(), 0.0D);
		Vec3 xyz = goblin.block.getEyePosition(partialTicks).subtract(goblin.getEyePosition(partialTicks)).multiply(1.0D, 0.5D, 1.0D);
		BlockChainRenderer.renderChain(goblin.block, xyz, 0.00D, yaw, partialTicks, stack, buffer, light, this.chainModel);
		BlockChainRenderer.renderChain(goblin.block, xyz, 0.25D, yaw, partialTicks, stack, buffer, light, this.chainModel);
		BlockChainRenderer.renderChain(goblin.block, xyz, 0.50D, yaw, partialTicks, stack, buffer, light, this.chainModel);
		BlockChainRenderer.renderChain(goblin.block, xyz, 0.75D, yaw, partialTicks, stack, buffer, light, this.chainModel);
		stack.popPose();
	}

	@Override
	public boolean shouldRender(T entity, Frustum clippingHelper, double camX, double camY, double camZ) {
		if (super.shouldRender(entity, clippingHelper, camX, camY, camZ)) {
			return true;
		} else {

			Vec3 vec3d = this.getPosition(entity.block, entity.block.getBbHeight() * 0.5D, 1.0F);
			Vec3 vec3d1 = this.getPosition(entity.block, entity.block.getEyeHeight(), 1.0F);
			return clippingHelper.isVisible(new AABB(vec3d1.x, vec3d1.y, vec3d1.z, vec3d.x, vec3d.y, vec3d.z));
		}
	}

	private Vec3 getPosition(Entity entity, double p_177110_2_, float p_177110_4_) {
		// [VanillaCopy] From GuardianRenderer
		double d0 = Mth.lerp(p_177110_4_, entity.xOld, entity.getX());
		double d1 = Mth.lerp(p_177110_4_, entity.yOld, entity.getY()) + p_177110_2_;
		double d2 = Mth.lerp(p_177110_4_, entity.zOld, entity.getZ());
		return new Vec3(d0, d1, d2);
	}

	@Override
	public ResourceLocation getTextureLocation(T entity) {
		return GOBLIN_TEXTURE;
	}
}
