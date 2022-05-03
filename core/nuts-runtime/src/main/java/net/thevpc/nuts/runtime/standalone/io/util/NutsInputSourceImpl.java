//package net.thevpc.nuts.runtime.standalone.io.util;
//
//import net.thevpc.nuts.*;
//import net.thevpc.nuts.io.*;
//import net.thevpc.nuts.spi.NutsPaths;
//import net.thevpc.nuts.text.NutsTextStyle;
//import net.thevpc.nuts.text.NutsTexts;
//import net.thevpc.nuts.util.NutsFunction;
//
//import java.io.*;
//import java.net.URL;
//import java.nio.file.Files;
//import java.nio.file.Path;
//
//public class NutsInputSourceImpl implements NutsInputSource{
//    private final Object value;
//    private final Type type;
//    private final boolean disposable;
//    private final NutsSession session;
//
//    private NutsInputSourceImpl(Object value, Type type, boolean disposable, NutsSession session) {
//        this.value = value;
//        this.type = type;
//        this.disposable = disposable;
//        this.session = session;
//        if (disposable) {
//            if (value instanceof NutsPath) {
//                if (((NutsPath) value).isFile()) {
//                    return;
//                }
//            }
//            throw new IllegalArgumentException("not disposable");
//        }
//    }
//
//    public static NutsInputSource ofSpecial(Object value, Type type, NutsSession session) {
//        return new NutsInputSourceImpl(value, type, false, session);
//    }
//
//    public static NutsInputSource ofDisposable(NutsPath value) {
//        return new NutsInputSourceImpl(value, Type.PATH, true, value.getSession());
//    }
//
//    public static NutsInputSource of(File value, NutsSession session) {
//        return of(NutsPath.of(value, session));
//    }
//
//    public static NutsInputSource of(URL value, NutsSession session) {
//        return of(NutsPath.of(value, session));
//    }
//
//    public static NutsInputSource of(Path value, NutsSession session) {
//        return of(NutsPath.of(value, session));
//    }
//
//    public static NutsInputSource of(String value, NutsSession session) {
//        return of(NutsPath.of(value, session));
//    }
//
//    public static NutsInputSource of(NutsInputSource value) {
//        return value;
//    }
//    public static NutsInputSource of(NutsPath value) {
//        return new NutsInputSourceImpl(value, Type.PATH, false, value.getSession());
//    }
//
//    public static NutsInputSource of(NutsPrintStream value) {
//        return new NutsInputSourceImpl(value, Type.NUTS_PRINT_STREAM, false, value.getSession());
//    }
//
//    public static NutsInputSource ofAnyOutputOrErr(Object value, NutsSession session) {
//        NutsInputSource a = ofAnyOutputOrNull(value, session);
//        if (a != null) {
//            return a;
//        }
//        throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("unsupported output from type %s", value.getClass().getName()));
//    }
//
//    public static NutsInputSource ofAnyInputOrErr(Object value, NutsSession session) {
//        NutsInputSource a = ofAnyInputOrNull(value, session);
//        if (a != null) {
//            return a;
//        }
//        throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("unsupported input from type %s", value.getClass().getName()));
//    }
//
//    public static NutsInputSource ofAnyOutputOrNull(Object value, NutsSession session) {
//        if (value == null) {
//            return null;
//        }
//        if (value instanceof ByteArrayOutputStream) {
//            return of((ByteArrayOutputStream) value,session);
//        }
//        if (value instanceof byte[]) {
//            ByteArrayOutputStream b = new ByteArrayOutputStream();
//            try {
//                b.write((byte[]) value);
//            } catch (IOException e) {
//                //ignore!
//            }
//            return of(b, session);
//        }
//        if (value instanceof NutsPrintStream) {
//            return of((NutsPrintStream) value);
//        }
//        if (value instanceof OutputStream) {
//            return of((OutputStream) value, session);
//        }
//        if (value instanceof NutsPath) {
//            return of((NutsPath) value);
//        }
//        if (value instanceof File) {
//            return of((File) value, session);
//        }
//        if (value instanceof URL) {
//            return of((URL) value, session);
//        }
//        if (value instanceof Path) {
//            return of((Path) value, session);
//        }
//        return null;
//    }
//
//    public static NutsInputSource ofAnyInputOrNull(Object value, NutsSession session) {
//        if (value == null) {
//            return null;
//        }
//        if (value instanceof byte[]) {
//            return of(new ByteArrayInputStream((byte[]) value), session);
//        }
//        if (value instanceof InputStream) {
//            return of((InputStream) value, session);
//        }
//        if (value instanceof NutsPath) {
//            return of((NutsPath) value);
//        }
//        if (value instanceof File) {
//            return of((File) value, session);
//        }
//        if (value instanceof URL) {
//            return of((URL) value, session);
//        }
//        if (value instanceof Path) {
//            return of((Path) value, session);
//        }
//        if (value instanceof String) {
//            return of((String) value, session);
//        }
//        return null;
//    }
//
//
//    public static NutsInputSourceImpl of(InputStream value, NutsSession session) {
//        return new NutsInputSourceImpl(value, Type.INPUT_STREAM, false, session);
//    }
//
//    public static NutsInputSourceImpl of(OutputStream value, NutsSession session) {
//        return new NutsInputSourceImpl(value, Type.OUTPUT_STREAM, false, session);
//    }
//
//    public boolean dispose() {
//        if (disposable) {
//            try {
//                Path f = ((NutsPath) value).toFile();
//                if (Files.isRegularFile(f)) {
//                    Files.delete(f);
//                    return true;
//                }
//            } catch (IOException e) {
//                //
//            }
//        }
//        return false;
//    }
//
//    public boolean isMultiRead() {
//        return value instanceof NutsPath;
//    }
//
//    public NutsInputSource toDisposable(NutsSession session) {
//        String name = getName();
//        Path tempFile = NutsPaths.of(session).createTempFile(name,session).toFile();
//        NutsCp copy = NutsCp.of(session);
//        if (type == Type.PATH) {
//            copy.from((NutsPath) value);
//        } else {
//            copy.from((InputStream) value);
//        }
//        copy.to(tempFile);
//        copy.run();
//        return ofDisposable(NutsPath.of(tempFile, session));
//    }
//
//    public boolean isDisposable() {
//        return disposable;
//    }
//
//    public NutsPath getPath() {
//        return (NutsPath) value;
//    }
//
//    public InputStream getInputStream() {
//        switch (type) {
//            case PATH:
//                return getPath().getInputStream();
//            case INPUT_STREAM:
//                return (InputStream) value;
//        }
//        throw new IllegalArgumentException("no an input stream");
//    }
//
//    public OutputStream getOutputStream() {
//        switch (type) {
//            case PATH:
//                return getPath().getOutputStream();
//            case OUTPUT_STREAM:
//                return (OutputStream) value;
//            case NUTS_PRINT_STREAM:
//                return ((NutsPrintStream) value).asOutputStream();
//        }
//        throw new IllegalArgumentException("no an output stream");
//    }
//
//    public Object getValue() {
//        return value;
//    }
//
//    public boolean isPath() {
//        return getType() == Type.PATH;
//    }
//
//    public NutsInputSourceMetadata getInputMetaData() {
//        switch (type) {
//            case PATH:
//                return getPath().toCompressedForm().getInputMetaData();
//            case INPUT_STREAM:
//            case OUTPUT_STREAM: {
//                NutsInputSourceMetadata q = NutsInputSourceMetadata.resolve(value);
//                if (q == null) {
//                    NutsString str = null;
//                    if (value instanceof ByteArrayInputStream) {
//                        str = NutsTexts.of(session).ofStyled("<memory-buffer>", NutsTextStyle.path());
//                    } else if (value instanceof ByteArrayOutputStream) {
//                        str = NutsTexts.of(session).ofStyled("<memory-buffer>", NutsTextStyle.path());
//                    } else if (value instanceof byte[]) {
//                        str = NutsTexts.of(session).ofStyled("<memory-buffer>", NutsTextStyle.path());
//                    } else {
//                        str = NutsTexts.of(session).ofStyled(value.toString(), NutsTextStyle.path());
//                    }
//                    NutsString finalStr = str;
//                    q = new DefaultNutsInputSourceMetadata(str.filteredText(), s-> finalStr,-1,null, null);
//                }
//                return q;
//            }
//            case NUTS_PRINT_STREAM: {
//                NutsPrintStream q = (NutsPrintStream) value;
//                return q.getInputSourceMetadata();
//            }
//            case DESCRIPTOR:{
//                NutsDescriptor q = (NutsDescriptor) value;
//                NutsId id = q.getId();
//                NutsString str;
//                if(id!=null){
//                    str=id.format(session);
//                }else{
//                    str = NutsTexts.of(session).ofStyled("<empty-descriptor>", NutsTextStyle.path());
//                }
//                return new DefaultNutsInputSourceMetadata(str.filteredText(), s->str,-1,null, null);
//            }
//            default: {
//                return new DefaultNutsInputSourceMetadata(
//                        value.toString(),
//                        s->NutsTexts.of(s).ofStyled(value.toString(), NutsTextStyle.path()),
//                        -1,
//                        null, null
//                );
//            }
//        }
//    }
//
//    public boolean isInputStream() {
//        return getType() == Type.INPUT_STREAM;
//    }
//
//    public boolean isOutputStream() {
//        return getType() == Type.OUTPUT_STREAM;
//    }
//
//    public String getName() {
//        if (isPath()) {
//            return getPath().getName();
//        }
//        if (value instanceof InputStream) {
//            return NutsInputSourceMetadata.of((InputStream) value).getName();
//        }
//        return value.toString();
//    }
//
//    public boolean isDirectory() {
//        return (isPath() && getPath().isDirectory());
//    }
//
//    public NutsInputSource[] list() {
//        if (isPath()) {
//            NutsPath p = getPath();
//            return p.list().map(NutsFunction.of(NutsInputSourceImpl::of, "NutsStreamOrPath::of"))
//                    .toArray(NutsInputSource[]::new);
//        }
//        return new NutsInputSource[0];
//    }
//
//    public Type getType() {
//        return type;
//    }
//
//    public long getContentLength() {
//        if (isPath()) {
//            return getPath().getContentLength();
//        }
//        return getInputMetaData().getContentLength();
//    }
//
//    public NutsInputSource setKindType(String s) {
//        if (isPath()) {
//            return of(getPath().setUserKind(s));
//        } else if (isInputStream()) {
//            NutsInputSourceMetadata md = getInputMetaData();
//            md.setUserKind(s);
//            return this;
//        }
//        return this;
//    }
//
//    public String toString() {
//        return String.valueOf(value);
//    }
//
//    public enum Type {
//        PATH,
//        INPUT_STREAM,
//        OUTPUT_STREAM,
//        NUTS_PRINT_STREAM,
//        DESCRIPTOR,
//        ;
//    }
//
//}
