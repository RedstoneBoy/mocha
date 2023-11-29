package com.bios.item;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Optional;

public class WandOfReturn extends Item {

    public WandOfReturn() {
        super(new FabricItemSettings()
                .maxCount(1)
                .maxDamage(200));
    }

    @Override
    public int getMaxUseTime(ItemStack stack) {
        return 20 * 2;
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.BOW;
    }

    @Override
    public int getEnchantability() {
        return 16;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        user.setCurrentHand(hand);
        return TypedActionResult.consume(user.getStackInHand(hand));
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        if (world.isClient || !(user instanceof ServerPlayerEntity player))
            return stack;

        stack.damage(1, user, u -> u.sendToolBreakStatus(user.getActiveHand()));

        teleportToSpawn(player);

        return stack;
    }

    private static void teleportToSpawn(ServerPlayerEntity player) {
        BlockPos spawnPos = player.getSpawnPointPosition();
        RegistryKey<World> spawnWorldKey = player.getSpawnPointDimension();
        ServerWorld spawnWorld = spawnWorldKey == null ? null : player.getServer().getWorld(spawnWorldKey);

        if (spawnPos == null || spawnWorldKey == null || spawnWorld == null) {
            teleportToWorldSpawn(player);
            return;
        }

        Optional<Vec3d> realSpawnPos = ServerPlayerEntity.findRespawnPosition(spawnWorld, spawnPos, player.getSpawnAngle(), false, true);

        if (realSpawnPos.isPresent()) {
            Vec3d pos = realSpawnPos.get();
            player.teleport(spawnWorld, pos.x, pos.y, pos.z, player.getYaw(), player.getPitch());
        } else {
            teleportToWorldSpawn(player);
        }
    }

    private static void teleportToWorldSpawn(ServerPlayerEntity player) {
        Vec3d pos = player.getServer().getOverworld().getSpawnPos().toCenterPos();
        ServerWorld world = player.getServer().getOverworld();
        player.teleport(world, pos.x, pos.y, pos.z, player.getYaw(), player.getPitch());
    }
}
