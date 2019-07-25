/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2019 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.core.format;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.impl.def.repos.DefaultNutsInstalledRepository;
import net.vpc.app.nuts.core.spi.NutsRepositoryConfigManagerExt;
import net.vpc.app.nuts.core.spi.NutsWorkspaceConfigManagerExt;
import net.vpc.app.nuts.core.spi.NutsWorkspaceExt;
import net.vpc.app.nuts.core.util.CoreNutsUtils;
import net.vpc.app.nuts.core.util.common.CoreCommonUtils;
import net.vpc.app.nuts.core.util.common.CoreStringUtils;

/**
 *
 * @author vpc
 */
public class FormattableNutsId {

    NutsId id;
    boolean i;
    boolean d;
    Boolean executable = null;
    Boolean executableApp = null;
    boolean fetched = false;
    boolean checkDependencies = false;
    NutsDefinition defFetched;
    NutsDefinition def;
    NutsDescriptor desc;
    NutsDependency dep;
    NutsSession session;
    Instant dte;
    String usr;
    char status_f;
    char status_e;
    char status_i;
    char status_s;
    char status_o;
    //    String display;
    boolean built = false;

    public static FormattableNutsId of(Object object, NutsSession session) {
        if (object instanceof NutsId) {
            NutsId v = (NutsId) object;
            return (new FormattableNutsId(v, session));
        } else if (object instanceof NutsDescriptor) {
            NutsDescriptor v = (NutsDescriptor) object;
            return (new FormattableNutsId(v, session));
        } else if (object instanceof NutsDefinition) {
            NutsDefinition v = (NutsDefinition) object;
            return (new FormattableNutsId(v, session));
        } else if (object instanceof NutsDependency) {
            NutsDependency v = (NutsDependency) object;
            return (new FormattableNutsId(v, session));
        } else if (object instanceof NutsDependencyTreeNode) {
            NutsDependencyTreeNode v = (NutsDependencyTreeNode) object;
            return (new FormattableNutsId(v, session));
        } else {
            return null;
        }

    }

    public FormattableNutsId(NutsDependencyTreeNode id, NutsSession session) {
        this(null, null, null, id.getDependency(), session);
    }

    public FormattableNutsId(NutsId id, NutsSession session) {
        this(id, null, null, null, session);
    }

    public FormattableNutsId(NutsDescriptor desc, NutsSession session) {
        this(null, desc, null, null, session);
    }

    public FormattableNutsId(NutsDefinition def, NutsSession session) {
        this(null, null, def, null, session);
    }

    public FormattableNutsId(NutsDependency dep, NutsSession session) {
        this(null, null, null, dep, session);
    }

    private FormattableNutsId(NutsId id, NutsDescriptor desc, NutsDefinition def, NutsDependency dep, NutsSession session) {
        if (id == null) {
            if (def != null) {
                id = def.getId();
            } else if (desc != null) {
                id = desc.getId();
            } else if (dep != null) {
                id = dep.getId();
            }
        }
        if (desc == null) {
            if (def != null) {
                desc = def.getDescriptor();
            }
        }
        this.session = session;
        this.id = id;
        this.def = def;
        this.dep = dep;
        this.desc = desc;
    }

    public String[] getMultiColumnRow(NutsFetchDisplayOptions oo) {
        NutsDisplayProperty[] a = oo.getDisplayProperties();
        String[] b = new String[a.length];
        for (int j = 0; j < b.length; j++) {
            b[j] = buildMain(oo, a[j]);
        }
        return b;
    }

    private static FormatHelper getFormatHelper(NutsWorkspace ws) {
        FormatHelper h = (FormatHelper) ws.userProperties().get(FormatHelper.class.getName());
        if (h != null) {
            return h;
        }
        FormatHelperResetListener h2 = (FormatHelperResetListener) ws.userProperties().get(FormatHelperResetListener.class.getName());
        if (h2 == null) {
            h2 = new FormatHelperResetListener();
            ws.addWorkspaceListener(h2);
        }
        h = new FormatHelper(ws);
        ws.userProperties().put(FormatHelper.class.getName(), h);
        return h;
    }

