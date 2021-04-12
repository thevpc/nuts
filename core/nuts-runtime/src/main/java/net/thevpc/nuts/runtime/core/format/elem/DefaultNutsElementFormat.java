package net.thevpc.nuts.runtime.core.format.elem;

import net.thevpc.nuts.*;

import java.io.*;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import net.thevpc.nuts.runtime.bundles.io.ByteArrayPrintStream;
import net.thevpc.nuts.runtime.core.format.DefaultFormatBase;
import net.thevpc.nuts.runtime.core.format.text.DefaultNutsTextManagerModel;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;

public class DefaultNutsElementFormat extends DefaultFormatBase<NutsElementFormat> implements NutsElementFormat {

    private Object value;
    private NutsContentType contentType = NutsContentType.JSON;
    private boolean compact;
    private final DefaultNutsTextManagerModel helper;

    public DefaultNutsElementFormat(DefaultNutsTextManagerModel helper) {
        super(helper.getWorkspace(), "element-format");
        this.helper = helper;
    }

    @Override
    public NutsContentType getContentType() {
        return contentType;
    }

    @Override
    public NutsElementFormat setContentType(NutsContentType contentType) {
        checkSession();
        if (contentType == null) {
            this.contentType = NutsContentType.JSON;
        } else {
            switch (contentType) {
                case TREE:
                case TABLE:
                case PLAIN: {
                    throw new NutsIllegalArgumentException(getSession(), "invalid content type " + contentType + ". Only structured content types are allowed.");
                }
            }
            this.contentType = contentType;
        }
        return this;
    }

    @Override
    public boolean isCompact() {
        return compact;
    }

    @Override
    public NutsElementFormat setCompact(boolean compact) {
        this.compact = compact;
        return this;
    }

    @Override
    public <T> T parse(URL url, Class<T> clazz) {
        checkSession();

        switch (contentType) {
            case JSON:
            case YAML:
            case XML:
            case TSON: {
                try {
                    try (InputStream is = NutsWorkspaceUtils.of(getSession()).openURL(url)) {
                        return parse(new InputStreamReader(is), clazz);
                    } catch (NutsException ex) {
                        throw ex;
                    } catch (UncheckedIOException ex) {
                        throw new NutsIOException(getSession(), ex);
                    } catch (RuntimeException ex) {
                        throw new NutsParseException(getSession(), "unable to parse url " + url, ex);
                    }
                } catch (IOException ex) {
                    throw new NutsParseException(getSession(), "unable to parse url " + url, ex);
                }
            }
        }
        throw new NutsIllegalArgumentException(getSession(), "invalid content type " + contentType + ". Only structured content types are allowed.");
    }

    @Override
    public <T> T parse(InputStream inputStream, Class<T> clazz) {
        checkSession();
        switch (contentType) {
            case JSON:
            case YAML:
            case XML:
            case TSON: {
                return parse(new InputStreamReader(inputStream), clazz);
            }
        }
        throw new NutsIllegalArgumentException(getSession(), "invalid content type " + contentType + ". Only structured content types are allowed.");
    }

    @Override
    public <T> T parse(String string, Class<T> clazz) {
        checkSession();
        switch (contentType) {
            case JSON:
            case YAML:
            case XML:
            case TSON: {
                return parse(new StringReader(string), clazz);
            }
        }
        throw new NutsIllegalArgumentException(getSession(), "invalid content type " + contentType + ". Only structured content types are allowed.");
    }

    @Override
    public <T> T parse(byte[] bytes, Class<T> clazz) {
        checkSession();
        switch (contentType) {
            case JSON:
            case YAML:
            case XML:
            case TSON: {
                return parse(new InputStreamReader(new ByteArrayInputStream(bytes)), clazz);
            }
        }
        throw new NutsIllegalArgumentException(getSession(), "invalid content type " + contentType + ". Only structured content types are allowed.");
    }

    private NutsElementStreamFormat resolveStucturedFormat() {
        checkSession();
        switch (contentType) {
            case JSON: {
                return helper.getJsonMan(getSession());
            }
            case YAML: {
                return helper.getYamlMan(getSession());
            }
            case XML: {
                return helper.getXmlMan(getSession());
            }
            case TSON: {
                throw new IllegalArgumentException("tson not supported yet");
            }
        }
        throw new NutsIllegalArgumentException(getSession(), "invalid content type " + contentType + ". Only structured content types are allowed.");
    }

