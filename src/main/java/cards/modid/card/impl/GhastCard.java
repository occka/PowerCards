package cards.modid.card.impl;

import cards.modid.card.PowerCard;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class GhastCard extends PowerCard {

    public GhastCard(Item.Properties properties) { super(properties); }

    @Override public Component getCardName() { return Component.translatable("item.powercaeds.ghast_card"); }
    @Override public Component getCardDescription() { return Component.translatable("item.powercaeds.ghast_card.desc"); }
    @Override public int getCooldownTicks() { return 14 * 20; }
    @Override public int getPrimaryColor() { return 0xE0E0E0; }
    @Override public int getSecondaryColor() { return 0x9E9E9E; }
    @Override public int getStructureChestLootWeight() { return 2; }
    @Override public boolean allowsDuplicateEquip() { return true; }

    @Override
    public void applyEffect(Player player, Level level) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        Vec3 look = player.getLookAngle();
        LargeFireball fireball = new LargeFireball(level, player, look, 2);
        fireball.setPos(player.getX() + look.x * 1.2, player.getEyeY() - 0.15, player.getZ() + look.z * 1.2);
        fireball.setDeltaMovement(look.scale(0.9));
        fireball.setCustomName(Component.literal("powercaeds_ghast_card_fireball"));
        fireball.setCustomNameVisible(false);
        serverLevel.addFreshEntity(fireball);

        serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.GHAST_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F);
    }
}
