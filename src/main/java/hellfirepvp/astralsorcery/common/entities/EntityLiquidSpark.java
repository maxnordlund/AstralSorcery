/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2017
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.entities;

import hellfirepvp.astralsorcery.client.effect.EffectHelper;
import hellfirepvp.astralsorcery.client.effect.fx.EntityFXFacingParticle;
import hellfirepvp.astralsorcery.client.effect.fx.EntityFXFloatingCube;
import hellfirepvp.astralsorcery.client.util.RenderingUtils;
import hellfirepvp.astralsorcery.common.base.LiquidInteraction;
import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import hellfirepvp.astralsorcery.common.network.PacketChannel;
import hellfirepvp.astralsorcery.common.network.packet.server.PktLiquidInteractionBurst;
import hellfirepvp.astralsorcery.common.util.ASDataSerializers;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityFlying;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityFlyHelper;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.awt.*;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: EntityLiquidSpark
 * Created by HellFirePvP
 * Date: 28.10.2017 / 14:39
 */
public class EntityLiquidSpark extends EntityFlying implements EntityTechnicalAmbient {

    private static final DataParameter<Integer> ENTITY_TARGET = EntityDataManager.createKey(EntityLiquidSpark.class, DataSerializers.VARINT);
    private static final DataParameter<FluidStack> FLUID_REPRESENTED = EntityDataManager.createKey(EntityLiquidSpark.class, ASDataSerializers.FLUID);

    private LiquidInteraction purpose;

    public Vector3 rotationDegreeAxis = new Vector3();
    public Vector3 prevRotationDegreeAxis = new Vector3();
    private Vector3 rotationVec = null;

    public EntityLiquidSpark(World worldIn) {
        super(worldIn);
        setSize(0.4F, 0.4F);
        this.noClip = true;
        this.moveHelper = new EntityFlyHelper(this);
        this.purpose = null;
    }

    public EntityLiquidSpark(World world, BlockPos spawnPos, LiquidInteraction purposeOfLiving) {
        super(world);
        setSize(0.4F, 0.4F);
        setPosition(spawnPos.getX() + 0.5, spawnPos.getY() + 0.5, spawnPos.getZ() + 0.5);
        this.noClip = true;
        this.moveHelper = new EntityFlyHelper(this);
        this.purpose = purposeOfLiving;
    }

    public void setTarget(EntityLiquidSpark other) {
        this.dataManager.set(ENTITY_TARGET, other.getEntityId());
    }

    public void setFluidRepresented(FluidStack fs) {
        this.dataManager.set(FLUID_REPRESENTED, fs);
    }

    public FluidStack getRepresentitiveFluid() {
        return this.dataManager.get(FLUID_REPRESENTED);
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBox(Entity entityIn) {
        return null;
    }

    @Override
    public boolean canBePushed() {
        return false;
    }

    @Override
    protected void entityInit() {
        super.entityInit();

        this.dataManager.register(ENTITY_TARGET, -1);
        this.dataManager.register(FLUID_REPRESENTED, new FluidStack(FluidRegistry.WATER, 1));
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();

        this.getAttributeMap().registerAttribute(SharedMonsterAttributes.FLYING_SPEED);

        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(2.0D);
        this.getEntityAttribute(SharedMonsterAttributes.FLYING_SPEED).setBaseValue(0.35);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if(isDead) return;

        this.noClip = getEntityWorld().getBlockState(this.getPosition()).getBlock().equals(BlocksAS.blockChalice);

        if(!world.isRemote) {
            if(purpose == null) {
                setDead();
                return;
            }

            int target = this.dataManager.get(ENTITY_TARGET);
            if(target == -1) {
                setDead();
                return;
            }
            Entity e = world.getEntityByID(target);
            if(e == null || e.isDead || !(e instanceof EntityLiquidSpark)) {
                setDead();
                return;
            }

            if(getDistanceToEntity(e) < 0.7F) {
                setDead();
                e.setDead();
                Vector3 at = Vector3.atEntityCenter(e)
                                .subtract(Vector3.atEntityCenter(this))
                                .divide(2)
                                .add(Vector3.atEntityCenter(this));
                purpose.triggerInteraction(this.world, at);
                PktLiquidInteractionBurst ev = new PktLiquidInteractionBurst(
                        this.purpose.getComponent1(),
                        this.purpose.getComponent2(),
                        at);
                PacketChannel.CHANNEL.sendToAllAround(ev, PacketChannel.pointFromPos(this.world, at.toBlockPos(), 32));
            } else {
                this.moveHelper.setMoveTo(e.posX, e.posY, e.posZ, 2.4F);
            }
        } else {
            if(rotationVec == null) {
                rotationVec = Vector3.random().normalize().multiply(1.5F);
            }

            this.prevRotationDegreeAxis = this.rotationDegreeAxis.clone();
            this.rotationDegreeAxis.add(this.rotationVec);

            playAmbientParticles();
        }
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return null;
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource p_184601_1_) {
        return null;
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected SoundEvent getFallSound(int heightIn) {
        return null;
    }

    @Override
    protected float getSoundVolume() {
        return 0;
    }

    @SideOnly(Side.CLIENT)
    private void playAmbientParticles() {
        TextureAtlasSprite tas = RenderingUtils.tryGetFlowingTextureOfFluidStack(this.dataManager.get(FLUID_REPRESENTED));

        Vector3 at = Vector3.atEntityCenter(this);
        EntityFXFloatingCube cube = RenderingUtils.spawnFloatingBlockCubeParticle(at, tas);
        cube.setTextureSubSizePercentage(1F / 16F).setMaxAge(20 + rand.nextInt(20));
        cube.setWorldLightCoord(Minecraft.getMinecraft().world, at.toBlockPos());
        cube.setScale(0.3F).tumble().setMotion(
                rand.nextFloat() * 0.02F * (rand.nextBoolean() ? 1 : -1),
                rand.nextFloat() * 0.02F * (rand.nextBoolean() ? 1 : -1),
                rand.nextFloat() * 0.02F * (rand.nextBoolean() ? 1 : -1));

        EntityFXFacingParticle p = EffectHelper.genericFlareParticle(at);
        p.setColor(Color.WHITE).scale(0.3F + rand.nextFloat() * 0.1F).setMaxAge(20 + rand.nextInt(10));
    }

}
