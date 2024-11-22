/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 *
 * Copyright [2020] [thevpc]
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License");
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.format;

import java.time.Instant;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.workspace.config.NRepositoryConfigManagerExt;
import net.thevpc.nuts.runtime.standalone.workspace.config.NConfigsExt;
import net.thevpc.nuts.runtime.standalone.repository.impl.main.NInstalledRepository;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.util.CoreNUtils;
import net.thevpc.nuts.runtime.standalone.text.util.NTextUtils;
import net.thevpc.nuts.runtime.standalone.util.CoreEnumUtils;
import net.thevpc.nuts.text.NString;
import net.thevpc.nuts.text.NTextBuilder;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.util.NLiteral;
import net.thevpc.nuts.util.NMsg;

/**
 *
 * @author thevpc
 */
public class NIdFormatHelper {

    NId id;
    NInstallStatus installStatus = NInstallStatus.NONE;
    Boolean executable = null;
    Boolean executableApp = null;
    boolean fetched = false;
    boolean checkDependencies = false;
    NDefinition defFetched;
    NDefinition def;
    NDescriptor desc;
    NDependency dep;
    NWorkspace workspace;
    Instant dte;
    String usr;
    char status_f;
//    char status_obs;
    char status_e;
    char status_i;
    char status_s;
    char status_o;
    //    String display;
    boolean built = false;
    boolean ntf = true;

    public static NIdFormatHelper of(Object object) {
        if (object instanceof NId) {
            NId v = (NId) object;
            return (new NIdFormatHelper(v, NWorkspace.of().get()));
        } else if (object instanceof NDescriptor) {
            NDescriptor v = (NDescriptor) object;
            return (new NIdFormatHelper(v, NWorkspace.of().get()));
        } else if (object instanceof NDefinition) {
            NDefinition v = (NDefinition) object;
            return (new NIdFormatHelper(v, NWorkspace.of().get()));
        } else if (object instanceof NDependency) {
            NDependency v = (NDependency) object;
            return (new NIdFormatHelper(v, NWorkspace.of().get()));
        } else if (object instanceof NDependencyTreeNode) {
            NDependencyTreeNode v = (NDependencyTreeNode) object;
            return (new NIdFormatHelper(v, NWorkspace.of().get()));
        } else {
            return null;
        }

    }

    public NIdFormatHelper(NDependencyTreeNode id, NWorkspace workspace) {
        this(null, null, null, id.getDependency(), workspace);
    }

    public NIdFormatHelper(NId id, NWorkspace workspace) {
        this(id, null, null, null, workspace);
    }

    public NIdFormatHelper(NDescriptor desc, NWorkspace workspace) {
        this(null, desc, null, null, workspace);
    }

    public NIdFormatHelper(NDefinition def, NWorkspace workspace) {
        this(null, null, def, null, workspace);
    }

    public NIdFormatHelper(NDependency dep, NWorkspace workspace) {
        this(null, null, null, dep, workspace);
    }

    private NIdFormatHelper(NId id, NDescriptor desc, NDefinition def, NDependency dep, NWorkspace workspace) {
        if (id == null) {
            if (def != null) {
                id = def.getId();
            } else if (desc != null) {
                id = desc.getId();
            } else if (dep != null) {
                id = dep.toId();
            }
        }
        if (desc == null) {
            if (def != null) {
                desc = def.getDescriptor();
            }
        }
        this.workspace = workspace;
        this.id = id;
        this.def = def;
        this.dep = dep;
        this.desc = desc;
    }

    public String[] getMultiColumnRowStrings(NFetchDisplayOptions oo) {
        Object[] oa = getMultiColumnRow(oo);
        String[] ss = new String[oa.length];
        for (int i = 0; i < oa.length; i++) {
            ss[i] = String.valueOf(oa[i]);
        }
        return ss;
    }

    public Object[] getMultiColumnRow(NFetchDisplayOptions oo) {
        NDisplayProperty[] a = oo.getDisplayProperties();
        Object[] b = new Object[a.length];
        for (int j = 0; j < b.length; j++) {
            b[j] = buildMain(oo, a[j]);
        }
        return b;
    }

