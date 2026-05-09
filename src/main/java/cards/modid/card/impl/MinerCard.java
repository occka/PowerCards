package cards.modid.card.impl;

import cards.modid.card.PowerCard;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class MinerCard extends PowerCard {
    private static final int LENGTH = 30;

    public MinerCard(Item.Properties properties) { super(properties); }

    @Override public Component getCardName() { return Component.translatable("item.powercaeds.miner_card"); }
    @Override public Component getCardDescription() { return Component.translatable("item.powercaeds.miner_card.desc"); }
    @Override public int getCooldownTicks() { return 20 * 20; }
    @Override public int getPrimaryColor() { return 0x4C565A; }
    @Override public int getSecondaryColor() { return 0x55DDF2; }
    @Override public int getStructureChestLootWeight() { return 2; }
    @Override public boolean allowsDuplicateEquip() { return true; }

    @Override
    public int[] getSymbolPattern() {
        return new int[]{ 0b00110, 0b01100, 0b11000, 0b01000, 0b11100, 0b01000 };
    }

    @Override
    public void applyEffect(Player player, Level level) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        Vec3 look = player.getLookAngle();
        Vec3 forward = new Vec3(look.x, look.y, look.z).normalize();
        Vec3 horizontal = new Vec3(look.x, 0, look.z);
        if (horizontal.lengthSqr() < 1.0E-4) horizontal = new Vec3(0, 0, 1);
        horizontal = horizontal.normalize();
        Vec3 right = new Vec3(-horizontal.z, 0, horizontal.x);

        Vec3 start = player.getEyePosition().add(forward.scale(1.0));
        for (int depth = 0; depth < LENGTH; depth++) {
            Vec3 center = start.add(forward.scale(depth));
            for (int side = -1; side <= 1; side++) {
                for (int vertical = -1; vertical <= 1; vertical++) {
                    BlockPos pos = BlockPos.containing(center.add(right.scale(side)).add(0, vertical, 0));
                    carveBlock(serverLevel, player, pos);
                }
            }

            if (depth > 0 && depth % 3 == 0) {
                placeLeftWallTorch(serverLevel, center, right);
            }
        }

        serverLevel.playSound(null, player.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 0.35F, 1.5F);
    }

    private static void carveBlock(ServerLevel level, Player player, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir() || state.is(Blocks.BEDROCK) || state.is(Blocks.END_PORTAL_FRAME) || state.is(Blocks.END_PORTAL)) return;

        if (state.is(Blocks.WATER)) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            return;
        }

        if (state.is(Blocks.LAVA)) {
            level.setBlock(pos, Blocks.COBBLESTONE.defaultBlockState(), 3);
            return;
        }

        level.destroyBlock(pos, true, player);
    }

    private static void placeLeftWallTorch(ServerLevel level, Vec3 center, Vec3 right) {
        BlockPos torchPos = BlockPos.containing(center.add(right.scale(-1)).add(0, 0, 0));
        if (level.getBlockState(torchPos).isAir()) {
            level.setBlock(torchPos, Blocks.TORCH.defaultBlockState(), 3);
        }
    }
}
