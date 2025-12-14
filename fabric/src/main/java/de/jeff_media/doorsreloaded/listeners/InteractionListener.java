package de.jeff_media.doorsreloaded.listeners;

import de.jeff_media.doorsreloaded.config.ModConfig;
import de.jeff_media.doorsreloaded.utils.DoorUtils;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.TrapdoorBlock;
import net.minecraft.block.enums.DoorHinge;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class InteractionListener {

    public static void register() {
        // Right Click (Use)
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (hand != Hand.MAIN_HAND) return ActionResult.PASS;
            if (world.isClient()) return ActionResult.PASS;

            BlockPos pos = hitResult.getBlockPos();
            BlockState state = world.getBlockState(pos);
            ModConfig config = ModConfig.getInstance();

            // Iron Door / Trapdoor handling
            boolean isDoor = DoorUtils.isDoor(state);
            boolean isTrapDoor = DoorUtils.isTrapDoor(state);
            
            // Check for Iron material using Blocks comparison or Tags if available, assuming Iron Door is the main one.
            // In 1.21, we can check block instance or tags.
            // Iron Door and Iron Trapdoor are specific blocks.
            boolean isIronDoor = state.isOf(Blocks.IRON_DOOR); 
            boolean isIronTrapdoor = state.isOf(Blocks.IRON_TRAPDOOR);
            // Or copper doors if we want to include them as "metal" that requires power? 
            // Copper doors in 1.21 can be opened by hand (unlike iron). Iron is the only one defaulting to locked.
            // So we strictly stick to IRON_DOOR and IRON_TRAPDOOR for the "allow opening with hands" feature if strictly following original plugin.
            // Original used Material.IRON_DOOR etc.

            if (isIronDoor || isIronTrapdoor) {
                if (config.allow_opening_irondoors_with_hands) {
                    boolean isOpen = false;
                    if (isIronDoor) isOpen = state.get(DoorBlock.OPEN);
                    else isOpen = state.get(TrapdoorBlock.OPEN);
                    
                    // Toggle
                    boolean newOpen = !isOpen;
                    BlockState newState;
                    
                    if (isIronDoor) {
                        newState = state.with(DoorBlock.OPEN, newOpen);
                        world.setBlockState(pos, newState, 10);
                        world.playSound(null, pos, newOpen ? SoundEvents.BLOCK_IRON_DOOR_OPEN : SoundEvents.BLOCK_IRON_DOOR_CLOSE, SoundCategory.BLOCKS, 1.0f, 1.0f);
                        if (newOpen) {
                            de.jeff_media.doorsreloaded.scheduler.DoorScheduler.scheduleAutoClose((net.minecraft.server.world.ServerWorld) world, pos);
                        }
                        if (config.allow_doubledoors) {
                            handleDoubleDoor(world, pos, state, newOpen);
                        }
                    } else {
                        newState = state.with(TrapdoorBlock.OPEN, newOpen);
                        world.setBlockState(pos, newState, 10);
                        world.playSound(null, pos, newOpen ? SoundEvents.BLOCK_IRON_TRAPDOOR_OPEN : SoundEvents.BLOCK_IRON_TRAPDOOR_CLOSE, SoundCategory.BLOCKS, 1.0f, 1.0f);
                        if (newOpen) {
                             de.jeff_media.doorsreloaded.scheduler.DoorScheduler.scheduleAutoClose((net.minecraft.server.world.ServerWorld) world, pos);
                        }
                    }

                    return ActionResult.SUCCESS; // Intercepted and handled
                }
            }

            // Double Door handling for other doors (Vanilla handles the initial toggle)
            if (isDoor && !isIronDoor && config.allow_doubledoors) {
                 // Predict the new state: Vanilla toggles it.
                 // NOTE: UseBlockCallback happens BEFORE vanilla logic.
                 // If we assume vanilla toggles, we need to apply the toggle to the neighbor.
                 boolean currentOpen = state.get(DoorBlock.OPEN);
                 boolean newOpen = !currentOpen;
                 
                 handleDoubleDoor(world, pos, state, newOpen);
                 if (newOpen) {
                      de.jeff_media.doorsreloaded.scheduler.DoorScheduler.scheduleAutoClose((net.minecraft.server.world.ServerWorld) world, pos);
                 }
                 // We return PASS so vanilla toggles the clicked door
                 return ActionResult.PASS;
            }

            return ActionResult.PASS;
        });

        // Left Click (Attack) - Knocking
        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
            if (hand != Hand.MAIN_HAND) return ActionResult.PASS;
            if (world.isClient()) return ActionResult.PASS;
            
            BlockState state = world.getBlockState(pos);
            ModConfig config = ModConfig.getInstance();

            if (!config.allow_knocking) return ActionResult.PASS;

            boolean isDoor = DoorUtils.isDoor(state);
            boolean isTrapDoor = DoorUtils.isTrapDoor(state);

            if (!isDoor && (!isTrapDoor || !config.allow_knocking_trapdoors)) {
                return ActionResult.PASS;
            }

            // Checks
            if (config.knocking_requires_shift && !player.isSneaking()) return ActionResult.PASS;
            if (config.knocking_requires_empty_hand && !player.getMainHandStack().isEmpty()) return ActionResult.PASS;

            // Play Sound
            String soundStr;
            if (state.isIn(BlockTags.WOODEN_DOORS) || state.isIn(BlockTags.WOODEN_TRAPDOORS)) {
                 soundStr = config.sound_knock_wood;
            } else {
                 soundStr = config.sound_knock_iron;
            }
            
            Identifier soundId = Identifier.tryParse(soundStr);
            SoundEvent sound = null;
            if (soundId != null) {
                sound = Registries.SOUND_EVENT.get(soundId);
            }
            if (sound == null) sound = SoundEvents.ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR; // Fallback

            SoundCategory category = SoundCategory.BLOCKS;
            try {
                category = SoundCategory.valueOf(config.sound_knock_category);
            } catch (IllegalArgumentException ignored) {}

            world.playSound(null, pos, sound, category, (float)config.sound_knock_volume, (float)config.sound_knock_pitch);

            return ActionResult.PASS;
        });
    }

    private static void handleDoubleDoor(World world, BlockPos pos, BlockState state, boolean newOpenState) {
        BlockPos neighborPos = DoorUtils.getDoubleDoorNeighbor(world, pos, state);
        if (neighborPos != null) {
            BlockState neighborState = world.getBlockState(neighborPos);
            if (neighborState.get(DoorBlock.OPEN) != newOpenState) {
                world.setBlockState(neighborPos, neighborState.with(DoorBlock.OPEN, newOpenState), 10);
                // We should also play sound/event? Vanilla usually plays sound for the clicked door.
                // We manually play sound for the other door?
                // Usually double doors sound better if only one sound plays or both.
                // We'll leave it silent or let the block update trigger sound?
                // setBlockState with flag 10 (2 | 8) updates clients and no physics? block update?
                // We might want to play sound.
            }
        }
    }
}
