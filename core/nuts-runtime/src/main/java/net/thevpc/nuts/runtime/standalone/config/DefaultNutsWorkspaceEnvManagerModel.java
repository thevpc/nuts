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
 * <p>
 * Copyright [2020] [thevpc] Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.config;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.DefaultNutsVal;
import net.thevpc.nuts.runtime.bundles.common.CorePlatformUtils;
import net.thevpc.nuts.runtime.core.common.DefaultObservableMap;
import net.thevpc.nuts.runtime.core.common.ObservableMap;
import net.thevpc.nuts.runtime.core.config.NutsWorkspaceConfigManagerExt;
import net.thevpc.nuts.runtime.core.parser.DefaultNutsIdParser;
import net.thevpc.nuts.runtime.standalone.gui.CoreNutsUtilGui;
import net.thevpc.nuts.runtime.standalone.util.NutsJavaSdkUtils;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author vpc
 */
public class DefaultNutsWorkspaceEnvManagerModel {

    //    private Map<String, String> options = new LinkedHashMap<>();
    protected ObservableMap<String, Object> userProperties;
    private NutsWorkspace workspace;
    private NutsId platform;
    private NutsId os;
    private NutsOsFamily osFamily;
    private NutsShellFamily shellFamily;
    private NutsId[] desktopEnvironments;
    private NutsDesktopEnvironmentFamily[] osDesktopEnvironmentFamilies;
    private NutsDesktopEnvironmentFamily osDesktopEnvironmentFamily;
    private NutsId arch;
    private NutsId osDist;
    private NutsArchFamily archFamily = NutsArchFamily.getArchFamily();

    public DefaultNutsWorkspaceEnvManagerModel(NutsWorkspace ws, NutsWorkspaceInitInformation info, NutsSession session) {
        this.workspace = ws;
        userProperties = new DefaultObservableMap<>();
//        String[] properties = info.getOptions().getProperties();
//        if (properties != null) {
//            for (String property : properties) {
//                if (property != null) {
//                    DefaultNutsArgument a = new DefaultNutsArgument(property);
//                    String key = a.getKey().getString();
//                    String value = a.getStringValue();
//                    this.options.put(key, value == null ? "" : value);
//                }
//            }
//        }
        DefaultNutsIdParser nip = new DefaultNutsIdParser(session);
        os = nip.parse(CorePlatformUtils.getPlatformOs(workspace));
        String platformOsDist = CorePlatformUtils.getPlatformOsDist(workspace);
        if (platformOsDist == null) {
            platformOsDist = "default";
        }
        osDist = nip.parse(platformOsDist);
        platform = NutsJavaSdkUtils.of(session).createJdkId(System.getProperty("java.version"), session);
        arch = session.id().parser().parse(System.getProperty("os.arch"));

    }

    NutsWorkspaceConfigMain getStoreModelMain() {
        return ((DefaultNutsWorkspaceConfigManager) workspace.config())
                .getModel()
                .getStoreModelMain();
    }

    public Map<String, String> getEnvMap() {
        Map<String, String> p = new LinkedHashMap<>();
        if (getStoreModelMain().getEnv() != null) {
            p.putAll(getStoreModelMain().getEnv());
        }
//        p.putAll(options);
        return p;
    }

    public NutsVal getEnv(String property) {
        Map<String, String> env = getStoreModelMain().getEnv();
        if (env != null) {
            return new DefaultNutsVal(env.get(property));
        }
        return new DefaultNutsVal(null);
    }

    public void setEnv(String property, String value, NutsSession session) {
        Map<String, String> env = getStoreModelMain().getEnv();
//        session = CoreNutsUtils.validate(session, workspace);
        if (NutsBlankable.isBlank(value)) {
            if (env != null && env.containsKey(property)) {
                env.remove(property);
                NutsWorkspaceConfigManagerExt.of(workspace.config())
                        .getModel()
                        .fireConfigurationChanged("env", session, ConfigEventType.MAIN);
            }
        } else {
            if (env == null) {
                env = new LinkedHashMap<>();
                getStoreModelMain().setEnv(env);
            }
            String old = env.get(property);
            if (!value.equals(old)) {
                env.put(property, value);
                NutsWorkspaceConfigManagerExt.of(workspace.config())
                        .getModel()
                        .fireConfigurationChanged("env", session, ConfigEventType.MAIN);
            }
        }
    }
//
//    private DefaultNutsWorkspaceCurrentConfig current() {
//        return NutsWorkspaceConfigManagerExt.of(workspace.config())
//                .getModel()
//                .current();
//    }

    public NutsId getArch() {
        return arch;
    }

    public NutsArchFamily getArchFamily() {
        return archFamily;
    }

    public NutsOsFamily getOsFamily() {
        if (osFamily == null) {
            osFamily = NutsOsFamily.getCurrent();
        }
        return osFamily;
    }

    public NutsShellFamily getShellFamily() {
        if (shellFamily == null) {
            shellFamily = NutsShellFamily.getCurrent();
        }
        return shellFamily;
    }

    public NutsId[] getDesktopEnvironments(NutsSession session) {
        if (desktopEnvironments == null) {
            desktopEnvironments = getDesktopEnvironments0(session);
        }
        return desktopEnvironments;
    }

    public boolean isGraphicalDesktopEnvironment() {
        return CoreNutsUtilGui.isGraphicalDesktopEnvironment();
    }

