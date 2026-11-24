package com.addon.project;

import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import moze_intel.projecte.api.proxy.IEMCProxy;
import moze_intel.projecte.api.proxy.ITransmutationProxy;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.math.BigInteger;
import java.util.UUID;
public class OutputBlockEntity extends BlockEntity {
    private UUID owner;
    private ItemStack target = ItemStack.EMPTY;

    private final ItemStackHandler buffer = new ItemStackHandler(1);

    public OutputBlockEntity(BlockPos pos, BlockState state){
        super(addonprojecte.OUTPUT_BLOCK_ENTITY.get(), pos, state);
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
        setChanged();
    }

    public void setTarget(ItemStack stack) {
        this.target = stack.copy();
        setChanged();
    }

    public ItemStackHandler getBuffer() {
        return buffer;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Buffer", buffer.serializeNBT(registries));
        if (owner != null) tag.putUUID("Owner", owner);
        if (!target.isEmpty()) tag.put("Target", target.save(registries));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        buffer.deserializeNBT(registries, tag.getCompound("Buffer"));
        if (tag.hasUUID("Owner")) owner = tag.getUUID("Owner");
        target = tag.contains("Target")
                ? ItemStack.parseOptional(registries, tag.getCompound("Target"))
                : ItemStack.EMPTY;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, OutputBlockEntity be) {
        if (be.owner == null) return;
        if (be.target.isEmpty()) return;
        if (!be.buffer.getStackInSlot(0).isEmpty()) return;

        long cost = IEMCProxy.INSTANCE.getValue(be.target);
        if (cost <= 0) return;

        IKnowledgeProvider provider = ITransmutationProxy.INSTANCE.getKnowledgeProviderFor(be.owner);
        BigInteger price = BigInteger.valueOf(cost);
        if (provider.getEmc().compareTo(price) < 0) return;

        provider.setEmc(provider.getEmc().subtract(price));

        be.buffer.setStackInSlot(0, be.target.copy());

        if (level.getServer().getPlayerList().getPlayer(be.owner) instanceof ServerPlayer sp) {
            provider.syncEmc(sp);
        }
    }
}
