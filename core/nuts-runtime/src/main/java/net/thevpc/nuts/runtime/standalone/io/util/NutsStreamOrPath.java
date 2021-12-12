package net.thevpc.nuts.runtime.standalone.io.util;

import net.thevpc.nuts.*;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

public class NutsStreamOrPath {
    private final Object value;
    private final Type type;
    private final boolean disposable;
    private final NutsSession session;

    private NutsStreamOrPath(Object value, Type type, boolean disposable, NutsSession session) {
        this.value = value;
        this.type = type;
        this.disposable = disposable;
        this.session = session;
        if (disposable) {
            if (value instanceof NutsPath) {
                if (((NutsPath) value).isFile()) {
                    return;
                }
            }
            throw new IllegalArgumentException("not disposable");
        }
    }

    public static NutsStreamOrPath ofSpecial(Object value, Type type, NutsSession session) {
        return new NutsStreamOrPath(value, type, false, session);
    }

    public static NutsStreamOrPath ofDisposable(NutsPath value) {
        return new NutsStreamOrPath(value, Type.PATH, true, value.getSession());
    }

    public static NutsStreamOrPath of(File value, NutsSession session) {
        return of(NutsPath.of(value, session));
    }

    public static NutsStreamOrPath of(URL value, NutsSession session) {
        return of(NutsPath.of(value, session));
    }

    public static NutsStreamOrPath of(Path value, NutsSession session) {
        return of(NutsPath.of(value, session));
    }

    public static NutsStreamOrPath of(String value, NutsSession session) {
        return of(NutsPath.of(value, session));
    }

    public static NutsStreamOrPath of(NutsPath value) {
        return new NutsStreamOrPath(value, Type.PATH, false, value.getSession());
    }

    public static NutsStreamOrPath of(NutsPrintStream value) {
        return new NutsStreamOrPath(value, Type.NUTS_PRINT_STREAM, false, value.getSession());
    }

