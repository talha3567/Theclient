package net.wurstclient.serverfinder;

import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.class_12239;
import net.minecraft.class_642;
import net.minecraft.class_644;
import net.wurstclient.WurstClient;

public class WurstServerPinger {
    private static final AtomicInteger threadNumber = new AtomicInteger(0);
    private class_642 server;
    private boolean done = false;
    private boolean failed = false;

    public void ping(String ip) {
        this.ping(ip, 25565);
    }

    public void ping(String ip, int port) {
        this.server = new class_642("", ip + ":" + port, class_642.class_8678.field_45611);
        new Thread(() -> this.pingInCurrentThread(ip, port), "Wurst Server Pinger #" + threadNumber.incrementAndGet()).start();
    }

    private void pingInCurrentThread(String ip, int port) {
        class_644 pinger = new class_644();
        System.out.println("Pinging " + ip + ":" + port + "...");
        try {
            pinger.method_3003(this.server, () -> {}, () -> {}, class_12239.method_75867((boolean)WurstClient.MC.field_1690.method_1639()));
            System.out.println("Ping successful: " + ip + ":" + port);
        }
        catch (UnknownHostException e) {
            System.out.println("Unknown host: " + ip + ":" + port);
            this.failed = true;
        }
        catch (Exception e2) {
            System.out.println("Ping failed: " + ip + ":" + port);
            this.failed = true;
        }
        pinger.method_3004();
        this.done = true;
    }

    public boolean isStillPinging() {
        return !this.done;
    }

    public boolean isWorking() {
        return !this.failed;
    }

    public String getServerIP() {
        return this.server.field_3761;
    }
}
