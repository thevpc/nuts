package net.thevpc.nuts.runtime.standalone.tson.impl.format;

import net.thevpc.nuts.runtime.standalone.tson.*;
import net.thevpc.nuts.runtime.standalone.tson.impl.util.AppendableWriter;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class TsonWriterImpl implements TsonWriter {
    private TsonSerializer marshaller;
    private TsonFormatBuilder formatBuilder = Tson.format();
    private TsonFormat format = formatBuilder.build();

    public TsonWriterImpl(TsonSerializer marshaller) {
        this.marshaller = marshaller;
    }

    @Override
    public TsonWriter setOptionCompact(boolean configValue) {
        formatBuilder.compact(configValue);
        format = formatBuilder.build();
        return this;
    }

    @Override
    public TsonWriter setOption(String configName, Object configValue) {
        formatBuilder.setOption(configName, configValue);
        format = formatBuilder.build();
        return this;
    }

    /// //////////////////////////////////

    @Override
    public void write(Appendable sb, Object any) {
        try (Writer writer = AppendableWriter.of(sb)) {
            write(writer, any);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
    @Override
    public void write(PrintStream sb, Object any) {
        try (Writer writer = AppendableWriter.of(sb)) {
            write(writer, any);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void write(Path file, Object any) {
        try (Writer writer = Files.newBufferedWriter(file)) {
            write(writer, any);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void write(File file, Object any) {
        try (Writer writer = new FileWriter(file)) {
            write(writer, any);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void write(OutputStream stream, Object any) {
        try (OutputStreamWriter writer = new OutputStreamWriter(stream)) {
            write(writer, any);
            writer.flush();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void write(OutputStream stream, String encoding, Object any) {
        try (OutputStreamWriter writer = new OutputStreamWriter(stream, encoding)) {
            write(writer, any);
            writer.flush();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void write(Writer writer, Object any) {
        TsonElement e = marshaller.serialize(any);
        try {
            writer.write(getFormat().format(e));
        } catch (IOException ee) {
            throw new UncheckedIOException(ee);
        }
    }

    /// //////////////////////////////////

    @Override
    public void writeDocument(Appendable sb, Object any) {
        try (Writer writer = AppendableWriter.of(sb)) {
            writeDocument(writer, any);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void writeDocument(Path file, Object any) {
        try (Writer writer = Files.newBufferedWriter(file)) {
            writeDocument(writer, any);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void writeDocument(File file, Object any) {
        try (Writer writer = new FileWriter(file)) {
            writeDocument(writer, any);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void writeDocument(OutputStream stream, Object any) {
        try (OutputStreamWriter writer = new OutputStreamWriter(stream)) {
            writeDocument(writer, any);
            writer.flush();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void writeDocument(OutputStream stream, String encoding, Object any) {
        try (OutputStreamWriter writer = new OutputStreamWriter(stream, encoding)) {
            writeDocument(writer, any);
            writer.flush();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void writeDocument(Writer writer, Object any) {
        try {
            if (any instanceof TsonDocument) {
                writer.write(getFormat().format((TsonDocument) any));
            } else {
                TsonElement e = marshaller.serialize(any);
                writer.write(getFormat().format(Tson.ofDocument().content(e).build()));
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void write(PrintStream sb, TsonElement any) {
        try (Writer writer = AppendableWriter.of(sb)) {
            write(writer, any);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /// //////////////////////////////////


    @Override
    public void write(Appendable sb, TsonElement any) {
        try (Writer writer = AppendableWriter.of(sb)) {
            write(writer, any);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void write(Path file, TsonElement any) {
        try (Writer writer = Files.newBufferedWriter(file)) {
            write(writer, any);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void write(File file, TsonElement any) {
        try (Writer writer = new FileWriter(file)) {
            write(writer, any);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void write(OutputStream stream, TsonElement any) {
        try (OutputStreamWriter writer = new OutputStreamWriter(stream)) {
            write(writer, any);
            writer.flush();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void write(OutputStream stream, String encoding, TsonElement any) {
        try (OutputStreamWriter writer = new OutputStreamWriter(stream, encoding)) {
            write(writer, any);
            writer.flush();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void write(Writer writer, TsonElement any) {
        try {
            writer.write(getFormat().format(any));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /// //////////////////////////////////

    @Override
    public void writeDocument(Appendable sb, TsonDocument any) {
        try (Writer writer = AppendableWriter.of(sb)) {
            writeDocument(writer, any);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void writeDocument(Path file, TsonDocument any) {
        try (Writer writer = Files.newBufferedWriter(file)) {
            writeDocument(writer, any);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void writeDocument(File file, TsonDocument any) {
        try (Writer writer = new FileWriter(file)) {
            writeDocument(writer, any);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void writeDocument(OutputStream stream, TsonDocument any) {
        try (OutputStreamWriter writer = new OutputStreamWriter(stream)) {
            writeDocument(writer, any);
            writer.flush();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void writeDocument(OutputStream stream, String encoding, TsonDocument any) {
        try (OutputStreamWriter writer = new OutputStreamWriter(stream, encoding)) {
            writeDocument(writer, any);
            writer.flush();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void writeDocument(Writer writer, TsonDocument any) {
        try {
            writer.write(getFormat().format(any));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private TsonFormat getFormat() {
        return format;
    }
}
