//package net.thevpc.nuts.runtime.standalone.definition.installstatus.filter;
//
//import net.thevpc.nuts.NInstallStatusFilter;
//import net.thevpc.nuts.NInstallStatusFilters;
//import net.thevpc.nuts.runtime.standalone.util.filters.NTypedFiltersParser;
//
//public class NInstallStatusFilterParser extends NTypedFiltersParser<NInstallStatusFilter> {
//    public NInstallStatusFilterParser(String str) {
//        super(str);
//    }
//
//    @Override
//    protected NInstallStatusFilters getTManager() {
//        return NInstallStatusFilters.of();
//    }
//
//    protected NInstallStatusFilter wordToPredicate(String word){
//        switch (word.toLowerCase()){
//            case "installed":return getTManager().byInstalled(true);
//            case "default":
//            case "defaultvalue":
//                return getTManager().byDefaultValue(true);
//            case "required":
//                return getTManager().byRequired(true);
//            case "obsolete":
//                return getTManager().byObsolete(true);
//            case "deployed":
//                return getTManager().byDeployed(true);
//            default:{
//                return super.wordToPredicate(word);
//            }
//        }
//    }
//
//}
