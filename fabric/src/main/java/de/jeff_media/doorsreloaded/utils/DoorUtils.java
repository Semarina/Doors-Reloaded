package de.jeff_media.doorsreloaded.utils;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.TrapdoorBlock;
import net.minecraft.block.enums.DoorHinge;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class DoorUtils {

    public static boolean isDoor(BlockState state) {
        return state.getBlock() instanceof DoorBlock;
    }

    public static boolean isTrapDoor(BlockState state) {
        return state.getBlock() instanceof TrapdoorBlock;
    }

    public static BlockPos getOtherDoorPart(World world, BlockPos pos, BlockState state) {
        if (!isDoor(state)) return null;

        Direction facing = state.get(DoorBlock.FACING);
        DoorHinge hinge = state.get(DoorBlock.HINGE);
        
        // Calculate offset to the other door based on hinge
        // If hinge is left, other door is to the right
        // If hinge is right, other door is to the left
        Direction offsetDir;
        if (hinge == DoorHinge.LEFT) {
             offsetDir = facing.rotateYClockwise();
        } else {
             offsetDir = facing.rotateYCounterclockwise();
        }

        BlockPos otherPos = pos.offset(offsetDir);
        BlockState otherState = world.getBlockState(otherPos);

        if (isDoor(otherState) && otherState.getBlock() == state.getBlock()) {
            // Check if they match in facing and callback (hinge should be opposite)
            if (otherState.get(DoorBlock.FACING) == state.get(DoorBlock.FACING) &&
                otherState.get(DoorBlock.HINGE) != hinge) {
                 // Also check half to match 
                 if (otherState.get(DoorBlock.HALF) == state.get(DoorBlock.HALF)) {
                     return otherPos;
                 }
            }
        }
        
        return null;
    }

    public static BlockPos getDoubleDoorNeighbor(World world, BlockPos pos, BlockState state) {
        // This is effectively the same as getOtherDoorPart but lets be explicit
        // Used to toggle the other door
        return getOtherDoorPart(world, pos, state);
    }
}