    protected NutsId[] getDesktopEnvironments0(NutsSession session) {
        if (!isGraphicalDesktopEnvironment()) {
            return new NutsId[]{
                    session.id().builder().setArtifactId("none").build()
            };
        }
        String _XDG_SESSION_DESKTOP = System.getenv("XDG_SESSION_DESKTOP");
        String _XDG_CURRENT_DESKTOP = System.getenv("XDG_CURRENT_DESKTOP");


        if (!NutsBlankable.isBlank(_XDG_SESSION_DESKTOP) && !NutsBlankable.isBlank(_XDG_SESSION_DESKTOP)) {
            String[] supportedSessions = new LinkedHashSet<>(
                    Arrays.stream(_XDG_CURRENT_DESKTOP.trim().split(":"))
                            .map(x -> x.trim().toLowerCase()).filter(x -> x.length() > 0)
                            .collect(Collectors.toList())
            ).toArray(new String[0]);
            String sd = _XDG_SESSION_DESKTOP.toLowerCase();
            List<NutsId> a = new ArrayList<>();
            for (int i = 0; i < supportedSessions.length; i++) {
                NutsIdBuilder nb = session.id().builder().setArtifactId(supportedSessions[i]);
                if ("kde".equals(sd)) {
                    String _KDE_FULL_SESSION = System.getenv("KDE_FULL_SESSION");
                    String _KDE_SESSION_VERSION = System.getenv("KDE_SESSION_VERSION");
                    if (_KDE_FULL_SESSION != null && "true".equals(_KDE_FULL_SESSION.trim())) {
                        nb.setProperty("full", "true");
                    }
                    if (_KDE_SESSION_VERSION != null) {
                        nb.setProperty("version", _KDE_SESSION_VERSION.trim());
                    }
                }
                String _XDG_SESSION_TYPE = System.getenv("XDG_SESSION_TYPE");
                String _XSESSION_IS_UP = System.getenv("XSESSION_IS_UP");
                String _XDG_SESSION_CLASS = System.getenv("XDG_SESSION_CLASS");
                if (_XDG_SESSION_TYPE != null) {
                    nb.setProperty("type", _XDG_SESSION_TYPE.trim().toLowerCase());
                }
                if (_XDG_SESSION_TYPE != null) {
                    nb.setProperty("class", _XDG_SESSION_CLASS.trim().toLowerCase());
                }
                a.add(nb.build());
            }
            if (a.isEmpty()) {
                a.add(session.id().builder().setArtifactId("unknown").build());
            }
            return a.toArray(new NutsId[0]);
        }
        if (NutsOsFamily.getCurrent() == NutsOsFamily.WINDOWS) {
            return new NutsId[]{
                    session.id().builder().setArtifactId("windows").build()
            };
        }
        return new NutsId[]{
                session.id().builder().setArtifactId("unknown").build()
        };
    }

    public NutsDesktopEnvironmentFamily[] getDesktopEnvironmentFamilies(NutsSession session) {
        if (osDesktopEnvironmentFamilies == null) {
            osDesktopEnvironmentFamilies = getDesktopEnvironmentFamilies0(session);
        }
        return osDesktopEnvironmentFamilies;
    }

    public NutsDesktopEnvironmentFamily[] getDesktopEnvironmentFamilies0(NutsSession session) {
        NutsId[] desktopEnvironments = getDesktopEnvironments(session);
        LinkedHashSet<NutsDesktopEnvironmentFamily> all = new LinkedHashSet<>();
        for (NutsId desktopEnvironment : desktopEnvironments) {
            all.add(NutsDesktopEnvironmentFamily.parseLenient(desktopEnvironment.getShortName()));
        }
        return all.toArray(new NutsDesktopEnvironmentFamily[0]);
    }

    public NutsDesktopEnvironmentFamily getDesktopEnvironmentFamily(NutsSession session) {
        if (osDesktopEnvironmentFamily == null) {
            osDesktopEnvironmentFamily = getDesktopEnvironmentFamily0(session);
        }
        return osDesktopEnvironmentFamily;
    }

    public NutsDesktopEnvironmentFamily getDesktopEnvironmentFamily0(NutsSession session) {
        NutsDesktopEnvironmentFamily[] all = getDesktopEnvironmentFamilies(session);
        if (all.length == 0) {
            return NutsDesktopEnvironmentFamily.UNKNOWN;
        }
        boolean unknown = false;
        boolean none = false;
        for (NutsDesktopEnvironmentFamily f : all) {
            switch (f) {
                case UNKNOWN: {
                    unknown = true;
                    break;
                }
                case NONE: {
                    none = true;
                    break;
                }
                default: {
                    return f;
                }
            }
        }
        if (none) {
            return NutsDesktopEnvironmentFamily.NONE;
        }
        return NutsDesktopEnvironmentFamily.UNKNOWN;
    }

    public NutsId getOs() {
        return os;
    }

    public NutsId getPlatform(NutsSession session) {
//        if (platform == null) {
//            platform = NutsWorkspaceConfigManagerExt.of(workspace.config())
//                    .getModel()
//                    .createSdkId("java", System.getProperty("java.version"), session);
//        }
        return platform;
    }

    public NutsId getOsDist() {
        return osDist;
    }

    public Map<String, Object> getProperties() {
        return userProperties;
    }

    public NutsVal getProperty(String property) {
        return new DefaultNutsVal(userProperties.get(property));
    }

    public <T> T getOrCreateProperty(Class<T> property, Supplier<T> supplier) {
        return getOrCreateProperty(property.getName(), supplier);
    }

    public <T> T getOrCreateProperty(String property, Supplier<T> supplier) {
        NutsVal a = getProperty(property);
        T o = (T) a.getObject();
        if (o != null) {
            return o;
        }
        o = supplier.get();
        setProperty(property, o);
        return o;
    }

    public void setProperty(String property, Object value) {
        if (value == null) {
            userProperties.remove(property);
        } else {
            userProperties.put(property, value);
        }
    }

    public NutsWorkspace getWorkspace() {
        return workspace;
    }

}
