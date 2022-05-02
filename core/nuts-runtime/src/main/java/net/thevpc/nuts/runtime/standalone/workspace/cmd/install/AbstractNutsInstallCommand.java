/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 *
 * <br>
 *
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
package net.thevpc.nuts.runtime.standalone.workspace.cmd.install;

import net.thevpc.nuts.*;

import java.util.*;
import java.util.function.Predicate;

import net.thevpc.nuts.boot.PrivateNutsUtilCollections;
import net.thevpc.nuts.cmdline.NutsArgument;
import net.thevpc.nuts.cmdline.NutsCommandLine;
import net.thevpc.nuts.runtime.standalone.util.CoreEnumUtils;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.NutsWorkspaceCommandBase;

/**
 *
 * type: Command Class
 *
 * @author thevpc
 */
public abstract class AbstractNutsInstallCommand extends NutsWorkspaceCommandBase<NutsInstallCommand> implements NutsInstallCommand {

    protected boolean defaultVersion = true;
    protected NutsInstallStrategy companions;
    protected NutsInstallStrategy installed;
    protected NutsInstallStrategy strategy = NutsInstallStrategy.DEFAULT;
    protected List<String> args;
    protected List<ConditionalArguments> conditionalArguments = new ArrayList<>();
    protected final Map<NutsId, NutsInstallStrategy> ids = new LinkedHashMap<>();
    protected NutsDefinition[] result;
    protected NutsId[] failed;

    protected static class ConditionalArguments {

        Predicate<NutsDefinition> predicate;
        List<String> args = new ArrayList<>();

        public ConditionalArguments(Predicate<NutsDefinition> predicate, List<String> args) {
            this.predicate = predicate;
            this.args = args;
        }

        public Predicate<NutsDefinition> getPredicate() {
            return predicate;
        }

        public List<String> getArgs() {
            return args;
        }
    }

    public AbstractNutsInstallCommand(NutsWorkspace ws) {
        super(ws, "install");
    }

    @Override
    public NutsInstallCommand addId(String id) {
        checkSession();
        return addId(id == null ? null : NutsId.of(id).get(session));
    }

    @Override
    public NutsInstallCommand setId(NutsId id) {
        return clearIds().addId(id);
    }

    @Override
    public NutsInstallCommand setId(String id) {
        return clearIds().addId(id);
    }

    @Override
    public NutsInstallCommand setIds(NutsId... ids) {
        return clearIds().addIds(ids);
    }

    @Override
    public NutsInstallCommand setIds(String... ids) {
        return clearIds().addIds(ids);
    }

    @Override
    public NutsInstallCommand addId(NutsId id) {
        if (id == null) {
            checkSession();
            throw new NutsNotFoundException(session, id);
        } else {
            ids.put(id, getStrategy());
        }
        return this;
    }

    @Override
    public NutsInstallCommand addIds(String... ids) {
        for (String id : ids) {
            addId(id);
        }
        return this;
    }

    @Override
    public NutsInstallCommand addIds(NutsId... ids) {
        for (NutsId id : ids) {
            addId(id);
        }
        return this;
    }

    @Override
    public NutsInstallCommand removeId(NutsId id) {
        if (id != null) {
            this.ids.remove(id);
        }
        return this;
    }

    @Override
    public NutsInstallCommand removeId(String id) {
        checkSession();
        if (id != null) {
            this.ids.remove(NutsId.of(id).get(session));
        }
        return this;
    }

    @Override
    public NutsInstallCommand clearIds() {
        this.ids.clear();
        return this;
    }

    @Override
    public NutsInstallCommand clearArgs() {
        this.args = null;
        return this;
    }

    @Override
    public List<String> getArgs() {
        return PrivateNutsUtilCollections.unmodifiableList(args);
    }

    @Override
    public NutsInstallCommand addArg(String arg) {
        if (this.args == null) {
            this.args = new ArrayList<>();
        }
        if (arg == null) {
            throw new NullPointerException();
        }
        this.args.add(arg);
        return this;
    }

    @Override
    public NutsInstallCommand addArgs(String... args) {
        return addArgs(args == null ? null : Arrays.asList(args));
    }

    @Override
    public NutsInstallCommand addConditionalArgs(Predicate<NutsDefinition> definition, String... args) {
        conditionalArguments.add(new ConditionalArguments(definition, Arrays.asList(args)));
        return this;
    }

