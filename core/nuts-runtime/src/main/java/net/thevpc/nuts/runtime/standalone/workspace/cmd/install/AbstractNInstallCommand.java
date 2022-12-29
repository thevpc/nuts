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
 *
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
package net.thevpc.nuts.runtime.standalone.workspace.cmd.install;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArgument;
import net.thevpc.nuts.cmdline.NCommandLine;
import net.thevpc.nuts.runtime.standalone.util.CoreEnumUtils;
import net.thevpc.nuts.runtime.standalone.util.collections.CoreCollectionUtils;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.NWorkspaceCommandBase;
import net.thevpc.nuts.spi.NSupportLevelContext;

import java.util.*;
import java.util.function.Predicate;

/**
 *
 * type: Command Class
 *
 * @author thevpc
 */
public abstract class AbstractNInstallCommand extends NWorkspaceCommandBase<NInstallCommand> implements NInstallCommand {

    protected boolean defaultVersion = true;
    protected NInstallStrategy companions;
    protected NInstallStrategy installed;
    protected NInstallStrategy strategy = NInstallStrategy.DEFAULT;
    protected List<String> args;
    protected List<ConditionalArguments> conditionalArguments = new ArrayList<>();
    protected final Map<NId, NInstallStrategy> ids = new LinkedHashMap<>();
    protected NDefinition[] result;
    protected NId[] failed;

    protected static class ConditionalArguments {

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

    public AbstractNInstallCommand(NSession ws) {
        super(ws, "install");
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return DEFAULT_SUPPORT;
    }

    @Override
    public NInstallCommand addId(String id) {
        checkSession();
        return addId(id == null ? null : NId.of(id).get(session));
    }

    @Override
    public NInstallCommand setId(NId id) {
        return clearIds().addId(id);
    }

    @Override
    public NInstallCommand setId(String id) {
        return clearIds().addId(id);
    }

    @Override
    public NInstallCommand setIds(NId... ids) {
        return clearIds().addIds(ids);
    }

    @Override
    public NInstallCommand setIds(String... ids) {
        return clearIds().addIds(ids);
    }

    @Override
    public NInstallCommand addId(NId id) {
        if (id == null) {
            checkSession();
            throw new NNotFoundException(session, id);
        } else {
            ids.put(id, getStrategy());
        }
        return this;
    }

    @Override
    public NInstallCommand addIds(String... ids) {
        for (String id : ids) {
            addId(id);
        }
        return this;
    }

    @Override
    public NInstallCommand addIds(NId... ids) {
        for (NId id : ids) {
            addId(id);
        }
        return this;
    }

    @Override
    public NInstallCommand removeId(NId id) {
        if (id != null) {
            this.ids.remove(id);
        }
        return this;
    }

    @Override
    public NInstallCommand removeId(String id) {
        checkSession();
        if (id != null) {
            this.ids.remove(NId.of(id).get(session));
        }
        return this;
    }

    @Override
    public NInstallCommand clearIds() {
        this.ids.clear();
        return this;
    }

    @Override
    public NInstallCommand clearArgs() {
        this.args = null;
        return this;
    }

    @Override
    public List<String> getArgs() {
        return CoreCollectionUtils.unmodifiableList(args);
    }

    @Override
    public NInstallCommand addArg(String arg) {
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
    public NInstallCommand addArgs(String... args) {
        return addArgs(args == null ? null : Arrays.asList(args));
    }

    @Override
    public NInstallCommand addConditionalArgs(Predicate<NDefinition> definition, String... args) {
        conditionalArguments.add(new ConditionalArguments(definition, Arrays.asList(args)));
        return this;
    }

    @Override
    public NInstallCommand addArgs(Collection<String> args) {
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
    public List<NId> getIds() {
        return CoreCollectionUtils.unmodifiableList(ids == null ? null : ids.keySet());
    }

    @Override
    public Map<NId, NInstallStrategy> getIdMap() {
        return ids == null ? new LinkedHashMap<>() : new LinkedHashMap<>(ids);
    }

    @Override
    public boolean isCompanions() {
        return companions != null;
    }

    @Override
    public NInstallCommand setCompanions(boolean value) {
        this.companions = value ? getStrategy() : null;
        return this;
    }

    @Override
    public NInstallStrategy getCompanions() {
        return companions;
    }

    @Override
    public boolean isInstalled() {
        return installed != null;
    }

    @Override
    public NInstallCommand setInstalled(boolean value) {
        this.installed = value ? getStrategy() : null;
        return this;
    }

    @Override
    public NInstallStrategy getInstalled() {
        return installed;
    }

    @Override
    public boolean isDefaultVersion() {
        return defaultVersion;
    }

    @Override
    public NInstallCommand setDefaultVersion(boolean defaultVersion) {
        this.defaultVersion = defaultVersion;
        return this;
    }

    @Override
    public NInstallCommand defaultVersion(boolean defaultVersion) {
        return setDefaultVersion(defaultVersion);
    }

    @Override
    public NInstallCommand defaultVersion() {
        return defaultVersion(true);
    }

    @Override
    public NInstallCommand companions(boolean value) {
        return setCompanions(value);
    }

    @Override
    public NInstallCommand companions() {
        return companions(true);
    }

    @Override
    public NInstallCommand setStrategy(NInstallStrategy value) {
        if (value == null) {
            value = NInstallStrategy.DEFAULT;
        }
        this.strategy = value;
        return this;
    }

    @Override
    public NInstallStrategy getStrategy() {
        return strategy;
    }

    @Override
    public boolean configureFirst(NCommandLine commandLine) {
        NArgument aa = commandLine.peek().get(session);
        if (aa == null) {
            return false;
        }
        boolean enabled = aa.isActive();
        switch (aa.key()) {
            case "-c":
            case "--companions": {
                commandLine.withNextBoolean((v, a, s) -> this.setCompanions(v));
                return true;
            }
            case "-i":
            case "--installed": {
                commandLine.withNextBoolean((v, a, s) -> this.setInstalled(v));
                return true;
            }
            case "-s":
            case "--strategy": {
                String val = commandLine.nextString().flatMap(NValue::asString).get(session);
                if (enabled) {
                    this.setStrategy(CoreEnumUtils.parseEnumString(val, NInstallStrategy.class, false));
                }
                return true;
            }
            case "--reinstall": {
                commandLine.skip();
                if (enabled) {
                    this.setStrategy(NInstallStrategy.REINSTALL);
                }
                return true;
            }
            case "--require": {
                commandLine.skip();
                if (enabled) {
                    this.setStrategy(NInstallStrategy.REQUIRE);
                }
                return true;
            }
            case "--repair": {
                commandLine.skip();
                if (enabled) {
                    this.setStrategy(NInstallStrategy.REPAIR);
                }
                return true;
            }
            case "-g":
            case "--args": {
                commandLine.skip();
                if (enabled) {
                    this.addArgs(commandLine.toStringArray());
                }
                commandLine.skipAll();
                return true;
            }

            default: {
                if (super.configureFirst(commandLine)) {
                    return true;
                }
                if (aa.isOption()) {
                    return false;
                } else {
                    commandLine.skip();
                    addId(aa.asString().get(session));
                    return true;
                }
            }
        }
    }
}
