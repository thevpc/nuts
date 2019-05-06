/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts;

/**
 *
 * @author vpc
 */
public interface NutsUpdateRepositoryStatisticsCommand extends NutsWorkspaceCommand {

    boolean isTrace();

    NutsUpdateRepositoryStatisticsCommand setTrace(boolean trace);

    NutsUpdateRepositoryStatisticsCommand trace();

    NutsUpdateRepositoryStatisticsCommand trace(boolean trace);


    NutsTraceFormat getTraceFormat() ;

    NutsUpdateRepositoryStatisticsCommand unsetTraceFormat(NutsOutputFormat f) ;

    NutsUpdateRepositoryStatisticsCommand traceFormat(NutsTraceFormat traceFormat) ;

    NutsUpdateRepositoryStatisticsCommand setTraceFormat(NutsTraceFormat f) ;

    NutsTraceFormat[] getTraceFormats() ;

    NutsSession getSession() ;

    NutsUpdateRepositoryStatisticsCommand session(NutsSession session) ;

    NutsUpdateRepositoryStatisticsCommand setSession(NutsSession session) ;

    NutsUpdateRepositoryStatisticsCommand outputFormat(NutsOutputFormat outputFormat) ;

    NutsUpdateRepositoryStatisticsCommand setOutputFormat(NutsOutputFormat outputFormat) ;

    NutsOutputFormat getOutputFormat() ;

    boolean isForce() ;

    NutsUpdateRepositoryStatisticsCommand force() ;

    NutsUpdateRepositoryStatisticsCommand force(boolean force) ;

    NutsUpdateRepositoryStatisticsCommand setForce(boolean force) ;

    NutsUpdateRepositoryStatisticsCommand run();

    NutsUpdateRepositoryStatisticsCommand parseOptions(String... args);

}
