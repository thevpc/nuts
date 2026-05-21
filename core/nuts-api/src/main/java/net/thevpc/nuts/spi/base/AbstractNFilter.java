package net.thevpc.nuts.spi.base;

import net.thevpc.nuts.artifact.NFilters;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.util.NFilter;
import net.thevpc.nuts.util.NFilterOp;

import java.util.Collections;
import java.util.List;

public abstract class AbstractNFilter implements NFilter {

    private NFilterOp op;

    public AbstractNFilter(NFilterOp op) {
        this.op = op;
    }

    @Override
    public NFilterOp filterOp() {
        return op;
    }

    @Override
    public List<NFilter> subFilters() {
        return Collections.emptyList();
    }

    @Override
    public NFilter or(NFilter other) {
        if (other == null) {
            return this;
        }
        switch (other.filterOp()) {
            case TRUE:
                return other;
            case FALSE:
                return this;
        }
        switch (filterOp()) {
            case TRUE:
                return other;
            case FALSE:
                return other;
        }
        return NFilters.of().any(this, other);
    }

    @Override
    public NFilter and(NFilter other) {
        if (other == null) {
            return this;
        }
        switch (other.filterOp()) {
            case TRUE:
                return this;
            case FALSE:
                return other;//false
        }
        switch (filterOp()) {
            case TRUE:
                return other;
            case FALSE:
                return this; //false
        }
        return NFilters.of().all(this, other);
    }

    @Override
    public NFilter neg() {
        return NFilters.of().not(this);
    }

    @Override
    public <T extends NFilter> T to(Class<T> type) {
        return NFilters.of().to(type, this);
    }

    @Override
    public Class<? extends NFilter> filterType() {
        return NFilters.of().detectType(this);
    }

    @Override
    public <T extends NFilter> NFilter simplify(Class<T> type) {
        return simplify().to(type);
    }

    @Override
    public NElement describe() {
        return NElement.ofString(toString());
    }
}
