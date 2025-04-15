package net.thevpc.nuts.runtime.standalone.tson.impl.elements;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class TsonBinaryStreamSource {
    public abstract InputStream open();

    public static TsonBinaryStreamSource of(URL url) {
        return new URLTsonBinaryStreamSource(url);
    }

    public static TsonBinaryStreamSource of(File url) {
        return new FileTsonBinaryStreamSource(url);
    }

    public static TsonBinaryStreamSource of(Path url) {
        return new PathTsonBinaryStreamSource(url);
    }

    public static TsonBinaryStreamSource of(byte[] url) {
        return new BytesTsonBinaryStreamSource(url);
    }

    public static TsonBinaryStreamSource of(InputStream url) {
        return new InputStreamTsonBinaryStreamSource(url);
    }

    private static class URLTsonBinaryStreamSource extends TsonBinaryStreamSource {
        private URL url;

        public URLTsonBinaryStreamSource(URL url) {
            this.url = url;
        }

        @Override
        public InputStream open() {
            try {
                return url.openStream();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    private static class BytesTsonBinaryStreamSource extends TsonBinaryStreamSource {
        private byte[] bytes;

        public BytesTsonBinaryStreamSource(byte[] bytes) {
            this.bytes = bytes;
        }

        @Override
        public InputStream open() {
            return new ByteArrayInputStream(bytes);
        }
    }

    private static class FileTsonBinaryStreamSource extends TsonBinaryStreamSource {
        private File file;

        public FileTsonBinaryStreamSource(File file) {
            this.file = file;
        }

        @Override
        public InputStream open() {
            try {
                return new FileInputStream(file);
            } catch (FileNotFoundException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    private static class PathTsonBinaryStreamSource extends TsonBinaryStreamSource {
        private Path file;

        public PathTsonBinaryStreamSource(Path file) {
            this.file = file;
        }

        @Override
        public InputStream open() {
            try {
                return Files.newInputStream(file);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }
}
