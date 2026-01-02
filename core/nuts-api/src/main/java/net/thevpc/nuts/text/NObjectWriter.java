/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
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
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts.text;

import net.thevpc.nuts.cmdline.NCmdLineConfigurable;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.io.NTerminal;
import net.thevpc.nuts.spi.NComponent;
import net.thevpc.nuts.util.NOptional;

import java.io.File;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.file.Path;

/**
 * Base Format Interface used to print "things".
 *
 * @author thevpc
 * @app.category Format
 * @since 0.5.5
 */
public interface NObjectWriter extends NCmdLineConfigurable, NComponent {
    static NOptional<NObjectWriter> get(Object any) {
        return NTexts.of().resolveWriter(any);
    }

    static NObjectWriter of(Object any) {
        return get(any).get();
    }

//    static <T> NObjectWriter of(T object, NTextFormat<T> format) {
//        NTexts texts = NTexts.of();
//        NAssert.requireNonNull(format, "format");
//        return texts.createFormat(object, format);
//    }

    /**
     * format current value and return the string result
     *
     * @return formatted current value
     */
    NText format(Object aValue);

    /**
     * Format the given value and return a plain (non-decorated) textual representation.
     * This is equivalent to formatting with NTF disabled and extracting filtered text.
     */
    default String formatPlain(Object aValue) {
        boolean ntf = isNtf();
        try {
            return format(aValue).filteredText();
        } finally {
            setNtf(ntf);
        }
    }

    /**
     * format current value and write result to {@code getSession().out()}.
     */
    void print(Object aValue);

    /**
     * format current value and write result to {@code getSession().out()} and
     * finally appends a new line.
     */
    void println(Object aValue);

    /**
     * format current value and write result to {@code out}
     *
     * @param aValue
     * @param out    recipient print stream
     */
    void print(Object aValue, NPrintStream out);

    /**
     * format current value and write result to {@code out}
     *
     * @param aValue
     * @param out    recipient writer
     */
    void print(Object aValue, Writer out);

    /**
     * format current value and write result to {@code out}
     *
     * @param aValue
     * @param out    recipient writer
     */
    void print(Object aValue, OutputStream out);

    /**
     * format current value and write result to {@code out}
     *
     * @param aValue
     * @param out    recipient path
     */
    void print(Object aValue, Path out);

    /**
     * format current value and write result to {@code out}
     *
     * @param aValue
     * @param out    recipient path
     */
    void print(Object aValue, NPath out);

    /**
     * format current value and write result to {@code out}
     *
     * @param aValue
     * @param out    recipient file
     */
    void print(Object aValue, File out);

    /**
     * format current value and write result to {@code terminal}
     *
     * @param aValue
     * @param terminal recipient terminal
     */
    void print(Object aValue, NTerminal terminal);

    /**
     * format current value and write result to {@code out} and finally appends
     * a new line.
     *
     * @param aValue
     * @param out    recipient
     */
    void println(Object aValue, Writer out);

    /**
     * format current value and write result to {@code out} and finally appends
     * a new line.
     *
     * @param aValue
     * @param out    recipient print stream
     */
    void println(Object aValue, NPrintStream out);

    /**
     * format current value and write result to {@code out} and finally appends
     * a new line.
     *
     * @param aValue
     * @param out    recipient print stream
     */
    void println(Object aValue, OutputStream out);

    /**
     * format current value and write result to {@code out} and finally appends
     * a new line.
     *
     * @param aValue
     * @param out    recipient path
     */
    void println(Object aValue, Path out);

    /**
     * format current value and write result to {@code out} and finally appends
     * a new line.
     *
     * @param aValue
     * @param out    recipient path
     */
    void println(Object aValue, NPath out);

    /**
     * format current value and write result to {@code terminal} and finally appends
     * a new line.
     *
     * @param aValue
     * @param terminal recipient terminal
     */
    void println(Object aValue, NTerminal terminal);

    /**
     * format current value and write result to {@code out} and finally appends
     * a new line.
     *
     * @param aValue
     * @param file   recipient file
     */
    void println(Object aValue, File file);

    /// //////////


    /**
     * format current value and write result to {@code getSession().out()}.
     */
    void write(Object aValue);

    /**
     * format current value and write result to {@code getSession().out()} and
     * finally appends a new line.
     */
    void writeln(Object aValue);

    /**
     * format current value and write result to {@code out}
     *
     * @param aValue
     * @param out    recipient print stream
     */
    void write(Object aValue, NPrintStream out);

    /**
     * format current value and write result to {@code out}
     *
     * @param aValue
     * @param out    recipient writer
     */
    void write(Object aValue, Writer out);

    /**
     * format current value and write result to {@code out}
     *
     * @param aValue
     * @param out    recipient writer
     */
    void write(Object aValue, OutputStream out);

    /**
     * format current value and write result to {@code out}
     *
     * @param aValue
     * @param out    recipient path
     */
    void write(Object aValue, Path out);

    /**
     * format current value and write result to {@code out}
     *
     * @param aValue
     * @param out    recipient path
     */
    void write(Object aValue, NPath out);

    /**
     * format current value and write result to {@code out}
     *
     * @param aValue
     * @param out    recipient file
     */
    void write(Object aValue, File out);

    /**
     * format current value and write result to {@code terminal}
     *
     * @param aValue
     * @param terminal recipient terminal
     */
    void write(Object aValue, NTerminal terminal);

    /**
     * format current value and write result to {@code out} and finally appends
     * a new line.
     *
     * @param aValue
     * @param out    recipient
     */
    void writeln(Object aValue, Writer out);

    /**
     * format current value and write result to {@code out} and finally appends
     * a new line.
     *
     * @param aValue
     * @param out    recipient print stream
     */
    void writeln(Object aValue, NPrintStream out);

    /**
     * format current value and write result to {@code out} and finally appends
     * a new line.
     *
     * @param aValue
     * @param out    recipient print stream
     */
    void writeln(Object aValue, OutputStream out);

    /**
     * format current value and write result to {@code out} and finally appends
     * a new line.
     *
     * @param aValue
     * @param out    recipient path
     */
    void writeln(Object aValue, Path out);

    /**
     * format current value and write result to {@code out} and finally appends
     * a new line.
     *
     * @param aValue
     * @param out    recipient path
     */
    void writeln(Object aValue, NPath out);

    /**
     * format current value and write result to {@code terminal} and finally appends
     * a new line.
     *
     * @param aValue
     * @param terminal recipient terminal
     */
    void writeln(Object aValue, NTerminal terminal);

    /**
     * format current value and write result to {@code out} and finally appends
     * a new line.
     *
     * @param aValue
     * @param file   recipient file
     */
    void writeln(Object aValue, File file);

    /// //////////
    /**
     * configure the current command with the given arguments. This is an
     * override of the {@link NCmdLineConfigurable#configure(boolean, java.lang.String...)
     * }
     * to help return a more specific return type;
     *
     * @param skipUnsupported when true, all unsupported options are skipped
     * @param args            argument to configure with
     * @return {@code this} instance
     */
    @Override
    NObjectWriter configure(boolean skipUnsupported, String... args);

    /**
     * true when Nuts Text Format is used for formatting (default)
     *
     * @return true when Nuts Text Format is used for formatting (default)
     */
    boolean isNtf();

    NObjectWriter setNtf(boolean ntf);
}
