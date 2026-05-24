package meteordevelopment.meteorclient.systems.modules.player;

import meteordevelopment.meteorclient.events.entity.player.FinishUsingItemEvent;
import meteordevelopment.meteorclient.events.entity.player.StoppedUsingItemEvent;
import meteordevelopment.meteorclient.events.meteor.MouseClickEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.systems.friends.Friend;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.input.KeyAction;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;

public class MiddleClickExtra
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Mode> mode;
    private final Setting<Boolean> message;
    private final Setting<String> friendMessage;
    private final Setting<Boolean> quickSwap;
    private final Setting<Boolean> swapBack;
    private final Setting<Boolean> disableInCreative;
    private final Setting<Boolean> notify;
    private boolean isUsing;
    private boolean wasHeld;
    private int itemSlot;
    private int selectedSlot;

    public MiddleClickExtra() {
        super(Categories.Player, "middle-click-extra", "Perform various actions when you middle click.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.mode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("mode")).description("Which item to use when you middle click.")).defaultValue(Mode.Pearl)).build());
        this.message = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("send-message")).description("Sends a message when you add a player as a friend.")).defaultValue(false)).visible(() -> this.mode.get() == Mode.AddFriend)).build());
        this.friendMessage = this.sgGeneral.add(((StringSetting.Builder)((StringSetting.Builder)((StringSetting.Builder)((StringSetting.Builder)new StringSetting.Builder().name("message-to-send")).description("Message to send when you add a player as a friend (use %player for the player's name)")).defaultValue("/msg %player I just friended you on Meteor.")).visible(() -> this.mode.get() == Mode.AddFriend)).build());
        this.quickSwap = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("quick-swap")).description("Allows you to use items in your inventory by simulating hotbar key presses. May get flagged by anticheats.")).defaultValue(false)).visible(() -> this.mode.get() != Mode.AddFriend)).build());
        this.swapBack = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("swap-back")).description("Swap back to your original slot when you finish using an item.")).defaultValue(false)).visible(() -> this.mode.get() != Mode.AddFriend && this.quickSwap.get() == false)).build());
        this.disableInCreative = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("disable-in-creative")).description("Middle click action is disabled in Creative mode.")).defaultValue(true)).build());
        this.notify = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("notify")).description("Notifies you when you do not have the specified item in your hotbar.")).defaultValue(true)).visible(() -> this.mode.get() != Mode.AddFriend)).build());
    }

    @Override
    public void onDeactivate() {
        this.stopIfUsing(false);
    }

    @EventHandler
    private void onMouseClick(MouseClickEvent event) {
        if (event.action != KeyAction.Press || event.button() != 2 || this.mc.screen != null) {
            return;
        }
        if (this.disabledByCreative()) {
            return;
        }
        if (this.mode.get() == Mode.AddFriend) {
            if (this.mc.crosshairPickEntity == null) {
                return;
            }
            Entity entity = this.mc.crosshairPickEntity;
            if (!(entity instanceof Player)) {
                return;
            }
            Player player = (Player)entity;
            if (!Friends.get().isFriend(player)) {
                Friends.get().add(new Friend(player));
                this.info("Added %s to friends", player.getName().getString());
                if (this.message.get().booleanValue()) {
                    String messageNotify = this.friendMessage.get().replace("%player", player.getName().getString());
                    ChatUtils.sendPlayerMsg(messageNotify);
                }
            } else {
                Friends.get().remove(Friends.get().get(player));
                this.info("Removed %s from friends", player.getName().getString());
            }
            return;
        }
        FindItemResult result = InvUtils.find(this.mode.get().item);
        if (!result.found() || !result.isHotbar() && !this.quickSwap.get().booleanValue()) {
            if (this.notify.get().booleanValue()) {
                this.warning("Unable to find specified item.", new Object[0]);
            }
            return;
        }
        this.selectedSlot = this.mc.player.getInventory().getSelectedSlot();
        this.itemSlot = result.slot();
        this.wasHeld = result.isMainHand();
        if (!this.wasHeld) {
            if (!this.quickSwap.get().booleanValue()) {
                InvUtils.swap(result.slot(), this.swapBack.get());
            } else {
                InvUtils.quickSwap().fromId(this.selectedSlot).to(this.itemSlot);
            }
        }
        if (this.mode.get().immediate) {
            this.mc.gameMode.useItem((Player)this.mc.player, InteractionHand.MAIN_HAND);
            this.swapBack(false);
        } else {
            this.mc.options.keyUse.setDown(true);
            this.isUsing = true;
        }
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!this.isUsing) {
            return;
        }
        boolean pressed = true;
        if (this.mc.player.getMainHandItem().getItem() instanceof BowItem) {
            pressed = BowItem.getPowerForTime((int)this.mc.player.getTicksUsingItem()) < 1.0f;
        }
        this.mc.options.keyUse.setDown(pressed);
    }

    @EventHandler
    private void onPacketSendEvent(PacketEvent.Send event) {
        if (event.packet instanceof ServerboundSetCarriedItemPacket) {
            this.stopIfUsing(true);
        }
    }

    @EventHandler
    private void onStoppedUsingItem(StoppedUsingItemEvent event) {
        this.stopIfUsing(false);
    }

    @EventHandler
    private void onFinishUsingItem(FinishUsingItemEvent event) {
        this.stopIfUsing(false);
    }

    private void stopIfUsing(boolean wasCancelled) {
        if (this.isUsing) {
            this.swapBack(wasCancelled);
            this.mc.options.keyUse.setDown(false);
            this.isUsing = false;
        }
    }

    void swapBack(boolean wasCancelled) {
        if (this.wasHeld) {
            return;
        }
        if (this.quickSwap.get().booleanValue()) {
            InvUtils.quickSwap().fromId(this.selectedSlot).to(this.itemSlot);
        } else {
            if (!this.swapBack.get().booleanValue() || wasCancelled) {
                return;
            }
            InvUtils.swapBack();
        }
    }

    private boolean disabledByCreative() {
        if (this.mc.player == null) {
            return false;
        }
        return this.disableInCreative.get() != false && this.mc.player.gameMode() == GameType.CREATIVE;
    }

    public static enum Mode {
        Pearl(Items.ENDER_PEARL, true),
        XP(Items.EXPERIENCE_BOTTLE, true),
        Rocket(Items.FIREWORK_ROCKET, true),
        WindCharge(Items.WIND_CHARGE, true),
        Bow(Items.BOW, false),
        Gap(Items.GOLDEN_APPLE, false),
        EGap(Items.ENCHANTED_GOLDEN_APPLE, false),
        Chorus(Items.CHORUS_FRUIT, false),
        AddFriend(null, true);

        private final Item item;
        private final boolean immediate;

        private Mode(Item item, boolean immediate) {
            this.item = item;
            this.immediate = immediate;
        }
    }
}
