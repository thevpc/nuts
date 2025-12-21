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

import net.thevpc.nuts.artifact.NArtifactNotFoundException;
import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.command.NPush;
import net.thevpc.nuts.core.NWorkspace;
import net.thevpc.nuts.util.NCollections;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.NWorkspaceCmdBase;
import net.thevpc.nuts.util.NScore;
import net.thevpc.nuts.util.NScorable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author thevpc
 */
@NScore(fixed = NScorable.DEFAULT_SCORE)
public abstract class AbstractDefaultNPush extends NWorkspaceCmdBase<NPush> implements NPush {

    protected boolean offline = false;
    protected List<String> args;
    protected final List<NId> ids = new ArrayList<>();
    protected List<NId> lockedIds;
    protected String repository;

    public AbstractDefaultNPush(NWorkspace workspace) {
        super("push");
    }

    @Override
    public NPush addId(String id) {
        return addId(id == null ? null : NId.get(id).get());
    }

    @Override
    public NPush addLockedId(String id) {
        return addLockedId(id == null ? null : NId.get(id).get());
    }

    @Override
    public NPush addId(NId id) {
        if (id == null) {
            throw new NArtifactNotFoundException(id);
        } else {
            ids.add(id);
        }
        return this;
    }

    @Override
    public NPush removeId(NId id) {
        if (id != null) {
            ids.remove(id);
        }
        return this;
    }

    @Override
    public NPush removeId(String id) {
        if (id != null) {
            ids.remove(NId.get(id).get());
        }
        return this;
    }

    @Override
    public NPush removeLockedId(NId id) {
        if (id != null) {
            if (lockedIds != null) {
                lockedIds.remove(id);
            }
        }
        return this;
    }

    @Override
    public NPush removeLockedId(String id) {
        if (id != null) {
            if (lockedIds != null) {
                lockedIds.remove(NId.get(id).get());
            }
        }
        return this;
    }

    @Override
    public NPush addLockedId(NId id) {
        if (id == null) {
            throw new NArtifactNotFoundException(id);
        } else {
            if (lockedIds == null) {
                lockedIds = new ArrayList<>();
            }
            lockedIds.add(id);
        }
        return this;
    }

    @Override
    public NPush addIds(String... ids) {
        for (String id : ids) {
            addId(id);
        }
        return this;
    }

    @Override
    public NPush addIds(NId... ids) {
        for (NId id : ids) {
            addId(id);
        }
        return this;
    }

    @Override
    public NPush addLockedIds(String... values) {
        for (String id : values) {
            addLockedId(id);
        }
        return this;
    }

    @Override
    public NPush addLockedIds(NId... values) {
        for (NId id : values) {
            addLockedId(id);
        }
        return this;
    }

    @Override
    public List<String> getArgs() {
        return NCollections.unmodifiableList(args);
    }

    @Override
    public NPush addArg(String arg) {
        if(arg!=null) {
            if (this.args == null) {
                this.args = new ArrayList<>();
            }
            this.args.add(arg);
        }
        return this;
    }

    @Override
    public NPush addArgs(String... args) {
        return addArgs(args == null ? null : Arrays.asList(args));
    }

    @Override
    public NPush addArgs(Collection<String> args) {
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
        return NCollections.unmodifiableList(ids);
    }

    @Override
    public List<NId> getLockedIds() {
        return NCollections.unmodifiableList(lockedIds);
    }

    @Override
    public boolean isOffline() {
        return offline;
    }

    @Override
    public NPush setOffline(boolean offline) {
        this.offline = offline;
        return this;
    }

    @Override
    public String getRepository() {
        return repository;
    }

    @Override
    public NPush setRepository(String repository) {
        this.repository = repository;
        return this;
    }

    @Override
    public NPush args(Collection<String> args) {
        return addArgs(args);
    }

    @Override
    public NPush clearArgs() {
        this.args = null;
        return this;
    }

    @Override
    public NPush clearIds() {
        this.ids.clear();
        return this;
    }

    @Override
    public NPush clearLockedIds() {
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
                return cmdLine.matcher().matchFlag((v) -> setOffline(v.booleanValue())).anyMatch();
            }
            case "-x":
            case "--freeze": {
                return cmdLine.matcher().matchEntry((v) -> {
                    for (String id : v.stringValue().split(",")) {
                        addLockedId(id);
                    }
                }).anyMatch();
            }
            case "-r":
            case "-repository":
            case "--from": {
                return cmdLine.matcher().matchEntry((v) -> setRepository(v.stringValue())).anyMatch();
            }
            case "-g":
            case "--args": {
                return cmdLine.matcher().matchTrueFlag((v) -> {
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
