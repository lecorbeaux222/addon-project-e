package com.addon.project;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class TestBlockEntity extends BlockEntity {
    private int counter = 0;

    public TestBlockEntity(BlockPos pos, BlockState state) {
        super(addonprojecte.TEST_BLOCK_ENTITY.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, TestBlockEntity be){
        be.counter++;
        if (be.counter % 100==0) {
            addonprojecte.LOGGER.info("test en {} : {}", pos, be.counter);
        }
    }
}
