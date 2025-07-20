package net.cebularz.helpinghand.client.model;

import net.cebularz.helpinghand.common.CommonClass;
import net.minecraft.client.model.AnimationUtils;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinArmPose;

public class TestEntityModel<T extends Mob> extends PlayerModel<T> {
    public static final ModelLayerLocation LOCATION = new ModelLayerLocation(CommonClass.path("test_layer"),"main");
    public final ModelPart rightEar;
    private final ModelPart leftEar;
    private final PartPose bodyDefault;
    private final PartPose headDefault;
    private final PartPose leftArmDefault;
    private final PartPose rightArmDefault;

    public TestEntityModel(ModelPart p_170810_) {
        super(p_170810_, false);
        this.rightEar = this.head.getChild("right_ear");
        this.leftEar = this.head.getChild("left_ear");
        this.bodyDefault = this.body.storePose();
        this.headDefault = this.head.storePose();
        this.leftArmDefault = this.leftArm.storePose();
        this.rightArmDefault = this.rightArm.storePose();
    }

    public static MeshDefinition createMesh(CubeDeformation cubeDeformation) {
        MeshDefinition meshdefinition = PlayerModel.createMesh(cubeDeformation, false);
        PartDefinition partdefinition = meshdefinition.getRoot();
        partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, cubeDeformation), PartPose.ZERO);
        addHead(cubeDeformation, meshdefinition);
        partdefinition.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
        return meshdefinition;
    }

    public static void addHead(CubeDeformation cubeDeformation, MeshDefinition mesh) {
        PartDefinition partdefinition = mesh.getRoot();
        PartDefinition partdefinition1 = partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-5.0F, -8.0F, -4.0F, 10.0F, 8.0F, 8.0F, cubeDeformation).texOffs(31, 1).addBox(-2.0F, -4.0F, -5.0F, 4.0F, 4.0F, 1.0F, cubeDeformation).texOffs(2, 4).addBox(2.0F, -2.0F, -5.0F, 1.0F, 2.0F, 1.0F, cubeDeformation).texOffs(2, 0).addBox(-3.0F, -2.0F, -5.0F, 1.0F, 2.0F, 1.0F, cubeDeformation), PartPose.ZERO);
        partdefinition1.addOrReplaceChild("left_ear", CubeListBuilder.create().texOffs(51, 6).addBox(0.0F, 0.0F, -2.0F, 1.0F, 5.0F, 4.0F, cubeDeformation), PartPose.offsetAndRotation(4.5F, -6.0F, 0.0F, 0.0F, 0.0F, (-(float)Math.PI / 6F)));
        partdefinition1.addOrReplaceChild("right_ear", CubeListBuilder.create().texOffs(39, 6).addBox(-1.0F, 0.0F, -2.0F, 1.0F, 5.0F, 4.0F, cubeDeformation), PartPose.offsetAndRotation(-4.5F, -6.0F, 0.0F, 0.0F, 0.0F, ((float)Math.PI / 6F)));
    }

    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.body.loadPose(this.bodyDefault);
        this.head.loadPose(this.headDefault);
        this.leftArm.loadPose(this.leftArmDefault);
        this.rightArm.loadPose(this.rightArmDefault);
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        float f = ((float)Math.PI / 6F);
        float f1 = ageInTicks * 0.1F + limbSwing * 0.5F;
        float f2 = 0.08F + limbSwingAmount * 0.4F;
        this.leftEar.zRot = (-(float)Math.PI / 6F) - Mth.cos(f1 * 1.2F) * f2;
        this.rightEar.zRot = ((float)Math.PI / 6F) + Mth.cos(f1) * f2;

        this.leftPants.copyFrom(this.leftLeg);
        this.rightPants.copyFrom(this.rightLeg);
        this.leftSleeve.copyFrom(this.leftArm);
        this.rightSleeve.copyFrom(this.rightArm);
        this.jacket.copyFrom(this.body);
        this.hat.copyFrom(this.head);
    }

    protected void setupAttackAnimation(T livingEntity, float ageInTicks) {
        if (this.attackTime > 0.0F && livingEntity instanceof Piglin && ((Piglin)livingEntity).getArmPose() == PiglinArmPose.ATTACKING_WITH_MELEE_WEAPON) {
            AnimationUtils.swingWeaponDown(this.rightArm, this.leftArm, livingEntity, this.attackTime, ageInTicks);
        } else {
            super.setupAttackAnimation(livingEntity, ageInTicks);
        }

    }

    private void holdWeaponHigh(T mob) {
        if (mob.isLeftHanded()) {
            this.leftArm.xRot = -1.8F;
        } else {
            this.rightArm.xRot = -1.8F;
        }

    }
}
