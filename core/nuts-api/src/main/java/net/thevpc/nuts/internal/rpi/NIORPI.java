package net.thevpc.nuts.internal.rpi;

import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.command.NExecutionEntry;
import net.thevpc.nuts.core.NRepository;
import net.thevpc.nuts.core.NStoreKey;
import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.mon.NProgressHandler;
import net.thevpc.nuts.mon.NProgressMonitor;
import net.thevpc.nuts.mon.NProgressRunner;
import net.thevpc.nuts.spi.NComponent;
import net.thevpc.nuts.spi.NPathSPI;
import net.thevpc.nuts.spi.base.NSystemTerminalBase;
import net.thevpc.nuts.io.NAsk;
import net.thevpc.nuts.text.NMsgTemplate;
import net.thevpc.nuts.util.NOptional;

import java.io.*;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Logger;

/**
 * Input/Output Internal Programming Interface
 */
public interface NIORPI extends NComponent {
    static NIORPI of() {
        return NExtensions.of(NIORPI.class);
    }

    <T> NAsk<T> createQuestion();

    <T> NAsk<T> createQuestion(NTerminal terminal);

    NMemoryPrintStream createInMemoryPrintStream();

    NMemoryPrintStream createInMemoryPrintStream(NTerminalMode mode);

    /**
     * create print stream that supports the given {@code mode}. If the given
     * {@code out} is a PrintStream that supports {@code mode}, it should be
     * returned without modification.
     *
     * @param out      stream to wrap
     * @param mode     mode to support
     * @param terminal terminal
     * @return {@code mode} supporting PrintStream
     */
    NPrintStream createPrintStream(OutputStream out, NTerminalMode mode, NSystemTerminalBase terminal);

    NPrintStream createPrintStream(OutputStream out, NTerminalMode expectedMode, NTerminalMode baseMode);

    NPrintStream createPrintStream(OutputStream out, NTerminalMode mode);

    NPrintStream createPrintStream(OutputStream out);

    NPrintStream createPrintStream(Writer out, NTerminalMode mode, NSystemTerminalBase terminal);

    NPrintStream createPrintStream(Writer out, NTerminalMode mode);

    NPrintStream createPrintStream(NPath out);

    NPrintStream createPrintStream(Writer out);

    NPrintStream createNullPrintStream();

    NInputSource createMultiRead(NInputSource source);

    NInputSource createInputSource(InputStream inputStream);

    /**
     * create input source
     * @param chars chars
     * @return NInputSource
     */
    NInputSource createInputSource(char[] chars);

    /**
     * create input source
     * @param stringValue stringValue
     * @return NInputSource
     */
    NInputSource createInputSource(String stringValue);

    NInputSource createInputSource(InputStream inputStream, NContentMetadata metadata);

    NInputSource createInputSource(NInputStreamProvider inputStream);

    NInputSource createInputSource(NInputStreamProvider inputStream, NContentMetadata metadata);

    NInputSource createInputSource(NReaderProvider inputStream, NContentMetadata metadata);

    NInputSource createInputSource(Reader inputStream);

    NInputSource createInputSource(Reader inputStream, NContentMetadata metadata);

    NInputSource createInputSource(byte[] inputStream);

    NInputSource createEmptyInputSource();

    NInputSource createInputSource(byte[] inputStream, NContentMetadata metadata);

    NOutputTarget createOutputTarget(OutputStream outputStream);

    NOutputTarget createOutputTarget(OutputStream outputStream, NContentMetadata metadata);

    NOutputTarget createOutputTarget(Writer writer, NContentMetadata metadata);

    NOutputTarget createOutputTarget(Writer writer);

    NOutputStreamBuilder createOutputStreamBuilder(OutputStream base);

    NNonBlockingInputStream createNonBlockingInputStream(InputStream base);

    NInterruptible<InputStream> createInterruptible(InputStream base);

    NInputSourceBuilder createInputSourceBuilder(InputStream inputStream);

    /**
     * return new terminal bound to the given session
     *
     * @return new terminal
     */
    NTerminal createTerminal();

    /**
     * return new terminal
     *
     * @param in  in
     * @param out out
     * @param err err
     * @return new terminal
     */
    NTerminal createTerminal(InputStream in, NPrintStream out, NPrintStream err);

    /**
     * return new terminal bound to the given parent terminal and session.
     *
     * @param terminal parent terminal (or null)
     * @return new terminal bound to the given parent terminal and session.
     */
    NTerminal createTerminal(NTerminal terminal);

    /**
     * return a new terminal with empty input and byte-array output/error.
     * Using such terminals help capturing all output/error stream upon execution.
     * This method is equivalent to createMemTerminal(false,session)
     *
     * @return a new terminal with empty input and byte-array output/error.
     */
    NTerminal createInMemoryTerminal();

    /**
     * return a new terminal with empty input and byte-array output/error.
     * Using such terminals help capturing all output/error stream upon execution.
     *
     * @param mergeErr when true out and err are merged into a single stream
     * @return a new terminal with empty input and byte-array output/error.
     */
    NTerminal createInMemoryTerminal(boolean mergeErr);

    /**
     * Checks for the current system terminal and does best effort
     * to enable a rich terminal. Rich terminals add somme features
     * including 'auto-complete'. This Method may replace the system
     * terminal and may even load a nuts extension to enable such features.
     */
    void enableRichTerm();


    List<NExecutionEntry> parseExecutionEntries(NPath file);


//    NExecutionEntry[] parse(NPath file);

