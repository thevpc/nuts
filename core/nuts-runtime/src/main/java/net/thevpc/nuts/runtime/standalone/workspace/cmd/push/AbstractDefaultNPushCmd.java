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
package net.thevpc.nuts.runtime.standalone.workspace.cmd.push;

import net.thevpc.nuts.*;
import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.util.NCoreCollectionUtils;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.NWorkspaceCmdBase;
import net.thevpc.nuts.spi.NSupportLevelContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author thevpc
 */
public abstract class AbstractDefaultNPushCmd extends NWorkspaceCmdBase<NPushCmd> implements NPushCmd {

    protected boolean offline = false;
    protected List<String> args;
    protected final List<NId> ids = new ArrayList<>();
    protected List<NId> lockedIds;
    protected String repository;

    public AbstractDefaultNPushCmd(NWorkspace workspace) {
        super("push");
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return NConstants.Support.DEFAULT_SUPPORT;
    }

    @Override
    public NPushCmd addId(String id) {
        return addId(id == null ? null : NId.get(id).get());
    }

    @Override
    public NPushCmd addLockedId(String id) {
        return addLockedId(id == null ? null : NId.get(id).get());
    }

    @Override
    public NPushCmd addId(NId id) {
        if (id == null) {
            throw new NNotFoundException(id);
        } else {
            ids.add(id);
        }
        return this;
    }

    @Override
    public NPushCmd removeId(NId id) {
        if (id != null) {
            ids.remove(id);
        }
        return this;
    }

    @Override
    public NPushCmd removeId(String id) {
        if (id != null) {
            ids.remove(NId.get(id).get());
        }
        return this;
    }

    @Override
    public NPushCmd removeLockedId(NId id) {
        if (id != null) {
            if (lockedIds != null) {
                lockedIds.remove(id);
            }
        }
        return this;
    }

    @Override
    public NPushCmd removeLockedId(String id) {
        if (id != null) {
            if (lockedIds != null) {
                lockedIds.remove(NId.get(id).get());
            }
        }
        return this;
    }

    @Override
    public NPushCmd addLockedId(NId id) {
        if (id == null) {
            throw new NNotFoundException(id);
        } else {
            if (lockedIds == null) {
                lockedIds = new ArrayList<>();
            }
            lockedIds.add(id);
        }
        return this;
    }

    @Override
    public NPushCmd addIds(String... ids) {
        for (String id : ids) {
            addId(id);
        }
        return this;
    }

    @Override
    public NPushCmd addIds(NId... ids) {
        for (NId id : ids) {
            addId(id);
        }
        return this;
    }

    @Override
    public NPushCmd addLockedIds(String... values) {
        for (String id : values) {
            addLockedId(id);
        }
        return this;
    }

    @Override
    public NPushCmd addLockedIds(NId... values) {
        for (NId id : values) {
            addLockedId(id);
        }
        return this;
    }

    @Override
    public List<String> getArgs() {
        return NCoreCollectionUtils.unmodifiableList(args);
    }

    @Override
    public NPushCmd addArg(String arg) {
        if(arg!=null) {
            if (this.args == null) {
                this.args = new ArrayList<>();
            }
            this.args.add(arg);
        }
        return this;
    }

    @Override
    public NPushCmd addArgs(String... args) {
        return addArgs(args == null ? null : Arrays.asList(args));
    }

    @Override
    public NPushCmd addArgs(Collection<String> args) {
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
        return NCoreCollectionUtils.unmodifiableList(ids);
    }

    @Override
    public List<NId> getLockedIds() {
        return NCoreCollectionUtils.unmodifiableList(lockedIds);
    }

    @Override
    public boolean isOffline() {
        return offline;
    }

    @Override
    public NPushCmd setOffline(boolean offline) {
        this.offline = offline;
        return this;
    }

    @Override
    public String getRepository() {
        return repository;
    }

    @Override
    public NPushCmd setRepository(String repository) {
        this.repository = repository;
        return this;
    }

    @Override
    public NPushCmd args(Collection<String> args) {
        return addArgs(args);
    }

    @Override
    public NPushCmd clearArgs() {
        this.args = null;
        return this;
    }

    @Override
    public NPushCmd clearIds() {
        this.ids.clear();
        return this;
    }

    @Override
    public NPushCmd clearLockedIds() {
        lockedIds = null;
        return this;
    }

    @Override
    public boolean configureFirst(NCmdLine cmdLine) {
        NArg a = cmdLine.peek().get();
        if (a == null) {
            return false;
        }
        switch (a.key()) {
            case "-o":
            case "--offline": {
                cmdLine.withNextFlag((v) -> setOffline(v.booleanValue()));
                return true;
            }
            case "-x":
            case "--freeze": {
                cmdLine.withNextEntry((v) -> {
                    for (String id : v.stringValue().split(",")) {
                        addLockedId(id);
                    }
                });
                return true;
            }
            case "-r":
            case "-repository":
            case "--from": {
                cmdLine.withNextEntry((v) -> setRepository(v.stringValue()));
                return true;
            }
            case "-g":
            case "--args": {
                return cmdLine.selector().withNextTrueFlag((v) -> {
                    this.addArgs(cmdLine.toStringArray());
                    cmdLine.skipAll();
                }).anyMatch();
            }
            default: {
                if (super.configureFirst(cmdLine)) {
                    return true;
                }
                if (a.isOption()) {
                    cmdLine.throwUnexpectedArgument();
                } else {
                    cmdLine.skip();
                    addId(a.asString().get());
                    return true;
                }
            }
        }
        return false;
    }
}
