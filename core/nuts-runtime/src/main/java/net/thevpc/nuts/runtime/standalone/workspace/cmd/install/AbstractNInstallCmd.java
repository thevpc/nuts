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

import net.thevpc.nuts.*;
import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.runtime.standalone.util.CoreEnumUtils;
import net.thevpc.nuts.util.NCoreCollectionUtils;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.NWorkspaceCmdBase;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.util.NLiteral;

import java.util.*;
import java.util.function.Predicate;

/**
 * type: Command Class
 *
 * @author thevpc
 */
public abstract class AbstractNInstallCmd extends NWorkspaceCmdBase<NInstallCmd> implements NInstallCmd {

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

    public AbstractNInstallCmd(NWorkspace workspace) {
        super("install");
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return NConstants.Support.DEFAULT_SUPPORT;
    }

    @Override
    public NInstallCmd addId(String id) {
        return addId(id == null ? null : NId.get(id).get());
    }

    @Override
    public NInstallCmd setId(NId id) {
        return clearIds().addId(id);
    }

    @Override
    public NInstallCmd setId(String id) {
        return clearIds().addId(id);
    }

    @Override
    public NInstallCmd setIds(NId... ids) {
        return clearIds().addIds(ids);
    }

    @Override
    public NInstallCmd setIds(String... ids) {
        return clearIds().addIds(ids);
    }

    @Override
    public NInstallCmd addId(NId id) {
        if (id == null) {
            throw new NNotFoundException(id);
        } else {
            ids.put(id, getStrategy());
        }
        return this;
    }

    @Override
    public NInstallCmd addIds(String... ids) {
        for (String id : ids) {
            addId(id);
        }
        return this;
    }

    @Override
    public NInstallCmd addIds(NId... ids) {
        for (NId id : ids) {
            addId(id);
        }
        return this;
    }

    @Override
    public NInstallCmd removeId(NId id) {
        if (id != null) {
            this.ids.remove(id);
        }
        return this;
    }

    @Override
    public NInstallCmd removeId(String id) {
        if (id != null) {
            this.ids.remove(NId.get(id).get());
        }
        return this;
    }

    @Override
    public NInstallCmd clearIds() {
        this.ids.clear();
        return this;
    }

    @Override
    public NInstallCmd clearArgs() {
        this.args = null;
        return this;
    }

    @Override
    public List<String> getArgs() {
        return NCoreCollectionUtils.unmodifiableList(args);
    }

    @Override
    public NInstallCmd addArg(String arg) {
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
    public NInstallCmd addArgs(String... args) {
        return addArgs(args == null ? null : Arrays.asList(args));
    }

    @Override
    public NInstallCmd addConditionalArgs(Predicate<NDefinition> definition, String... args) {
        conditionalArguments.add(new ConditionalArguments(definition, Arrays.asList(args)));
        return this;
    }

    @Override
    public NInstallCmd addArgs(Collection<String> args) {
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
        return NCoreCollectionUtils.unmodifiableList(ids == null ? null : ids.keySet());
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
    public NInstallCmd setCompanions(boolean value) {
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
    public NInstallCmd setInstalled(boolean value) {
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
    public NInstallCmd setDefaultVersion(boolean defaultVersion) {
        this.defaultVersion = defaultVersion;
        return this;
    }

    @Override
    public NInstallCmd defaultVersion(boolean defaultVersion) {
        return setDefaultVersion(defaultVersion);
    }

    @Override
    public NInstallCmd defaultVersion() {
        return defaultVersion(true);
    }

    @Override
    public NInstallCmd companions(boolean value) {
        return setCompanions(value);
    }

    @Override
    public NInstallCmd companions() {
        return companions(true);
    }

    @Override
    public NInstallCmd setStrategy(NInstallStrategy value) {
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
    public boolean configureFirst(NCmdLine cmdLine) {
        NArg aa = cmdLine.peek().get();
        if (aa == null) {
            return false;
        }
        boolean enabled = aa.isActive();
        switch (aa.key()) {
            case "-c":
            case "--companions": {
                cmdLine.withNextFlag((v, a) -> this.setCompanions(v));
                return true;
            }
            case "-i":
            case "--installed": {
                cmdLine.withNextFlag((v, a) -> this.setInstalled(v));
                return true;
            }
            case "-s":
            case "--strategy": {
                String val = cmdLine.nextEntry().flatMap(NLiteral::asString).get();
                if (enabled) {
                    this.setStrategy(CoreEnumUtils.parseEnumString(val, NInstallStrategy.class, false));
                }
                return true;
            }
            case "--reinstall": {
                cmdLine.skip();
                if (enabled) {
                    this.setStrategy(NInstallStrategy.REINSTALL);
                }
                return true;
            }
            case "--require": {
                cmdLine.skip();
                if (enabled) {
                    this.setStrategy(NInstallStrategy.REQUIRE);
                }
                return true;
            }
            case "--repair": {
                cmdLine.skip();
                if (enabled) {
                    this.setStrategy(NInstallStrategy.REPAIR);
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
