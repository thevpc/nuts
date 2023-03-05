/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <br>
 * <p>
 * Copyright [2020] [thevpc] Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.toolbox.nsh.jshell;

import net.thevpc.nuts.NApplicationContext;
import net.thevpc.nuts.NId;
import net.thevpc.nuts.NMsg;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.toolbox.nsh.cmdresolver.JShellCommandTypeResolver;
import net.thevpc.nuts.toolbox.nsh.cmds.JShellBuiltin;
import net.thevpc.nuts.toolbox.nsh.err.JShellErrorHandler;
import net.thevpc.nuts.toolbox.nsh.eval.JShellEvaluator;
import net.thevpc.nuts.toolbox.nsh.sys.JShellExternalExecutor;
import net.thevpc.nuts.toolbox.nsh.history.JShellHistory;
import net.thevpc.nuts.toolbox.nsh.options.JShellOptionsParser;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author vpc
 */
public class JShellConfiguration {

    private NApplicationContext applicationContext;
    private NId appId;
    private NSession session;
    private String[] args;
    private String serviceName;
    private JShellOptionsParser shellOptionsParser;
    private JShellEvaluator evaluator;
    private JShellCommandTypeResolver commandTypeResolver;
    private JShellErrorHandler errorHandler;
    private JShellExternalExecutor externalExecutor;
    private JShellHistory history;
    private Predicate<JShellBuiltin> builtinFilter;

    /**
     * defaults to true
     */
    private Boolean includeCoreBuiltins;
    /**
     * defaults to false
     */
    private Boolean includeDefaultBuiltins;

    /**
     * default false
     */
    private Boolean includeExternalExecutor;
    private Function<NSession, NMsg> headerMessageSupplier;

    public NApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public JShellConfiguration setApplicationContext(NApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        return this;
    }

    public String[] getArgs() {
        return args;
    }

    public JShellConfiguration setArgs(String... args) {
        this.args = args;
        return this;
    }

    public String getServiceName() {
        return serviceName;
    }

    public JShellConfiguration setServiceName(String serviceName) {
        this.serviceName = serviceName;
        return this;
    }

    public JShellOptionsParser getShellOptionsParser() {
        return shellOptionsParser;
    }

    public JShellConfiguration setShellOptionsParser(JShellOptionsParser shellOptionsParser) {
        this.shellOptionsParser = shellOptionsParser;
        return this;
    }

    public JShellEvaluator getEvaluator() {
        return evaluator;
    }

    public JShellConfiguration setEvaluator(JShellEvaluator evaluator) {
        this.evaluator = evaluator;
        return this;
    }

    public JShellCommandTypeResolver getCommandTypeResolver() {
        return commandTypeResolver;
    }

    public JShellConfiguration setCommandTypeResolver(JShellCommandTypeResolver commandTypeResolver) {
        this.commandTypeResolver = commandTypeResolver;
        return this;
    }

    public JShellErrorHandler getErrorHandler() {
        return errorHandler;
    }

    public JShellConfiguration setErrorHandler(JShellErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
        return this;
    }

    public JShellExternalExecutor getExternalExecutor() {
        return externalExecutor;
    }

    public JShellConfiguration setExternalExecutor(JShellExternalExecutor externalExecutor) {
        this.externalExecutor = externalExecutor;
        return this;
    }

    public JShellHistory getHistory() {
        return history;
    }

    public JShellConfiguration setHistory(JShellHistory history) {
        this.history = history;
        return this;
    }

    public NId getAppId() {
        return appId;
    }

    public JShellConfiguration setAppId(NId appId) {
        this.appId = appId;
        return this;
    }

    public NSession getSession() {
        return session;
    }

    public JShellConfiguration setSession(NSession session) {
        this.session = session;
        return this;
    }


    public Predicate<JShellBuiltin> getBuiltinFilter() {
        return builtinFilter;
    }

    public JShellConfiguration setBuiltinFilter(Predicate<JShellBuiltin> builtinFilter) {
        this.builtinFilter = builtinFilter;
        return this;
    }

    public Function<NSession, NMsg> getHeaderMessageSupplier() {
        return headerMessageSupplier;
    }

    public JShellConfiguration setHeaderMessageSupplier(Function<NSession, NMsg> headerMessageSupplier) {
        this.headerMessageSupplier = headerMessageSupplier;
        return this;
    }

    public Boolean getIncludeCoreBuiltins() {
        return includeCoreBuiltins;
    }

    public JShellConfiguration setIncludeCoreBuiltins(Boolean includeCoreBuiltins) {
        this.includeCoreBuiltins = includeCoreBuiltins;
        return this;
    }

    public Boolean getIncludeDefaultBuiltins() {
        return includeDefaultBuiltins;
    }

    public JShellConfiguration setIncludeDefaultBuiltins(Boolean includeDefaultBuiltins) {
        this.includeDefaultBuiltins = includeDefaultBuiltins;
        return this;
    }

    public Boolean getIncludeExternalExecutor() {
        return includeExternalExecutor;
    }

    public JShellConfiguration setIncludeExternalExecutor(Boolean includeExternalExecutor) {
        this.includeExternalExecutor = includeExternalExecutor;
        return this;
    }
}
