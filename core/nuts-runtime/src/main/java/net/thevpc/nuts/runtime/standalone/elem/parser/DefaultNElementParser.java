package net.thevpc.nuts.runtime.standalone.elem.parser;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.format.NContentType;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.reflect.NReflectRepository;
import net.thevpc.nuts.runtime.standalone.elem.*;
import net.thevpc.nuts.runtime.standalone.elem.parser.mapperstore.UserElementMapperStore;
import net.thevpc.nuts.runtime.standalone.text.DefaultNTextManagerModel;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceUtils;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.time.NProgressFactory;
import net.thevpc.nuts.util.*;

import java.io.*;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.file.Path;
import java.util.function.Consumer;

public class DefaultNElementParser implements NElementParser {

    //    public static final NutsPrimitiveElement NULL = new DefaultNPrimitiveElement(NutsElementType.NULL, null);
//    public static final NutsPrimitiveElement TRUE = new DefaultNPrimitiveElement(NutsElementType.BOOLEAN, true);
//    public static final NutsPrimitiveElement FALSE = new DefaultNPrimitiveElement(NutsElementType.BOOLEAN, false);

    private final DefaultNTextManagerModel model;
    private NContentType contentType = NContentType.JSON;
    private boolean logProgress;
    private boolean ntf;
    private boolean traceProgress;
    private NProgressFactory progressFactory;
    private UserElementMapperStore userElementMapperStore;


    public DefaultNElementParser() {
        this.model = NWorkspaceExt.of().getModel().textModel;
        userElementMapperStore = new UserElementMapperStore();
        this.userElementMapperStore.setReflectRepository(NReflectRepository.of());
    }


    @Override
    public boolean isNtf() {
        return ntf;
    }

    @Override
    public NElementParser setNtf(boolean ntf) {
        this.ntf = ntf;
        return this;
    }

    @Override
    public boolean isLogProgress() {
        return logProgress;
    }

    @Override
    public NElementParser setLogProgress(boolean logProgress) {
        this.logProgress = logProgress;
        return this;
    }

    @Override
    public boolean isTraceProgress() {
        return traceProgress;
    }

    @Override
    public NElementParser setTraceProgress(boolean traceProgress) {
        this.traceProgress = traceProgress;
        return this;
    }

    @Override
    public NContentType getContentType() {
        return contentType;
    }

    @Override
    public NElementParser setContentType(NContentType contentType) {
        if (contentType == null) {
            this.contentType = NContentType.JSON;
        } else {
//            switch (contentType) {
//                case TREE:
//                case TABLE:
//                case PLAIN: {
//                    throw new NutsIllegalArgumentException(session, "invalid content type " + contentType + ". Only structured content types are allowed.");
//                }
//            }
            this.contentType = contentType;
        }
        return this;
    }

    @Override
    public NElementParser json() {
        return setContentType(NContentType.JSON);
    }

    @Override
    public NElementParser yaml() {
        return setContentType(NContentType.YAML);
    }

    @Override
    public NElementParser tson() {
        return setContentType(NContentType.TSON);
    }

    @Override
    public NElementParser xml() {
        return setContentType(NContentType.XML);
    }

    @Override
    public <T> T parse(URL url, Class<T> clazz) {
        return parse(NPath.of(url), clazz);
    }

    private InputStream prepareInputStream(InputStream is, Object origin) {
        if (isLogProgress() || isTraceProgress()) {
            return NInputStreamMonitor.of()
                    .setSource(is)
                    .setOrigin(origin)
                    .setLogProgress(isLogProgress())
                    .setTraceProgress(isTraceProgress())
                    .setProgressFactory(getProgressFactory())
                    .create();
        }
        return is;
    }

    private InputStream prepareInputStream(NPath path) {
        if (isLogProgress()) {
            return NInputStreamMonitor.of()
                    .setSource(path)
                    .setOrigin(path)
                    .setLogProgress(isLogProgress())
                    .setTraceProgress(isTraceProgress())
                    .setProgressFactory(getProgressFactory())
                    .create();
        }
        return path.getInputStream();
    }

    @Override
    public <T> T parse(NPath path, Class<T> clazz) {
        switch (contentType) {
            case JSON:
            case YAML:
            case XML:
            case TSON: {
                try {
                    try (InputStream is = prepareInputStream(path)) {
                        return parseWithSource(new InputStreamReader(is), clazz, path);
                    } catch (NException ex) {
                        throw new NParseException(NMsg.ofC("unexpected error loading path %s : %s", path, ex), ex);
                    } catch (UncheckedIOException ex) {
                        throw new NParseException(NMsg.ofC("unable to load path %s : %s", path, ex), ex);
                    } catch (RuntimeException ex) {
                        throw new NParseException(NMsg.ofC("unable to parse path %s : %s", path, ex), ex);
                    }
                } catch (IOException ex) {
                    throw new NParseException(NMsg.ofC("unable to load path %s : %s", path, ex), ex);
                }
            }
        }
        throw new NIllegalArgumentException(NMsg.ofC("invalid content type %s. Only structured content types are allowed.", contentType));
    }

