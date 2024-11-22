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
package net.thevpc.nuts.runtime.standalone.workspace.config;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.env.NArchFamily;
import net.thevpc.nuts.env.NDesktopEnvironmentFamily;
import net.thevpc.nuts.env.NOsFamily;
import net.thevpc.nuts.env.NPlatformFamily;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.util.CorePlatformUtils;
import net.thevpc.nuts.util.NDefaultObservableMap;
import net.thevpc.nuts.util.NObservableMap;
import net.thevpc.nuts.runtime.standalone.app.gui.CoreNUtilGui;
import net.thevpc.nuts.runtime.standalone.util.jclass.NJavaSdkUtils;
import net.thevpc.nuts.util.*;

import java.lang.management.ManagementFactory;
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
public class DefaultNWorkspaceEnvManagerModel {

    private final Map<NPlatformFamily, List<NPlatformLocation>> configPlatforms = new LinkedHashMap<>();
    //    private Map<String, String> options = new LinkedHashMap<>();
    protected NObservableMap<String, Object> userProperties;
    private final NWorkspace workspace;
    private final NId platform;
    private final NId os;
    private NOsFamily osFamily;
    private String hostName;
    private String pid;
    private NShellFamily shellFamily;
    private Set<NId> desktopEnvironments;
    private Set<NDesktopEnvironmentFamily> osDesktopEnvironmentFamilies;
    private NDesktopEnvironmentFamily osDesktopEnvironmentFamily;
    private final NId arch;
    private final NId osDist;
    private final NArchFamily archFamily = NArchFamily.getCurrent();

    public DefaultNWorkspaceEnvManagerModel(NWorkspace ws) {
        this.workspace = ws;
        userProperties = new NDefaultObservableMap<>();
        os = NId.of(CorePlatformUtils.getPlatformOs()).get();
        String platformOsDist = CorePlatformUtils.getPlatformOsDist();
        if (platformOsDist == null) {
            platformOsDist = "default";
        }
        osDist = NId.of(platformOsDist).get();
        platform = NJavaSdkUtils.of(ws).createJdkId(System.getProperty("java.version"));
        arch = NId.of(System.getProperty("os.arch")).get();

    }

    public NId getArch() {
        return arch;
    }

    public NArchFamily getArchFamily() {
        return archFamily;
    }

    public String getPid() {
        if(pid==null){
            String fallback="";
            // Note: may fail in some JVM implementations
            // therefore fallback has to be provided

            // something like '<pid>@<hostname>', at least in SUN / Oracle JVMs
            final String jvmName = ManagementFactory.getRuntimeMXBean().getName();
            final int index = jvmName.indexOf('@');
            if (index < 1) {
                // part before '@' empty (index = 0) / '@' not found (index = -1)
                return pid=fallback;
            }

            try {
                return pid=String.valueOf(Long.toString(Long.parseLong(jvmName.substring(0, index))));
            } catch (NumberFormatException e) {
                // ignore
            }
            return pid=fallback;
        }
        return pid;
    }

    public String getHostName() {
        if (hostName == null) {
            switch (getOsFamily()) {
                case WINDOWS: {
                    String computername = System.getenv("COMPUTERNAME");
                    if (computername != null) {
                        hostName = computername;
                    } else {
                        try {
                            String hostname = NExecCmd.of(
                                    ).addCommand("hostname")
                                    .failFast()
                                    .getGrabbedOutOnlyString();
                            hostName = NStringUtils.trim(hostname);
                        } catch (Exception any) {
                            //
                        }
                    }
                    if (hostName == null) {
                        hostName = "";
                    }
                    break;
                }
                case UNIX:
                case LINUX:
                case MACOS:
                default: {
                    String h = null;
                    try {
                        h = NStringUtils.trim(NPath.of("/etc/hostname")
                                .readString());
                    } catch (Exception e) {
                        //ignore
                    }
                    if (NBlankable.isBlank(h)) {
                        h = NExecCmd.of()
                                .system()
                                .addCommand("/bin/hostname")
                                .getGrabbedOutOnlyString();
                    }
                    hostName = NStringUtils.trim(h);
                    break;
                }
            }
        }
        return hostName;
    }

