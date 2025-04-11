package net.thevpc.nuts.runtime.standalone.definition.filter;

import net.thevpc.nuts.NDefinitionFilter;
import net.thevpc.nuts.elem.NEDesc;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.util.NFilter;

public class NDefinitionFilterDelegateWithDescription extends NDefinitionFilterDelegate {
    private NDefinitionFilter base;
    private NEDesc description;

    public NDefinitionFilterDelegateWithDescription(NDefinitionFilter base, NEDesc description) {
        super();
        this.base = base;
        this.description = description;
    }

    @Override
    public NDefinitionFilter baseDefinitionFilter() {
        return base;
    }

    @Override
    public NFilter withDesc(NEDesc description) {
        this.description = description;
        return this;
    }

    @Override
    public NElement describe() {
        return NEDesc.safeDescribeOfBase(description, base);
    }
}
