package net.thevpc.nuts.runtime.standalone.definition.filter;

import net.thevpc.nuts.artifact.NDefinitionFilter;
import net.thevpc.nuts.elem.NElementDescribables;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.util.NFilter;

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
    public NFilter redescribe(Supplier<NElement> description) {
        this.description = description;
        return this;
    }

    @Override
    public NElement describe() {
        return NElementDescribables.safeDescribeOfBase(description, base);
    }
}
