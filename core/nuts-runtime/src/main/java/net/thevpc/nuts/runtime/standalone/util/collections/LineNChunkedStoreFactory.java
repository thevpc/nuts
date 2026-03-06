package net.thevpc.nuts.runtime.standalone.util.collections;

import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NChunkedStoreFactory;
import net.thevpc.nuts.util.NOptionalIterator;
import net.thevpc.nuts.util.NOptional;

import java.io.*;
import java.util.function.Consumer;

public class LineNChunkedStoreFactory implements NChunkedStoreFactory<String> {
    @Override
    public NOptionalIterator<String> scanner(InputStream inputStream) {
        return new NOptionalIterator<String>() {
            BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));

            @Override
            public NOptional<String> next() {
                String line = null;
                try {
                    while ((line = in.readLine()) != null) {
                        if (!line.isEmpty()) {
                            return NOptional.of(line);
                        }
                    }
                } catch (IOException e) {
                    throw new NIOException(NMsg.ofC("error reading stream : %s", e).asError(e));
                }
                return NOptional.ofEmpty();
            }

            @Override
            public void close() {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        //
                    }
                    in = null;
                }
            }
        };
    }

    @Override
    public boolean accept(String any) {
        if (any == null) {
            return false;
        }
        if (any.isEmpty()) {
            return false;
        }
        if (any.indexOf('\n') >= 0) {
            return false;
        }
        return any.indexOf('\r') < 0;
    }

    @Override
    public Consumer<String> appender(OutputStream outputStream) {
        return new StringNChunkedStoreAppender(outputStream);
    }

    private static class StringNChunkedStoreAppender implements Consumer<String>,AutoCloseable {
        PrintStream out;

        public StringNChunkedStoreAppender(OutputStream outputStream) {
            out = new PrintStream(outputStream);
        }

        @Override
        public void accept(String item) {
            out.println(item);
        }

        @Override
        public void close() {
            if (out != null) {
                try {
                    out.close();
                } catch (Exception e) {
                    //
                }
                out = null;
            }
        }
    }
}
