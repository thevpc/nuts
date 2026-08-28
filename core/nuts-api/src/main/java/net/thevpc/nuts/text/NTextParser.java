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

import net.thevpc.nuts.internal.rpi.NTextRPI;
import net.thevpc.nuts.io.NInputSource;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.nio.file.Path;
import java.util.function.Consumer;

/**
 * @app.category Format
 */
public interface NTextParser {
    /**
     * Creates a new instance of of.
     *
     * @return of result
     */
    static NTextParser of() {
        return NTextRPI.of().createParser();
    }

    /**
     * Parse incremental.
     *
     * @param buf buf
     * @param visitor visitor
     * @return parse incremental result
     */
    long parseIncremental(char buf, Consumer<NText> visitor);

    /**
     * Parse.
     *
     * @param in in
     * @param visitor visitor
     * @return parse result
     */
    long parse(InputStream in, Consumer<NText> visitor);

    /**
     * Parse.
     *
     * @param in in
     * @param visitor visitor
     * @return parse result
     */
    long parse(Reader in, Consumer<NText> visitor);

    /**
     * Parse.
     *
     * @param in in
     * @return parse result
     */
    NText parse(InputStream in);

    /**
     * Parse.
     *
     * @param in in
     * @return parse result
     */
    NText parse(Reader in);

    /**
     * Parse.
     *
     * @param in in
     * @return parse result
     */
    NText parse(NInputSource in);

    /**
     * Parse.
     *
     * @param in in
     * @return parse result
     */
    NText parse(File in);

    /**
     * Parse.
     *
     * @param in in
     * @return parse result
     */
    NText parse(Path in);

    /**
     * Parse.
     *
     * @param in in
     * @return parse result
     */
    NText parse(URL in);

    /**
     * Parse incremental.
     *
     * @param buf buf
     * @param off off
     * @param len len
     * @param visitor visitor
     * @return parse incremental result
     */
    long parseIncremental(byte[] buf, int off, int len, Consumer<NText> visitor);

    /**
     * Parse incremental.
     *
     * @param buf buf
     * @param off off
     * @param len len
     * @param visitor visitor
     * @return parse incremental result
     */
    long parseIncremental(char[] buf, int off, int len, Consumer<NText> visitor);

    /**
     * Parse incremental.
     *
     * @param buf buf
     * @param visitor visitor
     * @return parse incremental result
     */
    long parseIncremental(byte[] buf, Consumer<NText> visitor);

    /**
     * Parse incremental.
     *
     * @param buf buf
     * @param visitor visitor
     * @return parse incremental result
     */
    long parseIncremental(char[] buf, Consumer<NText> visitor);

    /**
     * Parse incremental.
     *
     * @param buf buf
     * @param visitor visitor
     * @return parse incremental result
     */
    long parseIncremental(String buf, Consumer<NText> visitor);

    /**
     * Parse remaining.
     *
     * @param visitor visitor
     * @return parse remaining result
     */
    long parseRemaining(Consumer<NText> visitor);

    /**
     * Checks if is incomplete.
     *
     * @return is incomplete result
     */
    boolean isIncomplete();

    /**
     * Creates a new instance of offer.
     *
     * @param c c
     */
    void offer(char c);

    /**
     * Creates a new instance of offer.
     *
     * @param c c
     */
    void offer(String c);

    /**
     * Creates a new instance of offer.
     *
     * @param c c
     */
    void offer(char[] c);

    /**
     * Creates a new instance of offer.
     *
     * @param c c
     * @param offset offset
     * @param len len
     */
    void offer(char[] c, int offset, int len);

    /**
     * Read.
     *
     * @return read result
     */
    NText read();

    /**
     * Read fully.
     *
     * @return read fully result
     */
    NText readFully();

    /**
     * Reset.
     */
    void reset();

    /**
     * Parse incremental.
     *
     * @param buf buf
     * @return parse incremental result
     */
    NText parseIncremental(byte[] buf);

    /**
     * Parse incremental.
     *
     * @param buf buf
     * @return parse incremental result
     */
    NText parseIncremental(char[] buf);

    /**
     * Parse incremental.
     *
     * @param buf buf
     * @return parse incremental result
     */
    NText parseIncremental(String buf);

    /**
     * Parse incremental.
     *
     * @param buf buf
     * @return parse incremental result
     */
    NText parseIncremental(char buf);

    /**
     * Parse incremental.
     *
     * @param buf buf
     * @param off off
     * @param len len
     * @return parse incremental result
     */
    NText parseIncremental(byte[] buf, int off, int len);

    /**
     * Parse incremental.
     *
     * @param buf buf
     * @param off off
     * @param len len
     * @return parse incremental result
     */
    NText parseIncremental(char[] buf, int off, int len);

    /**
     * Parse remaining.
     *
     * @return parse remaining result
     */
    NText parseRemaining();
}
