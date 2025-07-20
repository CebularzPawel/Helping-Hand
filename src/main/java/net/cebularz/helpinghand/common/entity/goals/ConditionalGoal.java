package net.cebularz.helpinghand.common.entity.goals;

import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;
import java.util.function.BooleanSupplier;

/**
 * A wrapper goal that only executes the wrapped goal when a condition is met.
 * This allows for conditional AI behaviors based on dynamic states.
 */
public class ConditionalGoal extends Goal {
    private final Goal wrappedGoal;
    private final BooleanSupplier condition;

    /**
     * Creates a conditional goal that wraps another goal.
     *
     * @param wrappedGoal The goal to execute when condition is true
     * @param condition The condition that must be true for the goal to run
     */
    public ConditionalGoal(Goal wrappedGoal, BooleanSupplier condition) {
        this.wrappedGoal = wrappedGoal;
        this.condition = condition;
        // Copy the flags from the wrapped goal
        this.setFlags(wrappedGoal.getFlags());
    }

    @Override
    public boolean canUse() {
        // First check our condition, then check the wrapped goal's condition
        return this.condition.getAsBoolean() && this.wrappedGoal.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        // Continue only if condition is still true AND wrapped goal wants to continue
        return this.condition.getAsBoolean() && this.wrappedGoal.canContinueToUse();
    }

    @Override
    public boolean isInterruptable() {
        return this.wrappedGoal.isInterruptable();
    }

    @Override
    public void start() {
        this.wrappedGoal.start();
    }

    @Override
    public void stop() {
        this.wrappedGoal.stop();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return this.wrappedGoal.requiresUpdateEveryTick();
    }

    @Override
    public void tick() {
        this.wrappedGoal.tick();
    }

    @Override
    public void setFlags(EnumSet<Flag> flags) {
        super.setFlags(flags);
        this.wrappedGoal.setFlags(flags);
    }

    /**
     * Gets the wrapped goal for direct access if needed
     */
    public Goal getWrappedGoal() {
        return this.wrappedGoal;
    }

    /**
     * Gets the condition for inspection
     */
    public BooleanSupplier getCondition() {
        return this.condition;
    }

    // Static factory methods for common conditions

    /**
     * Creates a conditional goal that only runs when the entity is NOT hired
     */
    public static ConditionalGoal whenNotHired(Goal goal, net.cebularz.helpinghand.common.entity.mercenary.BaseMercenary mercenary) {
        return new ConditionalGoal(goal, () -> !mercenary.isHired());
    }

    /**
     * Creates a conditional goal that only runs when the entity IS hired
     */
    public static ConditionalGoal whenHired(Goal goal, net.cebularz.helpinghand.common.entity.mercenary.BaseMercenary mercenary) {
        return new ConditionalGoal(goal, () -> mercenary.isHired());
    }

    /**
     * Creates a conditional goal that only runs during the day
     */
    public static ConditionalGoal whenDay(Goal goal, net.minecraft.world.entity.Mob mob) {
        return new ConditionalGoal(goal, () -> mob.level().isDay());
    }

    /**
     * Creates a conditional goal that only runs during the night
     */
    public static ConditionalGoal whenNight(Goal goal, net.minecraft.world.entity.Mob mob) {
        return new ConditionalGoal(goal, () -> !mob.level().isDay());
    }

    /**
     * Creates a conditional goal that only runs when health is below a threshold
     */
    public static ConditionalGoal whenLowHealth(Goal goal, net.minecraft.world.entity.LivingEntity entity, float threshold) {
        return new ConditionalGoal(goal, () -> entity.getHealth() / entity.getMaxHealth() < threshold);
    }

    /**
     * Combines multiple conditions with AND logic
     */
    public static ConditionalGoal whenAll(Goal goal, BooleanSupplier... conditions) {
        return new ConditionalGoal(goal, () -> {
            for (BooleanSupplier condition : conditions) {
                if (!condition.getAsBoolean()) {
                    return false;
                }
            }
            return true;
        });
    }

    /**
     * Combines multiple conditions with OR logic
     */
    public static ConditionalGoal whenAny(Goal goal, BooleanSupplier... conditions) {
        return new ConditionalGoal(goal, () -> {
            for (BooleanSupplier condition : conditions) {
                if (condition.getAsBoolean()) {
                    return true;
                }
            }
            return false;
        });
    }
}