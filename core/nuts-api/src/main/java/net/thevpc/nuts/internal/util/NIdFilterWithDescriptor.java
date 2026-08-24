package net.thevpc.nuts.internal.util;

import net.thevpc.nuts.artifact.NIdFilter;
import net.thevpc.nuts.elem.NDescribables;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.spi.base.NIdFilterDelegate;
import net.thevpc.nuts.util.NFilter;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * NIdFilterWithDescriptor class.
 *
 * @author thevpc
 * @since 0.8.0
 */
public class NIdFilterWithDescriptor extends NIdFilterDelegate {
    private NIdFilter base;
    private Supplier<NElement> description;

    /**
     * N id filter with descriptor.
     *
     * @param base base
     * @param description description
     * @return n id filter with descriptor result
     */
    public NIdFilterWithDescriptor(NIdFilter base, Supplier<NElement> description) {
      /**
       * Super.
       */
        super();
        this.base = base;
        this.description = description;
    }

    @Override
    public NIdFilter baseNIdFilter() {
        return base;
    }

    @Override
    public NFilter withDescription(Supplier<NElement> description) {
        this.description = description;
        return this;
    }

    @Override
    public NElement describe() {
        return NDescribables.safeDescribeOfBase(description, base);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        NIdFilterWithDescriptor that = (NIdFilterWithDescriptor) o;
        return Objects.equals(base, that.base);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), base);
    }
}
