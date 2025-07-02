package net.thevpc.nuts.runtime.standalone.definition.filter;

import net.thevpc.nuts.NDefinition;
import net.thevpc.nuts.NDefinitionFilter;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.util.NFilter;
import net.thevpc.nuts.util.NFilterOp;

import java.util.Objects;
import java.util.function.Supplier;

public abstract class NDefinitionFilterDelegate extends AbstractDefinitionFilter {
    public abstract NDefinitionFilter baseDefinitionFilter();

    public NDefinitionFilterDelegate() {
        super(NFilterOp.CUSTOM);
    }

    @Override
    public boolean acceptDefinition(NDefinition descriptor) {
        return baseDefinitionFilter().acceptDefinition(descriptor);
    }

    @Override
    public NFilter redescribe(Supplier<NElement> description) {
        return baseDefinitionFilter().redescribe(description);
    }

    @Override
    public NDefinitionFilter simplify() {
        return (NDefinitionFilter) baseDefinitionFilter().simplify();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NDefinitionFilterDelegate that = (NDefinitionFilterDelegate) o;
        return Objects.equals(baseDefinitionFilter(), that.baseDefinitionFilter());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(baseDefinitionFilter());
    }
}
