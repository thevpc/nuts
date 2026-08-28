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
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Input/Output Internal Programming Interface
 */
public interface NIORPI extends NComponent {
    /**
     * Creates a new instance of of.
     *
     * @return of result
     */
    static NIORPI of() {
        return NExtensions.of(NIORPI.class);
    }

    /**
     * Creates a new instance of create question.
     *
     * @return create question result
     */
    <T> NAsk<T> createQuestion();

    /**
     * Creates a new instance of create question.
     *
     * @param terminal terminal
     * @return create question result
     */
    <T> NAsk<T> createQuestion(NTerminal terminal);

    /**
     * Creates a new instance of create in memory print stream.
     *
     * @return create in memory print stream result
     */
    NMemoryPrintStream createInMemoryPrintStream();

    /**
     * Creates a new instance of create in memory print stream.
     *
     * @param mode mode
     * @return create in memory print stream result
     */
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

    /**
     * Creates a new instance of create print stream.
     *
     * @param out out
     * @param expectedMode expected mode
     * @param baseMode base mode
     * @return create print stream result
     */
    NPrintStream createPrintStream(OutputStream out, NTerminalMode expectedMode, NTerminalMode baseMode);

    /**
     * Creates a new instance of create print stream.
     *
     * @param out out
     * @param mode mode
     * @return create print stream result
     */
    NPrintStream createPrintStream(OutputStream out, NTerminalMode mode);

    /**
     * Creates a new instance of create print stream.
     *
     * @param out out
     * @return create print stream result
     */
    NPrintStream createPrintStream(OutputStream out);

    /**
     * Creates a new instance of create print stream.
     *
     * @param out out
     * @param mode mode
     * @param terminal terminal
     * @return create print stream result
     */
    NPrintStream createPrintStream(Writer out, NTerminalMode mode, NSystemTerminalBase terminal);

    /**
     * Creates a new instance of create print stream.
     *
     * @param out out
     * @param mode mode
     * @return create print stream result
     */
    NPrintStream createPrintStream(Writer out, NTerminalMode mode);

    /**
     * Creates a new instance of create print stream.
     *
     * @param out out
     * @return create print stream result
     */
    NPrintStream createPrintStream(NPath out);

    /**
     * Creates a new instance of create print stream.
     *
     * @param out out
     * @return create print stream result
     */
    NPrintStream createPrintStream(Writer out);

    /**
     * Creates a new instance of create null print stream.
     *
     * @return create null print stream result
     */
    NPrintStream createNullPrintStream();

    /**
     * Creates a new instance of create multi read.
     *
     * @param source source
     * @return create multi read result
     */
    NInputSource createMultiRead(NInputSource source);

    /**
     * Creates a new instance of create input source.
     *
     * @param inputStream input stream
     * @return create input source result
     */
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

    /**
     * Creates a new instance of create input source.
     *
     * @param inputStream input stream
     * @param metadata metadata
     * @return create input source result
     */
    NInputSource createInputSource(InputStream inputStream, NContentMetadata metadata);

    /**
     * Creates a new instance of create input source.
     *
     * @param inputStream input stream
     * @return create input source result
     */
    NInputSource createInputSource(NInputStreamProvider inputStream);

    /**
     * Creates a new instance of create input source.
     *
     * @param inputStream input stream
     * @param metadata metadata
     * @return create input source result
     */
    NInputSource createInputSource(NInputStreamProvider inputStream, NContentMetadata metadata);

    /**
     * Creates a new instance of create input source.
     *
     * @param inputStream input stream
     * @param metadata metadata
     * @return create input source result
     */
    NInputSource createInputSource(NReaderProvider inputStream, NContentMetadata metadata);

    /**
     * Creates a new instance of create input source.
     *
     * @param inputStream input stream
     * @return create input source result
     */
    NInputSource createInputSource(Reader inputStream);

    /**
     * Creates a new instance of create input source.
     *
     * @param inputStream input stream
     * @param metadata metadata
     * @return create input source result
     */
    NInputSource createInputSource(Reader inputStream, NContentMetadata metadata);

