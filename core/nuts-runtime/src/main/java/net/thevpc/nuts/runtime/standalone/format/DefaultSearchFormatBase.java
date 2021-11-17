/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <br>
 *
 * Copyright [2020] [thevpc]
 * Licensed under the Apache License, Version 2.0 (the "License"); you may 
 * not use this file except in compliance with the License. You may obtain a 
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an 
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language 
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
*/
package net.thevpc.nuts.runtime.standalone.format;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.util.NutsConfigurableHelper;

/**
 *
 * @author thevpc
 */
public abstract class DefaultSearchFormatBase implements NutsIterableFormat {

    private final NutsFetchDisplayOptions displayOptions;
    private final NutsSession session;
    private final NutsPrintStream writer;
    private final NutsContentType format;

    public DefaultSearchFormatBase(NutsSession session, NutsPrintStream writer, NutsContentType format, NutsFetchDisplayOptions options) {
        this.format = format;
        this.writer = writer;
        this.session = session;
        displayOptions = new NutsFetchDisplayOptions(session);
        if(options!=null){
            displayOptions.configure(true, options.toCommandLineOptions());
        }
    }

    @Override
    public NutsContentType getOutputFormat() {
        return format;
    }

    public NutsFetchDisplayOptions getDisplayOptions() {
        return displayOptions;
    }

    /**
     * configure the current command with the given arguments.
     *
     * @param skipUnsupported when true, all unsupported options are skipped
     * silently
     * @param commandLine arguments to configure with
     * @return {@code this} instance
     */
    @Override
    public boolean configure(boolean skipUnsupported, NutsCommandLine commandLine) {
        return NutsConfigurableHelper.configure(this, getSession(), skipUnsupported, commandLine);
    }

    /**
     * configure the current command with the given arguments. This is an
     * override of the {@link NutsCommandLineConfigurable#configure(boolean, java.lang.String...) }
     * to help return a more specific return type;
     *
     * @param args argument to configure with
     * @return {@code this} instance
     */
    @Override
    public NutsIterableFormat configure(boolean skipUnsupported, String... args) {
        return NutsConfigurableHelper.configure(this, getSession(), skipUnsupported, args, "search-" + getOutputFormat().id());
    }

    public NutsWorkspace getWorkspace() {
        return session.getWorkspace();
    }

    public NutsSession getSession() {
        return session;
    }

    public NutsPrintStream getWriter() {
        return writer;
    }

}