    public static class FormatHelperResetListener implements NutsWorkspaceListener, NutsRepositoryListener {
        private void _onReset(NutsWorkspace ws) {
            ws.userProperties().remove(FormatHelper.class.getName());
        }

        @Override
        public void onAddRepository(NutsWorkspaceEvent event) {
            _onReset(event.getWorkspace());
        }

        @Override
        public void onRemoveRepository(NutsWorkspaceEvent event) {
            _onReset(event.getWorkspace());
        }

        @Override
        public void onReloadWorkspace(NutsWorkspaceEvent event) {
            _onReset(event.getWorkspace());
        }

        @Override
        public void onCreateWorkspace(NutsWorkspaceEvent event) {
            _onReset(event.getWorkspace());
        }

        @Override
        public void onUpdateProperty(NutsWorkspaceEvent event) {

        }

        @Override
        public void onAddRepository(NutsRepositoryEvent event) {
            _onReset(event.getWorkspace());
        }

        @Override
        public void onRemoveRepository(NutsRepositoryEvent event) {
            _onReset(event.getWorkspace());
        }

        @Override
        public void onConfigurationChanged(NutsRepositoryEvent event) {
            _onReset(event.getWorkspace());
        }

        @Override
        public void onConfigurationChanged(NutsWorkspaceEvent event) {
            _onReset(event.getWorkspace());
        }
    }

    public static class FormatHelper {
        NutsWorkspace ws;

        public FormatHelper(NutsWorkspace ws) {
            this.ws = ws;
        }

        private Integer maxRepoNameSize;
        private Integer maxUserNameSize;

        public int maxRepoNameSize() {
            if (maxRepoNameSize != null) {
                return maxRepoNameSize;
            }
            int z = 0;
            Stack<NutsRepository> stack = new Stack<>();
            for (NutsRepository repository : ws.config().getRepositories()) {
                stack.push(repository);
            }
            while (!stack.isEmpty()) {
                NutsRepository r = stack.pop();
                int n = r.config().getName().length();
                if (n > z) {
                    z = n;
                }
                if (r.config().isSupportedMirroring()) {
                    for (NutsRepository repository : r.config().getMirrors()) {
                        stack.push(repository);
                    }
                }
            }
            return maxRepoNameSize = z;
        }

        public int maxUserNameSize() {
            if (maxUserNameSize != null) {
                return maxUserNameSize;
            }
            int z = "anonymous".length();
            NutsWorkspaceConfigManagerExt wc = NutsWorkspaceConfigManagerExt.of(ws.config());
            NutsUserConfig[] users = wc.getStoredConfigSecurity().getUsers();
            if (users != null) {
                for (NutsUserConfig user : users) {
                    String s = user.getUser();
                    if (s != null) {
                        z = Math.max(s.length(), z);
                    }
                }
            }
            Stack<NutsRepository> stack = new Stack<>();
            for (NutsRepository repository : ws.config().getRepositories()) {
                stack.push(repository);
            }
            while (!stack.isEmpty()) {
                NutsRepository r = stack.pop();
                NutsRepositoryConfigManagerExt rc = NutsRepositoryConfigManagerExt.of(r.config());
                NutsUserConfig[] users1 = rc.getUsers();
                if (users1 != null) {
                    for (NutsUserConfig user : users1) {
                        String s = user.getUser();
                        if (s != null) {
                            z = Math.max(s.length(), z);
                        }
                    }
                }
                if (r.config().isSupportedMirroring()) {
                    for (NutsRepository repository : r.config().getMirrors()) {
                        stack.push(repository);
                    }
                }
            }
            return maxUserNameSize = z;
        }
    }

