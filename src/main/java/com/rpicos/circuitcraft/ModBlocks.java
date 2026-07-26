package com.rpicos.circuitcraft;

import com.rpicos.circuitcraft.block.AcSourceBlock;
import com.rpicos.circuitcraft.block.AmmeterBlock;
import com.rpicos.circuitcraft.block.CapacitorBlock;
import com.rpicos.circuitcraft.block.CccsBlock;
import com.rpicos.circuitcraft.block.CcvsBlock;
import com.rpicos.circuitcraft.block.CurrentSourceBlock;
import com.rpicos.circuitcraft.block.DiodeBlock;
import com.rpicos.circuitcraft.block.FrequencyModuleBlock;
import com.rpicos.circuitcraft.block.FunctionGeneratorBlock;
import com.rpicos.circuitcraft.block.GroundBlock;
import com.rpicos.circuitcraft.block.InductorBlock;
import com.rpicos.circuitcraft.block.MemristorBlock;
import com.rpicos.circuitcraft.block.NmosBlock;
import com.rpicos.circuitcraft.block.NpnBlock;
import com.rpicos.circuitcraft.block.OpAmpBlock;
import com.rpicos.circuitcraft.block.PmosBlock;
import com.rpicos.circuitcraft.block.PnpBlock;
import com.rpicos.circuitcraft.block.PowerSupplyBlock;
import com.rpicos.circuitcraft.block.R2VConverterBlock;
import com.rpicos.circuitcraft.block.ResistorBlock;
import com.rpicos.circuitcraft.block.V2RConverterBlock;
import com.rpicos.circuitcraft.block.VccsBlock;
import com.rpicos.circuitcraft.block.VcvsBlock;
import com.rpicos.circuitcraft.block.VoltageModuleBlock;
import com.rpicos.circuitcraft.block.VoltmeterBlock;
import com.rpicos.circuitcraft.block.WireBlock;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.Function;

public final class ModBlocks {
	public static final Block RESISTOR = register("resistor", ResistorBlock::new, componentProperties());
	public static final Block CAPACITOR = register("capacitor", CapacitorBlock::new, componentProperties());
	public static final Block INDUCTOR = register("inductor", InductorBlock::new, componentProperties());
	public static final Block MEMRISTOR = register("memristor", MemristorBlock::new, componentProperties());
	public static final Block POWER_SUPPLY = register("power_supply", PowerSupplyBlock::new, componentProperties());
	public static final Block FUNCTION_GENERATOR = register("function_generator", FunctionGeneratorBlock::new, componentProperties());
	public static final Block WIRE = register("wire", WireBlock::new, componentProperties());
	public static final Block AMMETER = register("ammeter", AmmeterBlock::new, componentProperties());
	public static final Block VOLTAGE_MODULE = register("voltage_module", VoltageModuleBlock::new, componentProperties());
	public static final Block FREQUENCY_MODULE = register("frequency_module", FrequencyModuleBlock::new, componentProperties());
	public static final Block GROUND = register("ground", GroundBlock::new, componentProperties());
	public static final Block DIODE = register("diode", DiodeBlock::new, componentProperties());
	public static final Block OP_AMP = register("op_amp", OpAmpBlock::new, componentProperties());
	public static final Block AC_SOURCE = register("ac_source", AcSourceBlock::new, componentProperties());
	public static final Block R2V_CONVERTER = register("r2v_converter", R2VConverterBlock::new, componentProperties());
	public static final Block V2R_CONVERTER = register("v2r_converter", V2RConverterBlock::new, componentProperties());
	public static final Block CURRENT_SOURCE = register("current_source", CurrentSourceBlock::new, componentProperties());
	public static final Block VOLTMETER = register("voltmeter", VoltmeterBlock::new, componentProperties());
	public static final Block NPN = register("npn", NpnBlock::new, componentProperties());
	public static final Block PNP = register("pnp", PnpBlock::new, componentProperties());
	public static final Block NMOS = register("nmos", NmosBlock::new, componentProperties());
	public static final Block PMOS = register("pmos", PmosBlock::new, componentProperties());
	public static final Block VCVS = register("vcvs", VcvsBlock::new, componentProperties());
	public static final Block VCCS = register("vccs", VccsBlock::new, componentProperties());
	public static final Block CCCS = register("cccs", CccsBlock::new, componentProperties());
	public static final Block CCVS = register("ccvs", CcvsBlock::new, componentProperties());

	/** Plain block, no facing/block-entity: the Electrician's job site, not a circuit
	 *  participant, so it doesn't extend ComponentBlock or implement EntityBlock. */
	public static final Block BREADBOARD = register("breadboard", Block::new, breadboardProperties());

	/** The Electronics Engineer's job site - same shape as the Breadboard above. */
	public static final Block WORKBENCH = register("workbench", Block::new, breadboardProperties());

	private static BlockBehaviour.Properties componentProperties() {
		return BlockBehaviour.Properties.of().strength(1.0F).sound(SoundType.METAL);
	}

	private static BlockBehaviour.Properties breadboardProperties() {
		return BlockBehaviour.Properties.of().strength(2.5F).sound(SoundType.WOOD);
	}

	private static <T extends Block> T register(String path, Function<BlockBehaviour.Properties, T> factory, BlockBehaviour.Properties properties) {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, CircuitCraft.id(path));
		T block = factory.apply(properties.setId(key));
		return Registry.register(BuiltInRegistries.BLOCK, key, block);
	}

	public static void init() {
	}
}
