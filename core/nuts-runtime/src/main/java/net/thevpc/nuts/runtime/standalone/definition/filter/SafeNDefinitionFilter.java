package net.thevpc.nuts.runtime.standalone.definition.filter;

import net.thevpc.nuts.artifact.NDefinition;
import net.thevpc.nuts.artifact.NDefinitionFilter;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.text.NMsg;

public class SafeNDefinitionFilter extends AbstractDefinitionFilter {
    private NDefinitionFilter base;
    private NMsg source;

    public SafeNDefinitionFilter(NDefinitionFilter base, NMsg source) {
        super(base.getFilterOp());
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

}