    public String getSingleColumnRow(NutsFetchDisplayOptions oo) {
        NutsDisplayProperty[] a = oo.getDisplayProperties();
        StringBuilder sb = new StringBuilder();
        for (int j = 0; j < a.length; j++) {
            String s = buildMain(oo, a[j]);
            int z = 0;
            switch (a[j]) {
                case INSTALL_DATE: {
                    z = CoreNutsUtils.DEFAULT_DATE_TIME_FORMATTER_LENGTH;
                    break;
                }
                case REPOSITORY: {
                    z = getFormatHelper(session.getWorkspace()).maxRepoNameSize();
                    break;
                }
                case REPOSITORY_ID: {
                    z = CoreNutsUtils.DEFAULT_UUID_LENGTH;
                    break;
                }
                case INSTALL_USER: {
                    z = getFormatHelper(session.getWorkspace()).maxUserNameSize();
                    break;
                }
            }
            s = CoreStringUtils.alignLeft(s, z);
            if (j > 0) {
                sb.append(' ');
            }
            sb.append(s);
        }
        return sb.toString();
    }

    public String buildMain(NutsFetchDisplayOptions oo, NutsDisplayProperty dp) {
        NutsWorkspace ws = session.getWorkspace();
        if (oo.isRequireDefinition()) {
            buildLong();
        }
        if (dp == null) {
            dp = NutsDisplayProperty.ID;
        }
        switch (dp) {
            case ID: {
                return oo.getIdFormat().value(id).format();
            }
            case STATUS: {
                return getFormattedStatusString();
            }
            case FILE: {
                if (def != null && def.getContent() != null && def.getContent().getPath() != null) {
                    return def.getContent().getPath().toString();
                }
                return "@@missing-path@@";
            }
            case FILE_NAME: {
                if (def != null && def.getContent() != null && def.getContent().getPath() != null) {
                    return def.getContent().getPath().getFileName().toString();
                }
                return "@@missing-file-name@@";
            }
            case ARCH: {
                if (desc != null) {
                    return keywordArr1(desc.getArch());
                }
                return "@@missing-arch@@";
            }
            case NAME: {
                if (desc != null) {
                    return stringValue(desc.getName());
                }
                return "@@missing-name@@";
            }
            case OS: {
                if (desc != null) {
                    return keywordArr2(desc.getOs());
                }
                return "@@missing-os@@";
            }
            case OSDIST: {
                if (desc != null) {
                    return keywordArr2(desc.getOsdist());
                }
                return "@@missing-os@@";
            }
            case PACKAGING: {
                if (desc != null) {
                    return "{{" + stringValue(desc.getPackaging()) + "}}";
                }
                return "@@missing-packaging@@";
            }
            case PLATFORM: {
                if (desc != null) {
                    return keywordArr1(desc.getPlatform());
                }
                return "@@missing-platform@@";
            }
            case INSTALL_DATE: {
                if (def != null && def.getInstallInformation() != null) {
                    return stringValue(def.getInstallInformation().getInstallDate());
                }
                return "<null>";
            }
            case REPOSITORY: {
                String rname = null;
                if (def != null) {
                    if (def.getRepositoryName() != null) {
                        rname = def.getRepositoryName();
                    }
                    if (def.getRepositoryUuid() != null) {
                        NutsRepository r = ws.config().findRepositoryById(def.getRepositoryUuid(), true);
                        if (r != null) {
                            rname = r.config().getName();
                        }
                    }
                }
                if (rname == null && id != null) {
                    rname = id.getNamespace();
                }
                return stringValue(rname);
            }
            case REPOSITORY_ID: {
                String ruuid = null;
                if (def != null) {
                    if (def.getRepositoryUuid() != null) {
                        ruuid = def.getRepositoryUuid();
                    }
                }
                if (ruuid == null && id != null) {
                    String p = id.getNamespace();
                    NutsRepository r = ws.config().findRepositoryByName(p, true);
                    if (r != null) {
                        ruuid = r.uuid();
                    }
                }
                return stringValue(ruuid);
            }
            case INSTALL_USER: {
                if (def != null && def.getInstallInformation() != null) {
                    return stringValue(def.getInstallInformation().getInstallUser());
                }
                return "@@nobody@@";
            }
            case CACHE_FOLDER: {
                if (def != null) {
                    return stringValue(ws.config().getStoreLocation(def.getId(), NutsStoreLocation.CACHE));
                }
                return "@@nobody@@";
            }
            case CONFIG_FOLDER: {
                if (def != null) {
                    return stringValue(ws.config().getStoreLocation(def.getId(), NutsStoreLocation.CONFIG));
                }
                return "@@nobody@@";
            }
            case LIB_FOLDER: {
                if (def != null) {
                    return stringValue(ws.config().getStoreLocation(def.getId(), NutsStoreLocation.LIB));
                }
                return "@@nobody@@";
            }
            case LOG_FOLDER: {
                if (def != null) {
                    return stringValue(ws.config().getStoreLocation(def.getId(), NutsStoreLocation.LOG));
                }
                return "@@nobody@@";
            }
            case TEMP_FOLDER: {
                if (def != null) {
                    return stringValue(ws.config().getStoreLocation(def.getId(), NutsStoreLocation.TEMP));
                }
                return "@@nobody@@";
            }
            case VAR_LOCATION: {
                if (def != null) {
                    return stringValue(ws.config().getStoreLocation(def.getId(), NutsStoreLocation.VAR));
                }
                return "@@nobody@@";
            }
            case APPS_FOLDER: {
                if (def != null) {
                    return stringValue(ws.config().getStoreLocation(def.getId(), NutsStoreLocation.APPS));
                }
                return "@@nobody@@";
            }
            case EXEC_ENTRY: {
                if (def != null && def.getContent() != null && def.getContent().getPath() != null) {
                    List<String> results = new ArrayList<String>();
                    for (NutsExecutionEntry entry : ws.io().parseExecutionEntries(def.getContent().getPath())) {
                        if (entry.isDefaultEntry()) {
                            //should all mark?
                            results.add(entry.getName());
                        } else {
                            results.add(entry.getName());
                        }
                    }
                    if (results.size() == 1) {
                        return results.get(0);
                    }
                    return results.toString();
                }
                return "@@missing-class@@";
            }
            default: {
                throw new NutsUnsupportedArgumentException(ws, String.valueOf(dp));
            }
        }
    }

