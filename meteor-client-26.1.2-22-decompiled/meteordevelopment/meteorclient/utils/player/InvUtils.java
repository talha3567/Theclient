package meteordevelopment.meteorclient.utils.player;

import java.util.function.Predicate;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.mixininterface.IMultiPlayerGameMode;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.SlotUtils;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Range;

public class InvUtils {
    private static final Action ACTION = new Action();
    public static int previousSlot = -1;

    private InvUtils() {
    }

    private static Predicate<ItemStack> isOneOf(Item ... items) {
        return itemStack -> {
            for (Item item : items) {
                if (!itemStack.is((Object)item)) continue;
                return true;
            }
            return false;
        };
    }

    public static boolean testInMainHand(Predicate<ItemStack> predicate) {
        return predicate.test(MeteorClient.mc.player.getMainHandItem());
    }

    public static boolean testInMainHand(Item ... items) {
        return InvUtils.testInMainHand(InvUtils.isOneOf(items));
    }

    public static boolean testInOffHand(Predicate<ItemStack> predicate) {
        return predicate.test(MeteorClient.mc.player.getOffhandItem());
    }

    public static boolean testInOffHand(Item ... items) {
        return InvUtils.testInOffHand(InvUtils.isOneOf(items));
    }

    public static boolean testInHands(Predicate<ItemStack> predicate) {
        return InvUtils.testInMainHand(predicate) || InvUtils.testInOffHand(predicate);
    }

    public static boolean testInHands(Item ... items) {
        return InvUtils.testInMainHand(items) || InvUtils.testInOffHand(items);
    }

    public static boolean testInHotbar(Predicate<ItemStack> predicate) {
        if (InvUtils.testInHands(predicate)) {
            return true;
        }
        for (int i = 0; i <= 8; ++i) {
            ItemStack stack = MeteorClient.mc.player.getInventory().getItem(i);
            if (!predicate.test(stack)) continue;
            return true;
        }
        return false;
    }

    public static boolean testInHotbar(Item ... items) {
        return InvUtils.testInHotbar(InvUtils.isOneOf(items));
    }

    public static FindItemResult findEmpty() {
        return InvUtils.find(ItemStack::isEmpty);
    }

    public static FindItemResult findInHotbar(Item ... items) {
        return InvUtils.findInHotbar(InvUtils.isOneOf(items));
    }

    public static FindItemResult findInHotbar(Predicate<ItemStack> isGood) {
        if (InvUtils.testInOffHand(isGood)) {
            return new FindItemResult(40, MeteorClient.mc.player.getOffhandItem().getCount());
        }
        if (InvUtils.testInMainHand(isGood)) {
            return new FindItemResult(MeteorClient.mc.player.getInventory().getSelectedSlot(), MeteorClient.mc.player.getMainHandItem().getCount());
        }
        return InvUtils.find(isGood, 0, 8);
    }

    public static FindItemResult find(Item ... items) {
        return InvUtils.find(InvUtils.isOneOf(items));
    }

    public static FindItemResult find(Predicate<ItemStack> isGood) {
        if (MeteorClient.mc.player == null) {
            return new FindItemResult(0, 0);
        }
        return InvUtils.find(isGood, 0, MeteorClient.mc.player.getInventory().getContainerSize());
    }

    public static FindItemResult find(Predicate<ItemStack> isGood, int start, int end) {
        if (MeteorClient.mc.player == null) {
            return new FindItemResult(0, 0);
        }
        int slot = -1;
        int count = 0;
        for (int i = start; i <= end; ++i) {
            ItemStack stack = MeteorClient.mc.player.getInventory().getItem(i);
            if (!isGood.test(stack)) continue;
            if (slot == -1) {
                slot = i;
            }
            count += stack.getCount();
        }
        return new FindItemResult(slot, count);
    }

    public static FindItemResult findFastestTool(BlockState state) {
        float bestScore = 1.0f;
        int slot = -1;
        for (int i = 0; i < 9; ++i) {
            float score;
            ItemStack stack = MeteorClient.mc.player.getInventory().getItem(i);
            if (!stack.isCorrectToolForDrops(state) || !((score = stack.getDestroySpeed(state)) > bestScore)) continue;
            bestScore = score;
            slot = i;
        }
        return new FindItemResult(slot, 1);
    }

    public static boolean swap(int slot, boolean swapBack) {
        if (slot == 40) {
            return true;
        }
        if (slot < 0 || slot > 8) {
            return false;
        }
        if (swapBack && previousSlot == -1) {
            previousSlot = MeteorClient.mc.player.getInventory().getSelectedSlot();
        } else if (!swapBack) {
            previousSlot = -1;
        }
        MeteorClient.mc.player.getInventory().setSelectedSlot(slot);
        ((IMultiPlayerGameMode)MeteorClient.mc.gameMode).meteor$syncSelected();
        return true;
    }

