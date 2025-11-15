package de.jeff_media.doorsreloaded.scheduler;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Consumer;

public class FoliaPluginScheduler implements PluginScheduler {

    private final JavaPlugin plugin;
    private final Object regionScheduler;
    private final Object globalScheduler;
    private final Method regionRun;
    private final Method regionRunDelayed;
    private final Method regionRunAtFixedRate;
    private final Method globalRun;
    private final Method globalRunDelayed;
    private final Method globalRunAtFixedRate;

    public FoliaPluginScheduler(JavaPlugin plugin) {
        this.plugin = plugin;
        try {
            Server server = Bukkit.getServer();
            this.regionScheduler = server.getClass().getMethod("getRegionScheduler").invoke(server);
            this.globalScheduler = server.getClass().getMethod("getGlobalRegionScheduler").invoke(server);

            Class<?> pluginClass = Plugin.class;
            Class<?> locationClass = Location.class;
            Class<?> consumerClass = Consumer.class;

            this.regionRun = regionScheduler.getClass().getMethod("run", pluginClass, locationClass, consumerClass);
            this.regionRunDelayed = regionScheduler.getClass().getMethod("runDelayed", pluginClass, locationClass, consumerClass, long.class);
            this.regionRunAtFixedRate = regionScheduler.getClass().getMethod("runAtFixedRate", pluginClass, locationClass, consumerClass, long.class, long.class);
            this.globalRun = globalScheduler.getClass().getMethod("run", pluginClass, consumerClass);
            this.globalRunDelayed = globalScheduler.getClass().getMethod("runDelayed", pluginClass, consumerClass, long.class);
            this.globalRunAtFixedRate = globalScheduler.getClass().getMethod("runAtFixedRate", pluginClass, consumerClass, long.class, long.class);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to access Folia scheduling APIs", exception);
        }
    }

    @Override
    public void runSync(Runnable task) {
        invokeGlobal(globalRun, task);
    }

    @Override
    public void runLater(Runnable task, long delayTicks) {
        if (delayTicks <= 0L) {
            runSync(task);
            return;
        }
        invokeGlobal(globalRunDelayed, task, delayTicks);
    }

    @Override
    public void runRepeating(Runnable task, long delayTicks, long periodTicks) {
        invokeGlobal(globalRunAtFixedRate, task, delayTicks, periodTicks);
    }

    @Override
    public void runAtBlock(Block block, Runnable task) {
        invokeRegion(regionRun, block, task);
    }

    @Override
    public void runAtBlockLater(Block block, long delayTicks, Runnable task) {
        if (delayTicks <= 0L) {
            runAtBlock(block, task);
            return;
        }
        invokeRegion(regionRunDelayed, block, task, delayTicks);
    }

    private Consumer<Object> wrap(Runnable task) {
        return scheduledTask -> task.run();
    }

    private void invokeGlobal(Method method, Runnable task, long... parameters) {
        try {
            Object[] args = new Object[2 + parameters.length];
            args[0] = plugin;
            args[1] = wrap(task);
            for (int i = 0; i < parameters.length; i++) {
                args[i + 2] = parameters[i];
            }
            method.invoke(globalScheduler, args);
        } catch (IllegalAccessException | InvocationTargetException exception) {
            throw new RuntimeException("Failed to schedule global task on Folia", exception);
        }
    }

    private void invokeRegion(Method method, Block block, Runnable task, long... parameters) {
        try {
            Object[] args = new Object[3 + parameters.length];
            args[0] = plugin;
            args[1] = block.getLocation();
            args[2] = wrap(task);
            for (int i = 0; i < parameters.length; i++) {
                args[i + 3] = parameters[i];
            }
            method.invoke(regionScheduler, args);
        } catch (IllegalAccessException | InvocationTargetException exception) {
            throw new RuntimeException("Failed to schedule region task on Folia", exception);
        }
    }
}