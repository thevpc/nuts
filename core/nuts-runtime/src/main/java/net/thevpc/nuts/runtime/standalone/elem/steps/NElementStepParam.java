package net.thevpc.nuts.runtime.standalone.elem.steps;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementStep;
import net.thevpc.nuts.elem.NElementType;
import net.thevpc.nuts.elem.NListItemElement;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NOptional;

import java.util.Objects;

public class NElementStepParam implements NElementStep {
    private NElement value;

    public NElementStepParam(int index) {
        this.value = NElement.ofInt(index);
    }

    public NElementStepParam(String name) {
        this.value = NElement.ofString(name);
    }

    @Override
    public NOptional<NElement> step(NElement element) {
        if (element != null) {
            if (element.isParametrizedContainer()) {
                if (value.type() == NElementType.INT) {
                    return element.asListContainer().get().get(value.asIntValue().get());
                } else {
                    return element.asListContainer().get().get(value.asStringValue().get());
                }
            }
            if (element.isUplet()) {
                if (value.type() == NElementType.INT) {
                    return element.asUplet().get().get(value.asIntValue().get());
                } else {
                    return element.asUplet().get().get(value.asStringValue().get());
                }
            }
        }
        return NOptional.ofNamedEmpty(NMsg.ofC("Param %s of %s", value, element));
    }

    @Override
    public NElement toElement() {
        return NElement.ofNamedUplet("Param", value);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        NElementStepParam that = (NElementStepParam) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    @Override
    public String toString() {
        return toElement().toString();
    }
}
