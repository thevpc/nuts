/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . It's based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 *
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
package net.thevpc.nuts.runtime.standalone.workspace.cmd.install;

import net.thevpc.nuts.artifact.NDefinition;
import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.artifact.NArtifactNotFoundException;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.command.NInstall;
import net.thevpc.nuts.core.NWorkspace;
import net.thevpc.nuts.util.NCollections;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.NWorkspaceCmdBase;
import net.thevpc.nuts.util.NScore;
import net.thevpc.nuts.util.NScorable;

import java.util.*;
import java.util.function.Predicate;

/**
 * type: Command Class
 *
 * @author thevpc
 */
@NScore(fixed = NScorable.DEFAULT_SCORE)
public abstract class AbstractNInstall extends NWorkspaceCmdBase<NInstall> implements NInstall {

    protected boolean defaultVersion = true;
    protected InstallFlags companionsInstallFlags;
    protected InstallFlags installedInstallFlags;
    protected InstallFlags currentInstallFlags = new InstallFlags();
    protected List<String> args;
    protected List<ConditionalArguments> conditionalArguments = new ArrayList<>();
    protected final Map<NId, InstallFlags> ids = new LinkedHashMap<>();
    protected NDefinition[] result;
    protected NId[] failed;

    public static class ConditionalArguments {

        Predicate<NDefinition> predicate;
        List<String> args = new ArrayList<>();

        public ConditionalArguments(Predicate<NDefinition> predicate, List<String> args) {
            this.predicate = predicate;
            this.args = args;
        }

        public Predicate<NDefinition> getPredicate() {
            return predicate;
        }

        public List<String> getArgs() {
            return args;
        }
    }

    public AbstractNInstall(NWorkspace workspace) {
        super("install");
    }

    public boolean isForce() {
        return currentInstallFlags.force;
    }

    public NInstall setForce(boolean force) {
        currentInstallFlags.force = force;
        return this;
    }

    public boolean isSwitchVersion() {
        return currentInstallFlags.switchVersion;
    }

    public NInstall setSwitchVersion(boolean switchVersion) {
        currentInstallFlags.switchVersion = switchVersion;
        return this;
    }

    public boolean isRepair() {
        return currentInstallFlags.repair;
    }

    public NInstall setRepair(boolean repair) {
        currentInstallFlags.repair = repair;
        return this;
    }

    public boolean isDeployOnly() {
        return currentInstallFlags.deployOnly;
    }

    public NInstall setDeployOnly(boolean deployOnly) {
        currentInstallFlags.deployOnly = deployOnly;
        return this;
    }

    @Override
    public NInstall addId(String id) {
        return addId(id == null ? null : NId.get(id).get());
    }

    @Override
    public NInstall setId(NId id) {
        return clearIds().addId(id);
    }

    @Override
    public NInstall setId(String id) {
        return clearIds().addId(id);
    }

    @Override
    public NInstall setIds(NId... ids) {
        return clearIds().addIds(ids);
    }

    @Override
    public NInstall setIds(String... ids) {
        return clearIds().addIds(ids);
    }

    @Override
    public NInstall addId(NId id) {
        if (id == null) {
            throw new NArtifactNotFoundException(id);
        } else {
            ids.put(id, currentInstallFlags.copy());
        }
        return this;
    }

    @Override
    public NInstall addIds(String... ids) {
        for (String id : ids) {
            addId(id);
        }
        return this;
    }

    @Override
    public NInstall addIds(NId... ids) {
        for (NId id : ids) {
            addId(id);
        }
        return this;
    }

    @Override
    public NInstall removeId(NId id) {
        if (id != null) {
            this.ids.remove(id);
        }
        return this;
    }

    @Override
    public NInstall removeId(String id) {
        if (id != null) {
            this.ids.remove(NId.get(id).get());
        }
        return this;
    }

    @Override
    public NInstall clearIds() {
        this.ids.clear();
        return this;
    }

    @Override
    public NInstall clearArgs() {
        this.args = null;
        return this;
    }

    @Override
    public List<String> getArgs() {
        return NCollections.unmodifiableList(args);
    }

