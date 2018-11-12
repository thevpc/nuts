package net.vpc.app.nuts;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public interface NutsCommandExecBuilder {
    NutsSession getSession();

    NutsCommandExecBuilder setSession(NutsSession session);

    List<String> getCommand();

    NutsCommandExecBuilder addCommand(String... command);

    NutsCommandExecBuilder addCommand(List<String> command);

    NutsCommandExecBuilder setCommand(String... command);

    NutsCommandExecBuilder setCommand(List<String> command);

    Properties getEnv();

    NutsCommandExecBuilder addEnv(Map<String, String> env);

    NutsCommandExecBuilder setEnv(String k, String val);

    NutsCommandExecBuilder setEnv(Map<String, String> env);

    NutsCommandExecBuilder setEnv(Properties env);

    String getDirectory();

    NutsCommandExecBuilder setDirectory(String directory);

    InputStream getIn();

    NutsCommandExecBuilder setIn(InputStream in);

    NutsPrintStream getOut();

    NutsCommandExecBuilder setOutAndErrStringBuffer();

    NutsCommandExecBuilder setOutStringBuffer();

    NutsCommandExecBuilder setErrStringBuffer();

    String getOutString();

    String getErrString();

    NutsCommandExecBuilder setOut(NutsPrintStream out);

    NutsCommandExecBuilder setOut(PrintStream out);

    NutsCommandExecBuilder setErr(PrintStream err);

    NutsPrintStream getErr();

    NutsCommandExecBuilder setErr(NutsPrintStream err);

    NutsCommandExecBuilder exec();

    boolean isNativeCommand();

    NutsCommandExecBuilder setNativeCommand(boolean nativeCommand);

    int getResult();
}
