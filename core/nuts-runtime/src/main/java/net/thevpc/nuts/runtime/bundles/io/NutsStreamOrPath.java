package net.thevpc.nuts.runtime.bundles.io;

import net.thevpc.nuts.*;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public class NutsStreamOrPath {
    private final Object value;
    private final Type type;
    private final boolean disposable;

    public static NutsStreamOrPath ofSpecial(Object value, Type type) {
        return new NutsStreamOrPath(value,type,false);
    }

    private NutsStreamOrPath(Object value, Type type, boolean disposable) {
        this.value = value;
        this.type = type;
        this.disposable = disposable;
        if (disposable) {
            if (value instanceof NutsPath) {
                if (((NutsPath) value).isFile()) {
                    return;
                }
            }
            throw new IllegalArgumentException("not disposable");
        }
    }

    public static NutsStreamOrPath ofDisposable(NutsPath value) {
        return new NutsStreamOrPath(value, Type.PATH, true);
    }

    public static NutsStreamOrPath of(File value,NutsSession session) {
        return of(NutsPath.of(value,session));
    }

    public static NutsStreamOrPath of(URL value,NutsSession session) {
        return of(NutsPath.of(value,session));
    }

    public static NutsStreamOrPath of(Path value,NutsSession session) {
        return of(NutsPath.of(value,session));
    }

    public static NutsStreamOrPath of(String value,NutsSession session) {
        return of(NutsPath.of(value,session));
    }

    public static NutsStreamOrPath of(NutsPath value) {
        return new NutsStreamOrPath(value, Type.PATH, false);
    }

    public static NutsStreamOrPath of(NutsPrintStream value) {
        return new NutsStreamOrPath(value, Type.NUTS_PRINT_STREAM, false);
    }

    public static NutsStreamOrPath ofAnyInputOrNull(Object value, NutsSession session) {
        if (value == null) {
            return null;
        }
        if (value instanceof InputStream) {
            return of((InputStream) value);
        }
        if (value instanceof byte[]) {
            return of(new ByteArrayInputStream((byte[]) value));
        }
        if (value instanceof NutsPath) {
            return of((NutsPath) value);
        }
        if (value instanceof File) {
            return of((File) value,session);
        }
        if (value instanceof URL) {
            return of((URL) value,session);
        }
        if (value instanceof Path) {
            return of((Path) value,session);
        }
        if (value instanceof String) {
            return of((String) value,session);
        }
        return null;
    }

    public static NutsStreamOrPath of(InputStream value) {
        return new NutsStreamOrPath(value, Type.INPUT_STREAM, false);
    }

    public static NutsStreamOrPath of(OutputStream value) {
        return new NutsStreamOrPath(value, Type.OUTPUT_STREAM, false);
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
        switch (type){
            case PATH:return getPath().getInputStream();
            case INPUT_STREAM:return (InputStream) value;
        }
        throw new IllegalArgumentException("no an input stream");
    }

    public OutputStream getOutputStream() {
        switch (type){
            case PATH:return getPath().getOutputStream();
            case OUTPUT_STREAM:return (OutputStream) value;
            case NUTS_PRINT_STREAM:return ((NutsPrintStream) value).asOutputStream();
        }
        throw new IllegalArgumentException("no an output stream");
    }

    public Object getValue() {
        return value;
    }

    public boolean isPath() {
        return getType() == Type.PATH;
    }

    public NutsInputStreamMetadata getInputStreamMetaData() {
        return NutsInputStreamMetadata.of(getInputStream());
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
            return NutsInputStreamMetadata.of((InputStream) value).getName();
        }
        return value.toString();
    }

    public boolean isDirectory() {
        return (isPath() && getPath().isDirectory());
    }

    public NutsStreamOrPath[] list() {
        if (isPath()) {
            NutsPath p = getPath();
            return p.list().map(NutsStreamOrPath::of)
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
        return getInputStreamMetaData().getContentLength();
    }

    public NutsStreamOrPath setKindType(String s) {
        if(isPath()){
            return of(getPath().setUserKind(s));
        }else if(isInputStream()){
            return of(InputStreamMetadataAwareImpl.of((InputStream) value,new NutsDefaultInputStreamMetadata(getInputStreamMetaData()).setUserKind(s)));
        }
        return this;
    }

    public enum Type {
        PATH,
        INPUT_STREAM,
        OUTPUT_STREAM,
        NUTS_PRINT_STREAM,
        DESCRIPTOR,
        ;
    }

    @Override
    public String toString() {
        return "NutsStreamOrPath{" +
                "value=" + value +
                ", type=" + type +
                ", disposable=" + disposable +
                '}';
    }
}
