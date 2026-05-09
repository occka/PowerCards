package cards.modid.card.impl;

import cards.modid.card.PowerCard;
import cards.modid.component.CardSlotsComponent;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;

public class Md49Card extends PowerCard {
    public Md49Card(Item.Properties properties) { super(properties); }
    @Override public Component getCardName() { return Component.translatable("item.powercaeds.md49_card"); }
    @Override public Component getCardDescription() { return Component.translatable("item.powercaeds.md49_card.desc"); }
    @Override public int getCooldownTicks() { return 0; }
    @Override public int getPrimaryColor() { return 0x000000; }
    @Override public int getSecondaryColor() { return 0x2D2D2D; }
    @Override public int getStructureChestLootWeight() { return 1; }
    @Override public boolean canActivate() { return false; }
    @Override public int[] getSymbolPattern() { return new int[]{0b01110,0b10101,0b11111,0b01110,0b01010,0b00000}; }

    @Override
    public void applyEffect(Player player, Level level) {
    }

    @Override
    public void tickEffect(Player player, Level level, int slotIndex) {
        equipSet(player);
        if (!(level instanceof ServerLevel serverLevel) || level.getGameTime() % 20 != 0) return;

        BlockPos base = player.blockPosition();
        for (int x = -3; x <= 3; x++) for (int y = -1; y <= 1; y++) for (int z = -3; z <= 3; z++) {
            if (x * x + z * z > 9) continue;
            BlockPos pos = base.offset(x, y, z);
            if (serverLevel.getBlockState(pos).is(Blocks.GRASS_BLOCK)) serverLevel.setBlock(pos, Blocks.DIRT.defaultBlockState(), 3);
            if (serverLevel.getBlockState(pos).is(Blocks.SHORT_GRASS) || serverLevel.getBlockState(pos).is(Blocks.FERN) || serverLevel.getBlockState(pos).is(Blocks.DANDELION) || serverLevel.getBlockState(pos).is(Blocks.POPPY)) {
                serverLevel.setBlock(pos, Blocks.DEAD_BUSH.defaultBlockState(), 3);
            }
        }

        AABB area = player.getBoundingBox().inflate(3);
        for (LivingEntity entity : serverLevel.getEntitiesOfClass(LivingEntity.class, area, e -> e != player && e.isAlive())) {
            if (isUndead(entity)) {
                entity.heal(2.0F);
            } else {
                entity.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 0));
            }
        }

        for (Mob mob : serverLevel.getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(12))) {
            if (mob instanceof Enemy && mob.getTarget() == player) mob.setTarget(null);
        }
    }

    public static void registerEvents() {
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            if (!(entity instanceof Player player) || !hasMd49(player)) return true;
            String msg = source.type().msgId();
            return !("magic".equals(msg) || "wither".equals(msg) || "indirectMagic".equals(msg));
        });
        ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, baseDamageTaken, damageTaken, blocked) -> {
            if (source.getEntity() instanceof Player player && hasMd49(player)) {
                entity.addEffect(new MobEffectInstance(MobEffects.WITHER, 40, 0));
                entity.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 40, 0));
            }
        });
    }

    private static void equipSet(Player player) {
        player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.WITHER_SKELETON_SKULL));
        player.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.LEATHER_CHESTPLATE));
        player.setItemSlot(EquipmentSlot.LEGS, new ItemStack(Items.LEATHER_LEGGINGS));
        player.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.LEATHER_BOOTS));
    }

    private static boolean isUndead(LivingEntity entity) {
        String id = entity.getType().getDescriptionId();
        return id.contains("zombie") || id.contains("skeleton") || id.contains("wither") || id.contains("drowned") || id.contains("husk") || id.contains("stray") || id.contains("phantom");
    }

    private static boolean hasMd49(Player player) {
        CardSlotsComponent comp = player.getAttachedOrCreate(CardSlotsComponent.TYPE);
        for (int i = 0; i < CardSlotsComponent.SLOT_COUNT; i++) {
            if (comp.hasCard(i) && comp.getCardItem(i) instanceof Md49Card) return true;
        }
        return false;
    }
}
