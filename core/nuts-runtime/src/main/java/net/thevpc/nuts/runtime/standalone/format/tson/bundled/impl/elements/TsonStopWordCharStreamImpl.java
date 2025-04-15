package net.thevpc.nuts.runtime.standalone.format.tson.bundled.impl.elements;

import net.thevpc.nuts.runtime.standalone.format.tson.bundled.TsonCharStream;
import net.thevpc.nuts.runtime.standalone.format.tson.bundled.TsonElement;
import net.thevpc.nuts.runtime.standalone.format.tson.bundled.TsonElementType;
import net.thevpc.nuts.runtime.standalone.format.tson.bundled.TsonPrimitiveBuilder;
import net.thevpc.nuts.runtime.standalone.format.tson.bundled.impl.builders.TsonPrimitiveElementBuilderImpl;
import net.thevpc.nuts.runtime.standalone.format.tson.bundled.impl.util.AppendableWriter;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.util.Objects;

public class TsonStopWordCharStreamImpl extends AbstractPrimitiveTsonElement implements TsonCharStream {

    private TsonCharStreamSource value;
    private String stopWord;

    public TsonStopWordCharStreamImpl(TsonCharStreamSource value, String stopWord) {
        super(TsonElementType.CHAR_STREAM);
        this.value = value;
        if (stopWord == null || stopWord.isEmpty()) {
            throw new IllegalArgumentException("Illegal empty stop word");
        }
        for (char c : stopWord.toCharArray()) {
            switch (c) {
                case '{':
                case '}':
                case '[':
                case ']':
                case '(':
                case ')':
                case '\"':
                case '\'': {
                    throw new IllegalArgumentException("Illegal stop word : " + stopWord);
                }
                default: {
                    if (Character.isWhitespace(c)) {
                        throw new IllegalArgumentException("Illegal stop word : " + stopWord);
                    }
                }
            }
        }
        this.stopWord = stopWord;
    }

    @Override
    public String getStreamType() {
        return stopWord;
    }

    @Override
    public Reader value() {
        return value.open();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        TsonStopWordCharStreamImpl that = (TsonStopWordCharStreamImpl) o;
        return Objects.equals(value(), that.value());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), value());
    }

    @Override
    public TsonPrimitiveBuilder builder() {
        return new TsonPrimitiveElementBuilderImpl().copyFrom(this);
    }

    @Override
    protected int compareCore(TsonElement o) {
        return this == o ? 1 : 0;
    }

    @Override
    public TsonCharStream toCharStream() {
        return this;
    }

    @Override
    public String stringValue() {
        StringBuilder sb = new StringBuilder();
        try (AppendableWriter w = AppendableWriter.of(sb)) {
            try (Reader r = value()) {
                char[] b = new char[1024];
                int c;
                while ((c = r.read(b)) > 0) {
                    w.write(b, 0, c);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return sb.toString();
    }
}