    @Override
    public <T> T parse(InputStream inputStream, Class<T> clazz) {
        return parseWithSource(inputStream, clazz, null);
    }

    @Override
    public <T> T parseWithSource(InputStream inputStream, Class<T> clazz, Object source) {
        switch (contentType) {
            case JSON:
            case YAML:
            case XML:
            case TSON: {
                return parseWithSource(new InputStreamReader(prepareInputStream(inputStream, null)), clazz, source);
            }
        }
        throw new NIllegalArgumentException(NMsg.ofC("invalid content type %s. Only structured content types are allowed.", contentType));
    }

    @Override
    public <T> T parse(String string, Class<T> clazz) {
        return parseWithSource(string, clazz, null);
    }

    @Override
    public <T> T parseWithSource(String string, Class<T> clazz, Object source) {
        switch (contentType) {
            case JSON:
            case YAML:
            case XML:
            case TSON: {
                return parseWithSource(new StringReader(string), clazz, source);
            }
        }
        throw new NIllegalArgumentException(NMsg.ofC("invalid content type %s. Only structured content types are allowed.", contentType));
    }

    @Override
    public <T> T parse(byte[] bytes, Class<T> clazz) {
        return parseWithSource(bytes, clazz, null);
    }

    @Override
    public <T> T parseWithSource(byte[] bytes, Class<T> clazz, Object source) {
        switch (contentType) {
            case JSON:
            case YAML:
            case XML:
            case TSON: {
                return parseWithSource(new InputStreamReader(prepareInputStream(new ByteArrayInputStream(bytes), source)), clazz, source);
            }
        }
        throw new NIllegalArgumentException(NMsg.ofC("invalid content type %s. Only structured content types are allowed.", contentType));
    }


    @Override
    public <T> T parse(Reader reader, Class<T> clazz) {
        return parseWithSource(reader, clazz, null);
    }

    @Override
    public <T> T parseWithSource(Reader reader, Class<T> clazz, Object source) {
        return (T) createFactoryContext().createObject(model.getStreamFormat(contentType == null ? NContentType.JSON : contentType).parseElement(reader, createFactoryContext(), source), clazz);
    }

    @Override
    public <T> T parse(Path file, Class<T> clazz) {
        return parse(NPath.of(file), clazz);
    }

    @Override
    public <T> T parse(File file, Class<T> clazz) {
        return parse(NPath.of(file), clazz);
    }

    @Override
    public NElement parse(URL url) {
        return parse(url, NElement.class);
    }

    @Override
    public NElement parse(InputStream inputStream) {
        return parse(inputStream, NElement.class);
    }

    @Override
    public NElement parseWithSource(InputStream inputStream, Object source) {
        return parseWithSource(inputStream, NElement.class, source);
    }

    @Override
    public NElement parse(String string) {
        if (string == null || string.isEmpty()) {
            return NElement.ofNull();
        }
        return parse(string, NElement.class);
    }

    @Override
    public NElement parse(byte[] bytes) {
        return parse(bytes, NElement.class);
    }

    @Override
    public NElement parse(Reader reader) {
        return parse(reader, NElement.class);
    }

    @Override
    public NElement parseWithSource(String string, Object source) {
        if (string == null || string.isEmpty()) {
            return NElement.ofNull();
        }
        return parseWithSource(string, NElement.class, source);
    }

    @Override
    public NElement parseWithSource(byte[] bytes, Object source) {
        return parseWithSource(bytes, NElement.class, source);
    }

    @Override
    public NElement parseWithSource(Reader reader, Object source) {
        return parseWithSource(reader, NElement.class, source);
    }

    @Override
    public NElement parse(Path file) {
        return parse(file, NElement.class);
    }

    @Override
    public NElement parse(File file) {
        return parse(file, NElement.class);
    }

    @Override
    public NElement parse(NPath file) {
        return parse(file, NElement.class);
    }

    @Override
    public NElementMapperStore mapperStore() {
        return userElementMapperStore;
    }

    private DefaultNElementFactoryContext createFactoryContext() {
        NReflectRepository reflectRepository = NWorkspaceUtils.of().getReflectRepository();
        DefaultNElementFactoryContext c = new DefaultNElementFactoryContext(isNtf(), reflectRepository, userElementMapperStore);
        switch (getContentType()) {
            case XML:
            case JSON:
            case TSON:
            case YAML: {
                c.setNtf(false);
                break;
            }
        }
        return c;
    }

    @Override
    public NElementParser doWithMapperStore(Consumer<NElementMapperStore> doWith) {
        if (doWith != null) {
            doWith.accept(mapperStore());
        }
        return this;
    }

    public Object elementToObject(NElement o, Type type) {
        return createFactoryContext().createObject(o, type);
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return NConstants.Support.DEFAULT_SUPPORT;
    }

    @Override
    public NProgressFactory getProgressFactory() {
        return progressFactory;
    }

    @Override
    public NElementParser setProgressFactory(NProgressFactory progressFactory) {
        this.progressFactory = progressFactory;
        return this;
    }

}
