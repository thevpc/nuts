/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
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
package net.thevpc.nuts;

import java.io.InputStream;
import java.time.Instant;
import java.util.Map;
import java.util.logging.Filter;
import java.util.logging.Level;

/**
 * session is context defining common command options and parameters.
 *
 * @author thevpc
 * @app.category Base
 * @since 0.5.4
 */
public interface NutsSession extends NutsCommandLineConfigurable {

    /**
     * When true, operations are invited to print to output stream extra
     * information about processing. Output may be in different formats
     * according to {@link #getOutputFormat()} and {@link #isIterableOut()}
     *
     * @return true if trace flag is armed
     */
    boolean isTrace();

    /**
     * change trace flag value. When true, operations are invited to print to
     * output stream information about processing. Output may be in different
     * formats according to {@link #getOutputFormat()} and
     * {@link #isIterableOut()}
     *
     * @param trace new value
     * @return {@code this} instance
     */
    NutsSession setTrace(Boolean trace);

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

    NutsSession setIterableOut(boolean iterableOut);

    /**
     * true if NON iterable and NON plain formats are armed. equivalent to {@code !isIterableOut()
     * && getOutputFormat() != NutsContentType.PLAIN}
     *
     * @return true if non iterable format AND structured outpt format are
     * armed.
     */
    boolean isStructuredOut();

    NutsArrayElementBuilder getElemOut();

    NutsSession setElemOut(NutsArrayElementBuilder eout);

    /**
     * true if NON iterable and plain format are armed.
     *
     * @return true if non iterable format AND structured outpt format are
     * armed.
     */
    boolean isPlainOut();

    boolean isBot();

    Boolean getBot();

    NutsSession setBot(Boolean bot);

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
     * return current Output Format or {@code defaultValue} if null
     *
     * @param defaultValue value when Output Format is not set
     * @return current Output Format or {@code defaultValue} if null
     */
    NutsContentType getOutputFormat(NutsContentType defaultValue);

    /**
     * return effective trace output format. The effective trace output format
     * is the value of {@code getIterableFormat().getOutputFormat()} whenever {@code getIterableFormat()!=null
     * } otherwise it returns simply the value defined by calling
     * {@link #setOutputFormat(NutsContentType)}. If none of null null null     {@link #setIterableOut(boolean)}
     * {@link #setOutputFormat(NutsContentType)} has been called (or called
     * with null values) {@link NutsContentType#PLAIN} should be returned.
     *
     * @return effective trace output format
     */
    NutsContentType getOutputFormat();

    /**
     * set output format
     *
     * @param outputFormat output format
     * @return {@code this} instance
     */
    NutsSession setOutputFormat(NutsContentType outputFormat);

    /**
     * set json output format
     *
     * @return {@code this} instance
     */
    NutsSession json();

    /**
     * set plain text (default) output format
     *
     * @return {@code this} instance
     */
    NutsSession plain();

    /**
     * set properties output format
     *
     * @return {@code this} instance
     */
    NutsSession props();

    /**
     * set tree output format
     *
     * @return {@code this} instance
     */
    NutsSession tree();

    /**
     * set table output format
     *
     * @return {@code this} instance
     */
    NutsSession table();

    /**
     * set xml output format
     *
     * @return {@code this} instance
     */
    NutsSession xml();

    /**
     * return new instance copy of {@code this} session
     *
     * @return new instance copy of {@code this} session
     */
    NutsSession copy();

    /**
     * copy into this instance from the given value
     *
     * @param other other session to copy from
     * @return return {@code this} instance
     */
    NutsSession copyFrom(NutsSession other);

    /**
     * copy into this instance from the given value
     *
     * @param options other workspace otions to copy from
     * @return return {@code this} instance
     */
    NutsSession copyFrom(NutsWorkspaceOptions options);

    NutsId getAppId();

    NutsSession setAppId(NutsId appId);

    /**
     * return current fetch strategy. When no strategy (or null strategy) was
     * set, return workspace strategy default strategy. When none defines use
     * ONLINE
     *
     * @return {@code this} instance
     */
    NutsFetchStrategy getFetchStrategy();

