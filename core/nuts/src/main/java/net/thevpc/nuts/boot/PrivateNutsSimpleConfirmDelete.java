package net.thevpc.nuts.boot;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @app.category Internal
 */
class PrivateNutsSimpleConfirmDelete implements PrivateNutsConfirmDelete {

    private final List<File> ignoreDeletion = new ArrayList<>();
    private boolean force;

    @Override
    public boolean isForce() {
        return force;
    }

    @Override
    public void setForce(boolean value) {
        this.force = value;
    }

    @Override
    public boolean accept(File directory) {
        for (File ignored : ignoreDeletion) {
            String s = ignored.getPath() + File.separatorChar;
            if (s.startsWith(directory.getPath() + "/")) {
                return false;
            }
        }
        return true;
    }

    public void ignore(File directory) {
        ignoreDeletion.add(directory);
    }
}
