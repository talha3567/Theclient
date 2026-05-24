package net.wurstclient.altmanager;

import java.awt.Component;
import java.awt.HeadlessException;
import java.io.File;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import net.wurstclient.util.SwingUtils;

public final class ImportAltsFileChooser
extends JFileChooser {
    public static void main(String[] args) {
        SwingUtils.setLookAndFeel();
        ImportAltsFileChooser fileChooser = new ImportAltsFileChooser(new File(args[0]));
        fileChooser.setFileSelectionMode(0);
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("TXT file (username:password)", "txt"));
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("JSON file", "json"));
        if (fileChooser.showOpenDialog(null) != 0) {
            return;
        }
        String path = fileChooser.getSelectedFile().getAbsolutePath();
        System.out.println(path);
    }

    public ImportAltsFileChooser(File currentDirectory) {
        super(currentDirectory);
    }

    @Override
    protected JDialog createDialog(Component parent) throws HeadlessException {
        JDialog dialog = super.createDialog(parent);
        dialog.setAlwaysOnTop(true);
        return dialog;
    }
}
