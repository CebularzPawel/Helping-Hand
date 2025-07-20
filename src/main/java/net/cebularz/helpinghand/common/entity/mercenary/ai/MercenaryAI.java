package net.cebularz.helpinghand.common.entity.mercenary.ai;

import net.cebularz.helpinghand.common.entity.mercenary.BaseMercenary;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.pathfinder.Path;

import java.util.EnumSet;
import java.util.Objects;

public class MercenaryAI
{
    public static class MercenaryAttackEnemy extends Goal {
        private final BaseMercenary entity;
        private LivingEntity target;
        private final boolean doRangedAttack;
        private final int damageAmplifier;
        private int ticksUntilNextAttack;
        private final int attackInterval = 20;
        private final double speedModifier = 1.2D;
        private final double attackReachSqr = 4.0D;
        private final double rangedAttackReachSqr = 100.0D;
        private int lastSeenHirerAttackTimestamp = -1;

        public MercenaryAttackEnemy(BaseMercenary entity, int damageAmplifier, boolean ranged) {
            this.entity = entity;
            this.damageAmplifier = damageAmplifier;
            this.doRangedAttack = ranged;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        public MercenaryAttackEnemy(BaseMercenary entity, int damageAmplifier) {
            this(entity, damageAmplifier, false);
        }

        private Player getHirer() {
            if (!entity.isHired() || entity.getCurrentContract() == null) {
                return null;
            }
            return entity.level().getPlayerByUUID(entity.getCurrentContract().getHirer());
        }

        @Override
        public boolean canUse() {
            LivingEntity currentTarget = this.entity.getTarget();
            if (currentTarget != null && currentTarget.isAlive() && canAttackTarget(currentTarget)) {
                this.target = currentTarget;
                return true;
            }

            Player hirer = getHirer();
            if (hirer == null) {
                return false;
            }

            LivingEntity lastHurt = hirer.getLastHurtMob();
            int lastHurtTimestamp = hirer.getLastHurtMobTimestamp();

            if (lastHurt != null && lastHurt.isAlive() && lastHurtTimestamp != lastSeenHirerAttackTimestamp && canAttackTarget(lastHurt)) {
                this.target = lastHurt;
                this.lastSeenHirerAttackTimestamp = lastHurtTimestamp;
                return true;
            }

            return false;
        }

        private boolean canAttackTarget(LivingEntity target) {
            Player hirer = getHirer();
            if (hirer == null || target == hirer) {
                return false;
            }

            if (!this.entity.isWithinRestriction(target.blockPosition())) {
                return false;
            }

            if (target instanceof Player player) {
                return !player.isSpectator() && !player.isCreative();
            }

            return true;
        }

        @Override
        public boolean canContinueToUse() {
            if (this.target == null || !this.target.isAlive()) {
                return false;
            }

            return canAttackTarget(this.target);
        }

        @Override
        public void start() {
            if (this.target != null) {
                this.entity.setTarget(this.target);
                this.entity.setAggressive(true);
                this.ticksUntilNextAttack = 0;
            }
        }

        @Override
        public void stop() {
            this.entity.setTarget(null);
            this.entity.setAggressive(false);
            this.entity.getNavigation().stop();
            this.target = null;
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            if (this.target == null) {
                return;
            }

            double distanceToTargetSqr = this.entity.distanceToSqr(this.target.getX(), this.target.getY(), this.target.getZ());
            boolean canSee = this.entity.getSensing().hasLineOfSight(this.target);

            this.entity.getLookControl().setLookAt(this.target, 30.0F, 30.0F);

            if (doRangedAttack) {
                handleRangedCombat(distanceToTargetSqr, canSee);
            } else {
                handleMeleeCombat(distanceToTargetSqr, canSee);
            }
        }

        private void handleMeleeCombat(double distanceToTargetSqr, boolean canSee) {
            this.ticksUntilNextAttack--;

            if (distanceToTargetSqr <= this.attackReachSqr) {
                this.entity.getNavigation().stop();

                if (this.ticksUntilNextAttack <= 0 && canSee) {
                    this.entity.swing(this.entity.getUsedItemHand());
                    this.entity.doHurtTarget(this.target);
                    this.ticksUntilNextAttack = this.attackInterval;
                }
            } else {
                this.entity.getNavigation().moveTo(this.target, this.speedModifier);
                if (this.ticksUntilNextAttack < 0) {
                    this.ticksUntilNextAttack = 0;
                }
            }
        }

        private void handleRangedCombat(double distanceToTargetSqr, boolean canSee) {
            double optimalRangeSqr = 36.0D;
            double minRangeSqr = 9.0D;

            if (distanceToTargetSqr > rangedAttackReachSqr) {
                this.entity.getNavigation().moveTo(this.target, this.speedModifier);
            } else if (distanceToTargetSqr < minRangeSqr) {
                double deltaX = this.entity.getX() - this.target.getX();
                double deltaZ = this.entity.getZ() - this.target.getZ();
                double moveX = this.entity.getX() + deltaX;
                double moveZ = this.entity.getZ() + deltaZ;
                this.entity.getNavigation().moveTo(moveX, this.entity.getY(), moveZ, this.speedModifier);
            } else if (distanceToTargetSqr > optimalRangeSqr) {
                this.entity.getNavigation().moveTo(this.target, this.speedModifier * 0.8);
            } else {
                this.entity.getNavigation().stop();
            }

            if (--this.ticksUntilNextAttack <= 0) {
                if (!canSee || distanceToTargetSqr > rangedAttackReachSqr) {
                    this.ticksUntilNextAttack = 10;
                    return;
                }

                float distanceFactor = (float) Math.sqrt(distanceToTargetSqr);
                float clampedDistance = net.minecraft.util.Mth.clamp(distanceFactor / 10.0F, 0.1F, 1.0F);

                performRangedAttack(this.target, clampedDistance);
                this.ticksUntilNextAttack = this.attackInterval;
            }
        }

        private void performRangedAttack(LivingEntity target, float distanceFactor) {
            this.entity.performRangedAttack(target, distanceFactor);
        }
    }

