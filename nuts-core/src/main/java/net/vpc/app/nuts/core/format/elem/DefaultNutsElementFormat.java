package net.vpc.app.nuts.core.format.elem;

import java.util.HashMap;
import java.util.Map;
import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.util.NutsConfigurableHelper;

public class DefaultNutsElementFormat implements NutsElementFormat, NutsElementFactoryContext {

    private final NutsWorkspace ws;
    private final NutsElementFactoryService nvalueFactory;
    private NutsElementFactory fallback;
    private final Map<String, Object> properties = new HashMap<>();

    public DefaultNutsElementFormat(NutsWorkspace ws) {
        this.ws = ws;
        nvalueFactory = new DefaultNutsElementFactoryService(ws);
    }

    @Override
    public NutsWorkspace getWorkspace() {
        return ws;
    }

    @Override
    public NutsElement toElement(Object object) {
        return nvalueFactory.create(object, this);
    }

    @Override
    public NutsElementFactory getFallback() {
        return fallback;
    }

    @Override
    public Map<String, Object> getProperties() {
        return properties;
    }

    @Override
    public void setFallback(NutsElementFactory fallback) {
        this.fallback = fallback;
    }

    @Override
    public final NutsElementFormat configure(boolean skipUnsupported, String... args) {
        return NutsConfigurableHelper.configure(this, ws, skipUnsupported, args, "nuts-element-format");
    }

    @Override
    public final boolean configure(boolean skipUnsupported, NutsCommandLine commandLine) {
        return NutsConfigurableHelper.configure(this, ws, skipUnsupported, commandLine);
    }

    @Override
    public boolean configureFirst(NutsCommandLine cmdLine) {
        NutsArgument a = cmdLine.peek();
        if (a == null) {
            return false;
        }
        switch (a.getStringKey()) {
            //
        }
        return false;
    }
}
