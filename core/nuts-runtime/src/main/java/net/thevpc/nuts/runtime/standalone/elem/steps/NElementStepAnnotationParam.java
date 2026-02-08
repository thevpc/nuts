package net.thevpc.nuts.runtime.standalone.elem.steps;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NOptional;

import java.util.List;
import java.util.Objects;

public class NElementStepAnnotationParam implements NElementStep {
    private int annotationIndex;
    private NElement value;

    public NElementStepAnnotationParam(int annotationIndex, int index) {
        this.annotationIndex = annotationIndex;
        this.value = NElement.ofInt(index);
    }

    public NElementStepAnnotationParam(int annotationIndex, String name) {
        this.annotationIndex = annotationIndex;
        this.value = NElement.ofString(name);
    }

    @Override
    public NOptional<NElement> step(NElement element) {
        if (element != null) {
            List<NElementAnnotation> annotations = element.annotations();
            if (annotationIndex >= 0 && annotationIndex < annotations.size()) {
                NElementAnnotation a = annotations.get(annotationIndex);
                if (value.type() == NElementType.INT) {
                    return a.param(value.asIntValue().get());
                } else {
                    return a.param(value.asStringValue().get());
                }
            }
        }
        return NOptional.ofNamedEmpty(NMsg.ofC("Annotation at %s, param %s for %s", annotationIndex, value, element));
    }

    @Override
    public NElement toElement() {
        return NElement.ofNamedUplet("Annotation", NElement.ofInt(annotationIndex), value);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        NElementStepAnnotationParam that = (NElementStepAnnotationParam) o;
        return annotationIndex == that.annotationIndex && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(annotationIndex, value);
    }

    @Override
    public String toString() {
        return toElement().toString();
    }
}
