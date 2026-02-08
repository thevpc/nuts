package net.thevpc.nuts.runtime.standalone.elem.steps;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NOptional;

import java.util.Objects;

public class NElementStepSubList implements NElementStep {
    private int value;

    public NElementStepSubList(int index) {
        this.value = index;
    }

    @Override
    public NOptional<NElement> step(NElement element) {
        if (element != null) {
            if (element.isList()) {
                return element.asList().get().get(value).flatMap(x -> x.subList().instanceOf(NElement.class));
            }
        }
        return NOptional.ofNamedEmpty(NMsg.ofC("SubList  %s of %s", value, element));
    }

    @Override
    public NElement toElement() {
        return NElement.ofNamedUplet("SubList", NElement.ofInt(value));
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        NElementStepSubList that = (NElementStepSubList) o;
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