    /**
     * change fetch strategy
     *
     * @param mode new strategy or null
     * @return {@code this} instance
     */
    NutsSession setFetchStrategy(NutsFetchStrategy mode);

    /**
     * add session listener. supported listeners are instances of:
     * <ul>
     * <li>{@link NutsWorkspaceListener}</li>
     * <li>{@link NutsInstallListener}</li>
     * <li>{@link NutsMapListener}</li>
     * <li>{@link NutsRepositoryListener}</li>
     * </ul>
     *
     * @param listener listener
     * @return {@code this} instance
     */
    NutsSession addListener(NutsListener listener);

    /**
     * remove session listener. supported listeners are instances of:
     * <ul>
     * <li>{@link NutsWorkspaceListener}</li>
     * <li>{@link NutsInstallListener}</li>
     * <li>{@link NutsMapListener}</li>
     * <li>{@link NutsRepositoryListener}</li>
     * </ul>
     *
     * @param listener listener
     * @return {@code this} instance
     */
    NutsSession removeListener(NutsListener listener);

    /**
     * return registered listeners for the given type. Supported types are :
     * <ul>
     * <li>{@link NutsWorkspaceListener}</li>
     * <li>{@link NutsInstallListener}</li>
     * <li>{@link NutsMapListener}</li>
     * <li>{@link NutsRepositoryListener}</li>
     * </ul>
     *
     * @param <T>  listener type
     * @param type listener type class
     * @return registered listeners
     */
    <T extends NutsListener> T[] getListeners(Class<T> type);

    /**
     * return all registered listeners.
     *
     * @return all registered listeners.
     */
    NutsListener[] getListeners();

    /**
     * set session property
     *
     * @param key   property key
     * @param value property value
     * @return {@code this} instance
     */
    NutsSession setProperty(String key, Object value);

    /**
     * return defined properties
     *
     * @return defined properties
     */
    Map<String, Object> getProperties();

    /**
     * add session properties
     *
     * @param properties properties
     * @return {@code this} instance
     */
    NutsSession setProperties(Map<String, Object> properties);

    /**
     * return property value or null
     *
     * @param key property key
     * @return return property value or null
     */
    Object getProperty(String key);

    /**
     * return confirmation mode or {@link NutsConfirmationMode#ASK}
     *
     * @return confirmation mode
     */
    NutsConfirmationMode getConfirm();

    /**
     * set confirm mode.
     *
     * @param confirm confirm type.
     * @return {@code this} instance
     */
    NutsSession setConfirm(NutsConfirmationMode confirm);

    /**
     * add output format options
     *
     * @param options output format options.
     * @return {@code this} instance
     */
    NutsSession addOutputFormatOptions(String... options);

    /**
     * output format options
     *
     * @return output format options
     */
    String[] getOutputFormatOptions();

    /**
     * set output format options (clear and add)
     *
     * @param options output format options.
     * @return {@code this} instance
     */
    NutsSession setOutputFormatOptions(String... options);

    /**
     * current output stream
     *
     * @return current output stream
     */
    NutsPrintStream out();

    InputStream in();

    /**
     * current error stream
     *
     * @return current error stream
     */
    NutsPrintStream err();

    /**
     * return new instance of iterable output
     *
     * @return iterable output
     */
    NutsIterableFormat getIterableOutput();

    /**
     * current terminal
     *
     * @return current terminal
     */
    NutsSessionTerminal getTerminal();

    /**
     * set session terminal
     *
     * @param terminal session terminal
     * @return {@code this} instance
     */
    NutsSession setTerminal(NutsSessionTerminal terminal);

    /**
     * current workspace
     *
     * @return current workspace
     */
    NutsWorkspace getWorkspace();

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
    NutsSession setTransitive(Boolean value);

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
    NutsSession setCached(Boolean value);

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
    NutsSession setIndexed(Boolean value);

    /**
     * return expired date/time or zero if not set. Expire time is used to
     * expire any cached file that was downloaded before the given date/time
     *
     * @return expired date/time or zero
     * @since 0.8.0
     */
    Instant getExpireTime();

    /**
     * set expire instant. Expire time is used to expire any cached file that
     * was downloaded before the given date/time.
     *
     * @param value value
     * @return {@code this} instance
     * @since 0.8.0
     */
    NutsSession setExpireTime(Instant value);

