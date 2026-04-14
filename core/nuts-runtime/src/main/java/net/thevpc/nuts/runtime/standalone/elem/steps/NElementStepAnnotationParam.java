package net.thevpc.nuts.runtime.standalone.elem.steps;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NOptional;

import java.util.List;
import java.util.Objects;

public class NElementStepAnnotationParam implements NElementStep {
    private int annotationIndex;
    private NElement paramIndex;

    public NElementStepAnnotationParam(int annotationIndex, int paramIndex) {
        this.annotationIndex = annotationIndex;
        this.paramIndex = NElement.ofInt(paramIndex);
    }

    public NElementStepAnnotationParam(int annotationIndex, String name) {
        this.annotationIndex = annotationIndex;
        this.paramIndex = NElement.ofString(name);
    }

    public int annotationIndex() {
        return annotationIndex;
    }

    public NElement paramIndex() {
        return paramIndex;
    }

    @Override
    public NOptional<NElement> step(NElement element) {
        if (element != null) {
            List<NElementAnnotation> annotations = element.annotations();
            if (annotationIndex >= 0 && annotationIndex < annotations.size()) {
                NElementAnnotation a = annotations.get(annotationIndex);
                if (paramIndex.type() == NElementType.INT) {
                    return a.param(paramIndex.asIntValue().get());
                } else {
                    return a.param(paramIndex.asStringValue().get());
                }
            }
        }
        return NOptional.ofNamedEmpty(NMsg.ofC("Annotation at %s, param %s for %s", annotationIndex, paramIndex, element));
    }

    @Override
    public NElement toElement() {
        return NElement.ofNamedUplet("Annotation", NElement.ofInt(annotationIndex), paramIndex);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        NElementStepAnnotationParam that = (NElementStepAnnotationParam) o;
        return annotationIndex == that.annotationIndex && Objects.equals(paramIndex, that.paramIndex);
    }

    @Override
    public int hashCode() {
        return Objects.hash(annotationIndex, paramIndex);
    }

    @Override
    public String toString() {
        return toElement().toString();
    }
}
