package net.thevpc.nuts.runtime.standalone.elem;

import net.thevpc.nuts.elem.NElementPath;
import net.thevpc.nuts.elem.NElementStep;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NOptional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class DefaultNElementPath implements NElementPath {
    private NElementPath parent;
    private NElementStep step;
    private int size;

    public static NElementPath ROOT = new DefaultNElementPath();

    public DefaultNElementPath() {
    }

    public DefaultNElementPath(NElementPath parent, NElementStep step) {
        this.step = NAssert.requireNamedNonNull(step, "step");
        if (parent == null || parent.isRoot()) {
            this.parent = null;
            this.size = 1;
        } else {
            this.parent = parent;
            this.size = parent.size() + 1;
        }
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public NElementPath resolve(NElementStep step) {
        if (step == null) {
            return this;
        }
        return new DefaultNElementPath(this, step);
    }


    @Override
    public NElementStep step() {
        return step;
    }

    @Override
    public NOptional<NElementPath> parent() {
        return NOptional.ofNamed(parent, "parent");
    }

    @Override
    public boolean isRoot() {
        return size == 0;
    }

    @Override
    public String toString() {
        if (parent == null) {
            if (step == null) {
                return "/";
            }
            return "/" + step;
        }
        String s = parent.toString();
        if (!s.endsWith("/")) {
            s += "/";
        }
        return s + step.toString();
    }

    @Override
    public List<NElementStep> steps() {
        if (isRoot()) {
            return Collections.emptyList();
        }
        NElementStep[] result = new NElementStep[size];
        NElementPath current = this;
        for (int i = size - 1; i >= 0; i--) {
            result[i] = current.step();
            current = current.parent().orNull();

            // Safety check: if the tree is malformed or size is wrong
            if (current == null && i > 0) {
                // This would happen if size > 1 but parent is null
                break;
            }
        }
        return Arrays.asList(result);
    }
}