    public static boolean swapBack() {
        if (previousSlot == -1) {
            return false;
        }
        boolean return_ = InvUtils.swap(previousSlot, false);
        previousSlot = -1;
        return return_;
    }

    public static Action move() {
        InvUtils.ACTION.type = ContainerInput.PICKUP;
        InvUtils.ACTION.two = true;
        return ACTION;
    }

    public static Action click() {
        InvUtils.ACTION.type = ContainerInput.PICKUP;
        return ACTION;
    }

    public static Action quickSwap() {
        InvUtils.ACTION.type = ContainerInput.SWAP;
        return ACTION;
    }

    public static Action shiftClick() {
        InvUtils.ACTION.type = ContainerInput.QUICK_MOVE;
        return ACTION;
    }

    public static Action drop() {
        InvUtils.ACTION.type = ContainerInput.THROW;
        InvUtils.ACTION.data = 1;
        return ACTION;
    }

    public static Action dropOne() {
        InvUtils.ACTION.type = ContainerInput.THROW;
        InvUtils.ACTION.data = 0;
        return ACTION;
    }

    public static void dropHand() {
        if (!MeteorClient.mc.player.containerMenu.getCarried().isEmpty()) {
            MeteorClient.mc.gameMode.handleContainerInput(MeteorClient.mc.player.containerMenu.containerId, -999, 0, ContainerInput.PICKUP, (Player)MeteorClient.mc.player);
        }
    }

    public static class Action {
        private ContainerInput type = null;
        private boolean two = false;
        private int from = -1;
        private int to = -1;
        private int data = 0;
        private boolean isRecursive = false;

        private Action() {
        }

        public Action fromId(int id) {
            this.from = id;
            return this;
        }

        public Action from(int index) {
            return this.fromId(SlotUtils.indexToId(index));
        }

        public Action fromHotbar(@Range(from=0L, to=8L) int i) {
            return this.from(0 + i);
        }

        public Action fromOffhand() {
            return this.from(40);
        }

        public Action fromMain(@Range(from=0L, to=26L) int i) {
            return this.from(9 + i);
        }

        public Action fromArmor(int i) {
            return this.from(36 + (3 - i));
        }

        public void toId(int id) {
            this.to = id;
            this.run();
        }

        public void to(int index) {
            this.toId(SlotUtils.indexToId(index));
        }

        public void toHotbar(@Range(from=0L, to=8L) int i) {
            this.to(0 + i);
        }

        public void toOffhand() {
            this.to(40);
        }

        public void toMain(@Range(from=0L, to=26L) int i) {
            this.to(9 + i);
        }

        public void toArmor(int i) {
            this.to(36 + (3 - i));
        }

        public void slotId(int id) {
            this.from = this.to = id;
            this.run();
        }

        public void slot(int index) {
            this.slotId(SlotUtils.indexToId(index));
        }

        public void slotHotbar(@Range(from=0L, to=8L) int i) {
            this.slot(0 + i);
        }

        public void slotOffhand() {
            this.slot(40);
        }

        public void slotMain(@Range(from=0L, to=26L) int i) {
            this.slot(9 + i);
        }

        public void slotArmor(int i) {
            this.slot(36 + (3 - i));
        }

        private void run() {
            boolean hadEmptyCursor = MeteorClient.mc.player.containerMenu.getCarried().isEmpty();
            if (this.type == ContainerInput.SWAP) {
                this.data = this.from;
                this.from = this.to;
            }
            if (this.type != null && this.from != -1 && this.to != -1) {
                this.click(this.from);
                if (this.two) {
                    this.click(this.to);
                }
            }
            ContainerInput preType = this.type;
            boolean preTwo = this.two;
            int preFrom = this.from;
            int preTo = this.to;
            this.type = null;
            this.two = false;
            this.from = -1;
            this.to = -1;
            this.data = 0;
            if (!this.isRecursive && hadEmptyCursor && preType == ContainerInput.PICKUP && preTwo && preFrom != -1 && preTo != -1 && !MeteorClient.mc.player.containerMenu.getCarried().isEmpty()) {
                this.isRecursive = true;
                InvUtils.click().slotId(preFrom);
                this.isRecursive = false;
            }
        }

        private void click(int id) {
            MeteorClient.mc.gameMode.handleContainerInput(MeteorClient.mc.player.containerMenu.containerId, id, this.data, this.type, (Player)MeteorClient.mc.player);
        }
    }
}
