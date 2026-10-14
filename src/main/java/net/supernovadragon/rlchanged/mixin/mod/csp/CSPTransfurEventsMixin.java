package net.supernovadragon.rlchanged.mixin.mod.csp;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.ltxprogrammer.changed.entity.TransfurCause;
import net.ltxprogrammer.changed.entity.ai.ImmediateTransfurDecision;
import net.ltxprogrammer.changed.entity.variant.TransfurVariant;
import net.ltxprogrammer.changed.init.ChangedGameRules;
import net.ltxprogrammer.changed.process.ProcessTransfur;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.stonenibbler.changed_survive_protocol.common.data.CSPPlayerData;
import net.stonenibbler.changed_survive_protocol.common.event.CSPTransfurEvents;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;
import java.util.UUID;

import static net.ltxprogrammer.changed.init.ChangedGameRules.RULE_KEEP_BRAIN;


@Mixin(CSPTransfurEvents.class)
public class CSPTransfurEventsMixin {

	@Inject( method = "onKeepConscious", at = @At("HEAD"), cancellable = true, remap = false)
	private static void RLChangedCSP_onKeepConscious(ProcessTransfur.KeepConsciousEvent event, CallbackInfo ci) {
		ci.cancel();
	}

	@Shadow
	static TransfurVariant<?> resolveVariant(String strainId) {throw new UnsupportedOperationException("Implemented via mixin");}
	@Shadow
	private static void activateLatexNeeds(ServerPlayer player, CSPPlayerData data) {throw new UnsupportedOperationException("Implemented via mixin");}

	@Shadow
	@Final
	private static Set<UUID> ALLOWED_IMMEDIATE_TRANSFURS;
	@Inject(method = "forceUncontrolledTransfur", remap = false, cancellable = true, at=@At(
		value = "INVOKE",
		target = "Lnet/ltxprogrammer/changed/process/ProcessTransfur;transfur(Lnet/minecraft/world/entity/LivingEntity;Lnet/ltxprogrammer/changed/entity/ai/ImmediateTransfurDecision;)V"
	))
	private static void RLChangedCSP_forceUncontrolledTransfur(ServerPlayer player, CSPPlayerData data, CallbackInfo ci) {
		if(player.level().getGameRules().getBoolean(RULE_KEEP_BRAIN)) {
			ProcessTransfur.transfur(player, ImmediateTransfurDecision.safe(resolveVariant(data.getStrainId()), TransfurCause.DEFAULT, latex -> {
				activateLatexNeeds(player, data);
				data.setCoverage(0);
			}));
		} else {
			ProcessTransfur.transfur(player, ImmediateTransfurDecision.unsafe(resolveVariant(data.getStrainId()), TransfurCause.DEFAULT, latex -> {
				activateLatexNeeds(player, data);
				data.setCoverage(0);
			}));
		}
		ALLOWED_IMMEDIATE_TRANSFURS.remove(player.getUUID());
		ci.cancel();
	}


}
