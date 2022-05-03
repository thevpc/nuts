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

import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.util.NutsUtils;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;

/**
 * @app.category Format
 */
public interface NutsTextParser {
    static NutsTextParser of(NutsSession session) {
        NutsUtils.requireSession(session);
        return NutsTexts.of(session).parser();
    }

    String escapeText(String text);

    String filterText(String text);

    long parseIncremental(char buf, NutsTextVisitor visitor);

    long parse(InputStream in, NutsTextVisitor visitor);

    long parse(Reader in, NutsTextVisitor visitor);

    NutsText parse(InputStream in);

    NutsText parse(Reader in);

    long parseIncremental(byte[] buf, int off, int len, NutsTextVisitor visitor);

    long parseIncremental(char[] buf, int off, int len, NutsTextVisitor visitor);

    long parseIncremental(byte[] buf, NutsTextVisitor visitor);

    long parseIncremental(char[] buf, NutsTextVisitor visitor);

    long parseIncremental(String buf, NutsTextVisitor visitor);

    long parseRemaining(NutsTextVisitor visitor);

    boolean isIncomplete();

    void offer(char c);

    void offer(String c);

    void offer(char[] c);

    void offer(char[] c, int offset, int len);

    NutsText read();

    NutsText readFully();

    void reset();

    NutsText parseIncremental(byte[] buf);

    NutsText parseIncremental(char[] buf);

    NutsText parseIncremental(String buf);

    NutsText parseIncremental(char buf);

    NutsText parseIncremental(byte[] buf, int off, int len);

    NutsText parseIncremental(char[] buf, int off, int len);

    NutsText parseRemaining();

    NutsText parseResource(String resourceName, NutsTextFormatLoader loader);

    NutsText parseResource(String resourceName, Reader reader, NutsTextFormatLoader loader);

    NutsTextFormatLoader createLoader(ClassLoader loader);

    NutsTextFormatLoader createLoader(File root);


}
