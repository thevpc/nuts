package net.thevpc.nuts.runtime.standalone.definition.filter;

import net.thevpc.nuts.artifact.NDefinition;
import net.thevpc.nuts.artifact.NDefinitionFilter;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.text.NMsg;

import java.util.Objects;

public class SafeNDefinitionFilter extends AbstractDefinitionFilter {
    private NDefinitionFilter base;
    private NMsg source;

    public SafeNDefinitionFilter(NDefinitionFilter base, NMsg source) {
        super(base.filterOp());
        this.base = base;
        this.source = source;
    }

    @Override
    public NDefinitionFilter simplify() {
        return this;
    }

    @Override
    public boolean acceptDefinition(NDefinition definition) {
        if(base==null){
            return true;
        }
        try {
            return base.acceptDefinition(definition);
        }catch (Exception ex){
            NLog.of(SafeNDefinitionFilter.class).log(NMsg.ofC("[%s] unable to filter definition. error : ",source,ex).asFinestFail(ex));
            return false;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        SafeNDefinitionFilter that = (SafeNDefinitionFilter) o;
        return Objects.equals(base, that.base);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), base);
    }
}
