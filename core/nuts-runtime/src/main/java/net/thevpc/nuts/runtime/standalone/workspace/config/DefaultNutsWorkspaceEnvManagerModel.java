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
package net.thevpc.nuts.runtime.standalone.workspace.config;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NutsElement;
import net.thevpc.nuts.elem.NutsElements;
import net.thevpc.nuts.runtime.standalone.session.NutsSessionUtils;
import net.thevpc.nuts.runtime.standalone.util.CorePlatformUtils;
import net.thevpc.nuts.runtime.standalone.util.collections.DefaultObservableMap;
import net.thevpc.nuts.runtime.standalone.util.collections.ObservableMap;
import net.thevpc.nuts.runtime.standalone.app.gui.CoreNutsUtilGui;
import net.thevpc.nuts.runtime.standalone.util.jclass.NutsJavaSdkUtils;
import net.thevpc.nuts.util.NutsStringUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author thevpc
 */
public class DefaultNutsWorkspaceEnvManagerModel {

    private final Map<NutsPlatformFamily, List<NutsPlatformLocation>> configPlatforms = new LinkedHashMap<>();
    //    private Map<String, String> options = new LinkedHashMap<>();
    protected ObservableMap<String, Object> userProperties;
    private final NutsWorkspace workspace;
    private final NutsId platform;
    private final NutsId os;
    private NutsOsFamily osFamily;
    private NutsShellFamily shellFamily;
    private Set<NutsId> desktopEnvironments;
    private Set<NutsDesktopEnvironmentFamily> osDesktopEnvironmentFamilies;
    private NutsDesktopEnvironmentFamily osDesktopEnvironmentFamily;
    private final NutsId arch;
    private final NutsId osDist;
    private final NutsArchFamily archFamily = NutsArchFamily.getCurrent();

