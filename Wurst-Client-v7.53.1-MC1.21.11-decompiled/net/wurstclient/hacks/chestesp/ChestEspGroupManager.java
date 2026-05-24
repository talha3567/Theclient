package net.wurstclient.hacks.chestesp;

import java.util.List;
import java.util.stream.Stream;
import net.wurstclient.hacks.chestesp.ChestEspBlockGroup;
import net.wurstclient.hacks.chestesp.ChestEspEntityGroup;
import net.wurstclient.hacks.chestesp.ChestEspGroup;
import net.wurstclient.hacks.chestesp.groups.BarrelsGroup;
import net.wurstclient.hacks.chestesp.groups.ChestBoatsGroup;
import net.wurstclient.hacks.chestesp.groups.ChestCartsGroup;
import net.wurstclient.hacks.chestesp.groups.CraftersGroup;
import net.wurstclient.hacks.chestesp.groups.DispensersGroup;
import net.wurstclient.hacks.chestesp.groups.DroppersGroup;
import net.wurstclient.hacks.chestesp.groups.EnderChestsGroup;
import net.wurstclient.hacks.chestesp.groups.FurnacesGroup;
import net.wurstclient.hacks.chestesp.groups.HopperCartsGroup;
import net.wurstclient.hacks.chestesp.groups.HoppersGroup;
import net.wurstclient.hacks.chestesp.groups.NormalChestsGroup;
import net.wurstclient.hacks.chestesp.groups.PotsGroup;
import net.wurstclient.hacks.chestesp.groups.ShulkerBoxesGroup;
import net.wurstclient.hacks.chestesp.groups.TrapChestsGroup;

public final class ChestEspGroupManager {
    public final NormalChestsGroup normalChests = new NormalChestsGroup();
    public final TrapChestsGroup trapChests = new TrapChestsGroup();
    public final EnderChestsGroup enderChests = new EnderChestsGroup();
    public final ChestCartsGroup chestCarts = new ChestCartsGroup();
    public final ChestBoatsGroup chestBoats = new ChestBoatsGroup();
    public final BarrelsGroup barrels = new BarrelsGroup();
    public final PotsGroup pots = new PotsGroup();
    public final ShulkerBoxesGroup shulkerBoxes = new ShulkerBoxesGroup();
    public final HoppersGroup hoppers = new HoppersGroup();
    public final HopperCartsGroup hopperCarts = new HopperCartsGroup();
    public final DroppersGroup droppers = new DroppersGroup();
    public final DispensersGroup dispensers = new DispensersGroup();
    public final CraftersGroup crafters = new CraftersGroup();
    public final FurnacesGroup furnaces = new FurnacesGroup();
    public final List<ChestEspBlockGroup> blockGroups = List.of(this.normalChests, this.trapChests, this.enderChests, this.barrels, this.pots, this.shulkerBoxes, this.hoppers, this.droppers, this.dispensers, this.crafters, this.furnaces);
    public final List<ChestEspEntityGroup> entityGroups = List.of(this.chestCarts, this.chestBoats, this.hopperCarts);
    public final List<ChestEspGroup> allGroups = Stream.concat(this.blockGroups.stream(), this.entityGroups.stream()).toList();
}
