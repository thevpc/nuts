/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.toolbox.nfind;

import java.io.PrintStream;
import java.util.EnumSet;
import java.util.HashSet;
import net.vpc.app.nuts.NutsApplicationContext;
import net.vpc.app.nuts.NutsDependency;
import net.vpc.app.nuts.NutsDependencyFilter;
import net.vpc.app.nuts.NutsDependencyScope;
import net.vpc.app.nuts.NutsId;
import net.vpc.app.nuts.NutsSearchCommand;

/**
 *
 * @author vpc
 */
class FindContext {
    
//    HashSet<String> arch = new HashSet<String>();
//    HashSet<String> pack = new HashSet<String>();
//    HashSet<String> repos = new HashSet<String>();
//    EnumSet<NutsDependencyScope> scopes = EnumSet.of(NutsDependencyScope.PROFILE_RUN);
    NutsSearchCommand search;
//    boolean jsflag = false;
//    SearchMode fetchMode = SearchMode.ONLINE;
    boolean desc = false;
    boolean eff = false;
    boolean executable = true;
    boolean library = true;
    Boolean installed = null;
    Boolean installedDependencies = null;
    Boolean updatable = null;
//    boolean allVersions = true;
//    boolean duplicateVersions = true;
//    boolean sort = false;
//    boolean transitive = true;
    PrintStream out;
    PrintStream err;
//    String display = "id";

//    boolean longFormat = false;
//    boolean omitGroup = false;
//    boolean omitNamespace = true;
//    boolean omitImportedGroup = false;
//    boolean highlightImportedGroup = true;
//    boolean filePathOnly = false;
//    boolean fileNameOnly = false;
//    boolean showFile = false;
//    boolean showClass = false;
//    boolean showSummary = false;

    NutsApplicationContext context;
    long executionTimeNano;
    FindWhat executionSearch;
//    Boolean acceptOptional = false;
    NutsDependencyFilter dependencyFilter = null;
    NutsDependencyFilter equivalentDependencyFilter = new NutsDependencyFilter() {
        @Override
        public boolean accept(NutsId from, NutsDependency dependency) {
//            if (search.getScope().size()>0 && !NutsDependencyScope.expand(search.getScope()).contains(NutsDependencyScope.lenientParse(dependency.getScope()))) {
//                return false;
//            }
//            if (acceptOptional != null) {
//                if (acceptOptional.booleanValue() != dependency.isOptional()) {
//                    return false;
//                }
//            }
            if (dependencyFilter != null) {
                if (dependencyFilter.accept(from, dependency)) {
                    return false;
                }
            }
            return true;
        }
    };
    
}
