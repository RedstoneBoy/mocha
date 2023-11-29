package com.bios.enchantment;

import com.bios.Mocha;
import com.bios.MochaBlockTags;
import com.bios.task.Tasks;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShearsItem;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;

import java.util.*;

public class CascadeEnchantment extends MochaEnchantment {
    public CascadeEnchantment() {
        super(Rarity.RARE, EnchantmentTarget.DIGGER, new EquipmentSlot[] { EquipmentSlot.MAINHAND });
    }

    @Override
    public int getMinPower(int level) {
        return 10 + (level - 1) * 9;
    }

    @Override
    public int getMaxPower(int level) {
        return super.getMinPower(level) + 50;
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }

    private int maxBlocks(int level) {
        return switch (level) {
            case 1 -> 9;
            case 2 -> 49;
            case 3 -> 121;
            default -> 225;
        };
    }

    private int speed(int level) {
        return switch (level) {
            case 1 -> 1;
            case 2 -> 7;
            case 3 -> 11;
            default -> 15;
        };
    }

    @Override
    public void registerCallbacks() {
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            ItemStack stack = player.getStackInHand(player.getActiveHand());

            if (world.isClient
                    || player.getActiveHand() != Hand.MAIN_HAND
                    || stack == null
                    || stack.isEmpty()
                    || EnchantmentHelper.getLevel(this, stack) < 1
                    || !toolMatchesBlock(stack, state)
                    || !state.isIn(MochaBlockTags.CASCADABLE))
            {
                return;
            }

            int level = EnchantmentHelper.getLevel(this, stack);
            int maxBlocks = this.maxBlocks(level);
            int blocksPerTick = this.speed(level);

            scheduleBreak((ServerWorld) world, (ServerPlayerEntity) player, pos, state, stack, maxBlocks, blocksPerTick);
        });
    }

    private static boolean toolMatchesBlock(ItemStack tool, BlockState block) {
        return (tool.isIn(ItemTags.AXES) && block.isIn(BlockTags.AXE_MINEABLE))
                || (tool.isIn(ItemTags.PICKAXES) && block.isIn(BlockTags.PICKAXE_MINEABLE))
                || (tool.isIn(ItemTags.SHOVELS) && block.isIn(BlockTags.SHOVEL_MINEABLE))
                || (tool.isIn(ItemTags.HOES) && block.isIn(BlockTags.HOE_MINEABLE))
                || (tool.getItem() instanceof ShearsItem && block.isIn(BlockTags.LEAVES));
    }

    private static void scheduleBreak(ServerWorld world, ServerPlayerEntity player, BlockPos pos, BlockState block, ItemStack tool, int maxBlocks, int blocksPerTick) {
        Queue<BlockPos> toBreak = new ArrayDeque<>();
        Set<BlockPos> searched = new HashSet<>();

        Queue<BlockPos> toSearch = new ArrayDeque<>(Arrays.asList(adjacent(pos)));

        int count = 0;

        while (!toSearch.isEmpty()) {
            if (count >= maxBlocks) {
                break;
            }

            BlockPos cur = toSearch.remove();

            if (searched.contains(cur)) {
                continue;
            }

            BlockState curState = world.getBlockState(cur);

            searched.add(cur);

            if (curState.isOf(block.getBlock())) {
                toBreak.add(cur);
                count += 1;

                for (BlockPos adjacent : adjacent(cur)) {
                    if (!searched.contains(adjacent)) {
                        toSearch.add(adjacent);
                    }
                }
            }
        }

        Tasks.scheduleWorld(tickWorld -> {
            if (tickWorld != world) {
                return false;
            }

            if (player.getServerWorld() != world
                    || player.getMainHandStack() != tool
                    || player.isDead()) {
                return true;
            }

            int broken = 0;
            while (broken < blocksPerTick) {
                if (toBreak.isEmpty()) {
                    break;
                }

                if (tool.getDamage() >= tool.getMaxDamage() - 1) {
                    return true;
                }

                BlockPos cur = toBreak.remove();
                BlockState curState = world.getBlockState(cur);

                if (curState.isOf(block.getBlock())) {
                    Block curBlock = curState.getBlock();
                    BlockEntity curBlockEntity = world.getBlockEntity(cur);

                    player.incrementStat(Stats.MINED.getOrCreateStat(curBlock));
                    player.addExhaustion(0.005f);
                    List<ItemStack> drops = Block.getDroppedStacks(curState, world, cur, curBlockEntity, player, tool);
                    for (ItemStack drop : drops) {
                        if (drop.isEmpty() || !world.getGameRules().getBoolean(GameRules.DO_TILE_DROPS)) {
                            continue;
                        }

                        ItemEntity entity = new ItemEntity(world, player.getX(), player.getY(), player.getZ(), drop, 0.0, 0.0, 0.0);
                        entity.resetPickupDelay();
                        world.spawnEntity(entity);
                    }
                    curState.onStacksDropped(world, player.getBlockPos(), tool, true);
                    if (world.removeBlock(cur, false)) {
                        broken += 1;
                    }

                    tool.damage(1, player, t -> t.sendToolBreakStatus(Hand.MAIN_HAND));
                }
            }

            return toBreak.isEmpty();
        });
    }

    private static BlockPos[] adjacent(BlockPos center) {
        BlockPos[] positions = new BlockPos[26];

        int i = 0;
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && y == 0 && z == 0) continue;

                    positions[i] = center.add(x, y, z);
                    i++;
                }
            }
        }

        return positions;
    }
}
