/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . It's based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
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
package net.thevpc.nuts;

import net.thevpc.nuts.cmdline.NCmdLineConfigurable;
import net.thevpc.nuts.elem.NArrayElementBuilder;
import net.thevpc.nuts.format.NContentType;
import net.thevpc.nuts.format.NIterableFormat;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.io.NTerminal;
import net.thevpc.nuts.util.NCallable;
import net.thevpc.nuts.util.NObservableMapListener;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.util.NRunnable;

import java.io.Closeable;
import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.logging.Filter;
import java.util.logging.Level;

/**
 * session is context defining common command options and parameters.
 *
 * @author thevpc
 * @app.category Base
 * @since 0.5.4
 */
public interface NSession extends NCmdLineConfigurable, Closeable {
    static NSession of() {
        return get().get();
    }

    static NOptional<NSession> get() {
        return NWorkspace.get().map(NWorkspace::currentSession);
    }

    void runWith(NRunnable runnable);

    <T> T callWith(NCallable<T> callable);


    NOptional<Boolean> getTrace();

    Boolean getTrace(boolean inherit);

    /**
     * When true, operations are invited to print to output stream extra
     * information about processing. Output may be in different formats
     * according to {@link #getOutputFormat()} and {@link #isIterableOut()}
     *
     * @return true if trace flag is armed
     */
    boolean isTrace();

    NSession trace();

    /**
     * change trace flag value. When true, operations are invited to print to
     * output stream information about processing. Output may be in different
     * formats according to {@link #getOutputFormat()} and
     * {@link #isIterableOut()}
     *
     * @param trace new value
     * @return {@code this} instance
     */
    NSession setTrace(Boolean trace);

    /**
     * true if non iterable and plain formats along with trace flag are armed.
     * equivalent to {@code isTrace()
     * && !isIterableOut()
     * && getOutputFormat() == NutsContentType.PLAIN}
     *
     * @return true plain non iterable format AND trace are armed
     */
    boolean isPlainTrace();

    /**
     * true if iterable format and trace flag are armed. equivalent to {@code isTrace()
     * && isIterableOut()}
     *
     * @return true plain non iterable format AND trace are armed
     */
    boolean isIterableTrace();

    /**
     * true if NON iterable and NON plain formats along with trace flag are
     * armed. equivalent to {@code isTrace()
     * && !isIterableOut()
     * && getOutputFormat() == NutsContentType.PLAIN}
     *
     * @return true if NON iterable and NON plain formats along with trace flag
     * are armed.
     */
    boolean isStructuredTrace();

    /**
     * true if iterable format is armed. equivalent to
     * {@code  getIterableFormat()!=null}
     *
     * @return true if iterable format is armed.
     */
    boolean isIterableOut();

    NSession setIterableOut(boolean iterableOut);

    /**
     * true if NON iterable and NON plain formats are armed. equivalent to {@code !isIterableOut()
     * && getOutputFormat() != NutsContentType.PLAIN}
     *
     * @return true if non iterable format AND structured outpt format are
     * armed.
     */
    boolean isStructuredOut();

    NArrayElementBuilder getElemOut();

    NSession setElemOut(NArrayElementBuilder eout);

    /**
     * true if NON iterable and plain format are armed.
     *
     * @return true if non iterable format AND structured outpt format are
     * armed.
     */
    boolean isPlainOut();

    NOptional<Boolean> getBot();

    /**
     * @since 0.8.5
     * @return preview repo
     */
    NOptional<Boolean> getPreviewRepo();

    boolean isPreviewRepo();

    boolean isBot();

    NSession setBot(Boolean bot);

    NSession setPreviewRepo(Boolean bot);

    NSession bot();

    NSession yes();

    NSession no();

    NSession ask();

    /**
     * true if YES is armed.
     *
     * @return true if YES is armed.
     * @since 0.5.7
     */
    boolean isYes();

    /**
     * true if NO is armed.
     *
     * @return true if NO is armed.
     * @since 0.5.7
     */
    boolean isNo();

    /**
     * true if ASK is armed.
     *
     * @return true if ASK is armed.
     */
    boolean isAsk();

