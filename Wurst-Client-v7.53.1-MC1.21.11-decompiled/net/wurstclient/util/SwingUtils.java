package net.wurstclient.util;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JDialog;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public final class SwingUtils
extends Enum<SwingUtils> {
    private static final /* synthetic */ SwingUtils[] $VALUES;

    public static SwingUtils[] values() {
        return (SwingUtils[])$VALUES.clone();
    }

    public static SwingUtils valueOf(String name) {
        return Enum.valueOf(SwingUtils.class, name);
    }

    public static void setLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch (ReflectiveOperationException | UnsupportedLookAndFeelException e) {
            throw new RuntimeException(e);
        }
    }

    public static void setExitOnClose(JDialog dialog) {
        dialog.setDefaultCloseOperation(2);
        dialog.addWindowListener(new WindowAdapter(){

            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
    }

    private static /* synthetic */ SwingUtils[] $values() {
        return new SwingUtils[0];
    }

    static {
        $VALUES = SwingUtils.$values();
    }
}