    @Override
    public NutsInstallCommand addArgs(Collection<String> args) {
        if (this.args == null) {
            this.args = new ArrayList<>();
        }
        if (args != null) {
            for (String arg : args) {
                if (arg == null) {
                    throw new NullPointerException();
                }
                this.args.add(arg);
            }
        }
        return this;
    }

    @Override
    public List<NutsId> getIds() {
        return PrivateNutsUtilCollections.unmodifiableList(ids==null?null:ids.keySet());
    }

    @Override
    public Map<NutsId, NutsInstallStrategy> getIdMap() {
        return ids == null ? new LinkedHashMap<>() : new LinkedHashMap<>(ids);
    }

    @Override
    public boolean isCompanions() {
        return companions != null;
    }

    @Override
    public NutsInstallCommand setCompanions(boolean value) {
        this.companions = value ? getStrategy() : null;
        return this;
    }

    @Override
    public NutsInstallStrategy getCompanions() {
        return companions;
    }

    @Override
    public boolean isInstalled() {
        return installed != null;
    }

    @Override
    public NutsInstallCommand setInstalled(boolean value) {
        this.installed = value ? getStrategy() : null;
        return this;
    }

    @Override
    public NutsInstallStrategy getInstalled() {
        return installed;
    }

    @Override
    public boolean isDefaultVersion() {
        return defaultVersion;
    }

    @Override
    public NutsInstallCommand setDefaultVersion(boolean defaultVersion) {
        this.defaultVersion = defaultVersion;
        return this;
    }

    @Override
    public NutsInstallCommand defaultVersion(boolean defaultVersion) {
        return setDefaultVersion(defaultVersion);
    }

    @Override
    public NutsInstallCommand defaultVersion() {
        return defaultVersion(true);
    }

    @Override
    public NutsInstallCommand companions(boolean value) {
        return setCompanions(value);
    }

    @Override
    public NutsInstallCommand companions() {
        return companions(true);
    }

    @Override
    public NutsInstallCommand setStrategy(NutsInstallStrategy value) {
        if (value == null) {
            value = NutsInstallStrategy.DEFAULT;
        }
        this.strategy = value;
        return this;
    }

    @Override
    public NutsInstallStrategy getStrategy() {
        return strategy;
    }

    @Override
    public boolean configureFirst(NutsCommandLine cmdLine) {
        NutsArgument a = cmdLine.peek().get(session);
        if (a == null) {
            return false;
        }
        boolean enabled = a.isActive();
        switch(a.getStringKey().orElse("")) {
            case "-c":
            case "--companions": {
                boolean val = cmdLine.nextBooleanValueLiteral().get(session);
                if (enabled) {
                    this.setCompanions(val);
                }
                return true;
            }
            case "-i":
            case "--installed": {
                boolean val = cmdLine.nextBooleanValueLiteral().get(session);
                if (enabled) {
                    this.setInstalled(val);
                }
                return true;
            }
            case "-s":
            case "--strategy": {
                String val = cmdLine.nextString().flatMap(NutsValue::asString).get(session);
                if (enabled) {
                    this.setStrategy(CoreEnumUtils.parseEnumString(val, NutsInstallStrategy.class, false));
                }
                return true;
            }
            case "--reinstall": {
                cmdLine.skip();
                if (enabled) {
                    this.setStrategy(NutsInstallStrategy.REINSTALL);
                }
                return true;
            }
            case "--require": {
                cmdLine.skip();
                if (enabled) {
                    this.setStrategy(NutsInstallStrategy.REQUIRE);
                }
                return true;
            }
            case "--repair": {
                cmdLine.skip();
                if (enabled) {
                    this.setStrategy(NutsInstallStrategy.REPAIR);
                }
                return true;
            }
            case "-g":
            case "--args": {
                cmdLine.skip();
                if (enabled) {
                    this.addArgs(cmdLine.toStringArray());
                }
                cmdLine.skipAll();
                return true;
            }

            default: {
                if (super.configureFirst(cmdLine)) {
                    return true;
                }
                if (a.isOption()) {
                    return false;
                } else {
                    cmdLine.skip();
                    addId(a.asString().get(session));
                    return true;
                }
            }
        }
    }
}
