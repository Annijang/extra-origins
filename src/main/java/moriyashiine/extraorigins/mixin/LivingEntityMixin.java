package moriyashiine.extraorigins.mixin;

import io.github.apace100.origins.component.OriginComponent;
import moriyashiine.extraorigins.common.interfaces.BabyAccessor;
import moriyashiine.extraorigins.common.power.ModifySizePower;
import moriyashiine.extraorigins.common.registry.EOPowers;
import moriyashiine.extraorigins.common.registry.EOTags;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("ConstantConditions")
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
	@Shadow
	public abstract boolean addStatusEffect(StatusEffectInstance effect);
	
	public LivingEntityMixin(EntityType<?> type, World world) {
		super(type, world);
	}
	
	@Inject(method = "tick", at = @At("TAIL"))
	private void tick(CallbackInfo callbackInfo) {
		if (EOPowers.HOMESICK.isActive(this) && !world.getDimension().isPiglinSafe()) {
			addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 10, 1, true, false));
		}
	}
	
	@ModifyVariable(method = "damage", at = @At("HEAD"))
	private float damage(float amount, DamageSource source) {
		Entity attacker = source.getAttacker();
		if (EOPowers.CROSSBOW_MASTER.isActive(attacker)) {
			Entity projectile = source.getSource();
			if (projectile instanceof PersistentProjectileEntity && ((PersistentProjectileEntity) projectile).isShotFromCrossbow()) {
				amount *= 1.75f;
			}
		}
		if (EOPowers.ALL_THAT_GLITTERS.get(this) != null) {
			int armorPieces = 0;
			for (ItemStack stack : getArmorItems()) {
				if (EOTags.GOLDEN_ARMOR.contains(stack.getItem())) {
					armorPieces++;
				}
			}
			amount *= (1 - (armorPieces * 0.08f));
		}
		return amount;
	}
	
	@Inject(method = "getGroup", at = @At("HEAD"), cancellable = true)
	private void getGroup(CallbackInfoReturnable<EntityGroup> callbackInfo) {
		if (EOPowers.HOMESICK.isActive(this) && !world.getDimension().isPiglinSafe()) {
			callbackInfo.setReturnValue(EntityGroup.UNDEAD);
		}
	}
	
	@Inject(method = "canHaveStatusEffect", at = @At("HEAD"), cancellable = true)
	private void canHaveStatusEffect(StatusEffectInstance effect, CallbackInfoReturnable<Boolean> callbackInfo) {
		if (EOPowers.EFFECT_IMMUNITY.isActive(this)) {
			callbackInfo.setReturnValue(effect.getEffectType() == StatusEffects.BAD_OMEN);
		}
	}
	
	@Inject(method = "isAffectedBySplashPotions", at = @At("HEAD"), cancellable = true)
	private void isAffectedBySplashPotions(CallbackInfoReturnable<Boolean> callbackInfo) {
		if (EOPowers.EFFECT_IMMUNITY.isActive(this)) {
			callbackInfo.setReturnValue(false);
		}
	}
	
	@Inject(method = "isBaby", at = @At("HEAD"), cancellable = true)
	private void isBaby(CallbackInfoReturnable<Boolean> callbackInfo) {
		if (this instanceof BabyAccessor && ((BabyAccessor) this).getBaby()) {
			callbackInfo.setReturnValue(true);
		}
	}
	
	@Inject(method = "getJumpVelocity", at = @At("RETURN"), cancellable = true)
	private void getJumpVelocity(CallbackInfoReturnable<Float> callbackInfo) {
		if (((Object) this) instanceof PlayerEntity && OriginComponent.hasPower(this, ModifySizePower.class)) {
			callbackInfo.setReturnValue(callbackInfo.getReturnValue() * OriginComponent.getPowers(this, ModifySizePower.class).get(0).scale * 2);
		}
	}
}