    public static class MercenaryFollowOwner extends Goal {
        private final BaseMercenary mercenary;
        private LivingEntity owner;
        private final double speedModifier;
        private final float startDistance;
        private final float stopDistance;
        private final boolean teleportToOwner;
        private int recheckOwnerTimer = 0;

        public MercenaryFollowOwner(BaseMercenary mercenary, double speedModifier, float startDistance, float stopDistance, boolean teleportToOwner) {
            this.mercenary = mercenary;
            this.speedModifier = speedModifier;
            this.startDistance = startDistance;
            this.stopDistance = stopDistance;
            this.teleportToOwner = teleportToOwner;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        public MercenaryFollowOwner(BaseMercenary mercenary, double speedModifier, float startDistance, float stopDistance) {
            this(mercenary, speedModifier, startDistance, stopDistance, true);
        }

        private LivingEntity getOwner() {
            if (!mercenary.isHired() || mercenary.getCurrentContract() == null) {
                return null;
            }
            return mercenary.level().getPlayerByUUID(mercenary.getCurrentContract().getHirer());
        }

        @Override
        public boolean canUse() {
            if (this.mercenary.getTarget() != null) {
                return false;
            }

            LivingEntity owner = getOwner();
            if (owner == null || owner.isSpectator()) {
                return false;
            }

            if (this.mercenary.distanceToSqr(owner) < (double)(this.startDistance * this.startDistance)) {
                return false;
            }

            this.owner = owner;
            return true;
        }

        @Override
        public boolean canContinueToUse() {
            if (this.mercenary.getTarget() != null) {
                return false;
            }

            if (this.owner == null || !this.owner.isAlive()) {
                return false;
            }

            if (this.mercenary.getNavigation().isDone()) {
                return false;
            }

            if (!mercenary.isHired()) {
                return false;
            }

            return !(this.mercenary.distanceToSqr(this.owner) <= (double)(this.stopDistance * this.stopDistance));
        }

        @Override
        public void start() {
            if (this.owner != null) {
                this.mercenary.getNavigation().moveTo(this.owner, this.speedModifier);
            }
        }

        @Override
        public void stop() {
            this.owner = null;
            this.mercenary.getNavigation().stop();
        }

        @Override
        public void tick() {
            if (this.owner == null) {
                return;
            }

            if (++this.recheckOwnerTimer >= 20) {
                LivingEntity currentOwner = getOwner();
                if (currentOwner != this.owner) {
                    this.owner = currentOwner;
                    if (this.owner == null) {
                        return;
                    }
                }
                this.recheckOwnerTimer = 0;
            }

            this.mercenary.getLookControl().setLookAt(this.owner, 10.0F, (float)this.mercenary.getMaxHeadXRot());

            if (this.mercenary.getNavigation().getTargetPos() != null) {
                if (this.mercenary.getNavigation().getTargetPos().distManhattan(this.owner.blockPosition()) > 12) {
                    this.mercenary.getNavigation().moveTo(this.owner, this.speedModifier);
                }
            } else {
                this.mercenary.getNavigation().moveTo(this.owner, this.speedModifier);
            }

            if (this.teleportToOwner && this.mercenary.distanceToSqr(this.owner) >= 144.0D) {
                this.tryTeleportToOwner();
            }
        }

        private void tryTeleportToOwner() {
            if (this.owner instanceof Player) {
                this.mercenary.moveTo(owner.getX(), owner.getY(), owner.getZ(), owner.getYRot(), owner.getXRot());
            }
        }
    }

    public static class MercenaryDefendOwner extends TargetGoal {
        private final BaseMercenary mercenary;
        private LivingEntity attacker;
        private int timestamp;

        public MercenaryDefendOwner(BaseMercenary mercenary) {
            super(mercenary, false);
            this.mercenary = mercenary;
            this.setFlags(EnumSet.of(Goal.Flag.TARGET));
        }

        private LivingEntity getOwner() {
            if (!mercenary.isHired() || mercenary.getCurrentContract() == null) {
                return null;
            }
            return mercenary.level().getPlayerByUUID(mercenary.getCurrentContract().getHirer());
        }

        @Override
        public boolean canUse() {
            LivingEntity owner = getOwner();
            if (owner == null) {
                return false;
            }

            LivingEntity lastAttacker = owner.getLastHurtByMob();
            if (lastAttacker == null || !lastAttacker.isAlive()) {
                return false;
            }

            int attackTimestamp = owner.getLastHurtByMobTimestamp();
            if (attackTimestamp == this.timestamp) {
                return false;
            }

            if (!this.canAttack(lastAttacker, TargetingConditions.DEFAULT)) {
                return false;
            }

            this.attacker = lastAttacker;
            return true;
        }

        @Override
        public boolean canContinueToUse() {
            if (this.attacker == null || !this.attacker.isAlive()) {
                return false;
            }

            LivingEntity owner = getOwner();
            if (owner == null) {
                return false;
            }

            return this.canAttack(this.attacker, TargetingConditions.DEFAULT);
        }

        @Override
        public void start() {
            if (this.attacker != null) {
                this.mercenary.setTarget(this.attacker);
                LivingEntity owner = getOwner();
                if (owner != null) {
                    this.timestamp = owner.getLastHurtByMobTimestamp();
                }
            }
            super.start();
        }

        @Override
        public void stop() {
            this.attacker = null;
            super.stop();
        }
    }
}