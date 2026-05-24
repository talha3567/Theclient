package net.wurstclient.altmanager;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import net.wurstclient.altmanager.Alt;
import net.wurstclient.altmanager.AltsFile;
import net.wurstclient.altmanager.CrackedAlt;
import net.wurstclient.altmanager.LoginException;
import net.wurstclient.altmanager.MojangAlt;

public final class AltManager {
    private final AltsFile altsFile;
    private final ArrayList<Alt> alts = new ArrayList();
    private int numPremium;
    private int numCracked;

    public AltManager(Path altsFile, Path encFolder) {
        this.altsFile = new AltsFile(altsFile, encFolder);
        this.altsFile.load(this);
    }

    public boolean contains(String name) {
        for (Alt alt : this.alts) {
            if (!alt.getName().equalsIgnoreCase(name)) continue;
            return true;
        }
        return false;
    }

    public void add(Alt alt) {
        this.alts.add(alt);
        this.sortAlts();
        this.altsFile.save(this);
    }

    public void addAll(Collection<Alt> c) {
        this.alts.addAll(c);
        this.sortAlts();
        this.altsFile.save(this);
    }

    public void edit(Alt oldAlt, String newNameOrEmail, String newPassword) {
        this.remove(oldAlt);
        if (newPassword.isEmpty()) {
            this.add(new CrackedAlt(newNameOrEmail, oldAlt.isFavorite()));
        } else {
            this.add(new MojangAlt(newNameOrEmail, newPassword, "", oldAlt.isFavorite()));
        }
    }

    public void login(Alt alt) throws LoginException {
        boolean wasUnchecked = alt.isUncheckedPremium();
        alt.login();
        if (wasUnchecked) {
            ++this.numPremium;
        }
        if (!alt.isCracked()) {
            this.altsFile.save(this);
        }
    }

    public void toggleFavorite(Alt alt) {
        alt.setFavorite(!alt.isFavorite());
        this.sortAlts();
        this.altsFile.save(this);
    }

    public void remove(int index) {
        Alt alt = this.alts.get(index);
        this.alts.remove(index);
        if (alt.isCracked()) {
            --this.numCracked;
        } else if (alt.isCheckedPremium()) {
            --this.numPremium;
        }
        this.altsFile.save(this);
    }

    public void remove(Alt alt) {
        if (!this.alts.remove(alt)) {
            return;
        }
        if (alt.isCracked()) {
            --this.numCracked;
        } else if (alt.isCheckedPremium()) {
            --this.numPremium;
        }
        this.altsFile.save(this);
    }

    private void sortAlts() {
        Comparator<Alt> c = Comparator.comparing(a -> !a.isFavorite());
        c = c.thenComparing(Alt::isCracked);
        c = c.thenComparing(a -> a.getDisplayName().toLowerCase());
        ArrayList newAlts = this.alts.stream().distinct().sorted(c).collect(Collectors.toCollection(ArrayList::new));
        this.alts.clear();
        this.alts.addAll(newAlts);
        this.numCracked = (int)this.alts.stream().filter(Alt::isCracked).count();
        this.numPremium = (int)this.alts.stream().filter(Alt::isCheckedPremium).count();
    }

    public List<Alt> getList() {
        return Collections.unmodifiableList(this.alts);
    }

    public int getNumPremium() {
        return this.numPremium;
    }

    public int getNumCracked() {
        return this.numCracked;
    }

    public Exception getFolderException() {
        return this.altsFile.getFolderException();
    }
}
