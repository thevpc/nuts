package net.thevpc.nuts.runtime.standalone.elem;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementAnnotation;
import net.thevpc.nuts.util.NBlankable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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
    public List<NElement> children() {
        return params == null ? Collections.emptyList() : Arrays.asList(params);
    }

    @Override
    public boolean isBlank() {
        return NBlankable.isBlank(name) && (params == null || params.length == 0);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("@");
        if (isNamed()) {
            sb.append(name);
        }
        if (isParametrized()) {
            sb.append("(").append(children().stream().map(x -> x.toString()).collect(Collectors.joining(", "))).append(")");
        }
        return sb.toString();
    }
}
