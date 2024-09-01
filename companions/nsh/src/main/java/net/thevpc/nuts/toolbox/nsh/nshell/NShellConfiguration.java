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
 * Copyright [2020] [thevpc]  
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License"); 
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an 
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language 
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.toolbox.nsh.nshell;

import net.thevpc.nuts.NId;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.toolbox.nsh.cmdresolver.NShellCommandTypeResolver;
import net.thevpc.nuts.toolbox.nsh.cmds.NShellBuiltin;
import net.thevpc.nuts.toolbox.nsh.err.NShellErrorHandler;
import net.thevpc.nuts.toolbox.nsh.eval.NShellEvaluator;
import net.thevpc.nuts.toolbox.nsh.history.NShellHistory;
import net.thevpc.nuts.toolbox.nsh.options.NShellOptionsParser;
import net.thevpc.nuts.toolbox.nsh.sys.NShellExternalExecutor;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author vpc
 */
public class NShellConfiguration {

    private NSession session;
    private NId appId;
    private String[] args;
    private String serviceName;
    private NShellOptionsParser shellOptionsParser;
    private NShellEvaluator evaluator;
    private NShellCommandTypeResolver commandTypeResolver;
    private NShellErrorHandler errorHandler;
    private NShellExternalExecutor externalExecutor;
    private NShellHistory history;
    private Predicate<NShellBuiltin> builtinFilter;

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

    public NSession getSession() {
        return session;
    }

    public NShellConfiguration setSession(NSession session) {
        this.session = session;
        return this;
    }

    public String[] getArgs() {
        return args;
    }

    public NShellConfiguration setArgs(String... args) {
        this.args = args;
        return this;
    }

    public String getServiceName() {
        return serviceName;
    }

    public NShellConfiguration setServiceName(String serviceName) {
        this.serviceName = serviceName;
        return this;
    }

    public NShellOptionsParser getShellOptionsParser() {
        return shellOptionsParser;
    }

    public NShellConfiguration setShellOptionsParser(NShellOptionsParser shellOptionsParser) {
        this.shellOptionsParser = shellOptionsParser;
        return this;
    }

    public NShellEvaluator getEvaluator() {
        return evaluator;
    }

    public NShellConfiguration setEvaluator(NShellEvaluator evaluator) {
        this.evaluator = evaluator;
        return this;
    }

    public NShellCommandTypeResolver getCommandTypeResolver() {
        return commandTypeResolver;
    }

    public NShellConfiguration setCommandTypeResolver(NShellCommandTypeResolver commandTypeResolver) {
        this.commandTypeResolver = commandTypeResolver;
        return this;
    }

    public NShellErrorHandler getErrorHandler() {
        return errorHandler;
    }

    public NShellConfiguration setErrorHandler(NShellErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
        return this;
    }

    public NShellExternalExecutor getExternalExecutor() {
        return externalExecutor;
    }

    public NShellConfiguration setExternalExecutor(NShellExternalExecutor externalExecutor) {
        this.externalExecutor = externalExecutor;
        return this;
    }

    public NShellHistory getHistory() {
        return history;
    }

    public NShellConfiguration setHistory(NShellHistory history) {
        this.history = history;
        return this;
    }

    public NId getAppId() {
        return appId;
    }

    public NShellConfiguration setAppId(NId appId) {
        this.appId = appId;
        return this;
    }

    public Predicate<NShellBuiltin> getBuiltinFilter() {
        return builtinFilter;
    }

    public NShellConfiguration setBuiltinFilter(Predicate<NShellBuiltin> builtinFilter) {
        this.builtinFilter = builtinFilter;
        return this;
    }

    public Function<NSession, NMsg> getHeaderMessageSupplier() {
        return headerMessageSupplier;
    }

    public NShellConfiguration setHeaderMessageSupplier(Function<NSession, NMsg> headerMessageSupplier) {
        this.headerMessageSupplier = headerMessageSupplier;
        return this;
    }

    public Boolean getIncludeCoreBuiltins() {
        return includeCoreBuiltins;
    }

    public NShellConfiguration setIncludeCoreBuiltins(Boolean includeCoreBuiltins) {
        this.includeCoreBuiltins = includeCoreBuiltins;
        return this;
    }

    public Boolean getIncludeDefaultBuiltins() {
        return includeDefaultBuiltins;
    }

    public NShellConfiguration setIncludeDefaultBuiltins(Boolean includeDefaultBuiltins) {
        this.includeDefaultBuiltins = includeDefaultBuiltins;
        return this;
    }

    public Boolean getIncludeExternalExecutor() {
        return includeExternalExecutor;
    }

    public NShellConfiguration setIncludeExternalExecutor(Boolean includeExternalExecutor) {
        this.includeExternalExecutor = includeExternalExecutor;
        return this;
    }
}
