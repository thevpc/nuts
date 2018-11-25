package net.vpc.app.nuts;

public interface NutsCommandStringFormatter {
    boolean acceptArgument(int argIndex, String arg);

    String replaceArgument(int argIndex, String arg);

    boolean acceptEnvName(String envName, String envValue);

    boolean acceptRedirectInput();

    boolean acceptRedirectOutput();

    boolean acceptRedirectError();

    String replaceEnvName(String envName, String envValue);

    String replaceEnvValue(String envName, String envValue);
}
