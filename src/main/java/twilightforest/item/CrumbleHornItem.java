package twilightforest.item;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.level.BlockEvent;
import twilightforest.init.TFDataMaps;
import twilightforest.init.TFSounds;
import twilightforest.init.TFStats;
import twilightforest.util.WorldUtil;

public class CrumbleHornItem extends Item {

	public CrumbleHornItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		player.startUsingItem(hand);
		player.playSound(TFSounds.QUEST_RAM_AMBIENT.get(), 1.0F, 0.8F);
		return InteractionResultHolder.consume(player.getItemInHand(hand));
	}

	@Override
	public void onUseTick(Level level, LivingEntity living, ItemStack stack, int count) {
		if (count > 10 && count % 5 == 0 && level instanceof ServerLevel serverLevel) {
			int crumbled = this.doCrumble(serverLevel, living);

			if (crumbled > 0) {
				stack.hurtAndBreak(crumbled, living, LivingEntity.getSlotForHand(living.getUsedItemHand()));
			}

			serverLevel.playSound(null, living.getX(), living.getY(), living.getZ(), TFSounds.QUEST_RAM_AMBIENT.get(), living.getSoundSource(), 1.0F, 0.8F);
		}
	}

	@Override
	public UseAnim getUseAnimation(ItemStack stack) {
		return UseAnim.TOOT_HORN;
	}

	@Override
	public int getUseDuration(ItemStack stack, LivingEntity user) {
		return 72000;
	}

	@Override
	public boolean canContinueUsing(ItemStack oldStack, ItemStack newStack) {
		return oldStack.getItem() == newStack.getItem();
	}

	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
		return slotChanged || newStack.getItem() != oldStack.getItem();
	}

	private int doCrumble(ServerLevel serverLevel, LivingEntity living) {

		final double range = 3.0D;
		final double radius = 2.0D;

		Vec3 srcVec = new Vec3(living.getX(), living.getY() + living.getEyeHeight(), living.getZ());
		Vec3 lookVec = living.getLookAngle().scale(range);
		Vec3 destVec = srcVec.add(lookVec);

		AABB crumbleBox = new AABB(destVec.x() - radius, destVec.y() - radius, destVec.z() - radius, destVec.x() + radius, destVec.y() + radius, destVec.z() + radius);

		return this.crumbleBlocksInAABB(serverLevel, living, crumbleBox);
	}

	private int crumbleBlocksInAABB(ServerLevel serverLevel, LivingEntity living, AABB box) {
		int crumbled = 0;
		for (BlockPos pos : WorldUtil.getAllInBB(box)) {
			if (this.crumbleBlock(serverLevel, living, pos)) {
				crumbled++;
				if (living instanceof ServerPlayer player) {
					player.awardStat(TFStats.BLOCKS_CRUMBLED.get());
				}
			}
		}
		return crumbled;
	}

	private boolean crumbleBlock(ServerLevel serverLevel, LivingEntity living, BlockPos pos) {
		BlockState state = serverLevel.getBlockState(pos);
		Block block = state.getBlock();
		var crumbleMap = block.builtInRegistryHolder().getData(TFDataMaps.CRUMBLE_HORN);

		if (state.isAir() || crumbleMap == null) return false;

		if (living instanceof Player) {
			if (NeoForge.EVENT_BUS.post(new BlockEvent.BreakEvent(serverLevel, pos, state, (Player) living)).isCanceled())
				return false;
		}

		if (crumbleMap.result() == Blocks.AIR) {
			if (serverLevel.getRandom().nextFloat() < crumbleMap.chanceToCrumble()) {
				if (living instanceof Player player) {
					if (block.canHarvestBlock(state, serverLevel, pos, (Player) living)) {
						serverLevel.removeBlock(pos, false);
						block.playerDestroy(serverLevel, (Player) living, pos, state, serverLevel.getBlockEntity(pos), ItemStack.EMPTY);
						serverLevel.levelEvent(2001, pos, Block.getId(state));
						if (player instanceof ServerPlayer) {
							player.awardStat(Stats.ITEM_USED.get(this));
						}
						return true;
					}
				} else if (EventHooks.canEntityGrief(serverLevel, living)) {
					serverLevel.destroyBlock(pos, true);
					return true;
				}
			}
		} else {
			if (serverLevel.getRandom().nextFloat() < crumbleMap.chanceToCrumble()) {
				serverLevel.setBlock(pos, crumbleMap.result().withPropertiesOf(state), 3);
				serverLevel.levelEvent(2001, pos, Block.getId(state));
				if (living instanceof ServerPlayer player) {
					player.awardStat(Stats.ITEM_USED.get(this));
				}
				return true;
			}
		}
		return false;
	}
}