package com.rpicos.circuitcraft.mixin;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Set;

/** {@link net.minecraft.world.entity.ai.village.poi.PoiManager} only ever resolves a block
 *  state to a POI type via {@link PoiTypes#forState}, which reads a private static map that
 *  vanilla's own bootstrap populates via a private {@code registerBlockStates} method -
 *  registering a modded {@link PoiType} in {@code BuiltInRegistries.POINT_OF_INTEREST_TYPE}
 *  (what {@link com.rpicos.circuitcraft.ModVillagers} otherwise does) never touches that map,
 *  so a modded job-site block is never actually found by a villager's own job-site search.
 *  This invoker is the only way to reach that private method from mod code. */
@Mixin(PoiTypes.class)
public interface PoiTypesInvokerMixin {
	@Invoker("registerBlockStates")
	static void circuitcraft$registerBlockStates(Holder<PoiType> type, Set<BlockState> matchingStates) {
		throw new AssertionError("Mixin not applied");
	}
}
