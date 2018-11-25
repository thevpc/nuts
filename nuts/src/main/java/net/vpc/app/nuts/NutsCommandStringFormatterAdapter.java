package net.vpc.app.nuts;

public class NutsCommandStringFormatterAdapter implements NutsCommandStringFormatter {
    @Override
    public boolean acceptArgument(int argIndex, String arg) {
        return true;
    }

    @Override
    public String replaceArgument(int argIndex, String arg) {
        return null;
    }

    @Override
    public boolean acceptEnvName(String envName, String envValue) {
        return true;
    }

    @Override
    public boolean acceptRedirectInput() {
        return true;
    }

    @Override
    public boolean acceptRedirectOutput() {
        return true;
    }

    @Override
    public boolean acceptRedirectError() {
        return true;
    }

    @Override
    public String replaceEnvName(String envName, String envValue) {
        return null;
    }

    @Override
    public String replaceEnvValue(String envName, String envValue) {
        return null;
    }
}
