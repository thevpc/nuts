package net.thevpc.nuts.runtime.standalone.format.elem;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementAnnotation;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NStringBuilder;
import net.thevpc.nuts.util.NStringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class NElementAnnotationImpl implements NElementAnnotation {
    private final String name;
    private final NElement[] params;

    public NElementAnnotationImpl(String name, NElement[] params) {
        this.name = name;
        this.params = params;
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
        NElementToStringHelper.appendUplet("@" + (NStringUtils.trim(name)), params == null ? null : Arrays.asList(params), compact, sb);
        return sb.toString();
    }

}