    /**
     * Creates a new instance of create input source.
     *
     * @param inputStream input stream
     * @return create input source result
     */
    NInputSource createInputSource(byte[] inputStream);

    /**
     * Creates a new instance of create empty input source.
     *
     * @return create empty input source result
     */
    NInputSource createEmptyInputSource();

    /**
     * Creates a new instance of create input source.
     *
     * @param inputStream input stream
     * @param metadata metadata
     * @return create input source result
     */
    NInputSource createInputSource(byte[] inputStream, NContentMetadata metadata);

    /**
     * Creates a new instance of create output target.
     *
     * @param outputStream output stream
     * @return create output target result
     */
    NOutputTarget createOutputTarget(OutputStream outputStream);

    /**
     * Creates a new instance of create output target.
     *
     * @param outputStream output stream
     * @param metadata metadata
     * @return create output target result
     */
    NOutputTarget createOutputTarget(OutputStream outputStream, NContentMetadata metadata);

    /**
     * Creates a new instance of create output target.
     *
     * @param writer writer
     * @param metadata metadata
     * @return create output target result
     */
    NOutputTarget createOutputTarget(Writer writer, NContentMetadata metadata);

    /**
     * Creates a new instance of create output target.
     *
     * @param writer writer
     * @return create output target result
     */
    NOutputTarget createOutputTarget(Writer writer);

    /**
     * Creates a new instance of create output stream builder.
     *
     * @param base base
     * @return create output stream builder result
     */
    NOutputStreamBuilder createOutputStreamBuilder(OutputStream base);

    /**
     * Creates a new instance of create non blocking input stream.
     *
     * @param base base
     * @return create non blocking input stream result
     */
    NNonBlockingInputStream createNonBlockingInputStream(InputStream base);

    /**
     * Creates a new instance of create interruptible.
     *
     * @param base base
     * @return create interruptible result
     */
    NInterruptible<InputStream> createInterruptible(InputStream base);

    /**
     * Creates a new instance of create input source builder.
     *
     * @param inputStream input stream
     * @return create input source builder result
     */
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


    /**
     * Parse execution entries.
     *
     * @param file file
     * @return parse execution entries result
     */
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

    /**
     * Creates a new instance of create text cursor tracker.
     *
     * @return create text cursor tracker result
     */
    NTextCursorTracker createTextCursorTracker();

    /**
     * Creates a new instance of create text cursor tracker.
     *
     * @param tabSize tab size
     * @param maxRewindDepth max rewind depth
     * @return create text cursor tracker result
     */
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


    /**
     * Creates a new instance of create temp id file.
     *
     * @param name name
     * @param repository repository
     * @return create temp id file result
     */
    NPath createTempIdFile(String name, NId repository);

    /**
     * Creates a new instance of create temp id folder.
     *
     * @param name name
     * @param repository repository
     * @return create temp id folder result
     */
    NPath createTempIdFolder(String name, NId repository);

    /**
     * Creates a new instance of create temp id file.
     *
     * @param repository repository
     * @return create temp id file result
     */
    NPath createTempIdFile(NId repository);

    /**
     * Creates a new instance of create temp id folder.
     *
     * @param repository repository
     * @return create temp id folder result
     */
    NPath createTempIdFolder(NId repository);

    /**
     * expand path to Workspace Location
     *
     * @param path path to expand
     * @return expanded path
     */
    NPath createPath(String path);

    /**
     * Creates a new instance of create path.
     *
     * @param path path
     * @return create path result
     */
    NPath createPath(File path);

    /**
     * Creates a new instance of create path.
     *
     * @param path path
     * @return create path result
     */
    NPath createPath(Path path);

    /**
     * Creates a new instance of create path.
     *
     * @param path path
     * @return create path result
     */
    NPath createPath(URL path);

    /**
     * Creates a new instance of create path.
     *
     * @param path path
     * @param classLoader class loader
     * @return create path result
     */
    NPath createPath(String path, ClassLoader classLoader);

    /**
     * Creates a new instance of create path.
     *
     * @param path path
     * @return create path result
     */
    NPath createPath(NPathSPI path);

