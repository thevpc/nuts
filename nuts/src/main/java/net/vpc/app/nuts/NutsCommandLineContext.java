package net.vpc.app.nuts;

/**
 * 
 * @author vpc
 * @since 0.5.5
 */
public interface NutsCommandLineContext {
    String[] getArgs();
    NutsCommandAutoComplete getAutoComplete();

}
