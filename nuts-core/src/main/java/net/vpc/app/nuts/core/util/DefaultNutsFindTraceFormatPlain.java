/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.util;

import net.vpc.app.nuts.NutsDefinition;
import net.vpc.app.nuts.NutsDescriptor;
import net.vpc.app.nuts.NutsId;
import net.vpc.app.nuts.NutsOutputFormat;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.core.spi.NutsWorkspaceExt;
import net.vpc.app.nuts.NutsOutputListFormat;
import net.vpc.app.nuts.core.util.common.CoreCommonUtils;

/**
 *
 * @author vpc
 */
public class DefaultNutsFindTraceFormatPlain extends DefaultNutsFindTraceFormatBase<NutsOutputListFormat> {

//    public static final NutsTraceFormat INSTANCE = new DefaultNutsFindTraceFormatPlain();
    private boolean longFormat = false;

    public DefaultNutsFindTraceFormatPlain(NutsWorkspace ws) {
        super(ws,NutsOutputFormat.PLAIN);
    }

//    public DefaultNutsFindTraceFormatPlain(NutsFindCommand findCommand, NutsSession session) {
//        this.session = session;
//        longFormat = ((DefaultNutsFindCommand) findCommand).isLongFormat();
//    }

    @Override
    public NutsOutputListFormat setOption(String name, String value) {
        if(name!=null){
            switch(name){
                case "long":{
                    longFormat=CoreCommonUtils.parseBoolean(value, true);
                    break;
                }
            }
        }
        return this;
    }

    
    @Override
    public void formatStart() {
    }

    @Override
    public void formatElement(Object object, long index) {
        if (object instanceof NutsId) {
            NutsId id = (NutsId) object;
            formatElement(id, null, null, index);
        } else if (object instanceof NutsDescriptor) {
            formatElement(null, ((NutsDescriptor) object), null, index);
        } else if (object instanceof NutsDefinition) {
            formatElement(null, null, ((NutsDefinition) object), index);
        } else {
            getValidOut().printf("%N%n", object);
        }
        getValidOut().flush();
    }

    private void formatElement(NutsId id, NutsDescriptor desc, NutsDefinition def, long index) {
        if (id == null) {
            if (desc != null) {
                id = desc.getId();
            }
            if (def != null) {
                id = def.getId();
            }
        }
        if (longFormat) {
            boolean i = NutsWorkspaceExt.of(getWs()).getInstalledRepository().isInstalled(id);
            boolean d = NutsWorkspaceExt.of(getWs()).getInstalledRepository().isDefaultVersion(id);
//            Boolean updatable = null;
            Boolean executable = null;
            Boolean executableApp = null;
            boolean fetched = false;

            boolean checkDependencies = false;
            NutsDefinition defFetched = null;

            try {
                if (!i || def == null) {
                    defFetched = getWs().fetch().id(id).setSession(
                            getValidSession().setTrace(false)
                    ).offline()
                            .setIncludeInstallInformation(true)
                            .setIncludeContent(true)
                            .setAcceptOptional(false)
                            .includeDependencies(checkDependencies)
                            .getResultDefinition();
                    fetched = true;
                } else {
                    fetched = true;
                }
            } catch (Exception ex) {
                //ignore!!
            }

            if (def != null) {
                executable = def.getDescriptor().isExecutable();
                executableApp = def.getDescriptor().isNutsApplication();
            } else if (defFetched != null) {
                executable = defFetched.getDescriptor().isExecutable();
                executableApp = defFetched.getDescriptor().isNutsApplication();
            } else if (desc != null) {
                executable = desc.isExecutable();
                executableApp = desc.isNutsApplication();
            }

//            if (fetched) {
//                NutsId nut2 = null;
//                updatable = false;
//                try {
//                    nut2 = ws.fetch().id(id.setVersion("")).setSession(session.copy().setProperty("monitor-allowed", false)).setTransitive(true).wired().setTrace(false).getResultId();
//                } catch (Exception ex) {
//                    //ignore
//                }
//                if (nut2 != null && nut2.getVersion().compareTo(id.getVersion()) > 0) {
//                    updatable = true;
//                }
//            }
            getValidOut().printf("##%s%s## %N%n",
                    i && d ? "I" : i ? "i" : fetched ? "f" : "r",
                    executableApp != null ? (executableApp ? "X" : executable ? "x" : "-") : ".",
                    //                    updatable != null ? (updatable ? "u" : "-") : ".",
                    NutsWorkspaceUtils.getIdFormat(getWs()).toString(id));
        } else {
            getValidOut().printf("%N%n", NutsWorkspaceUtils.getIdFormat(getWs()).toString(id));
        }
    }

    @Override
    public void formatEnd(long count) {

    }

}
