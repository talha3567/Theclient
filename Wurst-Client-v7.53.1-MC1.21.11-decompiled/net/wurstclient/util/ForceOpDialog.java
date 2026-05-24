package net.wurstclient.util;

import java.awt.Component;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.Frame;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.filechooser.FileNameExtensionFilter;
import net.wurstclient.util.SwingUtils;

public class ForceOpDialog
extends JDialog {
    private final ArrayList<Component> components = new ArrayList();
    private JSpinner spDelay;
    private JCheckBox cbDontWait;
    private JLabel lPasswords;
    private JLabel lTime;
    private JLabel lAttempts;
    private int numPW = 50;
    private int lastPW = -1;

    public static void main(String[] args) {
        SwingUtils.setLookAndFeel();
        new ForceOpDialog(args[0]);
    }

    public ForceOpDialog(String username) {
        super((Frame)null, "ForceOP", false);
        this.setAlwaysOnTop(true);
        this.setSize(512, 248);
        this.setResizable(false);
        this.setLocationRelativeTo(null);
        this.setLayout(null);
        SwingUtils.setExitOnClose(this);
        this.addLabel("Password list", 4, 4);
        this.addPwListSelector();
        this.addHowToUseButton();
        this.addSeparator(4, 56, 498, 4);
        this.addLabel("Speed", 4, 64);
        this.addDelaySelector();
        this.addDontWaitCheckbox();
        this.addSeparator(4, 132, 498, 4);
        this.addLabel("Username: " + username, 4, 140);
        this.lPasswords = this.addLabel("Passwords: error", 4, 160);
        this.lTime = this.addPersistentLabel("Estimated time: error", 4, 180);
        this.lAttempts = this.addPersistentLabel("Attempts: error", 4, 200);
        this.addStartButton();
        this.updateNumPasswords();
        this.setVisible(true);
        this.toFront();
        new Thread(this::handleDialogInput, "ForceOP dialog input").start();
    }

    private void handleDialogInput() {
        try (BufferedReader bf = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));){
            String line = "";
            while ((line = bf.readLine()) != null) {
                this.messageFromWurst(line);
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void messageFromWurst(String line) {
        if (line.startsWith("numPW ")) {
            this.numPW = Integer.parseInt(line.substring(6));
            this.updateNumPasswords();
            return;
        }
        if (line.startsWith("index ")) {
            this.lastPW = Integer.parseInt(line.substring(6));
            this.updateTimeLabel();
            this.updateAttemptsLabel();
        }
    }

    private void addPwListSelector() {
        JRadioButton rbDefaultList = new JRadioButton("default", true);
        rbDefaultList.setLocation(4, 24);
        rbDefaultList.setSize(rbDefaultList.getPreferredSize());
        this.add(rbDefaultList);
        JRadioButton rbTXTList = new JRadioButton("TXT file", false);
        rbTXTList.setLocation(rbDefaultList.getX() + rbDefaultList.getWidth() + 4, 24);
        rbTXTList.setSize(rbTXTList.getPreferredSize());
        this.add(rbTXTList);
        ButtonGroup rbGroup = new ButtonGroup();
        rbGroup.add(rbDefaultList);
        rbGroup.add(rbTXTList);
        JButton bBrowse = new JButton("browse");
        bBrowse.setLocation(rbTXTList.getX() + rbTXTList.getWidth() + 4, 24);
        bBrowse.setSize(bBrowse.getPreferredSize());
        bBrowse.setEnabled(rbTXTList.isSelected());
        bBrowse.addActionListener(e -> this.browsePwList());
        this.add(bBrowse);
        rbDefaultList.addActionListener(e -> {
            bBrowse.setEnabled(false);
            System.out.println("list default");
        });
        rbTXTList.addActionListener(e -> bBrowse.setEnabled(true));
    }

    private void browsePwList() {
        JFileChooser fsTXTList = new JFileChooser();
        fsTXTList.setAcceptAllFileFilterUsed(false);
        fsTXTList.addChoosableFileFilter(new FileNameExtensionFilter("TXT files", "txt"));
        fsTXTList.setFileSelectionMode(0);
        int action = fsTXTList.showOpenDialog(this);
        if (action != 0) {
            return;
        }
        if (!fsTXTList.getSelectedFile().exists()) {
            JOptionPane.showMessageDialog(this, "File does not exist!", "Error", 0);
            return;
        }
        String pwList = fsTXTList.getSelectedFile().getPath();
        System.out.println("list " + pwList);
    }

    private void addHowToUseButton() {
        JButton bHowTo = new JButton("How to use");
        bHowTo.setFont(new Font(bHowTo.getFont().getName(), 1, 16));
        bHowTo.setSize(bHowTo.getPreferredSize());
        bHowTo.setLocation(506 - bHowTo.getWidth() - 32, 12);
        bHowTo.addActionListener(e -> this.openHowToUseLink());
        this.add(bHowTo);
    }

    private void openHowToUseLink() {
        try {
            String howToLink = "https://www.wurstclient.net/forceop-tutorial/";
            Desktop.getDesktop().browse(URI.create(howToLink));
        }
        catch (IOException e2) {
            throw new RuntimeException(e2);
        }
    }

    private void addDelaySelector() {
        JLabel lDelay1 = this.addLabel("Delay between attempts:", 4, 84);
        this.spDelay = new JSpinner();
        this.spDelay.setToolTipText("<html>50ms: Fastest, doesn't bypass AntiSpam plugins<br>1000ms: Recommended, bypasses most AntiSpam plugins<br>10000ms: Slowest, bypasses all AntiSpam plugins</html>");
        this.spDelay.setModel(new SpinnerNumberModel(1000, 50, 10000, 50));
        this.spDelay.setLocation(lDelay1.getX() + lDelay1.getWidth() + 4, 84);
        this.spDelay.setSize(60, (int)this.spDelay.getPreferredSize().getHeight());
        this.spDelay.addChangeListener(e -> this.updateTimeLabel());
        this.add(this.spDelay);
        this.addLabel("ms", this.spDelay.getX() + this.spDelay.getWidth() + 4, 84);
    }

    private void addDontWaitCheckbox() {
        this.cbDontWait = new JCheckBox("<html>Don't wait for \"<span style=\"color: red;\"><b>Wrong password!</b></span>\" messages</html>", false);
        this.cbDontWait.setToolTipText("Increases the speed but can cause inaccuracy.");
        this.cbDontWait.setLocation(4, 104);
        this.cbDontWait.setSize(this.cbDontWait.getPreferredSize());
        this.cbDontWait.addActionListener(e -> this.updateTimeLabel());
        this.add(this.cbDontWait);
    }

    private void addStartButton() {
        JButton bStart = new JButton("Start");
        bStart.setFont(new Font(bStart.getFont().getName(), 1, 18));
        bStart.setLocation(302, 144);
        bStart.setSize(192, 66);
        bStart.addActionListener(e -> this.startForceOP());
        this.add(bStart);
    }

    private JLabel addLabel(String text, int x, int y) {
        JLabel label = this.makeLabel(text, x, y);
        this.add(label);
        return label;
    }

    private JLabel addPersistentLabel(String text, int x, int y) {
        JLabel label = this.makeLabel(text, x, y);
        super.add(label);
        return label;
    }

    private JLabel makeLabel(String text, int x, int y) {
        JLabel label = new JLabel(text);
        label.setLocation(x, y);
        label.setSize(label.getPreferredSize());
        return label;
    }

    private void addSeparator(int x, int y, int width, int height) {
        JSeparator sepSpeedStart = new JSeparator();
        sepSpeedStart.setLocation(x, y);
        sepSpeedStart.setSize(width, height);
        this.add(sepSpeedStart);
    }

    @Override
    public Component add(Component comp) {
        this.components.add(comp);
        return super.add(comp);
    }

    private void updateNumPasswords() {
        this.updatePasswordsLabel();
        this.updateTimeLabel();
        this.updateAttemptsLabel();
    }

    private void updatePasswordsLabel() {
        this.lPasswords.setText("Passwords: " + this.numPW);
        this.lPasswords.setSize(this.lPasswords.getPreferredSize());
    }

    private void updateTimeLabel() {
        int remainingPW = this.numPW - (this.lastPW + 1);
        long timeMS = remainingPW * (Integer)this.spDelay.getValue();
        timeMS += (long)((int)(timeMS / 30000L * 5000L));
        if (!this.cbDontWait.isSelected()) {
            timeMS += (long)(remainingPW * 50);
        }
        String timeString = this.getTimeString(timeMS);
        this.lTime.setText("Estimated time: " + timeString);
        this.lTime.setSize(this.lTime.getPreferredSize());
    }

    private String getTimeString(long ms) {
        TimeUnit uDays = TimeUnit.DAYS;
        TimeUnit uHours = TimeUnit.HOURS;
        TimeUnit uMin = TimeUnit.MINUTES;
        TimeUnit uMS = TimeUnit.MILLISECONDS;
        long days = uMS.toDays(ms);
        long hours = uMS.toHours(ms) - uDays.toHours(days);
        long minutes = uMS.toMinutes(ms) - uHours.toMinutes(uMS.toHours(ms));
        long seconds = uMS.toSeconds(ms) - uMin.toSeconds(uMS.toMinutes(ms));
        return days + "d " + hours + "h " + minutes + "m " + seconds + "s";
    }

    private void updateAttemptsLabel() {
        this.lAttempts.setText("Attempts: " + (this.lastPW + 1) + "/" + this.numPW);
        this.lAttempts.setSize(this.lAttempts.getPreferredSize());
    }

    private void startForceOP() {
        this.components.forEach(c -> c.setEnabled(false));
        int delay = (Integer)this.spDelay.getValue();
        boolean waitForMsg = !this.cbDontWait.isSelected();
        System.out.println("start " + delay + " " + waitForMsg);
    }
}
