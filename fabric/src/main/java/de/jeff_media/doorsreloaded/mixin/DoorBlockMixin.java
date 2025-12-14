package de.jeff_media.doorsreloaded.mixin;

import de.jeff_media.doorsreloaded.config.ModConfig;
import de.jeff_media.doorsreloaded.utils.DoorUtils;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.DoorBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.block.WireOrientation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractBlock.class)
public class DoorBlockMixin {

    @Inject(method = "neighborUpdate", at = @At("RETURN"))
    public void onNeighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, WireOrientation wireOrientation, boolean notify, CallbackInfo ci) {
        if (!((Object)this instanceof DoorBlock)) return;
        if (!ModConfig.getInstance().check_for_redstone) return;
        if (world.isClient()) return;

        BlockState newState = world.getBlockState(pos);
        if (!(newState.getBlock() instanceof DoorBlock)) return; // Should be impossible if mingled correctly

        boolean wasOpen = state.get(DoorBlock.OPEN);
        boolean isOpen = newState.get(DoorBlock.OPEN);
        boolean wasPowered = state.get(DoorBlock.POWERED);
        boolean isPowered = newState.get(DoorBlock.POWERED);

        if (isPowered != wasPowered) {
            // Power state changed, and likely open state changed too handled by vanilla
            // We want to sync the other part
            if (isOpen != wasOpen) { // Only if it actually toggled
                 BlockPos neighborPos = DoorUtils.getDoubleDoorNeighbor(world, pos, newState);
                 if (neighborPos != null) {
                     BlockState neighborState = world.getBlockState(neighborPos);
                     if (neighborState.get(DoorBlock.OPEN) != isOpen) {
                         // Toggle neighbor to match
                         // Use flag 10 or similar?
                         world.setBlockState(neighborPos, neighborState.with(DoorBlock.OPEN, isOpen), 10);
                         // We don't play sound here usually because the primary door played it? 
                         // Or maybe we should?
                         // Vanilla redstone only plays sound for the powered door.
                         // So we probably should play sound to simulate the second door moving.
                         // However, if they are close, one sound might be enough, but standard is both move.
                     }
                 }
            }
        }
    }
}