    public NOsFamily getOsFamily() {
        if (osFamily == null) {
            osFamily = NOsFamily.getCurrent();
        }
        return osFamily;
    }

    public NShellFamily getShellFamily() {
        if (shellFamily == null) {
            shellFamily = NShellFamily.getCurrent();
        }
        return shellFamily;
    }

    /*fix this add field  like above*/
    public Set<NShellFamily> getShellFamilies() {
        return getShellFamilies(true);
    }

    public Set<NShellFamily> getShellFamilies(boolean allEvenNonInstalled) {
        ArrayList<NShellFamily> shellFamilies = new ArrayList<>();
        switch (this.getOsFamily()) {
            case UNIX:
            case LINUX:
            case MACOS: {
                LinkedHashSet<NShellFamily> families = new LinkedHashSet<>();
                families.add(this.getShellFamily());
                //add bash with existing rc
                NShellFamily[] all = {
                        NShellFamily.SH,
                        NShellFamily.BASH,
                        NShellFamily.ZSH,
                        NShellFamily.CSH,
                        NShellFamily.KSH,
                        NShellFamily.FISH
                };
                for (NShellFamily f : all) {
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
                LinkedHashSet<NShellFamily> families = new LinkedHashSet<>();
                families.add(this.getShellFamily());
                //add bash with existing rc
                families.add(NShellFamily.WIN_CMD);
                if (this.getOs().getVersion().compareTo("7") >= 0) {
                    families.add(NShellFamily.WIN_POWER_SHELL);
                }
                shellFamilies.addAll(families);

                break;
            }
            default: {
                shellFamilies.add(NShellFamily.UNKNOWN);
            }
        }
        return new LinkedHashSet<>(shellFamilies);
    }

    public Set<NId> getDesktopEnvironments() {
        if (desktopEnvironments == null) {
            desktopEnvironments = getDesktopEnvironments0();
        }
        return desktopEnvironments;
    }

    public boolean isGraphicalDesktopEnvironment() {
        return CoreNUtilGui.isGraphicalDesktopEnvironment();
    }

    protected NId[] getDesktopEnvironmentsXDGOrEmpty() {
        String _XDG_SESSION_DESKTOP = System.getenv("XDG_SESSION_DESKTOP");
        String _XDG_CURRENT_DESKTOP = System.getenv("XDG_CURRENT_DESKTOP");
        List<NId> a = new ArrayList<>();
        if (!NBlankable.isBlank(_XDG_SESSION_DESKTOP) && !NBlankable.isBlank(_XDG_SESSION_DESKTOP)) {
            String[] supportedSessions = new LinkedHashSet<>(
                    Arrays.stream(NStringUtils.trim(_XDG_CURRENT_DESKTOP).split(":"))
                            .map(x -> x.trim().toLowerCase()).filter(x -> x.length() > 0)
                            .collect(Collectors.toList())
            ).toArray(new String[0]);
            String sd = _XDG_SESSION_DESKTOP.toLowerCase();
            for (int i = 0; i < supportedSessions.length; i++) {
                NIdBuilder nb = NIdBuilder.of().setArtifactId(supportedSessions[i]);
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
        return a.toArray(new NId[0]);
    }

    protected Set<NId> getDesktopEnvironments0() {
        if (!isGraphicalDesktopEnvironment()) {
            return Collections.singleton(
                    NIdBuilder.of().setArtifactId(NDesktopEnvironmentFamily.HEADLESS.id()).build());
        }
        switch (NEnvs.of().getOsFamily()) {
            case WINDOWS: {
                return Collections.singleton(NIdBuilder.of().setArtifactId(NDesktopEnvironmentFamily.WINDOWS_SHELL.id()).build());
            }
            case MACOS: {
                return Collections.singleton(NIdBuilder.of().setArtifactId(NDesktopEnvironmentFamily.MACOS_AQUA.id()).build());
            }
            case UNIX:
            case LINUX: {
                NId[] all = getDesktopEnvironmentsXDGOrEmpty();
                if (all.length == 0) {
                    return Collections.singleton(NIdBuilder.of().setArtifactId(NDesktopEnvironmentFamily.UNKNOWN.id()).build());
                }
                return Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(all)));
            }
            default: {
                return Collections.singleton(NIdBuilder.of().setArtifactId(NDesktopEnvironmentFamily.UNKNOWN.id()).build());
            }
        }
    }

    public Set<NDesktopEnvironmentFamily> getDesktopEnvironmentFamilies() {
        if (osDesktopEnvironmentFamilies == null) {
            osDesktopEnvironmentFamilies = getDesktopEnvironmentFamilies0();
        }
        return osDesktopEnvironmentFamilies;
    }

    public Set<NDesktopEnvironmentFamily> getDesktopEnvironmentFamilies0() {
        Set<NId> desktopEnvironments = getDesktopEnvironments();
        LinkedHashSet<NDesktopEnvironmentFamily> all = new LinkedHashSet<>();
        for (NId desktopEnvironment : desktopEnvironments) {
            all.add(NDesktopEnvironmentFamily.parse(desktopEnvironment.getShortName()).orNull());
        }
        return new LinkedHashSet<>(all);
    }

    public NDesktopEnvironmentFamily getDesktopEnvironmentFamily() {
        if (osDesktopEnvironmentFamily == null) {
            osDesktopEnvironmentFamily = getDesktopEnvironmentFamily0();
        }
        return osDesktopEnvironmentFamily;
    }

    public NDesktopEnvironmentFamily getDesktopEnvironmentFamily0() {
        Set<NDesktopEnvironmentFamily> all = getDesktopEnvironmentFamilies();
        if (all.size() == 0) {
            return NDesktopEnvironmentFamily.UNKNOWN;
        }
        boolean unknown = false;
        boolean none = false;
        boolean headless = false;
        for (NDesktopEnvironmentFamily f : all) {
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
            return NDesktopEnvironmentFamily.HEADLESS;
        }
        if (none) {
            return NDesktopEnvironmentFamily.NONE;
        }
        if (unknown) {
            return NDesktopEnvironmentFamily.UNKNOWN;
        }
        return NDesktopEnvironmentFamily.UNKNOWN;
    }

    public NId getOs() {
        return os;
    }

    public NId getPlatform() {
//        if (platform == null) {
//            platform = NutsWorkspaceConfigManagerExt.of(workspace.config())
//                    .getModel()
//                    .createSdkId("java", System.getProperty("java.version"), session);
//        }
        return platform;
    }

    public NId getOsDist() {
        return osDist;
    }

    public Map<String, Object> getProperties() {
        return userProperties;
    }

    public NOptional<NLiteral> getProperty(String property) {
        Object v = userProperties.get(property);
        return NOptional.of(
                v == null ? null : NLiteral.of(v)
        );
    }

    public NElement getPropertyElement(String property) {
        return NElements.of()
                .setIndestructibleObjects(x -> !x.isPrimitive()
                        && !Number.class.isAssignableFrom(x)
                        && !Boolean.class.isAssignableFrom(x)
                        && !String.class.isAssignableFrom(x)
                        && !Instant.class.isAssignableFrom(x))
                .toElement(getProperty(property));
    }

    public <T> T getOrCreateProperty(Class<T> property, Supplier<T> supplier) {
        return getOrCreateProperty(property.getName(), supplier);
    }

    public <T> T getOrCreateProperty(String property, Supplier<T> supplier) {
        NElement a = getPropertyElement(property);
        T o = a.isCustom() ? (T) a.asCustom().get().getValue() : (T) a.asPrimitive().get().getRaw();
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

    public NWorkspace getWorkspace() {
        return workspace;
    }

    public Map<NPlatformFamily, List<NPlatformLocation>> getConfigPlatforms() {
        return configPlatforms;
    }
}