    @Override
    public NInstall addArg(String arg) {
        if(arg!=null) {
            if (this.args == null) {
                this.args = new ArrayList<>();
            }
            this.args.add(arg);
        }
        return this;
    }

    @Override
    public NInstall addArgs(String... args) {
        return addArgs(args == null ? null : Arrays.asList(args));
    }

    @Override
    public NInstall addConditionalArgs(Predicate<NDefinition> definition, String... args) {
        conditionalArguments.add(new ConditionalArguments(definition, Arrays.asList(args)));
        return this;
    }

    @Override
    public NInstall addArgs(Collection<String> args) {
        if (this.args == null) {
            this.args = new ArrayList<>();
        }
        if (args != null) {
            for (String arg : args) {
                if(arg!=null) {
                    this.args.add(arg);
                }
            }
        }
        return this;
    }

    @Override
    public List<NId> getIds() {
        return NCollections.unmodifiableList(ids == null ? null : ids.keySet());
    }

//    @Override
//    public Map<NId, NInstallStrategy> getIdMap() {
//        return ids == null ? new LinkedHashMap<>() : new LinkedHashMap<>(ids);
//    }

    @Override
    public boolean isCompanions() {
        return companionsInstallFlags != null;
    }

    @Override
    public NInstall setCompanions(boolean value) {
        this.companionsInstallFlags = value ? currentInstallFlags.copy() : null;
        return this;
    }

    @Override
    public boolean isInstalled() {
        return installedInstallFlags != null;
    }

    @Override
    public NInstall setInstalled(boolean value) {
        this.installedInstallFlags = value ? currentInstallFlags.copy() : null;
        return this;
    }

    @Override
    public boolean isDefaultVersion() {
        return defaultVersion;
    }

    @Override
    public NInstall setDefaultVersion(boolean defaultVersion) {
        this.defaultVersion = defaultVersion;
        return this;
    }

    @Override
    public NInstall defaultVersion(boolean defaultVersion) {
        return setDefaultVersion(defaultVersion);
    }

    @Override
    public NInstall defaultVersion() {
        return defaultVersion(true);
    }

    @Override
    public NInstall companions(boolean value) {
        return setCompanions(value);
    }

    @Override
    public NInstall companions() {
        return companions(true);
    }

//    @Override
//    public NInstallCmd setStrategy(NInstallStrategy value) {
//        if (value == null) {
//            value = NInstallStrategy.DEFAULT;
//        }
//        this.strategy = value;
//        return this;
//    }
//
//    @Override
//    public NInstallStrategy getStrategy() {
//        return strategy;
//    }

    @Override
    public boolean configureFirst(NCmdLine cmdLine) {
        NArg aa = cmdLine.peek().get();
        if (aa == null) {
            return false;
        }
        boolean enabled = aa.isUncommented();
        switch (aa.key()) {
            case "-c":
            case "--companions": {
                return cmdLine.matcher().matchFlag((v) -> this.setCompanions(v.booleanValue())).anyMatch();
            }
            case "-i":
            case "--installed": {
                return cmdLine.matcher().matchFlag((v) -> this.setInstalled(v.booleanValue())).anyMatch();
            }
//            case "-s":
//            case "--strategy": {
//                return cmdLine.matcher().matchEntry(a->this.setStrategy(NInstallStrategy.parse(a.stringValue()).get())).anyMatch();
//            }
            case "--reinstall": {
                return cmdLine.matcher().matchFlag(a->this.setForce(a.booleanValue())).anyMatch();
            }
            case "--deploy-only": {
                return cmdLine.matcher().matchFlag(a->this.setDeployOnly(a.booleanValue())).anyMatch();
            }
            case "--repair": {
                return cmdLine.matcher().matchTrueFlag(a->this.setRepair(a.booleanValue())).anyMatch();
            }
            case "-g":
            case "--args": {
                return cmdLine.matcher().matchAny(a->this.addArgs(cmdLine.nextAllAsStringArray())).anyMatch();
            }
            default: {
                if (super.configureFirst(cmdLine)) {
                    return true;
                }
                if (aa.isOption()) {
                    return false;
                } else {
                    cmdLine.skip();
                    addId(aa.asString().get());
                    return true;
                }
            }
        }
    }
}
