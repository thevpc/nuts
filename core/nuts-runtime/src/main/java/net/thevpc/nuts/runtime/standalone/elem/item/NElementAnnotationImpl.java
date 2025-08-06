package net.thevpc.nuts.runtime.standalone.elem.item;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementAnnotation;
import net.thevpc.nuts.runtime.standalone.elem.NElementToStringHelper;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NStringBuilder;
import net.thevpc.nuts.util.NStringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class NElementAnnotationImpl implements NElementAnnotation {
    private final String name;
    private final NElement[] params;

    public NElementAnnotationImpl(String name, NElement[] params) {
        this.name = name;
        this.params = params;
    }

    @Override
    public boolean isCustomTree() {
        if(params!=null){
            for (NElement param : params) {
                if(param.isCustomTree()){
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isParametrized() {
        return params != null;
    }

    public boolean isNamed() {
        return !name.isEmpty();
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public int size() {
        return params == null ? 0 : params.length;
    }

    @Override
    public NElement param(int index) {
        return params[index];
    }

    @Override
    public List<NElement> params() {
        return params == null ? null : Arrays.asList(params);
    }

    @Override
    public boolean isBlank() {
        return NBlankable.isBlank(name) && (params == null || params.length == 0);
    }

    public String toString() {
        return toString(false);
    }

    @Override
    public String toString(boolean compact) {
        NStringBuilder sb = new NStringBuilder();
        NElementToStringHelper.appendUplet("@" + (NStringUtils.trim(name)), null, compact, sb);
        if(params!=null){
            sb.append("(");
            NElementToStringHelper.appendChildren(params(), compact, new NElementToStringHelper.SemiCompactInfo().setMaxChildren(10).setMaxLineSize(120), sb);
            sb.append(")");
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        NElementAnnotationImpl that = (NElementAnnotationImpl) object;
        return Objects.equals(name, that.name) && Objects.deepEquals(params, that.params);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, Arrays.hashCode(params));
    }
}
