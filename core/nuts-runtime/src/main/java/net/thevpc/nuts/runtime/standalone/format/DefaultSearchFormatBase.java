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
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.cmdline.NCmdLineConfigurable;
import net.thevpc.nuts.format.NIterableFormat;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.runtime.standalone.util.NConfigurableHelper;

/**
 *
 * @author thevpc
 */
public abstract class DefaultSearchFormatBase implements NIterableFormat {

    private final NFetchDisplayOptions displayOptions;
    private final NSession session;
    private final NPrintStream writer;
    private final NContentType format;

    public DefaultSearchFormatBase(NSession session, NPrintStream writer, NContentType format, NFetchDisplayOptions options) {
        this.format = format;
        this.writer = writer;
        this.session = session;
        displayOptions = new NFetchDisplayOptions(session);
        if(options!=null){
            displayOptions.configure(true, options.toCommandLineOptions());
        }
    }

    @Override
    public NContentType getOutputFormat() {
        return format;
    }

    public NFetchDisplayOptions getDisplayOptions() {
        return displayOptions;
    }

    /**
     * configure the current command with the given arguments.
     *
     * @param skipUnsupported when true, all unsupported options are skipped
     * silently
     * @param cmdLine arguments to configure with
     * @return {@code this} instance
     */
    @Override
    public boolean configure(boolean skipUnsupported, NCmdLine cmdLine) {
        return NConfigurableHelper.configure(this, getSession(), skipUnsupported, cmdLine);
    }

    /**
     * configure the current command with the given arguments. This is an
     * override of the {@link NCmdLineConfigurable#configure(boolean, java.lang.String...) }
     * to help return a more specific return type;
     *
     * @param args argument to configure with
     * @return {@code this} instance
     */
    @Override
    public NIterableFormat configure(boolean skipUnsupported, String... args) {
        return NConfigurableHelper.configure(this, getSession(), skipUnsupported, args, "search-" + getOutputFormat().id());
    }

    public NWorkspace getWorkspace() {
        return session.getWorkspace();
    }

    public NSession getSession() {
        return session;
    }

    public NPrintStream getWriter() {
        return writer;
    }

    @Override
    public void configureLast(NCmdLine cmdLine) {
        if (!configureFirst(cmdLine)) {
            cmdLine.throwUnexpectedArgument();
        }
    }
}
