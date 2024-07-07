package twilightforest.entity.projectile;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import twilightforest.init.TFDamageTypes;
import twilightforest.init.TFEntities;

public class IceSnowball extends TFThrowable implements ItemSupplier {

	private static final int DAMAGE = 2;

	public IceSnowball(EntityType<? extends IceSnowball> type, Level world) {
		super(type, world);
	}

	public IceSnowball(Level world, LivingEntity thrower) {
		super(TFEntities.ICE_SNOWBALL.get(), world, thrower);
	}

	@Override
	public void tick() {
		super.tick();
		this.makeTrail(ParticleTypes.ITEM_SNOWBALL, 2);
	}

	@Override
	protected double getDefaultGravity() {
		return 0.006F;
	}

	@Override
	public boolean hurt(DamageSource source, float amount) {
		super.hurt(source, amount);
		this.die();
		return true;
	}

	@Override
	public void handleEntityEvent(byte id) {
		if (id == 3) {
			for (int j = 0; j < 8; ++j) {
				this.level().addParticle(ParticleTypes.ITEM_SNOWBALL, this.getX(), this.getY(), this.getZ(), 0.0D, 0.0D, 0.0D);
			}
		} else {
			super.handleEntityEvent(id);
		}
	}

	@Override
	protected void onHitEntity(EntityHitResult result) {
		super.onHitEntity(result);
		Entity target = result.getEntity();
		if (!this.level().isClientSide() && target instanceof LivingEntity)
			target.hurt(TFDamageTypes.getIndirectEntityDamageSource(this.level(), TFDamageTypes.SNOWBALL_FIGHT, this, this.getOwner()), DAMAGE);
	}

	@Override
	protected void onHit(HitResult result) {
		super.onHit(result);
		this.die();
	}

	private void die() {
		if (!this.level().isClientSide()) {
			this.level().broadcastEntityEvent(this, (byte) 3);
			this.discard();
		}
	}

	@Override
	public ItemStack getItem() {
		return new ItemStack(Items.SNOWBALL);
	}
}