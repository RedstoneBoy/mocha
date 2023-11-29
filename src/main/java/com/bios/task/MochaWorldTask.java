package com.bios.task;

import net.minecraft.server.world.ServerWorld;

public interface MochaWorldTask {
    /**
     *
     *
     * @param world
     * @return true if the task is finished
     */
    boolean tick(ServerWorld world);
}
