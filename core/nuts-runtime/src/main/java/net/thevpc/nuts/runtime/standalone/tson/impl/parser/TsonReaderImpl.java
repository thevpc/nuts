package net.thevpc.nuts.runtime.standalone.tson.impl.parser;

import net.thevpc.nuts.runtime.standalone.tson.*;
import net.thevpc.nuts.runtime.standalone.tson.impl.parser.javacc.JavaccHelper;
import net.thevpc.nuts.runtime.standalone.tson.impl.parser.javacc.ParseException;
import net.thevpc.nuts.runtime.standalone.tson.impl.parser.javacc.TsonStreamParserImpl;
import net.thevpc.nuts.runtime.standalone.tson.impl.util.StringBuilderReader;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

public class TsonReaderImpl implements TsonReader {

    private TsonStreamParserConfig config = new TsonStreamParserConfig();
    private TsonSerializer marshaller;

    public TsonReaderImpl(TsonSerializer marshaller) {
        this.marshaller = marshaller;
    }

    @Override
    public TsonReader setOption(String name, Object value) {
        if (name != null) {
            switch (name) {
                case "parser": {
                    break;
                }
                case "skipHeader": {
                    config.setSkipHeader(Boolean.valueOf(String.valueOf(value)));
                    break;
                }
                case "skipComments": {
                    config.setSkipComments(Boolean.valueOf(String.valueOf(value)));
                    break;
                }
                case "rawComments": {
                    config.setRawComments(Boolean.valueOf(String.valueOf(value)));
                    break;
                }
            }
        }
        return this;
    }

    @Override
    public boolean isSkipHeader() {
        return config.isSkipHeader();
    }

    @Override
    public TsonReader setSkipHeader(boolean skipHeader) {
        this.config.setSkipHeader(skipHeader);
        return this;
    }

    @Override
    public boolean isSkipComments() {
        return this.config.isSkipComments();
    }

    @Override
    public TsonReader setSkipComments(boolean skipComments) {
        this.config.setSkipComments(skipComments);
        return this;
    }

    /// //////////////////////////////
    @Override
    public <T> T read(InputStream stream, Class<? extends T> clazz) {
        return read(clazz, _fromReader(new InputStreamReader(stream), "stream"));
    }

