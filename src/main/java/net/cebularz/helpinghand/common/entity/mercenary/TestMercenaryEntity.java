package net.cebularz.helpinghand.common.entity.mercenary;

import net.cebularz.helpinghand.common.entity.mercenary.ai.MercenaryReputation;
import net.cebularz.helpinghand.core.ModAttachments;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class TestMercenaryEntity extends BaseMercenary{

    public TestMercenaryEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public void setRanged() {
        this.setRanged(true);
    }

    @Override
    public ItemStack getHiringItem() {
        return new ItemStack(Items.DIAMOND);
    }

    public static AttributeSupplier.Builder createAttribs(){
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH,20.0D)
                .add(Attributes.ATTACK_DAMAGE,1.3D)
                .add(Attributes.MOVEMENT_SPEED,0.34D)
                .add(Attributes.FOLLOW_RANGE,16.0D);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new GroundPathNavigation(this,level);
    }

    @Override
    public void performRangedAttack(LivingEntity livingEntity, float v) {

    }
}