    /**
     * Returns the store location.
     *
     * @param nLocationKey n location key
     * @return get store location result
     */
    NPath getStoreLocation(NStoreKey nLocationKey) ;

    /**
     * Creates a new instance of create origins.
     *
     * @param clazz clazz
     * @return create origins result
     */
    List<NPath> createOrigins(Class<?> clazz);

    /**
     * Creates a new instance of create origin.
     *
     * @param clazz clazz
     * @return create origin result
     */
    NOptional<NPath> createOrigin(Class<?> clazz);

    /**
     * detect nuts id from resources containing the given class
     * or null if not found. If multiple resolutions return the first.
     *
     * @param clazz to search for
     * @return nuts id detected from resources containing the given class
     */
    NOptional<NId> resolveId(Class<?> clazz);

    /**
     * Resolve id.
     *
     * @param path path
     * @return resolve id result
     */
    NOptional<NId> resolveId(NPath path);

    /**
     * detect all nuts ids from resources containing the given class.
     *
     * @param clazz to search for
     * @return all nuts ids detected from resources containing the given class
     */
    List<NId> resolveIds(Class<?> clazz);

    /**
     * Resolve ids.
     *
     * @param path path
     * @return resolve ids result
     */
    List<NId> resolveIds(NPath path);


    /**
     * Creates a new instance of create progress runner.
     *
     * @return create progress runner result
     */
    NProgressRunner createProgressRunner();

    /**
     * Creates a new instance of create silent progress monitor.
     *
     * @return create silent progress monitor result
     */
    NProgressMonitor createSilentProgressMonitor();

    /**
     * Current progress monitor.
     *
     * @return current progress monitor result
     */
    NOptional<NProgressMonitor> currentProgressMonitor();

    /**
     * Checks if is silent progress monitor.
     *
     * @param monitor monitor
     * @return is silent progress monitor result
     */
    boolean isSilentProgressMonitor(NProgressMonitor monitor);

    /**
     * Creates a new instance of create silent progress monitor.
     *
     * @param count count
     * @return create silent progress monitor result
     */
    NProgressMonitor[] createSilentProgressMonitor(int count);

    /**
     * Creates a new instance of create logger progress monitor.
     *
     * @param message message
     * @param freq freq
     * @return create logger progress monitor result
     */
    NProgressMonitor createLoggerProgressMonitor(NMsgTemplate message, long freq);

    /**
     * Creates a new instance of create logger progress monitor.
     *
     * @param message message
     * @param freq freq
     * @param out out
     * @return create logger progress monitor result
     */
    NProgressMonitor createLoggerProgressMonitor(NMsgTemplate message, long freq, Logger out);

    /**
     * Creates a new instance of create logger progress monitor.
     *
     * @param message message
     * @param freq freq
     * @param out out
     * @return create logger progress monitor result
     */
    NProgressMonitor createLoggerProgressMonitor(NMsgTemplate message, long freq, NLog out);

    /**
     * Creates a new instance of create out progress monitor.
     *
     * @param freq freq
     * @return create out progress monitor result
     */
    NProgressMonitor createOutProgressMonitor(long freq);

    /**
     * Creates a new instance of create out progress monitor.
     *
     * @param message message
     * @param freq freq
     * @return create out progress monitor result
     */
    NProgressMonitor createOutProgressMonitor(NMsgTemplate message, long freq);

    /**
     * Creates a new instance of create out progress monitor.
     *
     * @param message message
     * @param freq freq
     * @param out out
     * @return create out progress monitor result
     */
    NProgressMonitor createOutProgressMonitor(NMsgTemplate message, long freq, PrintStream out);

    /**
     * Creates a new instance of create print stream progress monitor.
     *
     * @param printStream print stream
     * @return create print stream progress monitor result
     */
    NProgressMonitor createPrintStreamProgressMonitor(PrintStream printStream);

    /**
     * Creates a new instance of create print stream progress monitor.
     *
     * @param messageFormat message format
     * @param printStream print stream
     * @return create print stream progress monitor result
     */
    NProgressMonitor createPrintStreamProgressMonitor(NMsgTemplate messageFormat, PrintStream printStream);

