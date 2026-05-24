package meteordevelopment.meteorclient.systems.modules.misc;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringListSetting;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.misc.text.RunnableClickEvent;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundResourcePackPushPacket;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerboundResourcePackPacket;
import net.minecraft.network.protocol.common.custom.BrandPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.apache.commons.lang3.Strings;

public class ServerSpoof
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Boolean> spoofBrand;
    private final Setting<String> brand;
    private final Setting<Boolean> resourcePack;
    private final Setting<Boolean> blockChannels;
    private final Setting<List<String>> channels;
    private MutableComponent msg;
    public boolean silentAcceptResourcePack;

    public ServerSpoof() {
        super(Categories.Misc, "server-spoof", "Spoof client brand, resource pack and channels.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.spoofBrand = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("spoof-brand")).description("Whether or not to spoof the brand.")).defaultValue(true)).build());
        this.brand = this.sgGeneral.add(((StringSetting.Builder)((StringSetting.Builder)((StringSetting.Builder)((StringSetting.Builder)new StringSetting.Builder().name("brand")).description("Specify the brand that will be send to the server.")).defaultValue("vanilla")).visible(this.spoofBrand::get)).build());
        this.resourcePack = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("resource-pack")).description("Spoof accepting server resource pack.")).defaultValue(false)).build());
        this.blockChannels = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("block-channels")).description("Whether or not to block some channels.")).defaultValue(true)).build());
        this.channels = this.sgGeneral.add(((StringListSetting.Builder)((StringListSetting.Builder)((StringListSetting.Builder)new StringListSetting.Builder().name("channels")).description("If the channel contains the keyword, this outgoing channel will be blocked.")).defaultValue("fabric", "minecraft:register").visible(this.blockChannels::get)).build());
        this.silentAcceptResourcePack = false;
        this.runInMainMenu = true;
    }

    @EventHandler
    private void onPacketSend(PacketEvent.Send event) {
        if (!this.isActive()) {
            return;
        }
        Packet<?> packet = event.packet;
        if (packet instanceof ServerboundCustomPayloadPacket) {
            ServerboundCustomPayloadPacket customPayloadPacket = (ServerboundCustomPayloadPacket)packet;
            Identifier id = customPayloadPacket.payload().type().id();
            if (this.blockChannels.get().booleanValue()) {
                for (String channel : this.channels.get()) {
                    if (!Strings.CI.contains((CharSequence)id.toString(), (CharSequence)channel)) continue;
                    event.cancel();
                    return;
                }
            }
            if (this.spoofBrand.get().booleanValue() && id.equals((Object)BrandPayload.TYPE.id())) {
                ServerboundCustomPayloadPacket spoofedPacket = new ServerboundCustomPayloadPacket((CustomPacketPayload)new BrandPayload(this.brand.get()));
                event.sendSilently((Packet<?>)spoofedPacket);
                event.cancel();
            }
        }
        if (this.silentAcceptResourcePack && event.packet instanceof ServerboundResourcePackPacket) {
            event.cancel();
        }
    }

    @EventHandler
    private void onPacketReceive(PacketEvent.Receive event) {
        if (!this.isActive() || !this.resourcePack.get().booleanValue()) {
            return;
        }
        Packet<?> packet = event.packet;
        if (!(packet instanceof ClientboundResourcePackPushPacket)) {
            return;
        }
        ClientboundResourcePackPushPacket packet2 = (ClientboundResourcePackPushPacket)packet;
        event.cancel();
        event.connection.send((Packet)new ServerboundResourcePackPacket(packet2.id(), ServerboundResourcePackPacket.Action.ACCEPTED));
        event.connection.send((Packet)new ServerboundResourcePackPacket(packet2.id(), ServerboundResourcePackPacket.Action.DOWNLOADED));
        event.connection.send((Packet)new ServerboundResourcePackPacket(packet2.id(), ServerboundResourcePackPacket.Action.SUCCESSFULLY_LOADED));
        this.msg = Component.literal((String)"This server has ");
        this.msg.append(packet2.required() ? "a required " : "an optional ").append("resource pack. ");
        MutableComponent link = Component.literal((String)"[Open URL]");
        link.setStyle(link.getStyle().withColor(ChatFormatting.BLUE).withUnderlined(Boolean.valueOf(true)).withClickEvent((ClickEvent)new ClickEvent.OpenUrl(URI.create(packet2.url()))).withHoverEvent((HoverEvent)new HoverEvent.ShowText((Component)Component.literal((String)"Click to open the pack url"))));
        MutableComponent acceptance = Component.literal((String)"[Accept Pack]");
        acceptance.setStyle(acceptance.getStyle().withColor(ChatFormatting.DARK_GREEN).withUnderlined(Boolean.valueOf(true)).withClickEvent((ClickEvent)new RunnableClickEvent(() -> {
            URL url = ServerSpoof.getParsedResourcePackUrl(packet2.url());
            if (url == null) {
                this.error("Invalid resource pack URL: " + packet2.url(), new Object[0]);
            } else {
                this.silentAcceptResourcePack = true;
                this.mc.getDownloadedPackSource().pushPack(packet2.id(), url, packet2.hash());
            }
        })).withHoverEvent((HoverEvent)new HoverEvent.ShowText((Component)Component.literal((String)"Click to accept and apply the pack."))));
        this.msg.append((Component)link).append(" ");
        this.msg.append((Component)acceptance).append(".");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!this.isActive() || !Utils.canUpdate() || this.msg == null) {
            return;
        }
        this.info((Component)this.msg);
        this.msg = null;
    }

    private static URL getParsedResourcePackUrl(String url) {
        try {
            URL uRL = new URI(url).toURL();
            String string = uRL.getProtocol();
            return !"http".equals(string) && !"https".equals(string) ? null : uRL;
        }
        catch (MalformedURLException | URISyntaxException exception) {
            return null;
        }
    }
}
