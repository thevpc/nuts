package net.thevpc.nuts.runtime.standalone.definition.filter;

import net.thevpc.nuts.artifact.NDefinitionFilter;
import net.thevpc.nuts.elem.NDescribables;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.util.NFilter;

import java.util.Objects;
import java.util.function.Supplier;

public class NDefinitionFilterDelegateWithDescription extends NDefinitionFilterDelegate {
    private NDefinitionFilter base;
    private Supplier<NElement> description;

    public NDefinitionFilterDelegateWithDescription(NDefinitionFilter base, Supplier<NElement> description) {
        super();
        this.base = base;
        this.description = description;
    }

    @Override
    public NDefinitionFilter baseDefinitionFilter() {
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
        NDefinitionFilterDelegateWithDescription that = (NDefinitionFilterDelegateWithDescription) o;
        return Objects.equals(base, that.base);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), base);
    }
}