    @Override
    public <T> T parse(Reader reader, Class<T> clazz) {
        return (T) elementToObject(resolveStucturedFormat().parseElement(reader, createFactoryContext()), clazz);
    }

    private DefaultNutsElementFactoryContext createFactoryContext() {
        return new DefaultNutsElementFactoryContext(this);
    }

    @Override
    public <T> T parse(Path file, Class<T> clazz) {
        checkSession();
        try (Reader r = Files.newBufferedReader(file)) {
            return parse(r, clazz);
        } catch (IOException ex) {
            throw new NutsIOException(getSession(), ex);
        }
    }

    @Override
    public <T> T parse(File file, Class<T> clazz) {
        checkSession();
        try (FileReader r = new FileReader(file)) {
            return parse(r, clazz);
        } catch (IOException ex) {
            throw new NutsIOException(getSession(), ex);
        }
    }

    @Override
    public boolean configureFirst(NutsCommandLine cmdLine) {
        return false;
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public NutsElementFormat setValue(Object value) {
        this.value = value;
        return this;
    }

    @Override
    public NutsElementPath compilePath(String pathExpression) {
        NutsSession session = getSession();
        if (session == null) {
            session = getWorkspace().createSession();
        }
        return NutsElementPathFilter.compile(pathExpression, session);
    }

//    public String getDefaulTagName() {
//        return defaultName;
//    }
//
//    public boolean isIgnoreNullValue() {
//        return ignoreNullValue;
//    }
//
//    public boolean isAutoResolveType() {
//        return autoResolveType;
//    }
//
//    public String getAttributePrefix() {
//        return attributePrefix;
//    }
//
//    public String getTypeAttributeName() {
//        return typeAttribute;
//    }
    @Override
    public <T> T convert(Object any, Class<T> to) {
        if (to == null || to.isInstance(any)) {
            return (T) any;
        }
        NutsElement e = convertToElement(any);
        return (T) elementToObject(e, to);
    }

    private void print(PrintStream out, NutsElementStreamFormat format) {
        checkSession();
        NutsElement elem = convertToElement(value);
        if (getWorkspace().term().setSession(getSession()).isFormatted(out)) {
            ByteArrayPrintStream bos = new ByteArrayPrintStream();
            format.printElement(elem, bos, compact, createFactoryContext());
            out.print(getWorkspace().formats().text().code(getContentType().id(), bos.toString()));
        } else {
            format.printElement(elem, out, compact, createFactoryContext());
        }
        out.flush();
    }

    @Override
    public void print(PrintStream out) {
        print(out, resolveStucturedFormat());
    }

//    @Override
//    public NutsElement objectToElement(Object o) {
//        return convert(o, NutsElement.class);
//    }
    @Override
    public NutsElement convertToElement(Object o) {
        return createFactoryContext().objectToElement(o, null);
    }

    public Object elementToObject(NutsElement o, Type type) {
        return createFactoryContext().elementToObject(o, type);
    }

    @Override
    public NutsElementEntryBuilder forEntry() {
        return new DefaultNutsElementEntryBuilder(getSession());
    }

    @Override
    public NutsPrimitiveElementBuilder forPrimitive() {
        return new DefaultNutsPrimitiveElementBuilder(getSession());
    }

    @Override
    public NutsObjectElementBuilder forObject() {
        return new DefaultNutsObjectElementBuilder(getSession());
    }

    @Override
    public NutsArrayElementBuilder forArray() {
        return new DefaultNutsArrayElementBuilder(getSession());
    }

    public NutsElementFactoryService getElementFactoryService() {
        return helper.getElementFactoryService(getSession());
    }

    public NutsPrimitiveElement forString(String str) {
        return str == null ? DefaultNutsPrimitiveElementBuilder.NULL : new DefaultNutsPrimitiveElement(NutsElementType.STRING, str);
    }

//    @Override
    public NutsPrimitiveElement forBoolean(boolean value) {
        return value ? DefaultNutsPrimitiveElementBuilder.TRUE : DefaultNutsPrimitiveElementBuilder.FALSE;
    }

//    @Override
    public NutsPrimitiveElement forTrue() {
        return DefaultNutsPrimitiveElementBuilder.TRUE;
    }

//    @Override
    public NutsPrimitiveElement forFalse() {
        return DefaultNutsPrimitiveElementBuilder.FALSE;
    }

}
