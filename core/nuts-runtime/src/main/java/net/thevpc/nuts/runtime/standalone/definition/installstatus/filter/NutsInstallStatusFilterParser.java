package net.thevpc.nuts.runtime.standalone.definition.installstatus.filter;

import net.thevpc.nuts.NutsInstallStatusFilter;
import net.thevpc.nuts.NutsInstallStatusFilters;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.runtime.standalone.util.filters.NutsTypedFiltersParser;

public class NutsInstallStatusFilterParser extends NutsTypedFiltersParser<NutsInstallStatusFilter> {
    public NutsInstallStatusFilterParser(String str,NutsSession session) {
        super(str,session);
    }

    @Override
    protected NutsInstallStatusFilters getTManager() {
        return NutsInstallStatusFilters.of(getSession());
    }

    protected NutsInstallStatusFilter wordToPredicate(String word){
        switch (word.toLowerCase()){
            case "installed":return getTManager().byInstalled(true);
            case "default":
            case "defaultvalue":
                return getTManager().byDefaultValue(true);
            case "required":
                return getTManager().byRequired(true);
            case "obsolete":
                return getTManager().byObsolete(true);
            case "deployed":
                return getTManager().byDeployed(true);
            default:{
                return super.wordToPredicate(word);
            }
        }
    }

}