    public DefaultNutsWorkspaceEnvManagerModel(NutsWorkspace ws, NutsSession session) {
        this.workspace = ws;
        userProperties = new DefaultObservableMap<>();
//        String[] properties = info.getOptions().getProperties();
//        if (properties != null) {
//            for (String property : properties) {
//                if (property != null) {
//                    DefaultNutsArgument a = new DefaultNutsArgument(property);
//                    String key = a.getKey().getString();
//                    String value = a.getStringValue().get(session);
//                    this.options.put(key, value == null ? "" : value);
//                }
//            }
//        }
        os = NutsId.of(CorePlatformUtils.getPlatformOs(NutsSessionUtils.defaultSession(workspace))).get();
        String platformOsDist = CorePlatformUtils.getPlatformOsDist(NutsSessionUtils.defaultSession(workspace));
        if (platformOsDist == null) {
            platformOsDist = "default";
        }
        osDist = NutsId.of(platformOsDist).get();
        platform = NutsJavaSdkUtils.of(session).createJdkId(System.getProperty("java.version"), session);
        arch = NutsId.of(System.getProperty("os.arch")).get();

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

    /*fix this add field  like above*/
    public Set<NutsShellFamily> getShellFamilies() {
        return getShellFamilies(true);
    }

    public Set<NutsShellFamily> getShellFamilies(boolean allEvenNonInstalled) {
        ArrayList<NutsShellFamily> shellFamilies = new ArrayList<>();
        switch (this.getOsFamily()) {
            case UNIX:
            case LINUX:
            case MACOS: {
                LinkedHashSet<NutsShellFamily> families = new LinkedHashSet<>();
                families.add(this.getShellFamily());
                //add bash with existing rc
                NutsShellFamily[] all = {
                        NutsShellFamily.SH,
                        NutsShellFamily.BASH,
                        NutsShellFamily.ZSH,
                        NutsShellFamily.CSH,
                        NutsShellFamily.KSH,
                        NutsShellFamily.FISH
                };
                for (NutsShellFamily f : all) {
                    if (f != null) {
                        Path path = Paths.get("/bin").resolve(f.id());
                        if (Files.exists(path)) {
                            families.add(f);
                        }
                    }
                }
                if (allEvenNonInstalled) {
                    families.addAll(Arrays.asList(all));
                }
                shellFamilies.addAll(families);
                break;
            }
            case WINDOWS: {
                LinkedHashSet<NutsShellFamily> families = new LinkedHashSet<>();
                families.add(this.getShellFamily());
                //add bash with existing rc
                families.add(NutsShellFamily.WIN_CMD);
                if (this.getOs().getVersion().compareTo("7") >= 0) {
                    families.add(NutsShellFamily.WIN_POWER_SHELL);
                }
                shellFamilies.addAll(families);

                break;
            }
            default: {
                shellFamilies.add(NutsShellFamily.UNKNOWN);
            }
        }
        return new LinkedHashSet<>(shellFamilies);
    }

    public Set<NutsId> getDesktopEnvironments(NutsSession session) {
        if (desktopEnvironments == null) {
            desktopEnvironments = getDesktopEnvironments0(session);
        }
        return desktopEnvironments;
    }

    public boolean isGraphicalDesktopEnvironment() {
        return CoreNutsUtilGui.isGraphicalDesktopEnvironment();
    }

    protected NutsId[] getDesktopEnvironmentsXDGOrEmpty(NutsSession session) {
        String _XDG_SESSION_DESKTOP = System.getenv("XDG_SESSION_DESKTOP");
        String _XDG_CURRENT_DESKTOP = System.getenv("XDG_CURRENT_DESKTOP");
        List<NutsId> a = new ArrayList<>();
        if (!NutsBlankable.isBlank(_XDG_SESSION_DESKTOP) && !NutsBlankable.isBlank(_XDG_SESSION_DESKTOP)) {
            String[] supportedSessions = new LinkedHashSet<>(
                    Arrays.stream(NutsStringUtils.trim(_XDG_CURRENT_DESKTOP).split(":"))
                            .map(x -> x.trim().toLowerCase()).filter(x -> x.length() > 0)
                            .collect(Collectors.toList())
            ).toArray(new String[0]);
            String sd = _XDG_SESSION_DESKTOP.toLowerCase();
            for (int i = 0; i < supportedSessions.length; i++) {
                NutsIdBuilder nb = NutsIdBuilder.of().setArtifactId(supportedSessions[i]);
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
                if (_XDG_SESSION_CLASS != null) {
                    nb.setProperty("class", _XDG_SESSION_CLASS.trim().toLowerCase());
                }
                a.add(nb.build());
            }
        }
        return a.toArray(new NutsId[0]);
    }

    protected Set<NutsId> getDesktopEnvironments0(NutsSession session) {
        if (!isGraphicalDesktopEnvironment()) {
            return Collections.singleton(
                    NutsIdBuilder.of().setArtifactId(NutsDesktopEnvironmentFamily.HEADLESS.id()).build());
        }
        switch (session.env().getOsFamily()) {
            case WINDOWS: {
                return Collections.singleton(NutsIdBuilder.of().setArtifactId(NutsDesktopEnvironmentFamily.WINDOWS_SHELL.id()).build());
            }
            case MACOS: {
                return Collections.singleton(NutsIdBuilder.of().setArtifactId(NutsDesktopEnvironmentFamily.MACOS_AQUA.id()).build());
            }
            case UNIX:
            case LINUX: {
                NutsId[] all = getDesktopEnvironmentsXDGOrEmpty(session);
                if (all.length == 0) {
                    return Collections.singleton(NutsIdBuilder.of().setArtifactId(NutsDesktopEnvironmentFamily.UNKNOWN.id()).build());
                }
                return Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(all)));
            }
            default: {
                return Collections.singleton(NutsIdBuilder.of().setArtifactId(NutsDesktopEnvironmentFamily.UNKNOWN.id()).build());
            }
        }
    }

    public Set<NutsDesktopEnvironmentFamily> getDesktopEnvironmentFamilies(NutsSession session) {
        if (osDesktopEnvironmentFamilies == null) {
            osDesktopEnvironmentFamilies = getDesktopEnvironmentFamilies0(session);
        }
        return osDesktopEnvironmentFamilies;
    }

    public Set<NutsDesktopEnvironmentFamily> getDesktopEnvironmentFamilies0(NutsSession session) {
        Set<NutsId> desktopEnvironments = getDesktopEnvironments(session);
        LinkedHashSet<NutsDesktopEnvironmentFamily> all = new LinkedHashSet<>();
        for (NutsId desktopEnvironment : desktopEnvironments) {
            all.add(NutsDesktopEnvironmentFamily.parse(desktopEnvironment.getShortName()).orNull());
        }
        return new LinkedHashSet<>(all);
    }

    public NutsDesktopEnvironmentFamily getDesktopEnvironmentFamily(NutsSession session) {
        if (osDesktopEnvironmentFamily == null) {
            osDesktopEnvironmentFamily = getDesktopEnvironmentFamily0(session);
        }
        return osDesktopEnvironmentFamily;
    }

    public NutsDesktopEnvironmentFamily getDesktopEnvironmentFamily0(NutsSession session) {
        Set<NutsDesktopEnvironmentFamily> all = getDesktopEnvironmentFamilies(session);
        if (all.size() == 0) {
            return NutsDesktopEnvironmentFamily.UNKNOWN;
        }
        boolean unknown = false;
        boolean none = false;
        boolean headless = false;
        for (NutsDesktopEnvironmentFamily f : all) {
            switch (f) {
                case UNKNOWN: {
                    unknown = true;
                    break;
                }
                case HEADLESS: {
                    headless = true;
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
        if (headless) {
            return NutsDesktopEnvironmentFamily.HEADLESS;
        }
        if (none) {
            return NutsDesktopEnvironmentFamily.NONE;
        }
        if (unknown) {
            return NutsDesktopEnvironmentFamily.UNKNOWN;
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

    public NutsOptional<NutsValue> getProperty(String property, NutsSession session) {
        Object v = userProperties.get(property);
        return NutsOptional.of(
                v == null ? null : NutsValue.of(v)
        );
    }

    public NutsElement getPropertyElement(String property, NutsSession session) {
        return NutsElements.of(session)
                .setIndestructibleObjects(x -> !x.isPrimitive()
                        && !Number.class.isAssignableFrom(x)
                        && !Boolean.class.isAssignableFrom(x)
                        && !String.class.isAssignableFrom(x)
                        && !Instant.class.isAssignableFrom(x))
                .toElement(getProperty(property, session));
    }

    public <T> T getOrCreateProperty(Class<T> property, Supplier<T> supplier, NutsSession session) {
        return getOrCreateProperty(property.getName(), supplier, session);
    }

    public <T> T getOrCreateProperty(String property, Supplier<T> supplier, NutsSession session) {
        NutsElement a = getPropertyElement(property, session);
        T o = a.isCustom() ? (T) a.asCustom().get(session).getValue() : (T) a.asPrimitive().get(session).getRaw();
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

    public Map<NutsPlatformFamily, List<NutsPlatformLocation>> getConfigPlatforms() {
        return configPlatforms;
    }
}