    @Override
    public <T> T read(CharSequence string, Class<? extends T> clazz) {
        try (Reader r = createStringReader(string)) {
            return read(clazz, _fromReader(r, "stream"));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public <T> T read(InputStream stream, String encoding, Class<? extends T> clazz) {
        try (Reader rr = new InputStreamReader(stream, encoding == null ? "UTF-8" : encoding)) {
            return read(clazz, _fromReader(rr, "stream"));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public <T> T read(Reader reader, Class<? extends T> clazz) {
        return read(clazz, _fromReader(reader, String.valueOf(reader)));
    }

    @Override
    public <T> T read(File file, Class<? extends T> clazz) {
        try (Reader is = new FileReader(file)) {
            return read(clazz, _fromReader(is, file));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public <T> T read(Path file, Class<? extends T> clazz) {
        try (Reader is = Files.newBufferedReader(file)) {
            return read(clazz, _fromReader(is, file));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public <T> T read(URL url, Class<? extends T> clazz) {
        try (Reader is = new InputStreamReader(url.openStream())) {
            return read(clazz, _fromReader(is, url));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /// //////////////////////////////
    @Override
    public TsonElement readElement(InputStream stream) {
        return readElement(_fromReader(new InputStreamReader(stream), "stream"));
    }

    @Override
    public TsonElement readElement(CharSequence string) {
        try (Reader r = createStringReader(string)) {
            return readElement(_fromReader(r, "stream"));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public TsonElement readElement(InputStream stream, String encoding) {
        try (Reader rr = new InputStreamReader(stream, encoding == null ? "UTF-8" : encoding)) {
            return readElement(_fromReader(rr, "stream"));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public TsonElement readElement(Reader reader) {
        return readElement(_fromReader(reader, "stream"));
    }

    @Override
    public TsonElement readElement(File file) {
        try (Reader is = new FileReader(file)) {
            return readElement(_fromReader(is, file));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public TsonElement readElement(Path file) {
        try (Reader is = Files.newBufferedReader(file)) {
            return readElement(_fromReader(is, file));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public TsonElement readElement(URL url) {
        try (Reader is = new InputStreamReader(url.openStream())) {
            return readElement(_fromReader(is, url));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /// //////////////////////////////

    @Override
    public TsonDocument readDocument(InputStream stream) {
        try (Reader rr = new InputStreamReader(stream)) {
            return readDocument(_fromReader(rr, "stream"));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public TsonDocument readDocument(CharSequence string) {
        try {
            try (Reader r = createStringReader(string)) {
                return readDocument(_fromReader(r, "stream"));
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Reader createStringReader(CharSequence string) {
        return (string instanceof StringBuilder) ? new StringBuilderReader((StringBuilder) string) : new StringReader(string.toString());
    }

    @Override
    public TsonDocument readDocument(InputStream stream, String encoding) {
        try (Reader rr = new InputStreamReader(stream, encoding == null ? "UTF-8" : encoding)) {
            return readDocument(_fromReader(rr, "stream"));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public TsonDocument readDocument(Reader reader) {
        return readDocument(_fromReader(reader, "stream"));
    }

    @Override
    public TsonDocument readDocument(File file) {
        try (Reader is = new FileReader(file)) {
            return readDocument(_fromReader(is, file));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public TsonDocument readDocument(Path file) {
        try (Reader is = Files.newBufferedReader(file)) {
            return readDocument(_fromReader(is, file));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public TsonDocument readDocument(URL url) {
        try (Reader is = new InputStreamReader(url.openStream())) {
            return readDocument(_fromReader(is, url));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /// //////////////////////////////
    @Override
    public void visitElement(InputStream stream, TsonParserVisitor visitor) {
        visitElement(visitor, _fromReader(new InputStreamReader(stream), "stream"));
    }

    @Override
    public void visitElement(CharSequence string, TsonParserVisitor visitor) {
        try (Reader r = createStringReader(string)) {
            visitElement(visitor, _fromReader(r, "stream"));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void visitElement(InputStream stream, String encoding, TsonParserVisitor visitor) {
        try (Reader rr = new InputStreamReader(stream, encoding == null ? "UTF-8" : encoding)) {
            visitElement(visitor, _fromReader(rr, "stream"));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void visitElement(Reader reader, TsonParserVisitor visitor) {
        visitElement(visitor, _fromReader(reader, "stream"));
    }

    @Override
    public void visitElement(File file, TsonParserVisitor visitor) {
        try (Reader is = new FileReader(file)) {
            visitElement(visitor, _fromReader(is, file));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void visitElement(Path file, TsonParserVisitor visitor) {
        try (Reader is = Files.newBufferedReader(file)) {
            visitElement(visitor, _fromReader(is, file));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void visitElement(URL url, TsonParserVisitor visitor) {
        try (Reader is = new InputStreamReader(url.openStream())) {
            visitElement(visitor, _fromReader(is, url));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /// //////////////////////////////
    @Override
    public void visitDocument(InputStream stream, TsonParserVisitor visitor) {
        visitDocument(visitor, _fromReader(new InputStreamReader(stream), "stream"));
    }

    @Override
    public void visitDocument(CharSequence string, TsonParserVisitor visitor) {
        try (Reader r = createStringReader(string)) {
            visitDocument(visitor, _fromReader(r, "stream"));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void visitDocument(InputStream stream, String encoding, TsonParserVisitor visitor) {
        try (Reader rr = new InputStreamReader(stream, encoding == null ? "UTF-8" : encoding)) {
            visitDocument(visitor, _fromReader(rr, "stream"));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void visitDocument(Reader reader, TsonParserVisitor visitor) {
        visitDocument(visitor, _fromReader(reader, "stream"));
    }

    @Override
    public void visitDocument(File file, TsonParserVisitor visitor) {
        try (Reader is = new FileReader(file)) {
            visitDocument(visitor, _fromReader(is, file));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void visitDocument(Path file, TsonParserVisitor visitor) {
        try (Reader is = Files.newBufferedReader(file)) {
            visitDocument(visitor, _fromReader(is, file));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void visitDocument(URL url, TsonParserVisitor visitor) {
        try (Reader is = new InputStreamReader(url.openStream())) {
            visitDocument(visitor, _fromReader(is, url));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void visitElement(String tsonString, TsonParserVisitor visitor) {
        try (StringReader is = new StringReader(tsonString)) {
            visitElement(visitor, _fromReader(is, "string"));
        }
    }

    @Override
    public void visitDocument(String tsonString, TsonParserVisitor visitor) {
        try (StringReader is = new StringReader(tsonString)) {
            visitDocument(visitor, _fromReader(is, "string"));
        }
    }

    @Override
    public <T> T read(TsonElement tson, Class<? extends T> clazz) {
        return marshaller.deserialize(tson, clazz);
    }

    private <T> T read(Class<? extends T> clazz, TsonStreamParser source) {
        ElementBuilderTsonParserVisitor r = new ElementBuilderTsonParserVisitor();
        config.setVisitor(r);
        source.setConfig(config);
        switch (clazz.getName()) {
            case "net.thevpc.nuts.runtime.standalone.tson.TsonElement": {
                try {
                    source.parseElement();
                } catch (Exception e) {
                    throw new TsonParseException(e, source.source());
                }
                return (T) r.getElement();
            }
            case "net.thevpc.nuts.runtime.standalone.tson.TsonDocument": {
                try {
                    source.parseDocument();
                } catch (Exception e) {
                    throw new TsonParseException(e, source.source());
                }
                return (T) r.getDocument();
            }
        }
        try {
            source.parseElement();
        } catch (Exception e) {
            throw new TsonParseException(e, source.source());
        }
        TsonElement elem = r.getElement();
        return marshaller.deserialize(elem, clazz);
    }

    private TsonElement readElement(TsonStreamParser source) {
        ElementBuilderTsonParserVisitor r = new ElementBuilderTsonParserVisitor();
        config.setVisitor(r);
        source.setConfig(config);
        try {
            source.parseElement();
        } catch (Exception e) {
            throw new TsonParseException(e, source.source());
        }
        return r.getElement();
    }

    private TsonDocument readDocument(TsonStreamParser source) {
        ElementBuilderTsonParserVisitor r = new ElementBuilderTsonParserVisitor();
        config.setVisitor(r);
        source.setConfig(config);
        try {
            source.parseDocument();
        } catch (Exception e) {
            throw new TsonParseException(e, source.source());
        }
        return r.getDocument();
    }

    private void visitElement(TsonParserVisitor r, TsonStreamParser source) {
        config.setVisitor(r);
        source.setConfig(config);
        try {
            source.parseElement();
        } catch (Exception e) {
            throw new TsonParseException(e, source.source());
        }
    }

    private void visitDocument(TsonParserVisitor r, TsonStreamParser source) {
        config.setVisitor(r);
        source.setConfig(config);
        try {
            source.parseDocument();
        } catch (Exception e) {
            throw new TsonParseException(e, source.source());
        }
    }

    private TsonStreamParser _fromReader(Reader reader, Object source) {
        TsonStreamParserImpl p = new TsonStreamParserImpl(reader);
        p.source(source);
        return new TsonStreamParser() {
            @Override
            public Object source() {
                return source;
            }

            @Override
            public void setConfig(TsonStreamParserConfig config) {
                p.setConfig(config);
            }

            @Override
            public void parseElement() {
                try{
                    p.parseElement();
                }catch (ParseException e){
                    throw JavaccHelper.createTsonParseException(e,source);
                }
            }

            @Override
            public void parseDocument()  {
                try{
                    p.parseDocument();
                }catch (ParseException e){
                    throw JavaccHelper.createTsonParseException(e,source);
                }
            }
        };
    }

}
