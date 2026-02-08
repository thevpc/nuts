package net.thevpc.nuts.runtime.standalone.elem;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementNavigator;
import net.thevpc.nuts.elem.NElementPath;
import net.thevpc.nuts.elem.NElementStep;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NOptional;

public class DefaultNElementNavigator implements NElementNavigator {
    private NElementNavigator parent;
    private NElement element;
    private NElementPath path;

    public DefaultNElementNavigator(NElementNavigator parent, NElement element, NElementPath path) {
        this.parent = parent;
        this.element = element;
        this.path = path;
    }

    @Override
    public NOptional<NElementNavigator> parent() {
        return NOptional.of(parent);
    }

    @Override
    public NElement element() {
        return element;
    }

    @Override
    public NElementPath path() {
        return path;
    }

    @Override
    public NOptional<NElementNavigator> resolve(NElementStep step) {
        if (step == null) {
            return NOptional.of(this);
        }
        NElementPath p2 = path.resolve(step);
        NOptional<NElement> n = step.step(element);
        if (n.isPresent()) {
            return NOptional.of(new DefaultNElementNavigator(this, n.get(), p2));
        }
        return NOptional.ofNamedEmpty(NMsg.ofC("step %s for %s at path %s", element, parent));
    }

    @Override
    public NOptional<NElementNavigator> resolve(NElementPath path) {
        if (path == null) {
            return NOptional.of(this);
        }
        NElementNavigator c = this;
        for (NElementStep step : path.steps()) {
            NOptional<NElementNavigator> e = c.resolve(step);
            if (!e.isPresent()) {
                return e;
            }
            c = e.get();
        }
        return NOptional.of(c);
    }
}
