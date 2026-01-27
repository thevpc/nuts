package net.thevpc.nuts.internal.rpi;

import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.command.NExecutionEntry;
import net.thevpc.nuts.core.NRepository;
import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.spi.NComponent;
import net.thevpc.nuts.spi.NPathSPI;
import net.thevpc.nuts.spi.NSystemTerminalBase;
import net.thevpc.nuts.io.NAsk;

import java.io.*;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;

/**
 * Input/Output Internal Programming Interface
 */
public interface NIORPI extends NComponent {
    static NIORPI of() {
        return NExtensions.of(NIORPI.class);
    }

    <T> NAsk<T> createQuestion();

    <T> NAsk<T> createQuestion(NTerminal terminal);

    NMemoryPrintStream ofInMemoryPrintStream();

    NMemoryPrintStream ofInMemoryPrintStream(NTerminalMode mode);

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
    NPrintStream ofPrintStream(OutputStream out, NTerminalMode mode, NSystemTerminalBase terminal);

    NPrintStream ofPrintStream(OutputStream out, NTerminalMode mode);

    NPrintStream ofPrintStream(OutputStream out);

    NPrintStream ofPrintStream(Writer out, NTerminalMode mode, NSystemTerminalBase terminal);

    NPrintStream ofPrintStream(Writer out, NTerminalMode mode);

    NPrintStream ofPrintStream(NPath out);

    NPrintStream ofPrintStream(Writer out);

    NPrintStream ofNullPrintStream();

    NInputSource ofMultiRead(NInputSource source);

    NInputSource ofInputSource(InputStream inputStream);

    NInputSource ofInputSource(InputStream inputStream, NContentMetadata metadata);

    NInputSource ofInputSource(NInputStreamProvider inputStream);

    NInputSource ofInputSource(NInputStreamProvider inputStream, NContentMetadata metadata);
    NInputSource ofInputSource(NReaderProvider inputStream, NContentMetadata metadata);

    NInputSource ofInputSource(Reader inputStream);

    NInputSource ofInputSource(Reader inputStream, NContentMetadata metadata);

    NInputSource ofInputSource(byte[] inputStream);
    NInputSource ofEmptyInputSource();

    NInputSource ofInputSource(byte[] inputStream, NContentMetadata metadata);

    NOutputTarget ofOutputTarget(OutputStream outputStream);

    NOutputTarget ofOutputTarget(OutputStream outputStream, NContentMetadata metadata);

    NOutputTarget ofOutputTarget(Writer writer, NContentMetadata metadata);

    NOutputTarget ofOutputTarget(Writer writer);

    NOutputStreamBuilder ofOutputStreamBuilder(OutputStream base);

    NNonBlockingInputStream ofNonBlockingInputStream(InputStream base);

    NInterruptible<InputStream> ofInterruptible(InputStream base);

    NInputSourceBuilder ofInputSourceBuilder(InputStream inputStream);

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
    NPath ofTempFile(String name);

    /**
     * create temp file in the repositoryId's temp folder
     *
     * @return newly created file path
     */
    NPath ofTempFile();

    /**
     * create temp folder in the workspace's temp folder
     *
     * @param name folder name
     * @return newly created temp folder
     */
    NPath ofTempFolder(String name);

    /**
     * create temp folder in the workspace's temp folder
     *
     * @return newly created temp folder
     */
    NPath ofTempFolder();

    /**
     * create temp file in the repositoryId's temp folder
     *
     * @param name file name
     * @return newly created file path
     */
    NPath ofTempRepositoryFile(String name, NRepository repository);

    /**
     * create temp file in the repositoryId's temp folder
     *
     * @return newly created file path
     */
    NPath ofTempRepositoryFile(NRepository repository);

    /**
     * create temp folder in the repository's temp folder
     *
     * @param name folder name
     * @return newly created temp folder
     */
    NPath ofTempRepositoryFolder(String name, NRepository repository);

    /**
     * create temp folder in the repository's temp folder
     *
     * @return newly created temp folder
     */
    NPath ofTempRepositoryFolder(NRepository repository);


    NPath ofTempIdFile(String name, NId repository);

    NPath ofTempIdFolder(String name, NId repository);

    NPath ofTempIdFile(NId repository);

    NPath ofTempIdFolder(NId repository);

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

}
