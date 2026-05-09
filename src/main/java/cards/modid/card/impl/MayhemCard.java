package cards.modid.card.impl;

import cards.modid.card.PowerCard;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class MayhemCard extends PowerCard {
    private static final List<PearlLaunch> LAUNCHES = new ArrayList<>();
    private static final Random RANDOM = new Random();

    public MayhemCard(Item.Properties properties) { super(properties); }
    @Override public Component getCardName() { return Component.translatable("item.powercaeds.mayhem_card"); }
    @Override public Component getCardDescription() { return Component.translatable("item.powercaeds.mayhem_card.desc"); }
    @Override public int getCooldownTicks() { return 45 * 20; }
    @Override public int getPrimaryColor() { return 0x0E2A1C; }
    @Override public int getSecondaryColor() { return 0x07100B; }
    @Override public int getStructureChestLootWeight() { return 5; }
    @Override public boolean allowsDuplicateEquip() { return true; }

    @Override public int[] getSymbolPattern() { return new int[]{0b10101,0b01010,0b11111,0b01010,0b10101,0b00000}; }

    @Override
    public void applyEffect(Player player, Level level) {
        if (!(level instanceof ServerLevel)) return;
        long now = level.getGameTime();
        for (int i = 0; i < 8; i++) {
            double angle = Math.PI * 2.0 * i / 8.0;
            Vec3 dir = new Vec3(Math.cos(angle), 0.55, Math.sin(angle)).normalize();
            long delay = 2 + RANDOM.nextInt(29);
            LAUNCHES.add(new PearlLaunch(player.getUUID(), now + delay, dir));
        }
    }

    public static void serverTick(MinecraftServer server) {
        long now = server.overworld().getGameTime();
        Iterator<PearlLaunch> iterator = LAUNCHES.iterator();
        while (iterator.hasNext()) {
            PearlLaunch launch = iterator.next();
            Player player = server.getPlayerList().getPlayer(launch.owner);
            if (player != null && player.level() instanceof ServerLevel level && now >= launch.launchTick) {
                ThrownEnderpearl pearl = new ThrownEnderpearl(EntityType.ENDER_PEARL, level);
                pearl.setOwner(player);
                pearl.setPos(player.getX(), player.getEyeY(), player.getZ());
                pearl.setDeltaMovement(launch.direction.scale(1.35));
                level.addFreshEntity(pearl);
                iterator.remove();
            }
        }
    }

    private record PearlLaunch(UUID owner, long launchTick, Vec3 direction) {}
}