    /**
     * return effective trace output format. The effective trace output format
     * is the value of {@code getIterableFormat().getOutputFormat()} whenever {@code getIterableFormat()!=null
     * } otherwise it returns simply the value defined by calling
     * {@link #setOutputFormat(NContentType)}. If none of null {@link #setIterableOut(boolean)}
     * {@link #setOutputFormat(NContentType)} has been called (or called with
     * null values) {@link NContentType#PLAIN} should be returned.
     *
     * @return effective trace output format
     */
    NOptional<NContentType> getOutputFormat();

    /**
     * set output format
     *
     * @param outputFormat output format
     * @return {@code this} instance
     */
    NSession setOutputFormat(NContentType outputFormat);

    /**
     * set json output format
     *
     * @return {@code this} instance
     */
    NSession json();

    /**
     * set plain text (default) output format
     *
     * @return {@code this} instance
     */
    NSession plain();

    /**
     * set properties output format
     *
     * @return {@code this} instance
     */
    NSession props();

    /**
     * set tree output format
     *
     * @return {@code this} instance
     */
    NSession tree();

    /**
     * set table output format
     *
     * @return {@code this} instance
     */
    NSession table();

    /**
     * set xml output format
     *
     * @return {@code this} instance
     */
    NSession xml();

    /**
     * return new instance copy of {@code this} session
     *
     * @return new instance copy of {@code this} session
     */
    NSession copy();

    /**
     * copy into this instance from the given value
     *
     * @param other other session to copy from
     * @return return {@code this} instance
     */
    NSession copyFrom(NSession other);

    /**
     * copy into this instance from the given value
     *
     * @param options other workspace options to copy from
     * @return return {@code this} instance
     */
    NSession copyFrom(NWorkspaceOptions options);

    /**
     * return current fetch strategy. When no strategy (or null strategy) was
     * set, return workspace strategy default strategy. When none defines use
     * ONLINE
     *
     * @return {@code this} instance
     */
    NOptional<NFetchStrategy> getFetchStrategy();

    /**
     * change fetch strategy
     *
     * @param mode new strategy or null
     * @return {@code this} instance
     */
    NSession setFetchStrategy(NFetchStrategy mode);

    /**
     * add session listener. supported listeners are instances of:
     * <ul>
     * <li>{@link NWorkspaceListener}</li>
     * <li>{@link NInstallListener}</li>
     * <li>{@link NObservableMapListener}</li>
     * <li>{@link NRepositoryListener}</li>
     * </ul>
     *
     * @param listener listener
     * @return {@code this} instance
     */
    NSession addListener(NListener listener);

    /**
     * remove session listener. supported listeners are instances of:
     * <ul>
     * <li>{@link NWorkspaceListener}</li>
     * <li>{@link NInstallListener}</li>
     * <li>{@link NObservableMapListener}</li>
     * <li>{@link NRepositoryListener}</li>
     * </ul>
     *
     * @param listener listener
     * @return {@code this} instance
     */
    NSession removeListener(NListener listener);

    /**
     * return registered listeners for the given type. Supported types are :
     * <ul>
     * <li>{@link NWorkspaceListener}</li>
     * <li>{@link NInstallListener}</li>
     * <li>{@link NObservableMapListener}</li>
     * <li>{@link NRepositoryListener}</li>
     * </ul>
     *
     * @param <T>  listener type
     * @param type listener type class
     * @return registered listeners
     */
    <T extends NListener> List<T> getListeners(Class<T> type);

    /**
     * return all registered listeners.
     *
     * @return all registered listeners.
     */
    List<NListener> getListeners();

    /**
     * return confirmation mode or {@link NConfirmationMode#ASK}
     *
     * @return confirmation mode
     */
//    NConfirmationMode getConfirm();
    NOptional<NConfirmationMode> getConfirm();

    /**
     * set confirm mode.
     *
     * @param confirm confirm type.
     * @return {@code this} instance
     */
    NSession setConfirm(NConfirmationMode confirm);

    /**
     * add output format options
     *
     * @param options output format options.
     * @return {@code this} instance
     */
    NSession addOutputFormatOptions(String... options);

    /**
     * output format options
     *
     * @return output format options
     */
    List<String> getOutputFormatOptions();

    /**
     * set output format options (clear and add)
     *
     * @param options output format options.
     * @return {@code this} instance
     */
    NSession setOutputFormatOptions(String... options);

    NSession setOutputFormatOptions(List<String> options);

    /**
     * current output stream
     *
     * @return current output stream
     */
    NPrintStream out();

    InputStream in();

