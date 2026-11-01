package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.event.IsaacFlightEvents;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.luojiuoscar.isaac_disaster.system.flight.IsaacFlightController;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public final class HowToJump implements IAbilityEffect {
    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        if (!(context.getEntity() instanceof ServerPlayer player) || !player.isAlive() || player.isRemoved()) return false;
        player.stopRiding();
        player.stopFallFlying();
        if (player.getAbilities().flying) {
            player.getAbilities().flying = false;
            player.onUpdateAbilities();
        }
        IsaacFlightController.getState(player).ifPresent(state -> {
            if (!state.isThrusting()) return;
            state.setEnabled(false);
            IsaacFlightController.stopThrust(player);
            IsaacFlightEvents.sendState(player);
        });
        player.setDeltaMovement(launchVelocity(player.getLookAngle()));
        player.hasImpulse = true;
        player.connection.send(new ClientboundSetEntityMotionPacket(player));
        return true;
    }

    static Vec3 launchVelocity(Vec3 look) {
        return look.scale(1.5).add(0, .75, 0);
    }
}