    public static NutsStreamOrPath ofAnyOutputOrErr(Object value, NutsSession session) {
        NutsStreamOrPath a = ofAnyOutputOrNull(value, session);
        if (a != null) {
            return a;
        }
        throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("unsupported output from type %s", value.getClass().getName()));
    }

    public static NutsStreamOrPath ofAnyInputOrErr(Object value, NutsSession session) {
        NutsStreamOrPath a = ofAnyInputOrNull(value, session);
        if (a != null) {
            return a;
        }
        throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("unsupported input from type %s", value.getClass().getName()));
    }

    public static NutsStreamOrPath ofAnyOutputOrNull(Object value, NutsSession session) {
        if (value == null) {
            return null;
        }
        if (value instanceof ByteArrayOutputStream) {
            return of((ByteArrayOutputStream) value,session);
        }
        if (value instanceof byte[]) {
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            try {
                b.write((byte[]) value);
            } catch (IOException e) {
                //ignore!
            }
            return of(b, session);
        }
        if (value instanceof NutsPrintStream) {
            return of((NutsPrintStream) value);
        }
        if (value instanceof OutputStream) {
            return of((OutputStream) value, session);
        }
        if (value instanceof NutsPath) {
            return of((NutsPath) value);
        }
        if (value instanceof File) {
            return of((File) value, session);
        }
        if (value instanceof URL) {
            return of((URL) value, session);
        }
        if (value instanceof Path) {
            return of((Path) value, session);
        }
        return null;
    }

    public static NutsStreamOrPath ofAnyInputOrNull(Object value, NutsSession session) {
        if (value == null) {
            return null;
        }
        if (value instanceof byte[]) {
            return of(new ByteArrayInputStream((byte[]) value), session);
        }
        if (value instanceof InputStream) {
            return of((InputStream) value, session);
        }
        if (value instanceof NutsPath) {
            return of((NutsPath) value);
        }
        if (value instanceof File) {
            return of((File) value, session);
        }
        if (value instanceof URL) {
            return of((URL) value, session);
        }
        if (value instanceof Path) {
            return of((Path) value, session);
        }
        if (value instanceof String) {
            return of((String) value, session);
        }
        return null;
    }

    public static NutsStreamOrPath of(InputStream value, NutsSession session) {
        return new NutsStreamOrPath(value, Type.INPUT_STREAM, false, session);
    }

    public static NutsStreamOrPath of(OutputStream value, NutsSession session) {
        return new NutsStreamOrPath(value, Type.OUTPUT_STREAM, false, session);
    }

    public boolean dispose() {
        if (disposable) {
            try {
                Path f = ((NutsPath) value).toFile();
                if (Files.isRegularFile(f)) {
                    Files.delete(f);
                    return true;
                }
            } catch (IOException e) {
                //
            }
        }
        return false;
    }

    public NutsStreamOrPath toMultiRead(NutsSession session) {
        if (value instanceof NutsPath) {
            return this;
        }
        return toDisposable(session);
    }

    public NutsStreamOrPath toDisposable(NutsSession session) {
        String name = getName();
        Path tempFile = NutsTmp.of(session).createTempFile(name).toFile();
        NutsCp copy = NutsCp.of(session);
        if (type == Type.PATH) {
            copy.from((NutsPath) value);
        } else {
            copy.from((InputStream) value);
        }
        copy.to(tempFile);
        copy.run();
        return ofDisposable(NutsPath.of(tempFile, session));
    }

    public boolean isDisposable() {
        return disposable;
    }

    public NutsPath getPath() {
        return (NutsPath) value;
    }

    public InputStream getInputStream() {
        switch (type) {
            case PATH:
                return getPath().getInputStream();
            case INPUT_STREAM:
                return (InputStream) value;
        }
        throw new IllegalArgumentException("no an input stream");
    }

    public OutputStream getOutputStream() {
        switch (type) {
            case PATH:
                return getPath().getOutputStream();
            case OUTPUT_STREAM:
                return (OutputStream) value;
            case NUTS_PRINT_STREAM:
                return ((NutsPrintStream) value).asOutputStream();
        }
        throw new IllegalArgumentException("no an output stream");
    }

    public Object getValue() {
        return value;
    }

    public boolean isPath() {
        return getType() == Type.PATH;
    }

    public NutsStreamMetadata getStreamMetaData() {
        switch (type) {
            case PATH:
                return getPath().getStreamMetadata();
            case INPUT_STREAM:
            case OUTPUT_STREAM: {
                NutsStreamMetadata q = NutsStreamMetadata.resolve(value);
                if (q == null) {
                    NutsString str = null;
                    if (value instanceof ByteArrayInputStream) {
                        str = NutsTexts.of(session).ofStyled("<memory-buffer>", NutsTextStyle.path());
                    } else if (value instanceof ByteArrayOutputStream) {
                        str = NutsTexts.of(session).ofStyled("<memory-buffer>", NutsTextStyle.path());
                    } else if (value instanceof byte[]) {
                        str = NutsTexts.of(session).ofStyled("<memory-buffer>", NutsTextStyle.path());
                    } else {
                        str = NutsTexts.of(session).ofStyled(value.toString(), NutsTextStyle.path());
                    }
                    q = new NutsDefaultStreamMetadata(str.filteredText(),str,-1,null, null);
                }
                return q;
            }
            case NUTS_PRINT_STREAM: {
                NutsPrintStream q = (NutsPrintStream) value;
                return q.getStreamMetadata();
            }
            case DESCRIPTOR:{
                NutsDescriptor q = (NutsDescriptor) value;
                NutsId id = q.getId();
                NutsString str;
                if(id!=null){
                    str=id.format();
                }else{
                    str = NutsTexts.of(session).ofStyled("<empty-descriptor>", NutsTextStyle.path());
                }
                return new NutsDefaultStreamMetadata(str.filteredText(),str,-1,null, null);
            }
            default: {
                return new NutsDefaultStreamMetadata(
                        value.toString(),
                        NutsTexts.of(session).ofStyled(value.toString(), NutsTextStyle.path()),
                        -1,
                        null, null
                );
            }
        }
    }

    public boolean isInputStream() {
        return getType() == Type.INPUT_STREAM;
    }

    public boolean isOutputStream() {
        return getType() == Type.OUTPUT_STREAM;
    }

    public String getName() {
        if (isPath()) {
            return getPath().getName();
        }
        if (value instanceof InputStream) {
            return NutsStreamMetadata.of((InputStream) value).getName();
        }
        return value.toString();
    }

    public boolean isDirectory() {
        return (isPath() && getPath().isDirectory());
    }

    public NutsStreamOrPath[] list() {
        if (isPath()) {
            NutsPath p = getPath();
            return p.list().map(NutsFunction.of(NutsStreamOrPath::of, "NutsStreamOrPath::of"))
                    .toArray(NutsStreamOrPath[]::new);
        }
        return new NutsStreamOrPath[0];
    }

    public Type getType() {
        return type;
    }

    public long getContentLength() {
        if (isPath()) {
            return getPath().getContentLength();
        }
        return getStreamMetaData().getContentLength();
    }

    public NutsStreamOrPath setKindType(String s) {
        if (isPath()) {
            return of(getPath().setUserKind(s));
        } else if (isInputStream()) {
            NutsStreamMetadata md = getStreamMetaData();
            md.setUserKind(s);
            return this;
        }
        return this;
    }

    public String toString() {
        return String.valueOf(value);
    }

    public enum Type {
        PATH,
        INPUT_STREAM,
        OUTPUT_STREAM,
        NUTS_PRINT_STREAM,
        DESCRIPTOR,
        ;
    }

}
