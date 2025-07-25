package net.cebularz.helpinghand.common.entity.mercenary;

import net.cebularz.helpinghand.common.data.reputation.MercenaryReputation;
import net.cebularz.helpinghand.common.entity.goals.ConditionalGoal;
import net.cebularz.helpinghand.common.entity.mercenary.ai.MercenaryAI;
import net.cebularz.helpinghand.common.entity.mercenary.ai.MercenaryContract;
import net.cebularz.helpinghand.common.entity.mercenary.ai.MercenaryHireSystem;
import net.cebularz.helpinghand.common.menu.MercenaryMenu;
import net.cebularz.helpinghand.core.ModAttachments;
import net.cebularz.helpinghand.networking.PayloadHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.ResetUniversalAngerTargetGoal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.UUID;

public abstract class BaseMercenary extends PathfinderMob implements NeutralMob, RangedAttackMob
{
    public static final EntityDataAccessor<Integer> DATA_TYPE = SynchedEntityData.defineId(BaseMercenary.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_REMAINING_ANGER_TIME = SynchedEntityData.defineId(BaseMercenary.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Boolean> DATA_HIRED = SynchedEntityData.defineId(BaseMercenary.class, EntityDataSerializers.BOOLEAN);

    public MercenaryType type;
    private MercenaryContract currentContract;
    private final MercenaryHireSystem hireSystem;
    private boolean ranged;

    private UUID ownerUUID;

    private static final UniformInt PERSISTENT_ANGER_TIME = TimeUtil.rangeOfSeconds(20, 60);
    @Nullable
    private UUID persistentAngerTarget;
    public BaseMercenary(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        this.type = MercenaryType.NONE;
        this.hireSystem = new MercenaryHireSystem(this);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MercenaryAI.MercenaryAttackEnemy(this,2));
        this.goalSelector.addGoal(4, new MercenaryAI.MercenaryFollowOwner(this,0.5D,6,1));
        this.goalSelector.addGoal(5, new ConditionalGoal(
                new WaterAvoidingRandomStrollGoal(this,0.41D), ()->!this.isHired()
        ));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class,3));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(0, new MercenaryAI.MercenaryDefendOwner(this));
        this.targetSelector.addGoal(1, (new HurtByTargetGoal(this, new Class[0])).setAlertOthers(new Class[0]));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, this::isAngryAt));
        this.targetSelector.addGoal(3, new ResetUniversalAngerTargetGoal<>(this, true));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_TYPE, MercenaryType.NONE.ordinal());
        builder.define(DATA_REMAINING_ANGER_TIME, 0);
        builder.define(DATA_HIRED, false);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("mercenaryType",getMercenaryType().ordinal());
        compound.putInt("remainingAngerTime",getRemainingPersistentAngerTime());
        compound.putBoolean("hired",isHired());
        if (ownerUUID != null) {
            compound.putUUID("OwnerUUID", ownerUUID);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        setMercenaryType(MercenaryType.values()[compound.getInt("mercenaryType")]);
        setRemainingPersistentAngerTime(compound.getInt("remainingAngerTime"));
        this.entityData.set(DATA_HIRED,compound.getBoolean("hired"));
        if (compound.hasUUID("OwnerUUID")) {
            ownerUUID = compound.getUUID("OwnerUUID");
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide && currentContract != null) {
            currentContract.tick();
            if (currentContract.isExpired()) {
                setContract(null);
            }
        }
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!this.level().isClientSide) {
            if (player.isShiftKeyDown()) {
                return super.mobInteract(player, hand);
            }

            int reputation = getReputationWith(player);
            if (reputation < -50) {
                player.sendSystemMessage(Component.literal("The mercenary glares at you menacingly and refuses to speak."));
                return InteractionResult.SUCCESS;
            }

            this.openMercenaryGui(player);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.CONSUME;
    }

    public void openMercenaryGui(Player player) {
        player.openMenu(new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return Component.empty();
            }

            @Override
            public @NotNull AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
                return new MercenaryMenu(i, inventory, new SimpleContainer(1), BaseMercenary.this);
            }
        }, buf -> buf.writeInt(this.getId()));
    }

    public int getRemainingPersistentAngerTime() {
        return this.entityData.get(DATA_REMAINING_ANGER_TIME);
    }

    public void setRemainingPersistentAngerTime(int time) {
        this.entityData.set(DATA_REMAINING_ANGER_TIME, time);
    }

    public void startPersistentAngerTimer() {
        this.setRemainingPersistentAngerTime(PERSISTENT_ANGER_TIME.sample(this.random));
    }

    @Nullable
    public UUID getPersistentAngerTarget() {
        return this.persistentAngerTarget;
    }

    public void setPersistentAngerTarget(@Nullable UUID target) {
        this.persistentAngerTarget = target;
    }

    public void setMercenaryType(MercenaryType value) {
        this.type = value;
        this.entityData.set(DATA_TYPE, value.ordinal());
    }

    public MercenaryType getMercenaryType() {
        if (!this.level().isClientSide) {
            return this.type;
        }
        int ordinal = this.entityData.get(DATA_TYPE);
        return MercenaryType.values()[ordinal];
    }

    public boolean isHired() {
        return this.entityData.get(DATA_HIRED);
    }

    public MercenaryContract getCurrentContract() {
        return this.currentContract;
    }

    public void setContract(MercenaryContract contract) {
        this.currentContract = contract;
        this.entityData.set(DATA_HIRED, contract != null);
    }

    public MercenaryHireSystem getHireSystem() {
        return this.hireSystem;
    }

    public void setOwner(Player player) {
        this.ownerUUID = player.getUUID();
    }

    public Player getOwner() {
        if (ownerUUID != null && this.level() != null) {
            return this.level().getPlayerByUUID(ownerUUID);
        }
        return null;
    }

    public boolean isOwnedBy(Player player) {
        return ownerUUID != null && ownerUUID.equals(player.getUUID());
    }

    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    @Override
    protected void populateDefaultEquipmentEnchantments(ServerLevelAccessor level, RandomSource random, DifficultyInstance difficulty) {
        super.populateDefaultEquipmentEnchantments(level, random, difficulty);
        this.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.DIAMOND_AXE));

        ItemStack mainHand = this.getItemInHand(InteractionHand.MAIN_HAND);
        if (!mainHand.isEmpty()) {
            float difficultyMultiplier = difficulty.getSpecialMultiplier();

            if (difficultyMultiplier > 0.25F && random.nextFloat() < 0.5F) {

            }

            this.setDropChance(EquipmentSlot.MAINHAND, 0.1F);
            this.setDropChance(EquipmentSlot.HEAD, 0.05F);
            this.setDropChance(EquipmentSlot.CHEST, 0.05F);
            this.setDropChance(EquipmentSlot.LEGS, 0.05F);
            this.setDropChance(EquipmentSlot.FEET, 0.05F);
        }
    }

    @Override
    public @org.jetbrains.annotations.Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, @org.jetbrains.annotations.Nullable SpawnGroupData spawnGroupData) {
        this.populateDefaultEquipmentEnchantments(level, level.getRandom(), difficulty);
        return super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
    }

    public boolean isRanged()
    {
        return ranged;
    }

    public MercenaryReputation getReputationSystem() {
        return this.getData(ModAttachments.REPUTATION.get());
    }

    public int getReputationWith(Player player) {
        return getReputationSystem().getReputationWith(player.getUUID());
    }

    public boolean canBeHiredBy(Player player) {
        return getReputationSystem().canBeHiredBy(player.getUUID());
    }

    public float getPriceMultiplierFor(Player player) {
        return getReputationSystem().getPriceMultiplierFor(player.getUUID());
    }

    public void increaseReputationWith(Player player, int amount) {
        getReputationSystem().increaseReputationWith(player.getUUID(), amount);
        syncReputationToClient(player);
    }

    public void decreaseReputationWith(Player player, int amount) {
        getReputationSystem().decreaseReputationWith(player.getUUID(), amount);
        syncReputationToClient(player);
    }

    public void syncReputationToClient(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            int reputation = getReputationWith(player);
            PayloadHandler.syncReputation(serverPlayer, this.getUUID(), reputation);
        }
    }

    public void setRanged(boolean value) {this.ranged = value;};

    public abstract void setRanged();

    public enum MercenaryType {
        NONE,
        ELF,
        HUMAN,
        UNDEAD,
        DWARFS
    }
}