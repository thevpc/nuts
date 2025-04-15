package net.thevpc.nuts.runtime.standalone.format.tson.bundled.impl.elements;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class TsonCharStreamSource {
    public abstract Reader open();

    public static TsonCharStreamSource of(URL url) {
        return new URLTsonCharSource(url);
    }

    public static TsonCharStreamSource of(File file) {
        return new FileTsonCharSource(file);
    }

    public static TsonCharStreamSource of(Path path) {
        return new PathTsonCharSource(path);
    }

    public static TsonCharStreamSource of(char[] bytes) {
        return new BytesTsonCharSource(bytes);
    }

    public static TsonCharStreamSource of(String bytes) {
        return new BytesTsonCharSource(bytes.toCharArray());
    }

    public static TsonCharStreamSource of(Reader reader) {
        return new ReaderTsonCharStreamSource(reader);
    }

    private static class URLTsonCharSource extends TsonCharStreamSource {
        private URL url;

        public URLTsonCharSource(URL url) {
            this.url = url;
        }

        @Override
        public Reader open() {
            try {
                return new InputStreamReader(url.openStream());
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    private static class BytesTsonCharSource extends TsonCharStreamSource {
        private char[] bytes;

        public BytesTsonCharSource(char[] bytes) {
            this.bytes = bytes;
        }

        @Override
        public Reader open() {
            return new StringReader(new String(bytes));
        }
    }

    private static class StringTsonCharSource extends TsonCharStreamSource {
        private String str;

        public StringTsonCharSource(String str) {
            this.str = str;
        }

        @Override
        public Reader open() {
            return new StringReader(str);
        }
    }

    private static class FileTsonCharSource extends TsonCharStreamSource {
        private File file;

        public FileTsonCharSource(File file) {
            this.file = file;
        }

        @Override
        public Reader open() {
            try {
                return new FileReader(file);
            } catch (FileNotFoundException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    private static class PathTsonCharSource extends TsonCharStreamSource {
        private Path file;

        public PathTsonCharSource(Path file) {
            this.file = file;
        }

        @Override
        public Reader open() {
            try {
                return Files.newBufferedReader(file);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }
}
