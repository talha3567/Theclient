package net.wurstclient.command;

import java.util.Arrays;
import net.minecraft.class_128;
import net.minecraft.class_129;
import net.minecraft.class_148;
import net.wurstclient.WurstClient;
import net.wurstclient.command.CmdException;
import net.wurstclient.command.CmdList;
import net.wurstclient.command.Command;
import net.wurstclient.events.ChatOutputListener;
import net.wurstclient.hacks.TooManyHaxHack;
import net.wurstclient.util.ChatUtils;

public final class CmdProcessor
implements ChatOutputListener {
    private final CmdList cmds;

    public CmdProcessor(CmdList cmds) {
        this.cmds = cmds;
    }

    @Override
    public void onSentMessage(ChatOutputListener.ChatOutputEvent event) {
        if (!WurstClient.INSTANCE.isEnabled()) {
            return;
        }
        String message = event.getOriginalMessage().trim();
        if (!message.startsWith(".")) {
            return;
        }
        event.cancel();
        this.process(message.substring(1));
    }

    public void process(String input) {
        try {
            Command cmd = this.parseCmd(input);
            TooManyHaxHack tooManyHax = WurstClient.INSTANCE.getHax().tooManyHaxHack;
            if (tooManyHax.isEnabled() && tooManyHax.isBlocked(cmd)) {
                ChatUtils.error(cmd.getName() + " is blocked by TooManyHax.");
                return;
            }
            this.runCmd(cmd, input);
        }
        catch (CmdNotFoundException e) {
            e.printToChat();
        }
    }

    private Command parseCmd(String input) throws CmdNotFoundException {
        String cmdName = input.split(" ")[0];
        Command cmd = this.cmds.getCmdByName(cmdName);
        if (cmd == null) {
            throw new CmdNotFoundException(input);
        }
        return cmd;
    }

    private void runCmd(Command cmd, String input) {
        String[] args = input.split(" ");
        args = Arrays.copyOfRange(args, 1, args.length);
        try {
            cmd.call(args);
        }
        catch (CmdException e) {
            e.printToChat(cmd);
        }
        catch (Throwable e) {
            class_128 report = class_128.method_560((Throwable)e, (String)"Running Wurst command");
            class_129 section = report.method_562("Affected command");
            section.method_577("Command input", () -> input);
            throw new class_148(report);
        }
    }

    private static class CmdNotFoundException
    extends Exception {
        private final String input;

        public CmdNotFoundException(String input) {
            this.input = input;
        }

        public void printToChat() {
            String cmdName = this.input.split(" ")[0];
            ChatUtils.error("Unknown command: ." + cmdName);
            StringBuilder helpMsg = new StringBuilder();
            if (this.input.startsWith("/")) {
                helpMsg.append("Use \".say " + this.input + "\"");
                helpMsg.append(" to send it as a chat command.");
            } else {
                helpMsg.append("Type \".help\" for a list of commands or ");
                helpMsg.append("\".say ." + this.input + "\"");
                helpMsg.append(" to send it as a chat message.");
            }
            ChatUtils.message(helpMsg.toString());
        }
    }
}
