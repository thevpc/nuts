package net.thevpc.nuts.spi.base;

import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.artifact.NIdFilter;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.util.NFilter;
import net.thevpc.nuts.util.NFilterOp;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * NIdFilterDelegate class.
 *
 * @author thevpc
 * @since 0.8.0
 */
public abstract class NIdFilterDelegate extends AbstractIdFilter {
    /**
     * Base n id filter.
     *
     * @return base n id filter result
     */
    public abstract NIdFilter baseNIdFilter();

    /**
     * N id filter delegate.
     *
     * @return n id filter delegate result
     */
    public NIdFilterDelegate() {
      /**
       * Super.
       *
       * @param NFilterOp.CUSTOM n filter op.custom
       */
        super(NFilterOp.CUSTOM);
    }

    @Override
    public boolean acceptId(NId id) {
        /**
         * Base n id filter.
         *
         * @param ).acceptId(id ).accept id(id
         * @return base n id filter result
         */
        return baseNIdFilter().acceptId(id);
    }

    @Override
    public NFilterOp filterOp() {
        /**
         * Base n id filter.
         *
         * @param ).filterOp( ).filter op(
         * @return base n id filter result
         */
        return baseNIdFilter().filterOp();
    }

    @Override
    public Class<? extends NFilter> filterType() {
        /**
         * Base n id filter.
         *
         * @param ).filterType( ).filter type(
         * @return base n id filter result
         */
        return baseNIdFilter().filterType();
    }

    @Override
    public NIdFilter simplify() {
      /**
       * Return.
       *
       * @param baseNIdFilter().simplify( base n id filter().simplify(
       */
        return (NIdFilter) baseNIdFilter().simplify();
    }

    @Override
    public <T extends NFilter> NFilter simplify(Class<T> type) {
        /**
         * Base n id filter.
         *
         * @param ).simplify( ).simplify(
         * @return base n id filter result
         */
        return baseNIdFilter().simplify();
    }

    @Override
    public <T extends NFilter> T to(Class<T> type) {
        /**
         * Base n id filter.
         *
         * @param ).to(type ).to(type
         * @return base n id filter result
         */
        return baseNIdFilter().to(type);
    }

    @Override
    public List<NFilter> subFilters() {
        /**
         * Base n id filter.
         *
         * @param ).subFilters( ).sub filters(
         * @return base n id filter result
         */
        return baseNIdFilter().subFilters();
    }

    @Override
    public NElement describe() {
        /**
         * Base n id filter.
         *
         * @param ).describe( ).describe(
         * @return base n id filter result
         */
        return baseNIdFilter().describe();
    }

    @Override
    public NFilter withDescription(Supplier<NElement> description) {
        /**
         * Base n id filter.
         *
         * @param ).withDescription(description ).with description(description
         * @return base n id filter result
         */
        return baseNIdFilter().withDescription(description);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NIdFilterDelegate that = (NIdFilterDelegate) o;
        return Objects.equals(baseNIdFilter(), that.baseNIdFilter());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(baseNIdFilter());
    }
}
