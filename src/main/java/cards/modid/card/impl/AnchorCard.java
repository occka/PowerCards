package cards.modid.card.impl;

import cards.modid.card.PowerCard;
import cards.modid.component.CardSlotsComponent;
import cards.modid.network.SyncCardSlotsPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;

import java.util.Set;

public class AnchorCard extends PowerCard {
    private static final String HAS_ANCHOR = "anchor_has";
    private static final String DIMENSION = "anchor_dimension";
    private static final String X = "anchor_x";
    private static final String Y = "anchor_y";
    private static final String Z = "anchor_z";
    private static final int COOLDOWN_TICKS = 120 * 20;

    public AnchorCard(Item.Properties properties) { super(properties); }

    @Override public Component getCardName() { return Component.translatable("item.powercaeds.anchor_card"); }
    @Override public Component getCardDescription() { return Component.translatable("item.powercaeds.anchor_card.desc"); }
    @Override public int getCooldownTicks() { return COOLDOWN_TICKS; }
    @Override public int getPrimaryColor() { return 0x6B2ECF; }
    @Override public int getSecondaryColor() { return 0xFF8C25; }
    @Override public int getStructureChestLootWeight() { return 1; }
    @Override public boolean usesDefaultCooldown() { return false; }

    @Override
    public int[] getSymbolPattern() {
        return new int[]{ 0b01110, 0b10101, 0b10101, 0b01110, 0b00100, 0b00100 };
    }

    @Override
    public void applyEffect(Player player, Level level) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        CardSlotsComponent comp = serverPlayer.getAttachedOrCreate(CardSlotsComponent.TYPE);

        for (int slot = 0; slot < CardSlotsComponent.SLOT_COUNT; slot++) {
            if (!comp.hasCard(slot) || comp.getCardItem(slot) != this) continue;
            ItemStack stack = comp.getCard(slot);
            if (!hasAnchor(stack)) {
                setAnchor(stack, serverPlayer);
                comp.setCard(slot, stack);
                SyncCardSlotsPacket.send(serverPlayer, comp);
                serverPlayer.level().playSound(null, serverPlayer.blockPosition(), SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.PLAYERS, 0.8F, 1.0F);
            } else if (teleportToAnchor(stack, serverPlayer)) {
                clearAnchor(stack);
                comp.setCard(slot, stack);
                comp.setCooldown(slot, COOLDOWN_TICKS);
                SyncCardSlotsPacket.send(serverPlayer, comp);
            }
            return;
        }
    }

    private static boolean hasAnchor(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getBoolean(HAS_ANCHOR).orElse(false);
    }

    private static void setAnchor(ItemStack stack, ServerPlayer player) {
        var tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        BlockPos pos = player.blockPosition();
        tag.putBoolean(HAS_ANCHOR, true);
        tag.putString(DIMENSION, player.level().dimension().identifier().toString());
        tag.putDouble(X, pos.getX() + 0.5);
        tag.putDouble(Y, pos.getY());
        tag.putDouble(Z, pos.getZ() + 0.5);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    private static void clearAnchor(ItemStack stack) {
        var tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.putBoolean(HAS_ANCHOR, false);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    private static boolean teleportToAnchor(ItemStack stack, ServerPlayer player) {
        var tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        String dimensionId = tag.getString(DIMENSION).orElse(Level.OVERWORLD.identifier().toString());
        ResourceKey<Level> dimension = ResourceKey.create(Registries.DIMENSION, Identifier.parse(dimensionId));
        ServerLevel targetLevel = player.level().getServer().getLevel(dimension);
        if (targetLevel == null) return false;

        double x = tag.getDouble(X).orElse(player.getX());
        double y = tag.getDouble(Y).orElse(player.getY());
        double z = tag.getDouble(Z).orElse(player.getZ());
        player.fallDistance = 0;
        player.teleportTo(targetLevel, x, y, z, Set.<Relative>of(), player.getYRot(), player.getXRot(), true);
        player.fallDistance = 0;
        targetLevel.playSound(null, x, y, z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.8F, 1.0F);
        return true;
    }
}