    /**
     * current error stream
     *
     * @return current error stream
     */
    NPrintStream err();

    /**
     * return new instance of iterable output
     *
     * @return iterable output
     */
    NIterableFormat getIterableOutput();

    /**
     * current terminal
     *
     * @return current terminal
     */
    NTerminal getTerminal();

    /**
     * set session terminal
     *
     * @param terminal session terminal
     * @return {@code this} instance
     */
    NSession setTerminal(NTerminal terminal);

    /**
     * current workspace
     *
     * @return current workspace
     */
    NWorkspace getWorkspace();

    NOptional<Boolean> getTransitive();

    /**
     * true when considering transitive repositories.
     *
     * @return true when considering transitive repositories
     */
    boolean isTransitive();

    /**
     * consider transitive repositories
     *
     * @param value nullable value
     * @return {@code this} instance
     */
    NSession setTransitive(Boolean value);

    NOptional<Boolean> getCached();

    /**
     * true when using cache
     *
     * @return true when using cache
     */
    boolean isCached();

    /**
     * use cache
     *
     * @param value value
     * @return {@code this} instance
     */
    NSession setCached(Boolean value);

    NOptional<Boolean> getIndexed();

    /**
     * true when using indexes
     *
     * @return true when using indexes
     */
    boolean isIndexed();

    /**
     * use index
     *
     * @param value value
     * @return {@code this} instance
     */
    NSession setIndexed(Boolean value);

    /**
     * return expired date/time or zero if not set. Expire time is used to
     * expire any cached file that was downloaded before the given date/time
     *
     * @return expired date/time or zero
     * @since 0.8.0
     */
    NOptional<Instant> getExpireTime();

    /**
     * set expire instant. Expire time is used to expire any cached file that
     * was downloaded before the given date/time.
     *
     * @param value value
     * @return {@code this} instance
     * @since 0.8.0
     */
    NSession setExpireTime(Instant value);

    /**
     * return progress options
     *
     * @return progress options
     */
    String getProgressOptions();

    boolean isProgress();

    /**
     * change progress options
     *
     * @param progressOptions options
     * @return {@code this} instance
     */
    NSession setProgressOptions(String progressOptions);

    NOptional<Boolean> getGui();

    boolean isGui();

    NSession setGui(Boolean gui);

    String getErrLinePrefix();

    NSession setErrLinePrefix(String errLinePrefix);

    String getOutLinePrefix();

    NSession setOutLinePrefix(String outLinePrefix);

    NOptional<Boolean> getDry();

    NOptional<Boolean> getShowStacktrace();

    NSession setDry(Boolean dry);

    NSession setShowStacktrace(Boolean showStacktrace);

    /**
     * equivalent to getDry().orDefault();
     *
     * @return true if dry mode
     */
    boolean isDry();

    Level getLogTermLevel();

    NSession setLogTermLevel(Level level);

    Filter getLogTermFilter();

    NSession setLogFilter(Filter filter);

    /**
     * configure session based on Workspace options
     *
     * @param options options to configure from
     * @return {@code this} instance
     * @since 0.8.1
     */
    NSession configure(NWorkspaceOptions options);

    Level getLogFileLevel();

    NSession setLogFileLevel(Level logFileLevel);

    Filter getLogFileFilter();

    NSession setLogFileFilter(Filter logFileFilter);

    NArrayElementBuilder eout();

    NSession flush();

    NOptional<NExecutionType> getExecutionType();

    NSession embedded();

    NSession system();

    NSession spawn();

    NSession setExecutionType(NExecutionType executionType);

    NOptional<String> getDebug();

    NSession setDebug(String debug);

    NOptional<String> getLocale();

    NSession setLocale(String locale);

    NOptional<NRunAs> getRunAs();

    NSession setRunAs(NRunAs runAs);

    ////////////////////////////////////////
    //COMMANDS
    ////////////////////////////////////
    /// CONFIG
    ////////////////////////////////////
    NSession sudo();

    NSession root();

    NSession currentUser();

    /**
     * return dependency solver Name
     *
     * @return dependency solver Name
     * @since 0.8.3
     */
    String getDependencySolver();

    /**
     * update dependency solver Name
     *
     * @param dependencySolver dependency solver name
     * @return {@code this} instance
     * @since 0.8.3
     */
    NSession setDependencySolver(String dependencySolver);

    void close();
}
