package net.wurstclient.hacks;

import java.util.Comparator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_243;
import net.minecraft.class_2828;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.PacketOutputListener;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.DontSaveState;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.Setting;
import net.wurstclient.settings.filterlists.EntityFilterList;
import net.wurstclient.settings.filterlists.RemoteViewFilterList;
import net.wurstclient.util.ChatUtils;
import net.wurstclient.util.EntityUtils;
import net.wurstclient.util.FakePlayerEntity;

@SearchTags(value={"remote view"})
@DontSaveState
public final class RemoteViewHack
extends Hack
implements UpdateListener,
PacketOutputListener {
    private final EntityFilterList entityFilters = RemoteViewFilterList.create();
    private class_1297 entity = null;
    private boolean wasInvisible;
    private FakePlayerEntity fakePlayer;

    public RemoteViewHack() {
        super("RemoteView");
        this.setCategory(Category.RENDER);
        this.entityFilters.forEach(x$0 -> this.addSetting((Setting)x$0));
    }

    @Override
    protected void onEnable() {
        if (this.entity == null) {
            Stream<class_1297> stream = StreamSupport.stream(RemoteViewHack.MC.field_1687.method_18112().spliterator(), true).filter(class_1309.class::isInstance).filter(e -> !e.method_31481() && ((class_1309)e).method_6032() > 0.0f).filter(e -> e != RemoteViewHack.MC.field_1724).filter(e -> !(e instanceof FakePlayerEntity));
            stream = this.entityFilters.applyTo(stream);
            this.entity = stream.min(Comparator.comparingDouble(EntityUtils::distanceToHitboxSq)).orElse(null);
            if (this.entity == null) {
                ChatUtils.error("Could not find a valid entity.");
                this.setEnabled(false);
                return;
            }
        }
        this.wasInvisible = this.entity.method_5767();
        RemoteViewHack.MC.field_1724.field_5960 = true;
        this.fakePlayer = new FakePlayerEntity();
        ChatUtils.message("Now viewing " + this.entity.method_5477().getString() + ".");
        EVENTS.add(UpdateListener.class, this);
        EVENTS.add(PacketOutputListener.class, this);
    }

    @Override
    protected void onDisable() {
        EVENTS.remove(UpdateListener.class, this);
        EVENTS.remove(PacketOutputListener.class, this);
        if (this.entity != null) {
            ChatUtils.message("No longer viewing " + this.entity.method_5477().getString() + ".");
            this.entity.method_5648(this.wasInvisible);
            this.entity = null;
        }
        RemoteViewHack.MC.field_1724.field_5960 = false;
        if (this.fakePlayer != null) {
            this.fakePlayer.resetPlayerPosition();
            this.fakePlayer.despawn();
        }
    }

    public void onToggledByCommand(String viewName) {
        if (!this.isEnabled() && viewName != null && !viewName.isEmpty()) {
            this.entity = StreamSupport.stream(RemoteViewHack.MC.field_1687.method_18112().spliterator(), false).filter(class_1309.class::isInstance).filter(e -> !e.method_31481() && ((class_1309)e).method_6032() > 0.0f).filter(e -> e != RemoteViewHack.MC.field_1724).filter(e -> !(e instanceof FakePlayerEntity)).filter(e -> viewName.equalsIgnoreCase(e.method_5477().getString())).min(Comparator.comparingDouble(EntityUtils::distanceToHitboxSq)).orElse(null);
            if (this.entity == null) {
                ChatUtils.error("Entity \"" + viewName + "\" could not be found.");
                return;
            }
        }
        this.setEnabled(!this.isEnabled());
    }

    @Override
    public void onUpdate() {
        if (this.entity.method_31481() || ((class_1309)this.entity).method_6032() <= 0.0f) {
            this.setEnabled(false);
            return;
        }
        RemoteViewHack.MC.field_1724.method_5719(this.entity);
        RemoteViewHack.MC.field_1724.method_23327(this.entity.method_23317(), this.entity.method_23318() - (double)RemoteViewHack.MC.field_1724.method_18381(RemoteViewHack.MC.field_1724.method_18376()) + (double)this.entity.method_18381(this.entity.method_18376()), this.entity.method_23321());
        RemoteViewHack.MC.field_1724.method_22862();
        RemoteViewHack.MC.field_1724.method_18799(class_243.field_1353);
        this.entity.method_5648(true);
    }

    @Override
    public void onSentPacket(PacketOutputListener.PacketOutputEvent event) {
        if (event.getPacket() instanceof class_2828) {
            event.cancel();
        }
    }
}
