package net.thevpc.nuts.runtime.standalone.workspace.cmd.install;

import net.thevpc.nuts.util.NCopiable;

public class InstallFlags implements Cloneable, NCopiable {
    protected boolean force = false;
    protected boolean switchVersion = false;
    protected boolean repair = false;
    protected boolean install = false;
    protected boolean deployOnly = false;
    protected boolean require = false;
    protected boolean update = false;

    public void merge(InstallFlags other) {
        this.force = this.force || other.force;
        this.switchVersion = this.switchVersion || other.switchVersion;
        this.repair = this.repair || other.repair;
        this.install = this.install || other.install;
        this.deployOnly = this.deployOnly || other.deployOnly;
        this.require = this.require || other.require;
        this.update = this.update || other.update;
    }

    @Override
    protected Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public InstallFlags copy() {
        return (InstallFlags) clone();
    }
}
