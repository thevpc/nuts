/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts;

import java.util.Properties;

/**
 *
 * @author vpc
 */
public class NutsCommandExecOptions {

    private String[] executorOptions;
    private Properties env;
    private String directory;
    private boolean failFast;
    private NutsExecutionType executionType;

    public String[] getExecutorOptions() {
        return executorOptions;
    }

    public NutsCommandExecOptions setExecutorOptions(String[] executorOptions) {
        this.executorOptions = executorOptions;
        return this;
    }

    public Properties getEnv() {
        return env;
    }

    public NutsCommandExecOptions setEnv(Properties env) {
        this.env = env;
        return this;
    }

    public String getDirectory() {
        return directory;
    }

    public NutsCommandExecOptions setDirectory(String directory) {
        this.directory = directory;
        return this;
    }

    public boolean isFailFast() {
        return failFast;
    }

    public NutsCommandExecOptions setFailFast(boolean failFast) {
        this.failFast = failFast;
        return this;
    }

    public NutsExecutionType getExecutionType() {
        return executionType;
    }

    public NutsCommandExecOptions setExecutionType(NutsExecutionType executionType) {
        this.executionType = executionType;
        return this;
    }

}
