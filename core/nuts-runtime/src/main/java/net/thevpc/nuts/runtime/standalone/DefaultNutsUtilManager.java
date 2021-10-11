package net.thevpc.nuts.runtime.standalone;

import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsStream;
import net.thevpc.nuts.NutsUtilManager;
import net.thevpc.nuts.NutsVal;
import net.thevpc.nuts.runtime.standalone.util.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public class DefaultNutsUtilManager implements NutsUtilManager {

    private DefaultNutsUtilModel model;
    private NutsSession session;

    public DefaultNutsUtilManager(DefaultNutsUtilModel model) {
        this.model = model;
    }

    public NutsSession getSession() {
        return session;
    }

    public NutsUtilManager setSession(NutsSession session) {
        this.session = NutsWorkspaceUtils.bindSession(model.getWorkspace(), session);
        return this;
    }

    @Override
    public NutsVal valOf(Object str) {
        return new DefaultNutsVal(str);
    }

    protected void checkSession() {
        NutsWorkspaceUtils.checkSession(model.getWorkspace(), getSession());
    }

    @Override
    public <T> NutsStream<T> streamOf(T[] str, String name) {
        if(str==null){
            return new NutsEmptyStream<T>(getSession(),name);
        }
        return new NutsListStream<T>(getSession(),name, Arrays.asList(str));
    }

    @Override
    public <T> NutsStream<T> streamOf(Iterable<T> str, String name) {
        if(str==null){
            return new NutsEmptyStream<T>(getSession(),name);
        }
        if(str instanceof List){
            return new NutsListStream<T>(getSession(),name, (List<T>) str);
        }
        if(str instanceof Collection){
            return new NutsCollectionStream<T>(getSession(),name, (Collection<T>) str);
        }
        return new NutsIterableStream<>(getSession(),name, (Iterable<T>) str);
    }

    @Override
    public <T> NutsStream<T> streamOf(Stream<T> str, String name) {
        return new NutsJavaStream<>(getSession(),name, str);
    }
}
