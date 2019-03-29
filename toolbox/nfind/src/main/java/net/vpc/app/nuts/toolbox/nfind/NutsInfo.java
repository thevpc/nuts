/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.toolbox.nfind;

import java.io.File;
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
            this.fetched = ws.isFetched(nuts.toString(), session);
        }
        return this.fetched;
    }

    public boolean isInstalled(boolean checkDependencies) {
        if (this.is_installed == null) {
            this.is_installed = isFetched() && ws.fetch(nuts).setSession(session).setAcceptOptional(false).includeDependencies(checkDependencies).fetchDefinition().getInstallation().isInstalled();
        }
        return this.is_installed;
    }

    public boolean isUpdatable() {
        if (this.is_updatable == null) {
            this.is_updatable = false;
            if (this.isFetched()) {
                NutsId nut2 = null;
                try {
                    nut2 = ws.fetch(nuts.setVersion("")).setSession(session.copy().setProperty("monitor-allowed", false)).setTransitive(true).wired().fetchId();
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

    public File getFile() {
        if (_fetchedFile == null) {
            try {
                _fetchedFile = ws.fetch(nuts).setSession(session).setTransitive(true).offline().fetchDefinition();
            } catch (Exception ex) {
                //
            }
        }
        if (_fetchedFile == null || _fetchedFile.getContent().getFile() == null) {
            return null;
        }
        return new File(_fetchedFile.getContent().getFile());
    }

    public NutsDescriptor getDescriptor() {
        if (descriptor == null) {
            //                    NutsDescriptor dd = ws.fetchDescriptor(nuts.toString(), true, session.copy().setTransitive(true).setFetchMode(NutsFetchMode.ONLINE));
            //                    if(dd.isExecutable()){
            //                        System.out.println("");
            //                    }
            try {
                descriptor = ws.fetch(nuts).setSession(session).setTransitive(true).wired().setIncludeEffective(true).fetchDescriptor();
            } catch (Exception ex) {
                descriptor = ws.fetch(nuts).setSession(session).setTransitive(true).wired().setIncludeEffective(false).fetchDescriptor();
            }
        }
        return descriptor;
    }
    
}
