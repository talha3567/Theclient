package net.wurstclient.altmanager;

import java.awt.Component;
import java.awt.HeadlessException;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import net.wurstclient.util.SwingUtils;

public final class ExportAltsFileChooser
extends JFileChooser {
    public static void main(String[] args) {
        SwingUtils.setLookAndFeel();
        int response = JOptionPane.showConfirmDialog(null, "This will create an unencrypted (plain text) copy of your alt list.\nStoring passwords in plain text is risky because they can easily be stolen by a virus.\nStore this copy somewhere safe and keep it outside of your Minecraft folder!", "Warning", 2, 2);
        if (response != 0) {
            return;
        }
        ExportAltsFileChooser fileChooser = new ExportAltsFileChooser();
        fileChooser.setFileSelectionMode(0);
        fileChooser.setAcceptAllFileFilterUsed(false);
        FileNameExtensionFilter txtFilter = new FileNameExtensionFilter("TXT file (username:password)", "txt");
        fileChooser.addChoosableFileFilter(txtFilter);
        FileNameExtensionFilter jsonFilter = new FileNameExtensionFilter("JSON file", "json");
        fileChooser.addChoosableFileFilter(jsonFilter);
        if (fileChooser.showSaveDialog(null) != 0) {
            return;
        }
        Object path = fileChooser.getSelectedFile().getAbsolutePath();
        FileFilter fileFilter = fileChooser.getFileFilter();
        if (fileFilter == txtFilter && !((String)path).endsWith(".txt")) {
            path = (String)path + ".txt";
        } else if (fileFilter == jsonFilter && !((String)path).endsWith(".json")) {
            path = (String)path + ".json";
        }
        System.out.println((String)path);
    }

    @Override
    protected JDialog createDialog(Component parent) throws HeadlessException {
        JDialog dialog = super.createDialog(parent);
        dialog.setAlwaysOnTop(true);
        return dialog;
    }
}