    /**
     * Creates a new instance of create print stream progress monitor.
     *
     * @param printStream print stream
     * @return create print stream progress monitor result
     */
    NProgressMonitor createPrintStreamProgressMonitor(NPrintStream printStream);

    /**
     * Creates a new instance of create print stream progress monitor.
     *
     * @param messageFormat message format
     * @param printStream print stream
     * @return create print stream progress monitor result
     */
    NProgressMonitor createPrintStreamProgressMonitor(NMsgTemplate messageFormat, NPrintStream printStream);

    /**
     * Creates a new instance of create logger progress monitor.
     *
     * @param messageFormat message format
     * @param printStream print stream
     * @return create logger progress monitor result
     */
    NProgressMonitor createLoggerProgressMonitor(NMsgTemplate messageFormat, Logger printStream);

    /**
     * Creates a new instance of create logger progress monitor.
     *
     * @param messageFormat message format
     * @param log log
     * @return create logger progress monitor result
     */
    NProgressMonitor createLoggerProgressMonitor(NMsgTemplate messageFormat, NLog log);

    /**
     * Creates a new instance of create logger progress monitor.
     *
     * @param logger logger
     * @return create logger progress monitor result
     */
    NProgressMonitor createLoggerProgressMonitor(Logger logger);

    /**
     * Creates a new instance of create logger progress monitor.
     *
     * @param logger logger
     * @return create logger progress monitor result
     */
    NProgressMonitor createLoggerProgressMonitor(NLog logger);

    /**
     * Creates a new instance of create logger progress monitor.
     *
     * @param milliseconds milliseconds
     * @return create logger progress monitor result
     */
    NProgressMonitor createLoggerProgressMonitor(long milliseconds);

    /**
     * Creates a new instance of create logger progress monitor.
     *
     * @return create logger progress monitor result
     */
    NProgressMonitor createLoggerProgressMonitor();

    /**
     * Creates a new instance of create out progress monitor.
     *
     * @param messageFormat message format
     * @return create out progress monitor result
     */
    NProgressMonitor createOutProgressMonitor(NMsgTemplate messageFormat);

    /**
     * Creates a new instance of create sys out progress monitor.
     *
     * @return create sys out progress monitor result
     */
    NProgressMonitor createSysOutProgressMonitor();

    /**
     * Creates a new instance of create sys err progress monitor.
     *
     * @return create sys err progress monitor result
     */
    NProgressMonitor createSysErrProgressMonitor();

    /**
     * Creates a new instance of create sys err progress monitor.
     *
     * @param messageFormat message format
     * @return create sys err progress monitor result
     */
    NProgressMonitor createSysErrProgressMonitor(NMsgTemplate messageFormat);

    /**
     * Creates a new instance of create out progress monitor.
     *
     * @return create out progress monitor result
     */
    NProgressMonitor createOutProgressMonitor();

    /**
     * Creates a new instance of create err progress monitor.
     *
     * @return create err progress monitor result
     */
    NProgressMonitor createErrProgressMonitor();

    /**
     * Creates a new instance of create err progress monitor.
     *
     * @param messageFormat message format
     * @return create err progress monitor result
     */
    NProgressMonitor createErrProgressMonitor(NMsgTemplate messageFormat);

    /**
     * Creates a new instance of create progress monitor.
     *
     * @param monitor monitor
     * @return create progress monitor result
     */
    NProgressMonitor createProgressMonitor(NProgressHandler monitor);

    /**
     * Creates a new instance of create progress monitor.
     *
     * @param monitor monitor
     * @return create progress monitor result
     */
    NProgressMonitor createProgressMonitor(NProgressMonitor monitor);

    NPathInfo createPathInfoNotFound(String path);

    NPathInfo createPathInfo(String name, String path, NPathType type, NPathType targetType, String targetPath, long size, boolean symbolicLink, Instant lastModified, Instant lastAccess, Instant creationTime, Set<NPathPermission> permissions, String owner, String group);
}
