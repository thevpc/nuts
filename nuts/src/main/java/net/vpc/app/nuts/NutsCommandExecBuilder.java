package net.vpc.app.nuts;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public interface NutsCommandExecBuilder {
    NutsCommandExecBuilder setFailFast();
    /**
     * when the execution returns a non zero result, an exception is thrown.
     * Particularly, if grabOutputString is used, error exception will state the output message
     *
     * @return this instance
     */
    NutsCommandExecBuilder setFailFast(boolean failFast);

    /**
     * failFast value
     * @return true if failFast is armed
     */
    boolean isFailFast();

    NutsSession getSession();

    NutsCommandExecBuilder setSession(NutsSession session);

    List<String> getCommand();

    NutsCommandExecBuilder addCommand(String... command);

    NutsCommandExecBuilder addCommand(List<String> command);

    NutsCommandExecBuilder addExecutorOptions(String... executorOptions);

    NutsCommandExecBuilder addExecutorOptions(List<String> executorOptions);

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

    PrintStream getOut();

    NutsCommandExecBuilder grabOutputString();

    NutsCommandExecBuilder grabErrorString();

    String getOutputString();

    String getErrorString();

    NutsCommandExecBuilder setOut(PrintStream out);

    NutsCommandExecBuilder setErr(PrintStream err);

    PrintStream getErr();

    NutsCommandExecBuilder exec();

    boolean isNativeCommand();

    boolean isRedirectErrorStream();

    NutsCommandExecBuilder setRedirectErrorStream();

    NutsCommandExecBuilder setRedirectErrorStream(boolean redirectErrorStream);


    NutsCommandExecBuilder setNativeCommand(boolean nativeCommand);

    int getResult();

    String getCommandString();

    String getCommandString(NutsCommandStringFormatter f);
}
