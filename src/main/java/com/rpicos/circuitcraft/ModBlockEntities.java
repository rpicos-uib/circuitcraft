package com.rpicos.circuitcraft;

import com.rpicos.circuitcraft.blockentity.AmmeterBlockEntity;
import com.rpicos.circuitcraft.blockentity.CapacitorBlockEntity;
import com.rpicos.circuitcraft.blockentity.DiodeBlockEntity;
import com.rpicos.circuitcraft.blockentity.FrequencyModuleBlockEntity;
import com.rpicos.circuitcraft.blockentity.FunctionGeneratorBlockEntity;
import com.rpicos.circuitcraft.blockentity.GroundBlockEntity;
import com.rpicos.circuitcraft.blockentity.InductorBlockEntity;
import com.rpicos.circuitcraft.blockentity.MemristorBlockEntity;
import com.rpicos.circuitcraft.blockentity.OpAmpBlockEntity;
import com.rpicos.circuitcraft.blockentity.PowerSupplyBlockEntity;
import com.rpicos.circuitcraft.blockentity.ResistorBlockEntity;
import com.rpicos.circuitcraft.blockentity.VoltageModuleBlockEntity;
import com.rpicos.circuitcraft.blockentity.WireBlockEntity;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.Set;

public final class ModBlockEntities {
	public static final BlockEntityType<ResistorBlockEntity> RESISTOR =
			register("resistor", ResistorBlockEntity::new, ModBlocks.RESISTOR);
	public static final BlockEntityType<CapacitorBlockEntity> CAPACITOR =
			register("capacitor", CapacitorBlockEntity::new, ModBlocks.CAPACITOR);
	public static final BlockEntityType<InductorBlockEntity> INDUCTOR =
			register("inductor", InductorBlockEntity::new, ModBlocks.INDUCTOR);
	public static final BlockEntityType<MemristorBlockEntity> MEMRISTOR =
			register("memristor", MemristorBlockEntity::new, ModBlocks.MEMRISTOR);
	public static final BlockEntityType<PowerSupplyBlockEntity> POWER_SUPPLY =
			register("power_supply", PowerSupplyBlockEntity::new, ModBlocks.POWER_SUPPLY);
	public static final BlockEntityType<FunctionGeneratorBlockEntity> FUNCTION_GENERATOR =
			register("function_generator", FunctionGeneratorBlockEntity::new, ModBlocks.FUNCTION_GENERATOR);
	public static final BlockEntityType<WireBlockEntity> WIRE =
			register("wire", WireBlockEntity::new, ModBlocks.WIRE);
	public static final BlockEntityType<AmmeterBlockEntity> AMMETER =
			register("ammeter", AmmeterBlockEntity::new, ModBlocks.AMMETER);
	public static final BlockEntityType<VoltageModuleBlockEntity> VOLTAGE_MODULE =
			register("voltage_module", VoltageModuleBlockEntity::new, ModBlocks.VOLTAGE_MODULE);
	public static final BlockEntityType<FrequencyModuleBlockEntity> FREQUENCY_MODULE =
			register("frequency_module", FrequencyModuleBlockEntity::new, ModBlocks.FREQUENCY_MODULE);
	public static final BlockEntityType<GroundBlockEntity> GROUND =
			register("ground", GroundBlockEntity::new, ModBlocks.GROUND);
	public static final BlockEntityType<DiodeBlockEntity> DIODE =
			register("diode", DiodeBlockEntity::new, ModBlocks.DIODE);
	public static final BlockEntityType<OpAmpBlockEntity> OP_AMP =
			register("op_amp", OpAmpBlockEntity::new, ModBlocks.OP_AMP);

	private static <T extends BlockEntity> BlockEntityType<T> register(
			String path, BlockEntityType.BlockEntitySupplier<T> factory, Block block) {
		ResourceKey<BlockEntityType<?>> key = ResourceKey.create(Registries.BLOCK_ENTITY_TYPE, CircuitCraft.id(path));
		BlockEntityType<T> type = new BlockEntityType<>(factory, Set.of(block));
		return Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, key, type);
	}

	public static void init() {
	}
}
