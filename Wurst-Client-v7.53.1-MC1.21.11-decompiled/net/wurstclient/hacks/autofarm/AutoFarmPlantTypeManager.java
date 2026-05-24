package net.wurstclient.hacks.autofarm;

import java.util.List;
import java.util.stream.Stream;
import net.minecraft.class_2338;
import net.minecraft.class_2680;
import net.wurstclient.hacks.autofarm.AutoFarmPlantType;
import net.wurstclient.hacks.autofarm.plants.AmethystPlantType;
import net.wurstclient.hacks.autofarm.plants.BambooPlantType;
import net.wurstclient.hacks.autofarm.plants.BeetrootsPlantType;
import net.wurstclient.hacks.autofarm.plants.CactusPlantType;
import net.wurstclient.hacks.autofarm.plants.CarrotsPlantType;
import net.wurstclient.hacks.autofarm.plants.ChorusPlantPlantType;
import net.wurstclient.hacks.autofarm.plants.CocoaBeanPlantType;
import net.wurstclient.hacks.autofarm.plants.GlowBerryPlantType;
import net.wurstclient.hacks.autofarm.plants.KelpPlantType;
import net.wurstclient.hacks.autofarm.plants.MelonPlantType;
import net.wurstclient.hacks.autofarm.plants.NetherWartPlantType;
import net.wurstclient.hacks.autofarm.plants.PitcherPlantPlantType;
import net.wurstclient.hacks.autofarm.plants.PotatoesPlantType;
import net.wurstclient.hacks.autofarm.plants.PumpkinPlantType;
import net.wurstclient.hacks.autofarm.plants.SeaPicklePlantType;
import net.wurstclient.hacks.autofarm.plants.SugarCanePlantType;
import net.wurstclient.hacks.autofarm.plants.SweetBerryPlantType;
import net.wurstclient.hacks.autofarm.plants.TorchflowerPlantType;
import net.wurstclient.hacks.autofarm.plants.TwistingVinesPlantType;
import net.wurstclient.hacks.autofarm.plants.WeepingVinesPlantType;
import net.wurstclient.hacks.autofarm.plants.WheatPlantType;
import net.wurstclient.settings.Setting;
import net.wurstclient.settings.ToggleAllPlantTypesSetting;
import net.wurstclient.util.BlockUtils;

public final class AutoFarmPlantTypeManager {
    public final AmethystPlantType amethystType = new AmethystPlantType();
    public final BambooPlantType bambooType = new BambooPlantType();
    public final BeetrootsPlantType beetrootsType = new BeetrootsPlantType();
    public final CactusPlantType cactusType = new CactusPlantType();
    public final CarrotsPlantType carrotsType = new CarrotsPlantType();
    public final ChorusPlantPlantType chorusFruitType = new ChorusPlantPlantType();
    public final CocoaBeanPlantType cocoaBeanType = new CocoaBeanPlantType();
    public final GlowBerryPlantType glowBerryType = new GlowBerryPlantType();
    public final KelpPlantType kelpType = new KelpPlantType();
    public final MelonPlantType melonType = new MelonPlantType();
    public final NetherWartPlantType netherWartType = new NetherWartPlantType();
    public final PitcherPlantPlantType pitcherPlantType = new PitcherPlantPlantType();
    public final PotatoesPlantType potatoesType = new PotatoesPlantType();
    public final PumpkinPlantType pumpkinType = new PumpkinPlantType();
    public final SeaPicklePlantType seaPickleType = new SeaPicklePlantType();
    public final SugarCanePlantType sugarCaneType = new SugarCanePlantType();
    public final SweetBerryPlantType sweetBerryPlantType = new SweetBerryPlantType();
    public final TorchflowerPlantType torchflowerType = new TorchflowerPlantType();
    public final TwistingVinesPlantType twistingVinesType = new TwistingVinesPlantType();
    public final WeepingVinesPlantType weepingVinesType = new WeepingVinesPlantType();
    public final WheatPlantType wheatType = new WheatPlantType();
    public final List<AutoFarmPlantType> plantTypes = List.of(this.amethystType, this.bambooType, this.beetrootsType, this.cactusType, this.carrotsType, this.chorusFruitType, this.cocoaBeanType, this.glowBerryType, this.kelpType, this.melonType, this.netherWartType, this.pitcherPlantType, this.potatoesType, this.pumpkinType, this.seaPickleType, this.sugarCaneType, this.sweetBerryPlantType, this.torchflowerType, this.twistingVinesType, this.weepingVinesType, this.wheatType);
    public final ToggleAllPlantTypesSetting toggleAllSetting = new ToggleAllPlantTypesSetting("All plant types", this.plantTypes.stream().flatMap(AutoFarmPlantType::getSettings));

    public AutoFarmPlantType getReplantingSpotType(class_2338 pos) {
        class_2680 state = BlockUtils.getState(pos);
        return this.plantTypes.stream().filter(type -> type.isReplantingSpot(pos, state)).findFirst().orElse(null);
    }

    public boolean shouldHarvestByMining(class_2338 pos) {
        class_2680 state = BlockUtils.getState(pos);
        return this.plantTypes.stream().filter(AutoFarmPlantType::isHarvestingEnabled).anyMatch(type -> type.shouldHarvestByMining(pos, state));
    }

    public boolean shouldHarvestByInteracting(class_2338 pos) {
        class_2680 state = BlockUtils.getState(pos);
        return this.plantTypes.stream().filter(AutoFarmPlantType::isHarvestingEnabled).anyMatch(type -> type.shouldHarvestByInteracting(pos, state));
    }

    public Stream<Setting> getSettings() {
        return Stream.concat(Stream.of(this.toggleAllSetting), this.plantTypes.stream().flatMap(AutoFarmPlantType::getSettings));
    }
}