    public FormattableNutsId buildLong() {
        if (!built) {
            built = true;
            NutsWorkspace ws = session.getWorkspace();
            DefaultNutsInstalledRepository rr = NutsWorkspaceExt.of(ws).getInstalledRepository();
            this.i = rr.isInstalled(id);
            this.d = rr.isDefaultVersion(id);
            NutsInstallInformation iif = rr.getInstallInfo(id);
            this.dte = iif == null ? null : iif.getInstallDate();
            this.usr = iif == null ? null : iif.getInstallUser();
//            Boolean updatable = null;
            this.executable = null;
            this.executableApp = null;
            this.fetched = false;

            this.checkDependencies = false;
            this.defFetched = null;

            try {
                if (!this.i || def == null) {
                    this.defFetched = ws.fetch().id(id).setSession(
                            session.setTrace(false)
                    ).offline()
                            .setInstallInformation(true)
                            .setContent(true)
                            .setOptional(false)
                            .dependencies(this.checkDependencies)
                            .getResultDefinition();
                    this.fetched = true;
                } else {
                    this.fetched = true;
                }
            } catch (Exception ex) {
                //ignore!!
            }

            if (def != null) {
                this.executable = def.getDescriptor().isExecutable();
                this.executableApp = def.getDescriptor().isNutsApplication();
            } else if (this.defFetched != null) {
                this.executable = this.defFetched.getDescriptor().isExecutable();
                this.executableApp = this.defFetched.getDescriptor().isNutsApplication();
            } else if (desc != null) {
                this.executable = desc.isExecutable();
                this.executableApp = desc.isNutsApplication();
            }
            this.status_f = this.i && this.d ? 'I' : this.i ? 'i' : this.fetched ? 'f' : 'r';
            if (def != null) {
                this.status_e = def.isApi() ? 'a'
                        : def.isRuntime() ? 'r'
                        : def.isExtension() ? 'e'
                        : def.isCompanion() ? 'c'
                        : '-';
            }
            this.status_i = buildComponentAppStatus();
            this.status_s = '-';
            this.status_o = '-';
            if (dep != null) {
                NutsDependencyScope ss = CoreCommonUtils.parseEnumString(dep.getScope(), NutsDependencyScope.class, true);
                if (ss != null) {
                    switch (ss) {
                        case API: {
                            this.status_s = 'c';
                            break;
                        }
                        case IMPLEMENTATION: {
                            this.status_s = 'i';
                            break;
                        }
                        case RUNTIME: {
                            this.status_s = 'r';
                            break;
                        }
                        case SYSTEM: {
                            this.status_s = 's';
                            break;
                        }
                        case PROVIDED: {
                            this.status_s = 'p';
                            break;
                        }
                        case TEST_COMPILE: {
                            this.status_s = 't';
                            break;
                        }
                        case IMPORT: {
                            this.status_s = 'm';
                            break;
                        }
                        case TEST_PROVIDED: {
                            this.status_s = 'P';
                            break;
                        }
                        case TEST_RUNTIME: {
                            this.status_s = 'R';
                            break;
                        }
                        case OTHER: {
                            this.status_s = 'O';
                            break;
                        }
                        default: {
                            this.status_s = '-';
                            break;
                        }
                    }
                }
                if (dep.isOptional()) {
                    this.status_s = 'o';
                }
            }

//            if (fetched) {
//                NutsId nut2 = null;
//                updatable = false;
//                try {
//                    nut2 = ws.fetch().parse(parse.setVersion("")).setSession(session.copy().setProperty("monitor-allowed", false)).setTransitive(true).wired().setTrace(false).getResultId();
//                } catch (Exception ex) {
//                    //ignore
//                }
//                if (nut2 != null && nut2.getVersion().compareTo(parse.getVersion()) > 0) {
//                    updatable = true;
//                }
//            }   
        }
        return this;
    }

