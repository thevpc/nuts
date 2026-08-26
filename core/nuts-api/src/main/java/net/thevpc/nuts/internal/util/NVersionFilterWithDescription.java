package net.thevpc.nuts.internal.util;

import net.thevpc.nuts.artifact.NVersionFilter;
import net.thevpc.nuts.elem.NDescribables;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.spi.base.NVersionFilterDelegate;
import net.thevpc.nuts.util.NFilter;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * NVersionFilterWithDescription class.
 *
 * @author thevpc
 * @since 0.8.0
 */
public class NVersionFilterWithDescription extends NVersionFilterDelegate {
    private NVersionFilter baseVersionFilter;
    private Supplier<NElement> description;

    /**
     * N version filter with description.
     *
     * @param baseVersionFilter base version filter
     * @param description description
     * @return n version filter with description result
     */
    public NVersionFilterWithDescription(NVersionFilter baseVersionFilter, Supplier<NElement> description) {
      /**
       * Super.
       */
        super();
        this.baseVersionFilter = baseVersionFilter;
        this.description = description;
    }

    @Override
    public NVersionFilter baseVersionFilter() {
        return baseVersionFilter;
    }

    @Override
    public NFilter withDescription(Supplier<NElement> description) {
        this.description=description;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        NVersionFilterWithDescription that = (NVersionFilterWithDescription) o;
        return Objects.equals(baseVersionFilter, that.baseVersionFilter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), baseVersionFilter);
    }

    @Override
    public NElement describe() {
        return NDescribables.safeDescribeOfBase(description, baseVersionFilter);
    }
}