    private static FormatHelper getFormatHelper(NWorkspace workspace) {
        FormatHelper h = (FormatHelper) NEnvs.of().getProperties().get(FormatHelper.class.getName());
        if (h != null) {
            return h;
        }
        FormatHelperResetListener h2 = (FormatHelperResetListener) NEnvs.of()
                .getProperty(FormatHelperResetListener.class.getName())
                .map(NLiteral::getRaw).orNull()
                ;
        if (h2 == null) {
            h2 = new FormatHelperResetListener();
            NEvents.of().addWorkspaceListener(h2);
        }
        h = new FormatHelper(workspace);
        NEnvs.of().setProperty(FormatHelper.class.getName(), h);
        return h;
    }

    public static class FormatHelperResetListener implements NWorkspaceListener, NRepositoryListener {

        private void _onReset(NSession session) {
            NEnvs.of().setProperty(FormatHelper.class.getName(), null);
        }

        @Override
        public void onAddRepository(NWorkspaceEvent event) {
            _onReset(event.getSession());
        }

        @Override
        public void onRemoveRepository(NWorkspaceEvent event) {
            _onReset(event.getSession());
        }

        @Override
        public void onReloadWorkspace(NWorkspaceEvent event) {
            _onReset(event.getSession());
        }

        @Override
        public void onCreateWorkspace(NWorkspaceEvent event) {
            _onReset(event.getSession());
        }

        @Override
        public void onUpdateProperty(NWorkspaceEvent event) {

        }

        @Override
        public void onAddRepository(NRepositoryEvent event) {
            _onReset(event.getSession());
        }

        @Override
        public void onRemoveRepository(NRepositoryEvent event) {
            _onReset(event.getSession());
        }

        @Override
        public void onConfigurationChanged(NRepositoryEvent event) {
            _onReset(event.getSession());
        }

        @Override
        public void onConfigurationChanged(NWorkspaceEvent event) {
            _onReset(event.getSession());
        }
    }

    public static class FormatHelper {

        NWorkspace workspace;

        public FormatHelper(NWorkspace workspace) {
            this.workspace = workspace;
        }

        private Integer maxRepoNameSize;
        private Integer maxUserNameSize;

