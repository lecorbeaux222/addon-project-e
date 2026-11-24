package com.addon.project;

import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import moze_intel.projecte.api.proxy.IEMCProxy;
import moze_intel.projecte.api.proxy.ITransmutationProxy;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.math.BigInteger;
import java.util.UUID;

public class TestBlockEntity extends BlockEntity {
    private int counter = 0;
    private UUID owner;

    private final ItemStackHandler inventory = new ItemStackHandler(1) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return IEMCProxy.INSTANCE.hasValue(stack);
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    public TestBlockEntity(BlockPos pos, BlockState state) {
        super(addonprojecte.TEST_BLOCK_ENTITY.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, TestBlockEntity be){
        be.counter++;
        if (be.counter % 100==0) {
            addonprojecte.LOGGER.info("test en {} : {}", pos, be.counter);
        }
        if (be.owner == null) return;
        if (be.counter % 20 != 0) return; // 1 conversion par seconde

        ItemStack stack = be.inventory.getStackInSlot(0);
        if (stack.isEmpty()) return;

        long unit = IEMCProxy.INSTANCE.getValue(stack);
        if (unit <= 0) return;

        BigInteger total = BigInteger.valueOf(unit).multiply(BigInteger.valueOf(stack.getCount()));

        IKnowledgeProvider provider = ITransmutationProxy.INSTANCE.getKnowledgeProviderFor(be.owner);
        provider.setEmc(provider.getEmc().add(total));
        be.inventory.setStackInSlot(0, ItemStack.EMPTY);

        if (level.getServer().getPlayerList().getPlayer(be.owner) instanceof ServerPlayer sp) {
            provider.syncEmc(sp);
        }
        addonprojecte.LOGGER.info("+{} EMC pour {}", total, be.owner);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Inventory", inventory.serializeNBT(registries));
        if (owner != null) tag.putUUID("Owner", owner);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        inventory.deserializeNBT(registries, tag.getCompound("Inventory"));
        if (tag.hasUUID("Owner")) owner = tag.getUUID("Owner");
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
        setChanged();
    }

    public ItemStackHandler getInventory() {
        return inventory;
    }
}
