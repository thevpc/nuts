/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.format;

import net.vpc.app.nuts.NutsArgument;
import net.vpc.app.nuts.NutsCommand;
import net.vpc.app.nuts.NutsOutputFormat;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.NutsIncrementalFormat;

/**
 *
 * @author vpc
 */
public class DefaultSearchFormatJson extends DefaultSearchFormatBase<NutsIncrementalFormat> {

    private boolean compact;

    public DefaultSearchFormatJson(NutsWorkspace ws) {
        super(ws, NutsOutputFormat.JSON);
    }

    @Override
    public void formatStart() {
        getValidOut().println("[");
    }

    @Override
    public boolean configureFirst(NutsCommand cmd) {
        NutsArgument a=cmd.peek();
        if(a==null){
            return false;
        }
        switch(a.getKey().getString()){
            case "--compact":{
                this.compact=cmd.nextBoolean().getValue().getBoolean();
                return true;
            }
        }
        return super.configureFirst(cmd);
    }

    @Override
    public void formatNext(Object object, long index) {
        if (index > 0) {
            getValidOut().print(", ");
        }
        getValidOut().printf("%N%n", getWs().io().json().compact(isCompact()).toJsonString(object));
        getValidOut().flush();
    }

    public boolean isCompact(){
        return compact;
    }

    @Override
    public void formatComplete(long count) {
        getValidOut().println("]");
    }

}
