/*
 * All Rights Reserved (c) MoriyaShiine
 */

package moriyashiine.extraorigins.mixin;

import io.github.apace100.apoli.component.PowerHolderComponent;
import moriyashiine.extraorigins.common.power.PreventBlockSlownessPower;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class EntityMixin {
	@Inject(method = "slowMovement", at = @At("HEAD"), cancellable = true)
	private void extraorigins$preventBlockSlowness(BlockState state, Vec3d multiplier, CallbackInfo ci) {
		PowerHolderComponent.getPowers((Entity) (Object) this, PreventBlockSlownessPower.class).forEach(preventBlockSlownessPower -> {
			if (preventBlockSlownessPower.shouldPreventSlowness(state.getBlock())) {
				ci.cancel();
			}
		});
	}
}