    /**
     * return progress options
     *
     * @return progress options
     */
    String getProgressOptions();

    /**
     * change progress options
     *
     * @param progressOptions options
     * @return {@code this} instance
     */
    NutsSession setProgressOptions(String progressOptions);

    boolean isGui();

    NutsSession setGui(Boolean gui);

    String getErrLinePrefix();

    NutsSession setErrLinePrefix(String errLinePrefix);

    String getOutLinePrefix();

    NutsSession setOutLinePrefix(String outLinePrefix);

    boolean isDry();

    NutsSession setDry(Boolean dry);

    Level getLogTermLevel();

    NutsSession setLogLevel(Level level);

    Filter getLogTermFilter();

    NutsSession setLogFilter(Filter filter);

    /**
     * configure session based on Workspace options
     *
     * @param options options to configure from
     * @return {@code this} instance
     * @since 0.8.1
     */
    NutsSession configure(NutsWorkspaceOptions options);

    Level getLogFileLevel();

    NutsSession setLogFileLevel(Level logFileLevel);

    Filter getLogFileFilter();

    NutsSession setLogFileFilter(Filter logFileFilter);

    NutsArrayElementBuilder eout();

    NutsSession flush();

    NutsExecutionType getExecutionType();

    NutsSession setExecutionType(NutsExecutionType executionType);

    String getDebug();

    NutsSession setDebug(String debug);

    String getLocale();

    NutsSession setLocale(String locale);

    NutsRunAs getRunAs();

    NutsSession setRunAs(NutsRunAs runAs);

    ////////////////////////////////////////

    //COMMANDS

    /**
     * @return new search command instance
     * @since 0.8.3
     */
    NutsSearchCommand search();

    /**
     * @return new fetch command instance
     * @since 0.8.3
     */
    NutsFetchCommand fetch();

    /**
     * @return new deploy command instance
     * @since 0.8.3
     */
    NutsDeployCommand deploy();

    /**
     * @return new undeploy command instance
     * @since 0.8.3
     */
    NutsUndeployCommand undeploy();

    /**
     * @return new execution command instance
     * @since 0.8.3
     */
    NutsExecCommand exec();

    /**
     * @return new installation command instance
     * @since 0.8.3
     */
    NutsInstallCommand install();

    /**
     * @return new un-installation command instance
     * @since 0.8.3
     */
    NutsUninstallCommand uninstall();

    /**
     * @return new update command instance
     * @since 0.8.3
     */
    NutsUpdateCommand update();

    /**
     * @return new push command instance
     * @since 0.8.3
     */
    NutsPushCommand push();

    /**
     * @return new update-stats manager instance
     * @since 0.8.3
     */
    NutsUpdateStatisticsCommand updateStatistics();

    /**
     * create info format instance
     *
     * @return info format
     * @since 0.8.3
     */
    NutsInfoCommand info();

    ////////////////////////////////////
    /// CONFIG
    ////////////////////////////////////

    /**
     * @return new alias/custom command manager instance
     * @since 0.8.3
     */
    NutsCustomCommandManager commands();

    /**
     * @return new extension manager instance
     * @since 0.8.3
     */
    NutsWorkspaceExtensionManager extensions();

    /**
     * @return new config manager instance
     * @since 0.8.3
     */
    NutsWorkspaceConfigManager config();

    /**
     * @return new repo manager instance
     * @since 0.8.3
     */
    NutsRepositoryManager repos();

    /**
     * @return new security manager instance
     * @since 0.8.3
     */
    NutsWorkspaceSecurityManager security();

    /**
     * @return new events manager instance
     * @since 0.8.3
     */

    NutsWorkspaceEventManager events();


    /**
     * @return new import manager instance
     * @since 0.8.3
     */
    NutsImportManager imports();


    /**
     * @return new location manager instance
     * @since 0.8.3
     */
    NutsWorkspaceLocationManager locations();

    /**
     * @return new env manager instance
     * @since 0.8.3
     */
    NutsWorkspaceEnvManager env();

    /**
     * @return new boot manager instance
     * @since 0.8.3
     */
    NutsBootManager boot();

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
    NutsSession setDependencySolver(String dependencySolver);
}