    /**
     * parse Execution Entries
     *
     * @param inputStream stream
     * @param type        stream type
     * @param sourceName  stream source name (optional)
     * @return execution entries (class names with main method)
     */
    List<NExecutionEntry> parseExecutionEntries(InputStream inputStream, String type, String sourceName);

    NTextCursorTracker createTextCursorTracker();

    NTextCursorTracker createTextCursorTracker(int tabSize, int maxRewindDepth);

    /**
     * create temp file in the repositoryId's temp folder
     *
     * @param name file name
     * @return newly created file path
     */
    NPath createTempFile(String name);

    /**
     * create temp file in the repositoryId's temp folder
     *
     * @return newly created file path
     */
    NPath createTempFile();

    /**
     * create temp folder in the workspace's temp folder
     *
     * @param name folder name
     * @return newly created temp folder
     */
    NPath createTempFolder(String name);

    /**
     * create temp folder in the workspace's temp folder
     *
     * @return newly created temp folder
     */
    NPath createTempFolder();

    /**
     * create temp file in the repositoryId's temp folder
     *
     * @param name file name
     * @return newly created file path
     */
    NPath createTempRepositoryFile(String name, NRepository repository);

    /**
     * create temp file in the repositoryId's temp folder
     *
     * @return newly created file path
     */
    NPath createTempRepositoryFile(NRepository repository);

    /**
     * create temp folder in the repository's temp folder
     *
     * @param name folder name
     * @return newly created temp folder
     */
    NPath createTempRepositoryFolder(String name, NRepository repository);

    /**
     * create temp folder in the repository's temp folder
     *
     * @return newly created temp folder
     */
    NPath createTempRepositoryFolder(NRepository repository);


    NPath createTempIdFile(String name, NId repository);

    NPath createTempIdFolder(String name, NId repository);

    NPath createTempIdFile(NId repository);

    NPath createTempIdFolder(NId repository);

    /**
     * expand path to Workspace Location
     *
     * @param path path to expand
     * @return expanded path
     */
    NPath createPath(String path);

    NPath createPath(File path);

    NPath createPath(Path path);

    NPath createPath(URL path);

    NPath createPath(String path, ClassLoader classLoader);

    NPath createPath(NPathSPI path);

    NPath getStoreLocation(NStoreKey nLocationKey) ;

    List<NPath> createOrigins(Class<?> clazz);

    NOptional<NPath> createOrigin(Class<?> clazz);

    /**
     * detect nuts id from resources containing the given class
     * or null if not found. If multiple resolutions return the first.
     *
     * @param clazz to search for
     * @return nuts id detected from resources containing the given class
     */
    NOptional<NId> resolveId(Class<?> clazz);

    NOptional<NId> resolveId(NPath path);

    /**
     * detect all nuts ids from resources containing the given class.
     *
     * @param clazz to search for
     * @return all nuts ids detected from resources containing the given class
     */
    List<NId> resolveIds(Class<?> clazz);

    List<NId> resolveIds(NPath path);


    NProgressRunner createProgressRunner();

    NProgressMonitor createSilentProgressMonitor();

    NOptional<NProgressMonitor> currentProgressMonitor();

    boolean isSilentProgressMonitor(NProgressMonitor monitor);

    NProgressMonitor[] createSilentProgressMonitor(int count);

    NProgressMonitor createLoggerProgressMonitor(NMsgTemplate message, long freq);

    NProgressMonitor createLoggerProgressMonitor(NMsgTemplate message, long freq, Logger out);

    NProgressMonitor createLoggerProgressMonitor(NMsgTemplate message, long freq, NLog out);

    NProgressMonitor createOutProgressMonitor(long freq);

    NProgressMonitor createOutProgressMonitor(NMsgTemplate message, long freq);

    NProgressMonitor createOutProgressMonitor(NMsgTemplate message, long freq, PrintStream out);

    NProgressMonitor createPrintStreamProgressMonitor(PrintStream printStream);

    NProgressMonitor createPrintStreamProgressMonitor(NMsgTemplate messageFormat, PrintStream printStream);

    NProgressMonitor createPrintStreamProgressMonitor(NPrintStream printStream);

    NProgressMonitor createPrintStreamProgressMonitor(NMsgTemplate messageFormat, NPrintStream printStream);

    NProgressMonitor createLoggerProgressMonitor(NMsgTemplate messageFormat, Logger printStream);

    NProgressMonitor createLoggerProgressMonitor(NMsgTemplate messageFormat, NLog log);

    NProgressMonitor createLoggerProgressMonitor(Logger logger);

    NProgressMonitor createLoggerProgressMonitor(NLog logger);

    NProgressMonitor createLoggerProgressMonitor(long milliseconds);

    NProgressMonitor createLoggerProgressMonitor();

    NProgressMonitor createOutProgressMonitor(NMsgTemplate messageFormat);

    NProgressMonitor createSysOutProgressMonitor();

    NProgressMonitor createSysErrProgressMonitor();

    NProgressMonitor createSysErrProgressMonitor(NMsgTemplate messageFormat);

    NProgressMonitor createOutProgressMonitor();

    NProgressMonitor createErrProgressMonitor();

    NProgressMonitor createErrProgressMonitor(NMsgTemplate messageFormat);

    NProgressMonitor createProgressMonitor(NProgressHandler monitor);

    NProgressMonitor createProgressMonitor(NProgressMonitor monitor);
}
