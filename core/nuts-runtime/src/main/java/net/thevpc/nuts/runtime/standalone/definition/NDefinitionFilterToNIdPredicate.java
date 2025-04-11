package net.thevpc.nuts.runtime.standalone.definition;

import net.thevpc.nuts.NDefinition;
import net.thevpc.nuts.NDefinitionFilter;
import net.thevpc.nuts.spi.base.AbstractNPredicate;

public class NDefinitionFilterToNIdPredicate extends AbstractNPredicate<NDefinition> {
    private final NDefinitionFilter filter;

    public NDefinitionFilterToNIdPredicate(NDefinitionFilter filter) {
        this.filter = filter;
    }

    @Override
    public boolean test(NDefinition t) {
        return filter.acceptDefinition(t);
    }

    @Override
    public String toString() {
        return filter.toString();
    }
}