        public int maxRepoNameSize() {
            if (maxRepoNameSize != null) {
                return maxRepoNameSize;
            }
            int z = 0;
            Stack<NRepository> stack = new Stack<>();
            for (NRepository repository : NRepositories.of()
                    .getRepositories()) {
                stack.push(repository);
            }
            while (!stack.isEmpty()) {
                NRepository r = stack.pop();
                int n = r.getName().length();
                if (n > z) {
                    z = n;
                }
                if (r.config().isSupportedMirroring()) {
                    for (NRepository repository : r.config()
                            .getMirrors()) {
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
            NConfigsExt wc = NConfigsExt.of(NConfigs.of());
            NUserConfig[] users = wc.getModel().getStoredConfigSecurity().getUsers();
            if (users != null) {
                for (NUserConfig user : users) {
                    String s = user.getUser();
                    if (s != null) {
                        z = Math.max(s.length(), z);
                    }
                }
            }
            Stack<NRepository> stack = new Stack<>();
            for (NRepository repository : NRepositories.of().getRepositories()) {
                stack.push(repository);
            }
            while (!stack.isEmpty()) {
                NRepository r = stack.pop();
                NRepositoryConfigManagerExt rc = NRepositoryConfigManagerExt.of(r.config());
                NUserConfig[] users1 = rc.getModel().findUsers();
                if (users1 != null) {
                    for (NUserConfig user : users1) {
                        String s = user.getUser();
                        if (s != null) {
                            z = Math.max(s.length(), z);
                        }
                    }
                }
                if (r.config().isSupportedMirroring()) {
                    for (NRepository repository : r.config().getMirrors()) {
                        stack.push(repository);
                    }
                }
            }
            return maxUserNameSize = z;
        }
    }

    public NString getSingleColumnRow(NFetchDisplayOptions oo) {
        NDisplayProperty[] a = oo.getDisplayProperties();
        NTexts txt = NTexts.of();
        NTextBuilder sb = txt.ofBuilder();
        for (int j = 0; j < a.length; j++) {
            NString s = buildMain(oo, a[j]);
            int z = 0;
            switch (a[j]) {
                case INSTALL_DATE: {
                    z = CoreNUtils.DEFAULT_DATE_TIME_FORMATTER_LENGTH;
                    break;
                }
                case REPOSITORY: {
                    z = getFormatHelper(workspace).maxRepoNameSize();
                    break;
                }
                case REPOSITORY_ID: {
                    z = CoreNUtils.DEFAULT_UUID_LENGTH;
                    break;
                }
                case INSTALL_USER: {
                    z = getFormatHelper(workspace).maxUserNameSize();
                    break;
                }
            }
            int len = txt.ofBuilder().append(s).textLength();
            if (j > 0) {
                sb.append(' ');
            }
            sb.append(s);
            if (len < z) {
                char[] c = new char[z - len];
                Arrays.fill(c, ' ');
                sb.append(new String(c));
            }
            // sb.append(s);
        }
        return sb.immutable();
    }

    public NString buildMain(NFetchDisplayOptions oo, NDisplayProperty dp) {
        NTexts text = NTexts.of();
        if (oo.isRequireDefinition()) {
            buildLong();
        }
        if (dp == null) {
            dp = NDisplayProperty.ID;
        }
        switch (dp) {
            case ID: {
                return oo.getIdFormat().setValue(id).setNtf(ntf).format();
            }
            case STATUS: {
                return getFormattedStatusString();
            }
            case FILE: {
                if (def != null && def.getContent().isPresent()) {
                    return text.ofText(def.getContent().orNull());
                }
                return text.ofStyled("missing-path", NTextStyle.error());
            }
            case FILE_NAME: {
                if (def != null && def.getContent().isPresent()) {
                    return text.ofPlain(def.getContent().get().getName());
                }
                return text.ofStyled("missing-file-name", NTextStyle.error());
            }
            case ARCH: {
                if (desc != null  && desc.getCondition().getArch().size()>0) {
                    return keywordArr1(desc.getCondition().getArch());
                }
                return text.ofStyled("missing-arch", NTextStyle.error());
            }
            case NAME: {
                if (desc != null) {
                    return stringValue(desc.getName());
                }
                return text.ofStyled("missing-name", NTextStyle.error());
            }
            case OS: {
                if (desc != null  && desc.getCondition().getOs().size()>0) {
                    return keywordArr2(desc.getCondition().getOs());
                }
                return text.ofStyled("missing-os", NTextStyle.error());
            }
            case OSDIST: {
                if (desc != null && desc.getCondition().getOsDist().size()>0) {
                    return keywordArr2(desc.getCondition().getOsDist());
                }
                return text.ofStyled("missing-osdist", NTextStyle.error());
            }
            case PACKAGING: {
                if (desc != null) {
                    return text.ofStyled(stringValue(desc.getPackaging()), NTextStyle.primary3());
                }
                return text.ofStyled("missing-packaging", NTextStyle.error());
            }
            case PLATFORM: {
                if (desc != null && desc.getCondition().getPlatform().size()>0) {
                    return keywordArr1(desc.getCondition().getPlatform());
                }
                return text.ofStyled("missing-platform", NTextStyle.error());
            }
            case PROFILE: {
                if (desc != null && desc.getCondition().getProfile().size()>0) {
                    return keywordArr1(desc.getCondition().getProfile());
                }
                return text.ofStyled("no-profile", NTextStyle.error());
            }
            case DESKTOP_ENVIRONMENT: {
                if (desc != null && desc.getCondition().getDesktopEnvironment().size()>0) {
                    return keywordArr1(desc.getCondition().getDesktopEnvironment());
                }
                return text.ofStyled("missing-desktop-environment", NTextStyle.error());
            }
            case INSTALL_DATE: {
                if (def != null && def.getInstallInformation().isPresent()) {
                    return stringValue(def.getInstallInformation().get().getCreatedInstant());
                }
                return text.ofStyled("<null>", NTextStyle.pale());
            }
            case REPOSITORY: {
                String rname = null;
                if (def != null) {
                    if (def.getRepositoryName() != null) {
                        rname = def.getRepositoryName();
                    }
                    if (def.getRepositoryUuid() != null) {
                        NRepository r = NRepositories.of()
                                .findRepositoryById(def.getRepositoryUuid()).orNull();
                        if (r != null) {
                            rname = r.getName();
                        }
                    }
                }
                if (rname == null && id != null) {
                    rname = id.getRepository();
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
                    String p = id.getRepository();
                    NRepository r = NRepositories.of()
                            .findRepositoryByName(p).orNull();
                    if (r != null) {
                        ruuid = r.getUuid();
                    }
                }
                return stringValue(ruuid);
            }
            case INSTALL_USER: {
                if (def != null && def.getInstallInformation().isPresent()) {
                    return stringValue(def.getInstallInformation().get().getInstallUser());
                }
                return text.ofStyled("nobody", NTextStyle.error());
            }
            case CACHE_FOLDER: {
                if (def != null) {
                    return stringValue(NLocations.of().getStoreLocation(def.getId(), NStoreType.CACHE));
                }
                return text.ofStyled("<null>", NTextStyle.error());
            }
            case CONF_FOLDER: {
                if (def != null) {
                    return stringValue(NLocations.of().getStoreLocation(def.getId(), NStoreType.CONF));
                }
                return text.ofStyled("<null>", NTextStyle.error());
            }
            case LIB_FOLDER: {
                if (def != null) {
                    return stringValue(NLocations.of().getStoreLocation(def.getId(), NStoreType.LIB));
                }
                return text.ofStyled("<null>", NTextStyle.error());
            }
            case LOG_FOLDER: {
                if (def != null) {
                    return stringValue(NLocations.of().getStoreLocation(def.getId(), NStoreType.LOG));
                }
                return text.ofStyled("<null>", NTextStyle.error());
            }
            case TEMP_FOLDER: {
                if (def != null) {
                    return stringValue(NLocations.of().getStoreLocation(def.getId(), NStoreType.TEMP));
                }
                return text.ofStyled("<null>", NTextStyle.error());
            }
            case VAR_LOCATION: {
                if (def != null) {
                    return stringValue(NLocations.of().getStoreLocation(def.getId(), NStoreType.VAR));
                }
                return text.ofStyled("<null>", NTextStyle.error());
            }
            case BIN_FOLDER: {
                if (def != null) {
                    return stringValue(NLocations.of().getStoreLocation(def.getId(), NStoreType.BIN));
                }
                return text.ofStyled("<null>", NTextStyle.error());
            }
            case EXEC_ENTRY: {
                if (def != null && def.getContent().isPresent()) {
                    List<NString> results = new ArrayList<>();
                    for (NExecutionEntry entry : NExecutionEntry.parse(def.getContent().get())) {
                        if (entry.isDefaultEntry()) {
                            //should all mark?
                            results.add(text.ofPlain(entry.getName()));
                        } else {
                            results.add(text.ofPlain(entry.getName()));
                        }
                    }
                    if (results.size() == 1) {
                        return results.get(0);
                    }
                    return text.ofBuilder().appendJoined(
                            text.ofPlain(","),
                            results
                    );
                }
                return text.ofStyled("<missing-class>", NTextStyle.error());
            }
            case INSTALL_FOLDER: {
                if (def != null && def.getInstallInformation().isPresent()) {
                    return stringValue(def.getInstallInformation().get().getInstallFolder());
                }
                return text.ofStyled("<null>", NTextStyle.pale());
            }
            case LONG_STATUS: {
                List<NString> all = new ArrayList<>();
                if (def != null && def.getDescriptor().getIdType() != null) {
                    switch (def.getDescriptor().getIdType()) {
                        case REGULAR: {
                            all.add(text.ofPlain(def.getDescriptor().getIdType().id()));
                            break;
                        }
                        default: {
                            all.add(text.ofStyled(def.getDescriptor().getIdType().id(), NTextStyle.primary1()));
                            break;
                        }
                    }
                }
                if (executableApp) {
                    all.add(text.ofStyled("application", NTextStyle.primary5()));
                } else if (executable) {
                    all.add(text.ofStyled("executable", NTextStyle.primary3()));
                } else {
                    all.add(text.ofStyled("library", NTextStyle.primary4()));
                }
                if (dep != null) {
                    NDependencyScope ss = CoreEnumUtils.parseEnumString(dep.getScope(), NDependencyScope.class, true);
                    if (dep.isOptional()) {
                        all.add(text.ofStyled("optional", NTextStyle.primary5()));
                    }
                    if (ss != null) {
                        all.add(text.ofStyled(NDependencyScope.API.id(), NTextStyle.primary5()));
                    }
                }
                return text.ofBuilder().appendJoined(text.ofStyled(",", NTextStyle.pale()),
                        all).build();

            }

            default: {
                throw new NUnsupportedEnumException(dp);
            }
        }
    }

    public NIdFormatHelper buildLong() {
        if (!built) {
            built = true;
            NWorkspace ws = workspace;
            NInstalledRepository rr = NWorkspaceExt.of().getInstalledRepository();
            this.installStatus = rr.getInstallStatus(id);
            NInstallInformation iif = rr.getInstallInformation(id);
            this.dte = iif == null ? null : iif.getCreatedInstant();
            this.usr = iif == null ? null : iif.getInstallUser();
//            Boolean updatable = null;
            this.executable = null;
            this.executableApp = null;
            this.fetched = false;

            this.checkDependencies = false;
            this.defFetched = null;

            try {
                if (this.installStatus.isNonDeployed() || def == null) {
                    this.defFetched = NFetchCmd.of(id)
                            .setFetchStrategy(NFetchStrategy.OFFLINE)
                            .setContent(true)
                            .setOptional(false)
                            .setDependencies(this.checkDependencies)
                            .getResultDefinition();
                    this.fetched = true;
                } else {
                    this.fetched = true;
                }
            } catch (Exception ex) {
                NLog.of(NIdFormatHelper.class).with().level(Level.FINE).error(ex)
                        .log(
                                NMsg.ofC("failed to build id format for %s", id));
            }

            if (def != null) {
                this.executable = def.getDescriptor().isExecutable();
                this.executableApp = def.getDescriptor().isApplication();
            } else if (this.defFetched != null) {
                this.executable = this.defFetched.getDescriptor().isExecutable();
                this.executableApp = this.defFetched.getDescriptor().isApplication();
            } else if (desc != null) {
                this.executable = desc.isExecutable();
                this.executableApp = desc.isApplication();
            }
            this.status_f = (this.installStatus.isDefaultVersion()) ? 'I'
                    : (this.installStatus.isInstalled()) ? 'i'
                    : (this.installStatus.isRequired()) ? 'd'
                    : this.fetched ? 'f' : 'r';
//            this.status_obs=(this.installStatus.isInstalled()?'O':'U');
            if (def != null) {
                switch (def.getDescriptor().getIdType()) {
                    case API: {
                        this.status_e = 'a';
                        break;
                    }
                    case RUNTIME: {
                        this.status_e = 'r';
                        break;
                    }
                    case EXTENSION: {
                        this.status_e = 'e';
                        break;
                    }
                    case COMPANION: {
                        this.status_e = 'c';
                        break;
                    }
                    case REGULAR: {
                        this.status_e = '-';
                        break;
                    }
                    default: {
                        this.status_e = '?';
                        break;
                    }
                }
            }
            this.status_i = buildComponentAppStatus();
            this.status_s = '-';
            this.status_o = '-';
            if (dep != null) {
                NDependencyScope ss = CoreEnumUtils.parseEnumString(dep.getScope(), NDependencyScope.class, true);
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
                        case TEST_API:
                        case TEST_IMPLEMENTATION:
                        case TEST_PROVIDED:
                        case TEST_RUNTIME:
                        case TEST_OTHER: {
                            this.status_s = 't';
                            break;
                        }
                        case IMPORT: {
                            this.status_s = 'm';
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
//                    nut2 = ws.fetch().parse(parse.setVersion("")).setSession(session.copy().setProperty("monitor-allowed", false)).setTransitive(true).wired().getResultId();
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

    public NString getFormattedStatusString() {
        NTexts text = NTexts.of();
        if (dep != null) {
            return text.ofStyled("" + status_f
                    //                    + status_obs
                    + status_e + status_i + status_s, NTextStyle.primary3());
        }
        return text.ofStyled("" + status_f
                //                + status_obs
                + status_e + status_i, NTextStyle.primary3());
    }

    public String getStatusString() {
        if (dep != null) {
            return "" + status_f
                    //                    + status_obs
                    + status_e + status_i + status_s;
        }
        return "" + status_f
                //                + status_obs
                + status_e + status_i;
    }

    private NString keywordArr1(List<String> any) {
        return keywordArr0(any, NTextStyle.primary1());
    }

    private NString keywordArr2(List<String> any) {
        return keywordArr0(any, NTextStyle.primary3());
    }

    private NString keywordArr0(List<String> any, NTextStyle style) {
        NTexts txt = NTexts.of();
        if (any == null || any.size() == 0) {
            return txt.ofBlank();
        }
        if (any.size() == 1) {
            return txt.ofBuilder().append(txt.ofStyled(stringValue(any.get(0)), style))
                    .immutable();
        }
        return txt.ofBuilder()
                .append("[")
                .appendJoined(
                        txt.ofPlain(","),
                        any.stream().map(x -> txt.ofStyled(stringValue(x), style)).collect(Collectors.toList())
                )
                .append("]").immutable();
    }

    private NString stringValue(Object any) {
        return NTextUtils.stringValueFormatted(any, false);
    }
}
