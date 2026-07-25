package com.rpicos.circuitcraft;

import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.item.trading.TradeSet;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Set;

/** The Electrician villager: trades the mod's own components instead of vanilla goods.
 *  Job site is {@link ModBlocks#BREADBOARD}. Unlike vanilla trades (hardcoded Java
 *  factories), this MC version's VillagerProfession only carries {@link ResourceKey}s
 *  pointing at data-driven TradeSet entries per level - the actual offers live under
 *  data/circuitcraft/{villager_trade,tags/villager_trade,trade_set}/electrician/. */
public final class ModVillagers {
	public static final ResourceKey<PoiType> ELECTRICIAN_POI =
			ResourceKey.create(Registries.POINT_OF_INTEREST_TYPE, CircuitCraft.id("electrician"));
	public static final ResourceKey<VillagerProfession> ELECTRICIAN =
			ResourceKey.create(Registries.VILLAGER_PROFESSION, CircuitCraft.id("electrician"));

	private static ResourceKey<TradeSet> tradeSetLevel(int level) {
		return ResourceKey.create(Registries.TRADE_SET, CircuitCraft.id("electrician/level_" + level));
	}

	public static void init() {
		Set<BlockState> jobSiteStates = ImmutableSet.copyOf(ModBlocks.BREADBOARD.getStateDefinition().getPossibleStates());
		Registry.register(BuiltInRegistries.POINT_OF_INTEREST_TYPE, ELECTRICIAN_POI, new PoiType(jobSiteStates, 1, 1));

		Int2ObjectMap<ResourceKey<TradeSet>> tradeSetsByLevel = Int2ObjectMap.ofEntries(
				Int2ObjectMap.entry(1, tradeSetLevel(1)),
				Int2ObjectMap.entry(2, tradeSetLevel(2)),
				Int2ObjectMap.entry(3, tradeSetLevel(3)),
				Int2ObjectMap.entry(4, tradeSetLevel(4)),
				Int2ObjectMap.entry(5, tradeSetLevel(5)));

		Registry.register(BuiltInRegistries.VILLAGER_PROFESSION, ELECTRICIAN, new VillagerProfession(
				Component.translatable("entity.circuitcraft.villager.electrician"),
				holder -> holder.is(ELECTRICIAN_POI),
				holder -> holder.is(ELECTRICIAN_POI),
				ImmutableSet.of(),
				ImmutableSet.of(),
				SoundEvents.VILLAGER_WORK_TOOLSMITH,
				tradeSetsByLevel));
	}
}