    private char buildComponentAppStatus() {
        return this.executableApp != null ? (this.executableApp ? 'X' : this.executable ? 'x' : '-') : '.';
    }

    public String getFormattedStatusString() {
        if (dep != null) {
            return "**" + status_f + status_e + status_i + status_s + "**";
        }
        return "**" + status_f + status_e + status_i + "**";
    }

    public String getStatusString() {
        if (dep != null) {
            return "" + status_f + status_e + status_i + status_s;
        }
        return "" + status_f + status_e + status_i;
    }

    private String keywordArr1(String[] any) {
        return keywordArr0(any, "[[", "]]");
    }

    private String keywordArr2(String[] any) {
        return keywordArr0(any, "{{", "}}");
    }

    private String keywordArr3(String[] any) {
        return keywordArr0(any, "**", "**");
    }

    private String keywordArr4(String[] any) {
        return keywordArr0(any, "==", "==");
    }

    private String keywordArr0(String[] any, String a, String b) {
        if (any == null || any.length == 0) {
            return "";
        }
        if (any.length == 1) {
            return a + stringValue(any[0]) + b;
        }
        return "\\[" + Arrays.stream(any).map(x -> (a + x + b)).collect(Collectors.joining(",")) + "\\]";
    }

    private String stringValue(Object any) {
        return CoreCommonUtils.stringValueFormatted(any, false, session);
    }
}
