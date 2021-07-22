/**
 * ====================================================================
 * thevpc-common-md : Simple Markdown Manipulation Library
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
package net.thevpc.nuts.lib.md;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;

public abstract class AbstractMdWriter implements MdWriter {
    private Writer writer;


    public AbstractMdWriter(Writer writer) {
        this.writer = writer;
    }

    public final void write(MdElement element) {
        writeImpl(element, new WriteContext(null));
    }

    public abstract void writeImpl(MdElement element, WriteContext last);

    protected void write(String text) {
        try {
            writer.write(text);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public void flush() {
        try {
            writer.flush();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    @Override
    public void close() {
        try {
            writer.close();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    protected void writeln() {
        try {
            writer.write("\n");
            writer.flush();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    protected void writeln(String text) {
        try {
            writer.write(text);
            writer.write("\n");
            writer.flush();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public static class WriteContext {
        private MdElement last;

        public WriteContext(MdElement last) {
            this.last = last;
        }

        public boolean isEndWithNewline() {
            return getLast() != null && getLast().isEndWithNewline();
        }

        public boolean hasLast() {
            return getLast()!=null;
        }

        public MdElement getLast() {
            return last;
        }

        public WriteContext setLast(MdElement last) {
            this.last = last;
            return this;
        }
    }
}
