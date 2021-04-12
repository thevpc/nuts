package net.thevpc.nuts.runtime.core.log;

import net.thevpc.nuts.*;
import java.util.logging.Handler;
import java.util.logging.Level;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;

public class DefaultNutsLogManager implements NutsLogManager {

    private DefaultNutsLogModel model;
    private NutsSession session;

    public DefaultNutsLogManager(DefaultNutsLogModel model) {
        this.model = model;
    }

    @Override
    public Handler[] getHandlers() {
        checkSession();
        return model.getHandlers();
    }

    @Override
    public NutsLogManager removeHandler(Handler handler) {
        checkSession();
        model.removeHandler(handler);
        return this;
    }

    @Override
    public NutsLogManager addHandler(Handler handler) {
        checkSession();
        model.addHandler(handler);
        return this;
    }

    @Override
    public Handler getTermHandler() {
        checkSession();
        return model.getTermHandler();
    }

    @Override
    public Handler getFileHandler() {
        checkSession();
        return model.getFileHandler();
    }

    @Override
    public NutsLogger of(String name) {
        checkSession();
        return model.of(name,getSession());
    }

    @Override
    public NutsLogger of(Class clazz) {
        checkSession();
        return model.of(clazz,getSession());
    }

    @Override
    public Level getTermLevel() {
        checkSession();
        return model.getTermLevel();
    }

    @Override
    public NutsLogManager setTermLevel(Level level) {
        checkSession();
        model.setTermLevel(level, session);
        return this;
    }

    @Override
    public Level getFileLevel() {
        checkSession();
        return model.getFileLevel();
    }

    @Override
    public NutsLogManager setFileLevel(Level level) {
        checkSession();
        model.setFileLevel(level, session);
        return this;
    }

    private void checkSession() {
        NutsWorkspaceUtils.checkSession(model.getWorkspace(), session);
    }

    @Override
    public NutsSession getSession() {
        return this.session;
    }

    @Override
    public NutsLogManager setSession(NutsSession session) {
        this.session = session;
        return this;
    }

    public DefaultNutsLogModel getModel() {
        return model;
    }
    

}
