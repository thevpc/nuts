/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts;

import java.nio.file.Path;
import java.util.Collection;

/**
 *
 * @author vpc
 */
public interface NutsUpdateStatisticsCommand extends NutsWorkspaceCommand {

    boolean isTrace();

    NutsUpdateStatisticsCommand setTrace(boolean trace);

    NutsUpdateStatisticsCommand trace();

    NutsUpdateStatisticsCommand trace(boolean trace);
    
    
    
    NutsTraceFormat getTraceFormat() ;

    NutsUpdateStatisticsCommand unsetTraceFormat(NutsOutputFormat f) ;

    NutsUpdateStatisticsCommand traceFormat(NutsTraceFormat traceFormat) ;

    NutsUpdateStatisticsCommand setTraceFormat(NutsTraceFormat f) ;

    NutsTraceFormat[] getTraceFormats() ;

    NutsSession getSession() ;

    NutsUpdateStatisticsCommand session(NutsSession session) ;

    NutsUpdateStatisticsCommand setSession(NutsSession session) ;

    NutsUpdateStatisticsCommand outputFormat(NutsOutputFormat outputFormat) ;

    NutsUpdateStatisticsCommand setOutputFormat(NutsOutputFormat outputFormat) ;

    NutsOutputFormat getOutputFormat() ;

    boolean isForce() ;

    NutsUpdateStatisticsCommand force() ;

    NutsUpdateStatisticsCommand force(boolean force) ;

    NutsUpdateStatisticsCommand setForce(boolean force) ;
    
    

    NutsUpdateStatisticsCommand clearRepos();

    NutsUpdateStatisticsCommand repo(String s);

    NutsUpdateStatisticsCommand addRepo(String s);

    NutsUpdateStatisticsCommand removeRepo(String s);

    NutsUpdateStatisticsCommand addRepos(String... all);

    NutsUpdateStatisticsCommand addRepos(Collection<String> all);

    NutsUpdateStatisticsCommand clearPaths();

    NutsUpdateStatisticsCommand path(Path s);

    NutsUpdateStatisticsCommand addPath(Path s);

    NutsUpdateStatisticsCommand removePath(Path s);

    NutsUpdateStatisticsCommand addPaths(Path... all);

    NutsUpdateStatisticsCommand addPaths(Collection<Path> all);

    NutsUpdateStatisticsCommand run();

    NutsUpdateStatisticsCommand parseOptions(String... args);

}
