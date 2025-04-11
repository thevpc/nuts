package net.thevpc.nuts.runtime.standalone.definition;

import net.thevpc.nuts.NDefinitionFilter;
import net.thevpc.nuts.NId;
import net.thevpc.nuts.spi.base.AbstractNPredicate;

public class NDefinitionFilterToNIdPredicate2 extends AbstractNPredicate<NId> {
    private final NDefinitionFilter filter;

    public NDefinitionFilterToNIdPredicate2(NDefinitionFilter filter) {
        this.filter = filter;
    }

    @Override
    public boolean test(NId t) {
        if (filter == null) {
            return true;
        }
        return filter.acceptDefinition(NDefinitionHelper.ofDefinition(t));
    }

    @Override
    public String toString() {
        return filter.toString();
    }
}
