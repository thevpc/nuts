/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.toolbox.nfind;

import java.nio.file.Path;
import java.util.List;
import net.vpc.app.nuts.NutsDefinition;
import net.vpc.app.nuts.NutsDescriptor;
import net.vpc.app.nuts.NutsId;
import net.vpc.app.nuts.NutsSession;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.app.NutsApplicationContext;

/**
 *
 * @author vpc
 */
class NutsInfo {

    NutsId nuts;
    String desc;
    Boolean fetched;
    Boolean is_installed;
    Boolean is_updatable;
    NutsApplicationContext context;
    NutsWorkspace ws;
    NutsSession session;
    NutsDescriptor descriptor;
    NutsDefinition _fetchedFile;
    NutsDefinition def;
    List<NutsInfo> children;
    boolean continued;
    boolean error;

    public NutsInfo(NutsIdExt nuts, NutsApplicationContext context) {
        this.nuts = nuts.id;
        this.desc = nuts.extra;
        this.context = context;
        ws = context.getWorkspace();
        session = context.getSession();
    }

    public boolean isFetched() {
        if (this.fetched == null) {
            this.fetched = getNutsDefinition(true) != null;
        }
        return this.fetched;
    }

    public NutsDefinition getNutsDefinition(boolean checkDependencies) {
        if (this.def == null) {
            def = ws.fetch().id(nuts).setSession(session).offline()
                    .setIncludeInstallInformation(true)
                    .setIncludeFile(true)
                    .setAcceptOptional(false)
                    .includeDependencies(checkDependencies)
                    .getResultDefinition();
        }
        return def;
    }

    public boolean isInstalled(boolean checkDependencies) {
        if (this.is_installed == null) {
            this.is_installed = getNutsDefinition(checkDependencies).getInstallation().isInstalled();
        }
        return this.is_installed;
    }

    public boolean isUpdatable() {
        if (this.is_updatable == null) {
            this.is_updatable = false;
            if (this.isFetched()) {
                NutsId nut2 = null;
                try {
                    nut2 = ws.fetch().id(nuts.setVersion("")).setSession(session.copy().setProperty("monitor-allowed", false)).setTransitive(true).wired().getResultId();
                } catch (Exception ex) {
                    //ignore
                }
                if (nut2 != null && nut2.getVersion().compareTo(nuts.getVersion()) > 0) {
                    this.is_updatable = true;
                }
            }
        }
        return this.is_updatable;
    }

    public Path getFile() {
        if (_fetchedFile == null) {
            try {
                _fetchedFile = getNutsDefinition(true);
            } catch (Exception ex) {
                //
            }
        }
        if (_fetchedFile == null || _fetchedFile.getContent().getPath() == null) {
            return null;
        }
        return _fetchedFile.getContent().getPath();
    }

    public NutsDescriptor getDescriptor() {
        if (descriptor == null) {
            //                    NutsDescriptor dd = ws.fetchDescriptor(nuts.toString(), true, session.copy().setTransitive(true).setFetchMode(NutsFetchMode.ONLINE));
            //                    if(dd.isExecutable()){
            //                        System.out.println("");
            //                    }
            try {
                descriptor = ws.fetch().id(nuts).setSession(session).setTransitive(true).wired().effective(true).getResultDescriptor();
            } catch (Exception ex) {
                descriptor = ws.fetch().id(nuts).setSession(session).setTransitive(true).wired().effective(false).getResultDescriptor();
            }
        }
        return descriptor;
    }

}
