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
 *
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
package net.thevpc.nuts.elem;

import net.thevpc.nuts.cmdline.NCmdLineConfigurable;
import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.format.NContentType;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.io.NTerminal;
import net.thevpc.nuts.spi.NComponent;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.time.NProgressFactory;

import java.io.File;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.file.Path;
import java.util.function.Predicate;

/**
 * Class responsible of manipulating {@link NElement} type. It help parsing
 * from, converting to and formatting such types.
 *
 * @author thevpc
 * @app.category Format
 * @since 0.5.5
 */
public interface NElementWriter extends NComponent, NCmdLineConfigurable {


    static NElementWriter of() {
        return NExtensions.of(NElementWriter.class);
    }

    static NElementWriter ofJson() {
        return of().setNtf(false).json();
    }

    static NElementWriter ofProps() {
        return of().setNtf(false).setContentType(NContentType.PROPS);
    }

    static NElementWriter ofXml() {
        return of().setNtf(false).setContentType(NContentType.XML);
    }

    static NElementWriter ofTree() {
        return of().setNtf(false).setContentType(NContentType.TREE);
    }

    static NElementWriter ofTson() {
        return of().setNtf(false).setContentType(NContentType.TSON);
    }

    static NElementWriter ofYaml() {
        return of().setNtf(false).setContentType(NContentType.YAML);
    }

    static NElementWriter ofTable() {
        return of().setNtf(false).setContentType(NContentType.TABLE);
    }

    static NElementWriter ofPlain() {
        return of().setNtf(false).setContentType(NContentType.PLAIN);
    }

    static NElementWriter ofNtfJson() {
        return of().setNtf(true).json();
    }

    static NElementWriter ofNtfProps() {
        return of().setNtf(true).setContentType(NContentType.PROPS);
    }

    static NElementWriter ofNtfXml() {
        return of().setNtf(true).setContentType(NContentType.XML);
    }

    static NElementWriter ofNtfTree() {
        return of().setNtf(true).setContentType(NContentType.TREE);
    }

    static NElementWriter ofNtfTson() {
        return of().setNtf(true).setContentType(NContentType.TSON);
    }

    static NElementWriter ofNtfYaml() {
        return of().setNtf(true).setContentType(NContentType.YAML);
    }

    static NElementWriter ofNtfTable() {
        return of().setNtf(true).setContentType(NContentType.TABLE);
    }

    /**
     * return parse content type
     *
     * @return content type
     * @since 0.8.1
     */
    NContentType getContentType();

    /**
     * set the parse content type. defaults to JSON. Non structured content
     * types are not allowed.
     *
     * @param contentType contentType
     * @return {@code this} instance
     * @since 0.8.1
     */
    NElementWriter setContentType(NContentType contentType);

    NElementWriter json();

    NElementWriter yaml();

    NElementWriter tson();

    NElementWriter xml();

    NElementWriter table();

    NElementWriter tree();

    NElementWriter props();

    /**
     * true is compact json flag is armed
     *
     * @return true is compact json flag is armed
     */
    boolean isCompact();

    /**
     * enable compact json
     *
     * @param compact true to enable compact mode
     * @return {@code this} instance
     */
    NElementWriter setCompact(boolean compact);

    Predicate<Class<?>> getIndestructibleObjects();

    NElementWriter setIndestructibleFormat();

    NElementWriter setIndestructibleObjects(Predicate<Class<?>> destructTypeFilter);


    boolean isLogProgress();

    NElementWriter setLogProgress(boolean logProgress);

    boolean isTraceProgress();

    NElementWriter setTraceProgress(boolean traceProgress);

    NProgressFactory getProgressFactory();

    NElementWriter setProgressFactory(NProgressFactory progressFactory);


    /**
     * format current value and write result to {@code getSession().out()}.
     */
    void write(Object object);

    /**
     * format current value and write result to {@code getSession().out()} and
     * finally appends a new line.
     */
    void writeln(Object object);

    /**
     * format current value and write result to {@code out}
     *
     * @param out recipient print stream
     */
    void write(Object object, NPrintStream out);

    /**
     * format current value and write result to {@code out}
     *
     * @param out recipient writer
     */
    void write(Object object, Writer out);

    /**
     * format current value and write result to {@code out}
     *
     * @param out recipient writer
     */
    void write(Object object, OutputStream out);

    /**
     * format current value and write result to {@code out}
     *
     * @param out recipient path
     */
    void write(Object object, Path out);

    /**
     * format current value and write result to {@code out}
     *
     * @param out recipient path
     */
    void write(Object object, NPath out);

    /**
     * format current value and write result to {@code out}
     *
     * @param out recipient file
     */
    void write(Object object, File out);

    /**
     * format current value and write result to {@code terminal}
     *
     * @param terminal recipient terminal
     */
    void write(Object object, NTerminal terminal);

    /**
     * format current value and write result to {@code out} and finally appends
     * a new line.
     *
     * @param out recipient
     */
    void writeln(Object object, Writer out);

    /**
     * format current value and write result to {@code out} and finally appends
     * a new line.
     *
     * @param out recipient print stream
     */
    void writeln(Object object, NPrintStream out);

    /**
     * format current value and write result to {@code out} and finally appends
     * a new line.
     *
     * @param out recipient print stream
     */
    void writeln(Object object, OutputStream out);

    /**
     * format current value and write result to {@code out} and finally appends
     * a new line.
     *
     * @param out recipient path
     */
    void writeln(Object object, Path out);

    /**
     * format current value and write result to {@code out} and finally appends
     * a new line.
     *
     * @param out recipient path
     */
    void writeln(Object object, NPath out);

    /**
     * format current value and write result to {@code terminal} and finally appends
     * a new line.
     *
     * @param terminal recipient terminal
     */
    void writeln(Object object, NTerminal terminal);

    /**
     * format current value and write result to {@code out} and finally appends
     * a new line.
     *
     * @param file recipient file
     */
    void writeln(Object object, File file);

    String toString(Object object);

    NText toText(Object object);


    boolean isNtf();

    NElementWriter setNtf(boolean nft);


    NElementWriter configure(boolean skipUnsupported, String... args);
}
