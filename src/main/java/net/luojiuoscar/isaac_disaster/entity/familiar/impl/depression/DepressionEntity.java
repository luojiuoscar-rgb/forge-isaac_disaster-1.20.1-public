package net.luojiuoscar.isaac_disaster.entity.familiar.impl.depression;

import net.luojiuoscar.isaac_disaster.entity.familiar.base.chain.AbstractChainFamiliarEntity;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeTrigger;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.SimpleTrigger;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public final class DepressionEntity extends AbstractChainFamiliarEntity {
    public static final float BASE_SCALE = 0.6F;
    private static final CompositeTrigger TRIGGER = new CompositeTrigger(List.of(
            new SimpleTrigger(ModTriggerTypes.EMTPY, ModExecutableEffects.DEPRESSION_TRAIL)
    ));
    private long nextTrailTick = Long.MIN_VALUE;

    public DepressionEntity(EntityType<? extends DepressionEntity> type, Level level) { super(type, level); }

    @Override
    protected void tickChainServer(ServerPlayer owner, Vec3 movement) {
        super.tickChainServer(owner, movement);
        long tick = level().getGameTime();
        if (tick < nextTrailTick || isRecallPending() || !isValidFamiliar()) return;
        nextTrailTick = tick + 2;
        var context = new ExecutableEffectContext(owner);
        context.set(ContextKeys.TARGET_POSITION, position());
        context.set(ContextKeys.AMPLIFIER, 1 + getEnhancement());
        TRIGGER.fire(context, ModTriggerTypes.EMTPY);
    }
}
