package net.thevpc.nuts.runtime.core.filters.installstatus;

import net.thevpc.nuts.NutsInstallStatusFilter;
import net.thevpc.nuts.NutsInstallStatusFilterManager;
import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.runtime.core.filters.NutsTypedFiltersParser;

public class NutsInstallStatusFilterParser extends NutsTypedFiltersParser<NutsInstallStatusFilter> {
    public NutsInstallStatusFilterParser(String str,NutsWorkspace ws) {
        super(str,ws);
    }

    @Override
    protected NutsInstallStatusFilterManager getTManager() {
        return ws.filters().installStatus();
    }

    protected NutsInstallStatusFilter worldToPredicate(String word){
        switch (word.toLowerCase()){
            case "installed":return getTManager().byInstalled();
            case "default":
            case "defaultvalue":
                return getTManager().byDefaultValue();
            case "required":
                return getTManager().byRequired();
            case "obsolete":
                return getTManager().byObsolete();
            case "deployed":
                return getTManager().byDeployed();
            default:{
                return super.worldToPredicate(word);
            }
        }
    }

}
