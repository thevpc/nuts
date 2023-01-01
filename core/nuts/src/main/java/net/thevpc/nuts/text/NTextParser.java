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
package net.thevpc.nuts.text;

import net.thevpc.nuts.NSession;
import net.thevpc.nuts.io.NInputSource;
import net.thevpc.nuts.util.NAssert;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.nio.file.Path;

/**
 * @app.category Format
 */
public interface NTextParser {
    static NTextParser of(NSession session) {
        NAssert.requireSession(session);
        return NTexts.of(session).parser();
    }

    long parseIncremental(char buf, NTextVisitor visitor);

    long parse(InputStream in, NTextVisitor visitor);

    long parse(Reader in, NTextVisitor visitor);

    NText parse(InputStream in);

    NText parse(Reader in);

    NText parse(NInputSource in);

    NText parse(File in);

    NText parse(Path in);

    NText parse(URL in);

    long parseIncremental(byte[] buf, int off, int len, NTextVisitor visitor);

    long parseIncremental(char[] buf, int off, int len, NTextVisitor visitor);

    long parseIncremental(byte[] buf, NTextVisitor visitor);

    long parseIncremental(char[] buf, NTextVisitor visitor);

    long parseIncremental(String buf, NTextVisitor visitor);

    long parseRemaining(NTextVisitor visitor);

    boolean isIncomplete();

    void offer(char c);

    void offer(String c);

    void offer(char[] c);

    void offer(char[] c, int offset, int len);

    NText read();

    NText readFully();

    void reset();

    NText parseIncremental(byte[] buf);

    NText parseIncremental(char[] buf);

    NText parseIncremental(String buf);

    NText parseIncremental(char buf);

    NText parseIncremental(byte[] buf, int off, int len);

    NText parseIncremental(char[] buf, int off, int len);

    NText parseRemaining();
}
