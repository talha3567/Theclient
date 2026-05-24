package net.wurstclient.serverfinder;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import net.minecraft.class_11909;
import net.minecraft.class_156;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_342;
import net.minecraft.class_364;
import net.minecraft.class_4068;
import net.minecraft.class_4185;
import net.minecraft.class_4267;
import net.minecraft.class_437;
import net.minecraft.class_500;
import net.minecraft.class_641;
import net.minecraft.class_642;
import net.wurstclient.serverfinder.WurstServerPinger;
import net.wurstclient.util.MathUtils;

public class ServerFinderScreen
extends class_437 {
    private final class_500 prevScreen;
    private class_342 ipBox;
    private class_342 maxThreadsBox;
    private class_4185 searchButton;
    private ServerFinderState state;
    private int maxThreads;
    private int checked;
    private int working;

    public ServerFinderScreen(class_500 prevScreen) {
        super((class_2561)class_2561.method_43470((String)"Server Finder"));
        this.prevScreen = prevScreen;
    }

    public void method_25426() {
        this.searchButton = class_4185.method_46430((class_2561)class_2561.method_43470((String)"Search"), b -> this.searchOrCancel()).method_46434(this.field_22789 / 2 - 100, this.field_22790 / 4 + 96 + 12, 200, 20).method_46431();
        this.method_37063((class_364)this.searchButton);
        this.searchButton.field_22763 = false;
        this.method_37063((class_364)class_4185.method_46430((class_2561)class_2561.method_43470((String)"Tutorial"), b -> class_156.method_668().method_670("https://www.wurstclient.net/serverfinder-tutorial/")).method_46434(this.field_22789 / 2 - 100, this.field_22790 / 4 + 120 + 12, 200, 20).method_46431());
        this.method_37063((class_364)class_4185.method_46430((class_2561)class_2561.method_43470((String)"Back"), b -> this.method_25419()).method_46434(this.field_22789 / 2 - 100, this.field_22790 / 4 + 144 + 12, 200, 20).method_46431());
        this.ipBox = new class_342(this.field_22793, this.field_22789 / 2 - 100, this.field_22790 / 4 + 34, 200, 20, (class_2561)class_2561.method_43473());
        this.ipBox.method_1880(200);
        this.method_25429((class_364)this.ipBox);
        this.method_25395((class_364)this.ipBox);
        this.maxThreadsBox = new class_342(this.field_22793, this.field_22789 / 2 - 32, this.field_22790 / 4 + 58, 26, 12, (class_2561)class_2561.method_43473());
        this.maxThreadsBox.method_1880(3);
        this.maxThreadsBox.method_1852("128");
        this.method_25429((class_364)this.maxThreadsBox);
        this.state = ServerFinderState.NOT_RUNNING;
    }

    private void searchOrCancel() {
        if (this.state.isRunning()) {
            this.state = ServerFinderState.CANCELLED;
            this.ipBox.field_22763 = true;
            this.maxThreadsBox.field_22763 = true;
            this.searchButton.method_25355((class_2561)class_2561.method_43470((String)"Search"));
            return;
        }
        this.state = ServerFinderState.RESOLVING;
        this.maxThreads = Integer.parseInt(this.maxThreadsBox.method_1882());
        this.ipBox.field_22763 = false;
        this.maxThreadsBox.field_22763 = false;
        this.searchButton.method_25355((class_2561)class_2561.method_43470((String)"Cancel"));
        this.checked = 0;
        this.working = 0;
        new Thread(this::findServers, "Server Finder").start();
    }

    private void findServers() {
        try {
            int[] changes;
            InetAddress addr = InetAddress.getByName(this.ipBox.method_1882().split(":")[0].trim());
            int[] ipParts = new int[4];
            for (int i = 0; i < 4; ++i) {
                ipParts[i] = addr.getAddress()[i] & 0xFF;
            }
            this.state = ServerFinderState.SEARCHING;
            ArrayList<WurstServerPinger> pingers = new ArrayList<WurstServerPinger>();
            for (int change : changes = new int[]{0, 1, -1, 2, -2, 3, -3}) {
                for (int i2 = 0; i2 <= 255; ++i2) {
                    if (this.state == ServerFinderState.CANCELLED) {
                        return;
                    }
                    int[] ipParts2 = (int[])ipParts.clone();
                    ipParts2[2] = ipParts[2] + change & 0xFF;
                    ipParts2[3] = i2;
                    String ip = ipParts2[0] + "." + ipParts2[1] + "." + ipParts2[2] + "." + ipParts2[3];
                    WurstServerPinger pinger = new WurstServerPinger();
                    pinger.ping(ip);
                    pingers.add(pinger);
                    while (pingers.size() >= this.maxThreads) {
                        if (this.state == ServerFinderState.CANCELLED) {
                            return;
                        }
                        this.updatePingers(pingers);
                    }
                }
            }
            while (pingers.size() > 0) {
                if (this.state == ServerFinderState.CANCELLED) {
                    return;
                }
                this.updatePingers(pingers);
            }
            this.state = ServerFinderState.DONE;
        }
        catch (UnknownHostException e) {
            this.state = ServerFinderState.UNKNOWN_HOST;
        }
        catch (Exception e) {
            e.printStackTrace();
            this.state = ServerFinderState.ERROR;
        }
    }

    private void updatePingers(ArrayList<WurstServerPinger> pingers) {
        for (int i = 0; i < pingers.size(); ++i) {
            WurstServerPinger pinger = pingers.get(i);
            if (pinger.isStillPinging()) continue;
            ++this.checked;
            if (pinger.isWorking()) {
                ++this.working;
                String name = "Grief me #" + this.working;
                String ip = pinger.getServerIP();
                this.addServerToList(name, ip);
            }
            pingers.remove(i);
            --i;
        }
    }

    private void addServerToList(String name, String ip) {
        class_641 serverList = this.prevScreen.method_2529();
        if (serverList.method_44295(ip) != null) {
            return;
        }
        serverList.method_2988(new class_642(name, ip, class_642.class_8678.field_45611), false);
        serverList.method_2987();
        class_4267 listWidget = this.prevScreen.field_3043;
        listWidget.method_20122(null);
        listWidget.method_20125(serverList);
    }

    public void method_25393() {
        this.searchButton.field_22763 = MathUtils.isInteger(this.maxThreadsBox.method_1882()) && !this.ipBox.method_1882().isEmpty();
    }

    public boolean method_25402(class_11909 context, boolean doubleClick) {
        if (context.method_74245() == 3) {
            this.method_25419();
            return true;
        }
        return super.method_25402(context, doubleClick);
    }

    public void method_25394(class_332 context, int mouseX, int mouseY, float partialTicks) {
        context.method_25300(this.field_22793, "Server Finder", this.field_22789 / 2, 20, -1);
        context.method_25300(this.field_22793, "This will search for servers with similar IPs", this.field_22789 / 2, 40, -6250336);
        context.method_25300(this.field_22793, "to the IP you type into the field below.", this.field_22789 / 2, 50, -6250336);
        context.method_25300(this.field_22793, "The servers it finds will be added to your server list.", this.field_22789 / 2, 60, -6250336);
        context.method_25303(this.field_22793, "Server address:", this.field_22789 / 2 - 100, this.field_22790 / 4 + 24, -6250336);
        this.ipBox.method_25394(context, mouseX, mouseY, partialTicks);
        context.method_25303(this.field_22793, "Max. threads:", this.field_22789 / 2 - 100, this.field_22790 / 4 + 60, -6250336);
        this.maxThreadsBox.method_25394(context, mouseX, mouseY, partialTicks);
        context.method_25300(this.field_22793, this.state.toString(), this.field_22789 / 2, this.field_22790 / 4 + 73, -6250336);
        context.method_25303(this.field_22793, "Checked: " + this.checked + " / 1792", this.field_22789 / 2 - 100, this.field_22790 / 4 + 84, -6250336);
        context.method_25303(this.field_22793, "Working: " + this.working, this.field_22789 / 2 - 100, this.field_22790 / 4 + 94, -6250336);
        for (class_4068 drawable : this.field_33816) {
            drawable.method_25394(context, mouseX, mouseY, partialTicks);
        }
    }

    public void method_25419() {
        this.state = ServerFinderState.CANCELLED;
        this.field_22787.method_1507((class_437)this.prevScreen);
    }

    static enum ServerFinderState {
        NOT_RUNNING(""),
        SEARCHING("\u00a72Searching..."),
        RESOLVING("\u00a72Resolving..."),
        UNKNOWN_HOST("\u00a74Unknown Host!"),
        CANCELLED("\u00a74Cancelled!"),
        DONE("\u00a72Done!"),
        ERROR("\u00a74An error occurred!");

        private final String name;

        private ServerFinderState(String name) {
            this.name = name;
        }

        public boolean isRunning() {
            return this == SEARCHING || this == RESOLVING;
        }

        public String toString() {
            return this.name;
        }
    }
}
