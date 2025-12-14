package de.jeff_media.doorsreloaded.scheduler;

import de.jeff_media.doorsreloaded.config.ModConfig;
import de.jeff_media.doorsreloaded.utils.DoorUtils;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.TrapdoorBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DoorScheduler {

    private static final Map<GlobalPos, Long> scheduledClosures = new ConcurrentHashMap<>();

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            long currentTime = System.currentTimeMillis();
            Iterator<Map.Entry<GlobalPos, Long>> iterator = scheduledClosures.entrySet().iterator();

            while (iterator.hasNext()) {
                Map.Entry<GlobalPos, Long> entry = iterator.next();
                if (currentTime >= entry.getValue()) {
                    GlobalPos globalPos = entry.getKey();
                    ServerWorld world = server.getWorld(globalPos.dimension());
                    if (world != null) {
                        try {
                            closeDoor(world, globalPos.pos());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    iterator.remove();
                }
            }
        });
    }

    public static void scheduleAutoClose(ServerWorld world, BlockPos pos) {
        long delaySeconds = ModConfig.getInstance().autoclose;
        if (delaySeconds <= 0) return;

        GlobalPos globalPos = GlobalPos.create(world.getRegistryKey(), pos);
        scheduledClosures.put(globalPos, System.currentTimeMillis() + (delaySeconds * 1000L));
    }

    private static void closeDoor(ServerWorld world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        if (!DoorUtils.isDoor(state) && !DoorUtils.isTrapDoor(state)) return;

        boolean isOpen;
        if (DoorUtils.isDoor(state)) {
            isOpen = state.get(DoorBlock.OPEN);
        } else {
            isOpen = state.get(TrapdoorBlock.OPEN);
        }

        if (isOpen) {
            // Close it
            if (DoorUtils.isDoor(state)) {
                world.setBlockState(pos, state.with(DoorBlock.OPEN, false), 10);
                world.playSound(null, pos, SoundEvents.BLOCK_IRON_DOOR_CLOSE, SoundCategory.BLOCKS, 1.0f, 1.0f); // Default sound
                
                // Double door check
                if (ModConfig.getInstance().allow_doubledoors) {
                    BlockPos otherPos = DoorUtils.getOtherDoorPart(world, pos, state);
                    if (otherPos != null) {
                        BlockState otherState = world.getBlockState(otherPos);
                        if (otherState.get(DoorBlock.OPEN)) {
                             world.setBlockState(otherPos, otherState.with(DoorBlock.OPEN, false), 10);
                             // Remove other door from schedule if present to avoid double close sound/logic
                             scheduledClosures.remove(GlobalPos.create(world.getRegistryKey(), otherPos));
                        }
                    }
                }
            } else {
                 world.setBlockState(pos, state.with(TrapdoorBlock.OPEN, false), 10);
                 world.playSound(null, pos, SoundEvents.BLOCK_IRON_TRAPDOOR_CLOSE, SoundCategory.BLOCKS, 1.0f, 1.0f);
            }
        }
    }
}
