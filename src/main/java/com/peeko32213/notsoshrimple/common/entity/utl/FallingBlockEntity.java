package com.peeko32213.notsoshrimple.common.entity.utl;

import com.peeko32213.notsoshrimple.core.registry.NSSEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

public class FallingBlockEntity extends Entity {
    public int duration;
    protected static final EntityDataAccessor<BlockPos> DATA_START_POS = SynchedEntityData.defineId(FallingBlockEntity.class, EntityDataSerializers.BLOCK_POS);
    private static final EntityDataAccessor<BlockState> BLOCK_STATE = SynchedEntityData.defineId(FallingBlockEntity.class, EntityDataSerializers.BLOCK_STATE);

    public FallingBlockEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.duration = 20;
    }

    public FallingBlockEntity(Level level, double x, double y, double z, BlockState blockState, int duration) {
        this(NSSEntities.FALLING_BLOCK.get(), level);
        this.setBlockState(blockState);
        this.setPos(x, y + (double)((1.0F - this.getBbHeight()) / 2.0F), z);
        this.setDeltaMovement(Vec3.ZERO);
        this.duration = duration;
        this.xo = x;
        this.yo = y;
        this.zo = z;
        this.setStartPos(this.blockPosition());
    }

    public void setStartPos(BlockPos pos) {
        this.entityData.set(DATA_START_POS, pos);
    }

    public BlockPos getStartPos() {
        return this.entityData.get(DATA_START_POS);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_START_POS, BlockPos.ZERO);
        this.entityData.define(BLOCK_STATE, Blocks.AIR.defaultBlockState());
    }

    public BlockState getBlockState() {
        return this.entityData.get(BLOCK_STATE);
    }

    public void setBlockState(BlockState blockState) {
        this.entityData.set(BLOCK_STATE, blockState);
    }

    @Override
    public void tick() {
        if (!this.isNoGravity()) {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0D, -0.04D, 0.0D));
        }
        this.move(MoverType.SELF, this.getDeltaMovement());
        this.setDeltaMovement(this.getDeltaMovement().scale(0.98D));

        if (this.onGround() && tickCount > duration) {
            discard();
        }
        if (tickCount > 300) {
            discard();
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        BlockState blockState = getBlockState();
        compound.put("block_state", NbtUtils.writeBlockState(blockState));
        compound.putInt("Time", this.duration);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        this.setBlockState(NbtUtils.readBlockState(this.level().holderLookup(Registries.BLOCK), compound.getCompound("block_state")));
        this.duration = compound.getInt("Time");
    }

    @Override
    public boolean displayFireAnimation() {
        return false;
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}