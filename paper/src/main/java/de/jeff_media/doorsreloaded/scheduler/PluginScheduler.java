package de.jeff_media.doorsreloaded.scheduler;

import org.bukkit.block.Block;

public interface PluginScheduler {

    void runSync(Runnable task);

    void runLater(Runnable task, long delayTicks);

    void runRepeating(Runnable task, long delayTicks, long periodTicks);

    void runAtBlock(Block block, Runnable task);

    void runAtBlockLater(Block block, long delayTicks, Runnable task);
}