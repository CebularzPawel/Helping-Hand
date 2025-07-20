package net.cebularz.helpinghand.core;

import net.cebularz.helpinghand.Constants;
import net.cebularz.helpinghand.common.entity.mercenary.TestMercenaryEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModEntity
{
    public static final DeferredRegister<EntityType<?>> REGISTER = DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, Constants.MOD_ID);

    public static final Supplier<EntityType<TestMercenaryEntity>> TEST_ENTITY = REGISTER.register("test_entity",
            ()-> EntityType.Builder.of(TestMercenaryEntity::new, MobCategory.CREATURE).build("test_entity"));
}
