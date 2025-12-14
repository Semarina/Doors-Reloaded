package de.jeff_media.doorsreloaded.scheduler;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

public class PaperPluginScheduler implements PluginScheduler {

    private final JavaPlugin plugin;
    private final BukkitScheduler scheduler;

    public PaperPluginScheduler(JavaPlugin plugin) {
        this.plugin = plugin;
        this.scheduler = Bukkit.getScheduler();
    }

    @Override
    public void runSync(Runnable task) {
        scheduler.runTask(plugin, task);
    }

    @Override
    public void runLater(Runnable task, long delayTicks) {
        scheduler.runTaskLater(plugin, task, delayTicks);
    }

    @Override
    public void runRepeating(Runnable task, long delayTicks, long periodTicks) {
        scheduler.runTaskTimer(plugin, task, delayTicks, periodTicks);
    }

    @Override
    public void runAtBlock(Block block, Runnable task) {
        scheduler.runTask(plugin, task);
    }

    @Override
    public void runAtBlockLater(Block block, long delayTicks, Runnable task) {
        scheduler.runTaskLater(plugin, task, delayTicks);
    }
}