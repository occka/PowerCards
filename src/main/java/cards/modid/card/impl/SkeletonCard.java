package cards.modid.card.impl;

import cards.modid.card.PowerCard;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public class SkeletonCard extends PowerCard {

    public SkeletonCard(Item.Properties properties) { super(properties); }

    @Override public Component getCardName() { return Component.translatable("item.powercaeds.skeleton_card"); }
    @Override public Component getCardDescription() { return Component.translatable("item.powercaeds.skeleton_card.desc"); }
    @Override public int getCooldownTicks() { return 8 * 20; }
    @Override public int getPrimaryColor() { return 0x9E9E9E; }
    @Override public int getSecondaryColor() { return 0x616161; }
    @Override public int getStructureChestLootWeight() { return 5; }
    @Override public boolean allowsDuplicateEquip() { return true; }

    @Override
    public void applyEffect(Player player, Level level) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        Arrow arrow = EntityType.ARROW.create(serverLevel, EntitySpawnReason.MOB_SUMMONED);
        if (arrow == null) return;
        arrow.setOwner(player);
        arrow.setPos(player.getX(), player.getEyeY() - 0.1, player.getZ());
        arrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 3.6F, 0.0F);
        arrow.setBaseDamage(4.5D); // чуть сильнее fully charged bow
        serverLevel.addFreshEntity(arrow);
        serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.SKELETON_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F);
    }
}
