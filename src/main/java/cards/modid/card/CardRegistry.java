package cards.modid.card;

import cards.modid.PowerCaeds;
import cards.modid.card.impl.DashCard;
import cards.modid.card.impl.GhastCard;
import cards.modid.card.impl.GlockCard;
import cards.modid.card.impl.MidasCard;
import cards.modid.card.impl.NovaCard;
import cards.modid.card.impl.AncientCard;
import cards.modid.card.impl.AnchorCard;
import cards.modid.card.impl.ArtzCard;
import cards.modid.card.impl.BlazeCard;
import cards.modid.card.impl.ChestCard;
import cards.modid.card.impl.DeepCard;
import cards.modid.card.impl.ForcewallCard;
import cards.modid.card.impl.FreezeCard;
import cards.modid.card.impl.InvisCard;
import cards.modid.card.impl.MayhemCard;
import cards.modid.card.impl.Md49Card;
import cards.modid.card.impl.MinerCard;
import cards.modid.card.impl.SkeletonCard;
import cards.modid.card.impl.SpeedCard;
import cards.modid.card.impl.TotemCard;
import cards.modid.card.impl.TurmCard;
import cards.modid.card.impl.TimeStopCard;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * HOW TO ADD A NEW CARD:
 *   1. Create YourCard extends PowerCard in cards.modid.card.impl
 *   2. Add: public static final YourCard YOUR_CARD = register("your_card", YourCard::new);
 *   3. Done.
 */
public class CardRegistry {

    private static final List<PowerCard> CARDS = new ArrayList<>();

    // ── Cards ─────────────────────────────────────────────────────────────────
    public static final SpeedCard SPEED_CARD =
            register("speed_card", SpeedCard::new);
    public static final TotemCard TOTEM_CARD =
            register("totem_card", TotemCard::new);
    public static final DashCard DASH_CARD =
            register("dash_card", DashCard::new);
    public static final MidasCard MIDAS_CARD =
            register("midas_card", MidasCard::new);
    public static final SkeletonCard SKELETON_CARD =
            register("skeleton_card", SkeletonCard::new);
    public static final GhastCard GHAST_CARD =
            register("ghast_card", GhastCard::new);
    public static final AncientCard ANCIENT_CARD =
            register("ancient_card", AncientCard::new);
    public static final GlockCard GLOCK_CARD =
            register("glock_card", GlockCard::new);
    public static final NovaCard NOVA_CARD =
            register("nova_card", NovaCard::new);
    public static final MinerCard MINER_CARD =
            register("miner_card", MinerCard::new);
    public static final ChestCard CHEST_CARD =
            register("chest_card", ChestCard::new);
    public static final BlazeCard BLAZE_CARD =
            register("blaze_card", BlazeCard::new);
    public static final AnchorCard ANCHOR_CARD =
            register("anchor_card", AnchorCard::new);
    public static final ForcewallCard FORCEWALL_CARD =
            register("forcewall_card", ForcewallCard::new);
    public static final FreezeCard FREEZE_CARD =
            register("freeze_card", FreezeCard::new);
    public static final TimeStopCard TIMESTOP_CARD =
            register("timestop_card", TimeStopCard::new);
    public static final InvisCard INVIS_CARD =
            register("invis_card", InvisCard::new);
    public static final MayhemCard MAYHEM_CARD =
            register("mayhem_card", MayhemCard::new);
    public static final TurmCard TURM_CARD =
            register("turm_card", TurmCard::new);
    public static final DeepCard DEEP_CARD =
            register("deep_card", DeepCard::new);
    public static final ArtzCard ARTZ_CARD =
            register("artz_card", ArtzCard::new);
    public static final Md49Card MD49_CARD =
            register("md49_card", Md49Card::new);

    // ── Internal ──────────────────────────────────────────────────────────────
    private static <T extends PowerCard> T register(String name, Function<Item.Properties, T> factory) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM,
                Identifier.fromNamespaceAndPath(PowerCaeds.MOD_ID, name));
        T card = factory.apply(new Item.Properties().setId(key));
        Registry.register(BuiltInRegistries.ITEM, key, card);
        CARDS.add(card);
        return card;
    }

    public static void init() {
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(
                entries -> BuiltInRegistries.ITEM.stream()
                        .filter(i -> i instanceof PowerCard)
                        .map(ItemStack::new)
                        .forEach(entries::accept)
        );

        PowerCaeds.LOGGER.info("[PowerCaeds] CardRegistry initialized — {} card(s).", getCount());
    }

    public static List<PowerCard> getCards() {
        return Collections.unmodifiableList(CARDS);
    }

    public static long getCount() {
        return CARDS.size();
    }
}
