package com.bios.task;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import java.util.ArrayList;

public class Tasks {
    private static ArrayList<MochaWorldTask> worldTasks = new ArrayList<>();

    public static void scheduleWorld(MochaWorldTask task) {
        worldTasks.add(task);
    }

    public static void registerEvents() {
        ServerTickEvents.START_WORLD_TICK.register(world -> {
            int i = 0;
            while (i < worldTasks.size()) {
                boolean remove = worldTasks.get(i).tick(world);
                if (remove) {
                    worldTasks.remove(i);
                } else {
                    i += 1;
                }
            }
        });
    }
}
