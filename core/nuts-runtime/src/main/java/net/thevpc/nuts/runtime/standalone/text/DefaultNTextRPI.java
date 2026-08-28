package net.thevpc.nuts.runtime.standalone.text;

import net.thevpc.nuts.artifact.*;
import net.thevpc.nuts.command.NExec;
import net.thevpc.nuts.concurrent.NScoredCallable;
import net.thevpc.nuts.elem.NDescribables;
import net.thevpc.nuts.internal.rpi.NTextRPI;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.log.NMsgIntent;
import net.thevpc.nuts.math.NDoubleFormat;
import net.thevpc.nuts.math.NNumberFormat;
import net.thevpc.nuts.pipeline.NStream;
import net.thevpc.nuts.reflect.NScorable;
import net.thevpc.nuts.reflect.NScore;
import net.thevpc.nuts.runtime.standalone.format.NDescriptorInputSourceWriterSPI;
import net.thevpc.nuts.runtime.standalone.format.NDurationWriterSPI;
import net.thevpc.nuts.runtime.standalone.format.NObjectWriterAdapter;
import net.thevpc.nuts.runtime.standalone.format.impl.NChronometerViewWriterSPI;
import net.thevpc.nuts.runtime.standalone.format.impl.NChronometerWriterSPI;
import net.thevpc.nuts.runtime.standalone.io.path.*;
import net.thevpc.nuts.runtime.standalone.io.printstream.NByteArrayPrintStream;
import net.thevpc.nuts.runtime.standalone.io.printstream.OutputStreamExt;
import net.thevpc.nuts.runtime.standalone.io.printstream.OutputTargetExt;
import net.thevpc.nuts.runtime.standalone.io.util.InputStreamExt;
import net.thevpc.nuts.runtime.standalone.io.util.InputStreamTee;
import net.thevpc.nuts.runtime.standalone.io.util.NInputStreamSource;
import net.thevpc.nuts.runtime.standalone.io.util.NNonBlockingInputStreamAdapter;
import net.thevpc.nuts.runtime.standalone.reflect.NUseDefaultUtils;
import net.thevpc.nuts.runtime.standalone.text.art.table.DefaultNTableCellSpecBuilder;
import net.thevpc.nuts.runtime.standalone.text.util.NTextUtils;
import net.thevpc.nuts.runtime.standalone.util.BytesSizeFormat;
import net.thevpc.nuts.runtime.standalone.collections.NClassMapImpl;
import net.thevpc.nuts.runtime.standalone.xtra.digest.DefaultNDigest;
import net.thevpc.nuts.spi.*;
import net.thevpc.nuts.spi.base.NContentMetadataProviderWriterSPI;
import net.thevpc.nuts.mon.NChronometer;
import net.thevpc.nuts.mon.NChronometerView;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.util.NRef;

import net.thevpc.nuts.reflect.NReflectUtils;
import net.thevpc.nuts.runtime.standalone.text.highlighter.CustomStyleCodeHighlighter;
import net.thevpc.nuts.runtime.standalone.text.parser.*;
import net.thevpc.nuts.runtime.standalone.text.util.DefaultNDurationFormat2;
import net.thevpc.nuts.runtime.standalone.text.util.DefaultUnitFormat;
import net.thevpc.nuts.runtime.standalone.util.CoreStringUtils;
import net.thevpc.nuts.reflect.NClassMap;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.text.*;
import net.thevpc.nuts.time.NDuration;
import net.thevpc.nuts.util.*;

import java.io.*;
import java.lang.reflect.Array;
import java.net.URL;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAmount;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.stream.Collectors;

@NComponentScope(NScopeType.SESSION)
@NScore(fixed = NScorable.DEFAULT_SCORE)
public class DefaultNTextRPI implements NTextRPI {

    private final DefaultNTextManagerModel shared;
    private final NClassMap<Object, NTextMapper> textMappers = new NClassMapImpl<>(NTextMapper.class);
    private final NClassMap<Object, NObjectWriterMapper> writerMappers = new NClassMapImpl<>(NObjectWriterMapper.class);
    private final Map<String, Set<NTextFormatProvider>> providers = new HashMap<>();

    public DefaultNTextRPI() {
        this.shared = NWorkspaceExt.of().getModel().textModel;
        registerDefaultTextMappers();
        registerDefaultsObjectWriters();
        registerDefaultTextFormatProviders();
    }

    private void registerTextFormatProvider(NTextFormatProvider provider) {
        NAssert.requireNamedNonNull(provider, "provider");
        String[] types = provider.types();
        if (types != null) {
            for (String type : types) {
                if (!NBlankable.isBlank(type)) {
                    String type2 = NNameFormat.LOWER_KEBAB_CASE.format(NStringUtils.strip(type));
                    providers.computeIfAbsent(type2, r -> new LinkedHashSet<>()).add(provider);
                }
            }
        }
    }

    private void registerDefaultTextMappers() {
        registerTextMapper(NFormatted.class, (o) -> (((NFormatted) o).format()));
        registerTextMapper(NTextFormattable.class, (o) -> (NText) o);
        registerTextMapper(NMsgFormattable.class, (o) -> _NMsg_toString((((NMsgFormattable) o).toMsg())));
        registerTextMapper(NMsg.class, (o) -> _NMsg_toString((NMsg) o));
        registerTextMapper(NText.class, (o) -> (NText) o);
        registerTextMapper(InputStream.class, (o) -> {
            NContentMetadata metaData = NInputSource.of((InputStream) o).metaData();
            return NText.ofStyled(metaData.name().orElse(o.toString()), NTextStyle.path());
        });
        registerTextMapper(OutputStream.class, (o) -> NText.ofStyled(o.toString(), NTextStyle.path()));
        registerTextMapper(NPrintStream.class, (o) -> NText.ofStyled(o.toString(), NTextStyle.path()));
        registerTextMapper(Writer.class, (o) -> NText.ofStyled(o.toString(), NTextStyle.path()));
        registerTextMapper(NEnum.class, (o) -> NText.ofStyled(((NEnum) o).id(), NTextStyle.option()));
        registerTextMapper(Enum.class, (o) -> (o instanceof NEnum) ? NText.ofStyled(((NEnum) o).id(), NTextStyle.option()) : createStyled(((Enum<?>) o).name(), NTextStyle.option()));
        registerTextMapper(Number.class, (o) -> NText.ofStyled(o.toString(), NTextStyle.number()));
        registerTextMapper(Date.class, (o) -> NText.ofStyled(o.toString(), NTextStyle.date()));
        registerTextMapper(Temporal.class, (o) -> NText.ofStyled(o.toString(), NTextStyle.date()));
        registerTextMapper(TemporalAmount.class, (o) -> NText.ofStyled(o.toString(), NTextStyle.date()));
        registerTextMapper(Boolean.class, (o) -> NText.ofStyled(o.toString(), NTextStyle.bool()));
        registerTextMapper(Path.class, (o) -> NText.ofStyled(o.toString(), NTextStyle.path()));
        registerTextMapper(File.class, (o) -> NText.ofStyled(o.toString(), NTextStyle.path()));
        registerTextMapper(URL.class, (o) -> NText.ofStyled(o.toString(), NTextStyle.path()));
        registerTextMapper(NTreeNode.class, (o) -> NTextArt.of().treeRenderer().get().render((NTreeNode) o));
        registerTextMapper(NTableModel.class, (o) -> NTextArt.of().tableRenderer().get().render((NTableModel) o));
        registerTextMapper(Class.class, (o) -> {
            Class cc = (Class) o;
            Class dc = cc.getDeclaringClass();
            if (dc != null) {
                NText p = NText.of(dc);
                NTextBuilder tb = new DefaultNTextBuilder();
                tb.append(p);
                tb.append(NText.ofStyled(".", NTextStyle.comments()));
                tb.append(NText.ofStyled(cc.getSimpleName(), NTextStyle.option()));
                return tb.build();
            } else {
                NTextBuilder tb = new DefaultNTextBuilder();
                Package p = cc.getPackage();
                if (p != null) {
                    tb.append(NText.ofStyled(p.getName(), NTextStyle.comments()));
                    tb.append(NText.ofStyled(".", NTextStyle.comments()));
                }
                tb.append(NText.ofStyled(cc.getSimpleName(), NTextStyle.info()));
                return tb.build();
            }
        });
        registerTextMapper(Level.class, (o) -> {
            switch (((Level) o).getName()) {
                case "OFF":
                    return NText.ofStyled(o.toString(), NTextStyle.pale());
                case "SEVERE":
                    return NText.ofStyled(o.toString(), NTextStyle.error());
                case "WARNING":
                    return NText.ofStyled(o.toString(), NTextStyle.warn());
                case "INFO":
                    return NText.ofStyled(o.toString(), NTextStyle.info());
                case "CONFIG":
                    return NText.ofStyled(o.toString(), NTextStyle.config());
                case "FINE":
                case "FINER":
                case "FINEST":
                    return NText.ofStyled(o.toString(), NTextStyle.pale());
                case "ALL":
                    return NText.ofStyled(o.toString(), NTextStyle.success());
                default:
                    return NText.ofStyled(o.toString(), NTextStyle.bold());
            }
        });
        registerTextMapper(Throwable.class, (o) -> NText.ofStyled(
                createText(CoreStringUtils.exceptionToMessage((Throwable) o)),
                NTextStyle.error()
        ));
        registerTextMapper(Collection.class, (o) -> {
            NTextBuilder b = createBuilder();
            b.append("[", NTextStyle.separator());
            boolean first = true;
            for (Object v : ((Collection) o)) {
                if (!first) {
                    b.append(",", NTextStyle.separator());
                    b.append(" ");
                } else {
                    first = false;
                }
                b.append(NText.of(v));
            }
            b.append("]", NTextStyle.separator());
            return b.build();
        });
        registerTextMapper(Map.Entry.class, (o) -> {
            NTextBuilder b = createBuilder();
            Map.Entry e = (Map.Entry) o;
            b.append(NText.of(e.getKey()));
            b.append(":", NTextStyle.separator());
            b.append(" ");
            b.append(NText.of(e.getValue()));
            return b.build();
        });
        registerTextMapper(Map.class, (o) -> {
            NTextBuilder b = createBuilder();
            b.append("{", NTextStyle.separator());
            boolean first = true;
            for (Map.Entry<?, ?> v : ((Map<?, ?>) o).entrySet()) {
                if (!first) {
                    b.append(",", NTextStyle.separator());
                    b.append(" ");
                } else {
                    first = false;
                }
                b.append(NText.of(v));
            }
            b.append("}", NTextStyle.separator());
            return b.build();
        });
    }

    private void registerDefaultTextFormatProviders() {
        registerTextFormatProvider(new NTextFormatProvider() {
            @Override
            public String[] types() {
                return new String[]{"duration", "time", "period"};
            }

            @Override
            public <T> NScoredCallable<NTextFormat<T>> resolveFormat(String pattern, Class<T> expectedType) {
                if (NDuration.class.equals(expectedType)) {
                    return NScoredCallable.ofValid((NTextFormat<T>) new DurationNTextFormatFromNDuration(pattern));
                }
                if (Duration.class.equals(expectedType)) {
                    return NScoredCallable.ofValid((NTextFormat<T>) new DurationNTextFormatFromDuration(pattern));
                }
                if (Number.class.equals(expectedType)) {
                    return NScoredCallable.ofValid((NTextFormat<T>) new DurationNTextFormatFromNumber(pattern));
                }
                return NScoredCallable.ofInvalid(NMsg.ofC("unknown duration format with type %s. Expected Duration or NDuration.", expectedType));
            }
        });
        registerTextFormatProvider(new NTextFormatProvider() {
            @Override
            public String[] types() {
                return new String[]{"double", "decimal", "number"};
            }

            @Override
            public <T> NScoredCallable<NTextFormat<T>> resolveFormat(String pattern, Class<T> expectedType) {
                if (Number.class.isAssignableFrom(expectedType)) {
                    return NScoredCallable.ofValid(new CustomNumberNTextFormat<T>(pattern, expectedType));
                }
                return NScoredCallable.ofInvalid(NMsg.ofC("unknown duration format with type %s. Expected Number.", expectedType));
            }
        });

        registerTextFormatProvider(new NTextFormatProvider() {
            @Override
            public String[] types() {
                return new String[]{"m", "meter", "meters", "metric", "distance"};
            }

            @Override
            public <T> NScoredCallable<NTextFormat<T>> resolveFormat(String pattern, Class<T> expectedType) {
                if (Number.class.isAssignableFrom(expectedType)) {
                    String p = NStringUtils.strip(pattern);
                    return NScoredCallable.ofValid((NTextFormat<T>) new DefaultUnitFormat("m " + (p.isEmpty() ? "M-3 M3 I2 D2" : p)));
                }
                return NScoredCallable.ofInvalid(NMsg.ofC("unknown metric format with type %s. Expected Number.", expectedType));
            }
        });

        registerTextFormatProvider(new NTextFormatProvider() {
            @Override
            public String[] types() {
                return new String[]{"memory", "bytes", "byte", "size"};
            }

            @Override
            public <T> NScoredCallable<NTextFormat<T>> resolveFormat(String pattern, Class<T> expectedType) {
                if (Number.class.isAssignableFrom(expectedType)) {
                    String p = NStringUtils.strip(pattern);
                    return NScoredCallable.ofValid((NTextFormat<T>) new BytesSizeFormat(p));
                }
                return NScoredCallable.ofInvalid(NMsg.ofC("unknown memory format with type %s. Expected Number.", expectedType));
            }
        });

        registerTextFormatProvider(new NTextFormatProvider() {
            @Override
            public String[] types() {
                return new String[]{"freq", "freqs", "frequency", "frequencies", "hz"};
            }

            @Override
            public <T> NScoredCallable<NTextFormat<T>> resolveFormat(String pattern, Class<T> expectedType) {
                if (Number.class.isAssignableFrom(expectedType)) {
                    String p = NStringUtils.strip(pattern);
                    return NScoredCallable.ofValid((NTextFormat<T>) new DefaultUnitFormat("Hz " + (p.isEmpty() ? "M1 M12 I2 D3" : p)));
                }
                return NScoredCallable.ofInvalid(NMsg.ofC("unknown frequency format with type %s. Expected Number.", expectedType));
            }
        });
    }

    private void registerDefaultsObjectWriters() {
        registerObjectWriter(NExec.class, (o, f) -> NExecWriter.of());

        registerObjectWriter(NVersion.class, (o, f) -> NVersionWriter.of());

        registerObjectWriter(NId.class, (o, f) -> NIdWriter.of());
        registerObjectWriterFromConverter(NIdBuilder.class, o -> ((NIdBuilder) o).build());

        registerObjectWriter(NDescriptor.class, (o, f) -> NDescriptorWriter.of());
        registerObjectWriterFromConverter(NDescriptorBuilder.class, o -> ((NDescriptorBuilder) o).build());

        registerObjectWriter(NDependency.class, (o, f) -> NDependencyWriter.of());
        registerObjectWriterFromConverter(NDependencyBuilder.class, o -> ((NDependencyBuilder) o).build());

        registerObjectWriter(NCmdLine.class, (o, f) -> NCmdLineWriter.of());

        registerObjectWriter(NCompressedPath.class, (o, f) -> new NCompressedPath.MyPathObjectWriter());
        registerObjectWriter(NCompressedPathBase.class, (o, f) -> new NCompressedPathBase.MyPathObjectWriter());
        registerObjectWriter(NPathBase.class, (o, f) -> new NPathBase.PathObjectWriter());

        registerObjectWriter(NObjectWriterSPI.class, (o, f) -> NObjectWriter.of(o));
        registerObjectWriterFromSPI(NChronometer.class, o -> new NChronometerWriterSPI((NChronometer) o));
        registerObjectWriterFromSPI(NChronometerView.class, o -> new NChronometerViewWriterSPI((NChronometerView) o));
        registerObjectWriterFromSPI(NDuration.class, o -> new NDurationWriterSPI((NDuration) o));
        registerObjectWriterFromConverter(NByteArrayPrintStream.MyAbstractMultiReadNInputSource.class, o -> ((NByteArrayPrintStream.MyAbstractMultiReadNInputSource) o).getValue());
        registerObjectWriterFromSPI(InputStreamExt.class, o -> new NContentMetadataProviderWriterSPI((InputStreamExt) o, ((InputStreamExt) o).getSourceName(), "input-stream"));
        registerObjectWriterFromSPI(InputStreamTee.class, o -> new NContentMetadataProviderWriterSPI((InputStreamTee) o, null, "input-stream-tee"));
        registerObjectWriterFromSPI(OutputStreamExt.class, o -> new NContentMetadataProviderWriterSPI((OutputStreamExt) o, null, "output-stream"));
        registerObjectWriterFromSPI(NNonBlockingInputStreamAdapter.class, o -> new NContentMetadataProviderWriterSPI((NNonBlockingInputStreamAdapter) o, ((NNonBlockingInputStreamAdapter) o).getSourceName(), "input-stream"));
        registerObjectWriterFromSPI(NInputStreamSource.class, o -> new NContentMetadataProviderWriterSPI((NInputStreamSource) o, null, "input-stream"));
        registerObjectWriterFromSPI(NPrintStream.class, o -> new NContentMetadataProviderWriterSPI(((NPrintStream) o), null, "print-stream"));
        registerObjectWriterFromSPI(OutputTargetExt.class, o -> new NContentMetadataProviderWriterSPI((OutputTargetExt) o, ((OutputTargetExt) o).getSourceName(), "output-stream"));
        registerObjectWriterFromSPI(OutputTargetExt.class, o -> new NContentMetadataProviderWriterSPI((OutputTargetExt) o, ((OutputTargetExt) o).getSourceName(), "output-stream"));

        registerObjectWriterFromSPI(DefaultNDigest.NDescriptorInputSource.class, o -> new NDescriptorInputSourceWriterSPI((DefaultNDigest.NDescriptorInputSource) o));

        registerObjectWriter(NPathFromSPI.class, (o, f) -> new NObjectWriterAdapter() {
                    @Override
                    public NFormatAndValue<Object, NObjectWriter> getBase(Object aValue) {
                        NPathFromSPI b = (NPathFromSPI) o;
                        NPathSPI base = ((NPathFromSPI) o).getBase();
                        NObjectWriterSPI fspi = null;
                        if (NUseDefaultUtils.isUseDefault(base.getClass(), "formatter", NPath.class)) {
                        } else {
                            fspi = base.formatter(b);
                        }
                        if (fspi != null) {
                            return new NFormatAndValue<>(fspi, new NObjectWriterFromSPI(fspi));
                        }
                        return new NFormatAndValue<>(o, new NPathBase.PathObjectWriter());
                    }
                }
        );
    }


    private void registerTextMapper(Class clz, NTextMapper mapper) {
        if (mapper == null) {
            this.textMappers.remove(clz);
        } else {
            this.textMappers.put(clz, mapper);
        }
    }


    private NText _NMsg_toString(NMsg m) {
        NMsgType format = m.format();
        if (format == null) {
            format = NMsgType.JFORMAT;
        }
        Object msg = m.message();
        switch (format) {
            case CFORMAT: {
                return new NMsgCFormatHelper(m).format();
            }
            case JFORMAT: {
                return new NMsgJFormatHelper(m).format();
            }
            case SFORMAT: {
                return new NMsgSFormatHelper(m).format();
            }
            case VFORMAT: {
                return new NMsgVFormatHelper(m).format();
            }
            case MFORMAT: {
                return new NMsgMFormatHelper(m, this).format();
            }
            case CUSTOM: {
                NMsgCustomFormatter ff = NWorkspaceExt.of().getModel().textModel.customFormatters.get(m.getCustomFormatId());
                if (ff == null) {
                    throw new NIllegalArgumentException(NMsg.ofC("missing customer NMsg formatter %s", m.getCustomFormatId()));
                }
                return ff.format(m);
            }
            case PLAIN: {
                if (m.isNtf()) {
                    if (msg instanceof String) {
                        return this.createText((String) msg);
                    }
                    return this.createText(msg);
                }
                if (msg instanceof String) {
                    return this.createPlain((String) msg);
                }
                return this.createText(msg);
            }
            case STYLED: {
                return this.createStyled(this.createText(msg), m.styles());
            }
            case CODE: {
                return this.createCodeOrCommand(m.codeLang(), (String) msg);
            }
        }
        throw new NUnsupportedEnumException(format);
    }


    public NText fg(String t, int level) {
        return fg(createPlain(t), level);
    }

    public NText fg(NText t, int level) {
        NTextStyle textStyle = NTextStyle.primary(level);
        return createStyled(t, NTextStyles.of(textStyle));
    }

    @Override
    public NTextBuilder createBuilder() {
        return new DefaultNTextBuilder();
    }

    @Override
    public NTableCellSpecBuilder createCellSpecBuilder() {
        return new DefaultNTableCellSpecBuilder();
    }

    @Override
    public NTreeNode createTreeNode(NText text, NTreeNode[] children) {
        return new NTreeNode() {
            @Override
            public NText content() {
                return text == null ? NText.ofBlank() : text;
            }

            @Override
            public List<NTreeNode> children() {
                return children == null ? Collections.emptyList() : Arrays.stream(children).filter(x -> x != null).collect(Collectors.toList());
            }
        };
    }

    @Override
    public NText createBlank() {
        return createPlain("");
    }

    @Override
    public NText createText(NMsg t) {
        return _NMsg_toString(t);
    }

    @Override
    public NText createText(Object t) {
        if (t == null) {
            return createBlank();
        }
        if (t instanceof NText) {
            return (NText) t;
        }
        Class<?> c = t.getClass();
        if (c.isArray()) {
            NTextBuilder b = createBuilder();
            b.append("[", NTextStyle.separator());
            int max = Array.getLength(t);
            if (max > 0) {
                b.append(createText(Array.get(t, 0)));
                for (int i = 1; i < max; i++) {
                    b.append(",", NTextStyle.separator());
                    b.append(" ");
                    b.append(createText(Array.get(t, i)));
                }
            }
            b.append("]", NTextStyle.separator());
            return b.build();
        }
        NTextMapper e = textMappers.get(c);
        if (e != null) {
            return e.ofText(t);
        }
        NObjectWriter nFormat = NObjectWriter.get(t).orNull();
        if (nFormat != null) {
            return (nFormat.ntf(true).format(t));
        }
        return createPlain(t.toString());
    }

    @Override
    public NTextPlain createPlain(String t) {
        return new DefaultNTextPlain(t);
    }

    @Override
    public NTextList createList(NText... nodes) {
        return createList(Arrays.asList(nodes));
    }

    @Override
    public NTextList createList(Collection<NText> nodes) {
        if (nodes == null) {
            return new DefaultNTextList();
        }
        return new DefaultNTextList(nodes.toArray(new NText[0]));
    }

    @Override
    public NText createStyled(String other, NTextStyles styles) {
        return createStyled(other == null ? null : createPlain(other), styles);
    }


    @Override
    public NText createStyled(NText other, NTextStyles styles) {
        if (other == null) {
            return createBlank();
        }
        if (styles == null || styles.isPlain()) {
            return other;
        }
        return new DefaultNTextStyled(
                "##:" + styles.id() + ":", "##",
                other, true, styles);
    }


    @Override
    public NText createStyled(String plainText, NTextStyle style) {
        return createStyled(createPlain(plainText), style);
    }

    @Override
    public NText createStyled(NMsg other, NTextStyles styles) {
        return createStyled(createText(other), styles);
    }

    @Override
    public NText createStyled(NMsg other, NTextStyle style) {
        return createStyled(createText(other), style);
    }

    /**
     * this is the default theme!
     *
     * @param other other
     * @param style textNodeStyle
     * @return NutsText
     */
    public NText createStyled(NText other, NTextStyle style) {
        return createStyled(other, NTextStyles.of(style));
    }

    @Override
    public NTextCmd createCommand(NTerminalCmd command) {
        return new DefaultNTextCommand("```!", command, "", "```");
    }

    @Override
    public NText createCodeOrCommand(String lang, String text) {
        return createCodeOrCommand(lang, text, " ");
    }

    @Override
    public NText createCodeOrCommand(String text) {
        if (text == null) {
            text = "";
        }
        int i = 0;
        while (i < text.length()) {
            char c = text.charAt(i);
            if (c != ':'
                    && !Character.isWhitespace(c)
                    && (
                    (c >= 'a' && c <= 'z')
                            || (c >= 'A' && c <= 'Z')
                            || (
                            i > 0 &&
                                    (
                                            (c >= '0' && c <= '9')
                                                    || (c == '_')
                                                    || (c == '-')
                                    )
                                    || (i == 0 && c == '!')
                    )
            )) {
                i++;
            } else {
                break;
            }
        }
        String cmd = null;
        String value = null;
        if (i == text.length()) {
            //this is a command only text, try
            if (text.startsWith("!")) {
                cmd = text.trim();
                value = "";
            } else {
                cmd = "";
                value = text;
            }
            return createCodeOrCommand(cmd, value);
        } else {
            char sep = ' ';
            if (i < text.length()) {
                cmd = text.substring(0, i);
                sep = text.charAt(i);
                //normalize separator
                if (sep == ' ' || sep == '\t' || sep == ':') {
                    //ok
                    value = text.substring(i + 1);
                } else if (sep == '\n') {
                    value = text.substring(i + 1);
                    if (value.length() > 0 && value.charAt(0) == '\r') {
                        value = value.substring(1);
                    }
                } else if (sep == '\r') {
                    sep = '\n';
                    value = text.substring(i + 1);
                } else {
                    value = text.substring(i);
                    sep = ' ';
                }
            } else {
                cmd = null;
                value = text;
            }
            return createCodeOrCommand(cmd, value, String.valueOf(sep));
        }
    }

    @Override
    public NText createCodeOrCommand(String name, String text, String sep) {
        checkValidSeparator(sep);
        if (name != null && name.startsWith("!")) {
            switch (name) {
                case "!anchor": {
                    return createAnchor(text.trim(), sep);
                }
                case "!link": {
                    return createLink(text.trim(), sep);
                }
                case "!include": {
                    return createInclude(text, sep);
                }
            }
            return createCommand(NTerminalCmd.of(name.substring(1), text));
        }
        return createCode(text, name, sep);
    }

    private void checkValidSeparator(String sep) {
        for (char c : sep.toCharArray()) {
            if (c != ':' && !Character.isWhitespace(c)) {
                throw new NIllegalArgumentException(NMsg.ofC("invalid separator '%s'", c));
            }
        }
    }

    @Override
    public NTextCode createCode(String lang, String text) {
        return createCode(text, lang, " ");
    }

    @Override
    public NTextCode createCode(String text, String lang, String sep) {
        checkValidSeparator(sep);
        if (text == null) {
            text = "";
        }
        DefaultNTextRPI factory0 = (DefaultNTextRPI) NTextRPI.of();
        return factory0.createCode("```",
                lang, sep, "```", text
        );
    }

    @Override
    public NTitleSequence createNumbering() {
        return new DefaultNTitleSequence("");
    }

    @Override
    public NTitleSequence createNumbering(String pattern) {
        return new DefaultNTitleSequence((pattern == null || pattern.isEmpty()) ? "1.1.1.a.1" : pattern);
    }

    @Override
    public NTextAnchor createAnchor(String anchorName) {
        return createAnchor(anchorName, " ");
    }

    @Override
    public NTextAnchor createAnchor(String anchorName, String sep) {
        checkValidSeparator(sep);
        return createAnchor(
                "```!",
                sep, "```", anchorName
        );
    }

    @Override
    public NTextLink createLink(String value) {
        return createLink(value, " ");
    }

    @Override
    public NTextLink createLink(String value, String sep) {
        checkValidSeparator(sep);
        return new DefaultNTextLink(sep, value);
    }

    @Override
    public NTextInclude createInclude(String value) {
        return createInclude(value, " ");
    }

    @Override
    public NTextInclude createInclude(String value, String sep) {
        checkValidSeparator(sep);
        return new DefaultNTextInclude(sep, value);
    }

    public NOptional<NTextTheme> getTheme(String name) {
        return shared.getTheme(name);
    }

    @Override
    public NTextTheme currentTheme() {
        return shared.getTheme();
    }

    @Override
    public NTextRPI setTheme(NTextTheme theme) {
        shared.setTheme(theme);
        return this;
    }

    @Override
    public NTextRPI setTheme(String theme) {
        shared.setTheme(theme);
        return this;
    }

    @Override
    public NCodeHighlighter codeHighlighter(String kind) {
        return shared.getCodeHighlighter(kind);
    }

    @Override
    public NTextRPI registerCodeHighlighter(NCodeHighlighter format) {
        shared.addCodeHighlighter(format);
        return this;
    }

    @Override
    public NTextRPI unregisterCodeHighlighter(String id) {
        shared.removeCodeHighlighter(id);
        return this;
    }

    @Override
    public List<NCodeHighlighter> codeHighlighters() {
        return Arrays.asList(shared.getCodeHighlighters());
    }

    @Override
    public NText createText(String t) {
        return t == null ? createBlank() : NTextParser.of().parse(new StringReader(t));
    }

    @Override
    public NTextParser createParser() {
        return AbstractNTextNodeParserDefaults.createDefault();
    }

    public NText bg(String t, int level) {
        return bg(createPlain(t), level);
    }

    public NText bg(NText t, int variant) {
        NTextStyle textStyle = NTextStyle.secondary(variant);
        return createStyled(t, NTextStyles.of(textStyle));
    }

    public NText comments(String image) {
        return fg(image, 4);
    }

    public NText literal(String image) {
        return fg(image, 1);
    }

    public NText stringLiteral(String image) {
        return fg(image, 3);
    }

    public NText numberLiteral(String image) {
        return fg(image, 1);
    }

    public NText reservedWord(String image) {
        return fg(image, 1);
    }

    public NText annotation(String image) {
        return fg(image, 3);
    }

    public NText separator(String image) {
        return fg(image, 6);
    }

    public NText commandName(String image) {
        return fg(image, 1);
    }

    public NText subCommand1Name(String image) {
        return fg(image, 2);
    }

    public NText subCommand2Name(String image) {
        return fg(image, 3);
    }

    public NText optionName(String image) {
        return fg(image, 4);
    }

    public NText userInput(String image) {
        return fg(image, 8);
    }

    public NCodeHighlighter resolveCodeHighlighter(String kind) {
        if (kind == null) {
            kind = "";
        }
        NCodeHighlighter format = codeHighlighter(kind);
        if (format != null) {
            return format;
        }
        if (kind.length() > 0) {
            try {
                String cc = kind.toUpperCase();
                int x = cc.length();
                while (Character.isDigit(cc.charAt(x - 1))) {
                    x--;
                }
                if (x < cc.length()) {
                    NTextStyle found = NTextStyle.of(NTextStyleType.valueOf(expandAlias(kind.toUpperCase().substring(0, x))),
                            NLiteral.of(kind.substring(x)).asInt().orElse(0)
                    );
                    return new CustomStyleCodeHighlighter(found);
                } else {
                    NTextStyle found = NTextStyle.of(NTextStyleType.valueOf(expandAlias(kind.toUpperCase())));
                    return new CustomStyleCodeHighlighter(found);
                }
            } catch (Exception ex) {
                //ignore
            }
        }
        return codeHighlighter("plain");
    }

    private String expandAlias(String ss) {
        switch (ss.toUpperCase()) {
            case "BOOL": {
                ss = "BOOLEAN";
                break;
            }
            case "KW": {
                ss = "KEYWORD";
                break;
            }
        }
        return ss;
    }

    @Override
    public NTextTitle createTitle(NText other, int level) {
        String prefix = CoreStringUtils.fillString('#', level) + ")";
        return new DefaultNTextTitle(
                prefix, level, other
        );
    }

    @Override
    public NTextTitle createTitle(String other, int level) {
        return createTitle(createPlain(other), level);
    }


    public NTextCode createCode(String start, String kind, String separator, String end, String text) {
        return new DefaultNTextCode(start, kind, separator, end, text);
    }

    public NTextCmd createCommand(String start, NTerminalCmd command, String separator, String end) {
        return new DefaultNTextCommand(start, command, separator, end);
    }

    public NTextAnchor createAnchor(String start, String separator, String end, String value) {
        return new DefaultNTextAnchor(start, separator, end, value);
    }

    public NText createTitle(String start, int level, NText child, boolean complete) {
        return new DefaultNTextTitle(start, level, child);
    }





    @Override
    public String escapeText(String str) {
        return NTextUtils.escapeText0(str);
    }

    private void writeFilteredText(NText t, ByteArrayOutputStream out) {
        if (t != null) {
            if (t instanceof NTextPlain) {
                try {
                    out.write(((NTextPlain) t).value().getBytes());
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            } else if (t instanceof NTextList) {
                for (NText child : ((NTextList) t).children()) {
                    writeFilteredText(child, out);
                }
            } else {
                throw new IllegalArgumentException("unexpected");
            }
        }
    }

    @Override
    public String filterText(String text) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            NText parsed = NTextParser.of().parse(new StringReader(text));
            parsed = parsed.transform(new NTextTransformConfig().filtered(true));
            writeFilteredText(parsed, out);
            return out.toString();
        } catch (Exception ex) {
            NLog.of(AbstractNTextNodeParser.class)
                    .log(NMsg.ofC("error parsing : %s", text)
                            .withIntent(NMsgIntent.ALERT)
                            .withLevel(Level.FINEST)
                    );
            return text;
        }
    }

    public NOptional<NObjectWriter> createWriter(Object format) {
        if (format == null) {
            return NOptional.ofNamedEmpty("null");
        }
        if (format instanceof NText) {
            return NOptional.of((NObjectWriter) format);
        }
        if (format instanceof NObjectWriterSPI) {
            return NOptional.of(new NObjectWriterFromSPI((NObjectWriterSPI) format));
        }
        Class<?> c = format.getClass();
        NObjectWriterMapper e = writerMappers.get(c);
        if (e != null) {
            NObjectWriter n = e.ofFormat(format, this);
            if (n != null) {
                return NOptional.of(n);
            }
        }
        return NOptional.ofNamedEmpty("format for " + format.getClass().getSimpleName());
    }

    @Override
    public <T> NOptional<NStringFormat<T>> createStringFormat(String type, String pattern, Class<T> expectedType) {
        NOptional<NTextFormat<T>> e = createTextFormat(type, pattern, expectedType);
        if (e.isEmpty()) {
            return NOptional.ofEmpty(() -> NMsg.ofC("unknown %s format with type %s. Expected %s.", type, expectedType, "Double"));
        }
        return e.map(x -> x);
    }

    private <T> NOptional<NTextFormat<T>> createTextFormatDefault(String type, String pattern, Class<T> expectedType) {
        String p = NStringUtils.strip(pattern);
        if (Number.class.isAssignableFrom(expectedType)) {
            return NOptional.of((NTextFormat<T>) new DefaultUnitFormat(type + " " + (p.isEmpty() ? "M-6 M12 I2 D3" : p)));
        }
        return NOptional.ofEmpty(() -> NMsg.ofC("unknown %s format with type %s. Expected %s.", type, expectedType, "Number"));
    }

    public <T> NOptional<NTextFormat<T>> createTextFormat(String type, String pattern, Class<T> expectedType) {
        NAssert.requireNamedNonNull(type, "type");
        NAssert.requireNamedNonNull(expectedType, "expectedType");
        if (expectedType.isPrimitive()) {
            expectedType = (Class) NReflectUtils.toBoxedType(expectedType).get();
        }
        Class<T> finalExpectedType = expectedType;
        Set<NTextFormatProvider> p = providers.get(NNameFormat.LOWER_KEBAB_CASE.format(NStringUtils.strip(type)));
        if (p != null) {
            NOptional<NScoredCallable<NTextFormat<T>>> b = NScorable.<NScoredCallable<NTextFormat<T>>>query()
                    .fromStream(p.stream().map(x -> x.resolveFormat(pattern, finalExpectedType)))
                    .best();
            return b.map(NScoredCallable::call)
                    .orElseGetOptionalFrom(() -> createTextFormatDefault(type, pattern, finalExpectedType))
                    .withMessage(() -> NMsg.ofC("unknown %s format with type %s. Expected .", type, finalExpectedType, "Number"));
        }
        return createTextFormatDefault(type, pattern, expectedType);
    }

    private interface NTextMapper {
        NText ofText(Object t);
    }

//    @NScore(fixed = NScorable.DEFAULT_SCORE)
//    private static class NFormatDefaultObjectWriterBase<T> extends DefaultObjectWriterBase<NObjectWriter> {
//        private final NTextFormat<T> format;
//        private final T object;
//
//        public NFormatDefaultObjectWriterBase(NTextFormat<T> format, T object) {
//            super("NTextFormat");
//            this.format = format;
//            this.object = object;
//        }
//
//        @Override
//        public void print(Object aValue, NPrintStream out) {
//            NText u = format.toText((T)aValue);
//            out.print(u);
//        }
//
//        @Override
//        public boolean configureFirst(NCmdLine cmdLine) {
//            return false;
//        }
//
//    }

    private interface NObjectWriterMapper {
        NObjectWriter ofFormat(Object t, NTextRPI texts);
    }

    private static class DurationNTextFormatFromNDuration implements NTextFormat<NDuration> {
        DefaultNDurationFormat2 d;

        public DurationNTextFormatFromNDuration(String pattern) {
            this.d = new DefaultNDurationFormat2(pattern);
        }

        @Override
        public NText toText(NDuration object) {
            return d.format(object);
        }
    }

    private static class DurationNTextFormatFromDuration implements NTextFormat<Duration> {
        DefaultNDurationFormat2 d;

        public DurationNTextFormatFromDuration(String pattern) {
            this.d = new DefaultNDurationFormat2(pattern);
        }

        @Override
        public NText toText(Duration object) {
            return d.format(object);
        }
    }

    private static class CustomNumberNTextFormat<T> implements NTextFormat<T>, NNumberFormat, NDoubleFormat {
        private final DecimalFormat d;
        private final Class<T> expectedType;
        private final String suffix;
        private final boolean percent;

        public CustomNumberNTextFormat(String pattern, Class<T> expectedType) {
            this.expectedType = expectedType;
            NAssert.requireNamedTrue(Number.class.isAssignableFrom(expectedType), expectedType.getSimpleName());
            if (NBlankable.isBlank(pattern)) {
                d = null;
                suffix = "";
                percent = false;
//            } else if (pattern.endsWith("%")) {
//                d = new DecimalFormat(pattern.substring(0, pattern.length() - 1));
//                suffix = "%";
//                percent = true;
//            } else if (pattern.endsWith("'°'")) {
//                d = new DecimalFormat(pattern.substring(0, pattern.length() - 3));
//                suffix = "°";
//                percent = false;
//            } else if (pattern.endsWith("°")) {
//                d = new DecimalFormat(pattern.substring(0, pattern.length() - 1));
//                suffix = "°";
//                percent = false;
            } else {
                d = new DecimalFormat(pattern);
                suffix = "";
                percent = false;
            }
        }

        public NText toText(T object) {
            return toText((Number) object);
        }

        public NText toText(Number object) {
            if (object == null) {
                return NTextBuilder.of().build();
            }
            if (d == null) {
                if (!NBlankable.isBlank(suffix)) {
                    return NTextBuilder.of()
                            .append(object).append(suffix, NTextStyle.separator())
                            .build();
                }
                return NText.of(object);
            }
            if (percent) {
                NTextBuilder b = NTextBuilder.of()
                        .append(d.format(object.doubleValue() * 100.0), NTextStyle.number());
                if (!NBlankable.isBlank(suffix)) {
                    b.append(suffix, NTextStyle.separator());
                }
                return b.build();
            } else {
                NTextBuilder b = NTextBuilder.of()
                        .append(d.format(object.doubleValue()), NTextStyle.number());
                if (!NBlankable.isBlank(suffix)) {
                    b.append(suffix, NTextStyle.separator());
                }
                return b.build();
            }
        }

        @Override
        public String formatDouble(double value) {
            return toText(value).toString();
        }

        @Override
        public String formatNumber(Number value) {
            return toText(value).toString();
        }
    }

    private static class DurationNTextFormatFromNumber implements NTextFormat<Number> {
        DefaultNDurationFormat2 d;

        public DurationNTextFormatFromNumber(String pattern) {
            d = new DefaultNDurationFormat2(pattern);
        }

        @Override
        public NText toText(Number object) {
            if (object == null) {
                return NText.ofBlank();
            }
            return d.format(NDuration.ofMillis(object.longValue()));
        }
    }

    private abstract class NObjectWriterMapperFromSPI implements NObjectWriterMapper {
        abstract NObjectWriterSPI toSpi(Object o);

        @Override
        public NObjectWriter ofFormat(Object t, NTextRPI texts) {
            return new NObjectWriterAdapter() {
                @Override
                public NFormatAndValue<Object, NObjectWriter> getBase(Object aValue) {
                    NObjectWriterSPI spi = toSpi(aValue);
                    return new NFormatAndValue<>(spi, NObjectWriter.of(spi));
                }
            };
        }
    }

    private abstract class NObjectWriterMapperBridge implements NObjectWriterMapper {
        abstract Object convert(Object o);

        @Override
        public NObjectWriter ofFormat(Object t, NTextRPI texts) {
            return new NObjectWriterAdapter() {
                @Override
                public NFormatAndValue<Object, NObjectWriter> getBase(Object aValue) {
                    Object spi = convert(aValue);
                    return new NFormatAndValue<>(spi, NObjectWriter.of(spi));
                }
            };
        }
    }


    private void registerObjectWriterFromConverter(Class clz, Function<Object, Object> mapper) {
        registerObjectWriter(clz, new NObjectWriterMapperBridge() {
            @Override
            Object convert(Object o) {
                return mapper.apply(o);
            }
        });
    }

    private void registerObjectWriterFromSPI(Class clz, Function<Object, NObjectWriterSPI> mapper) {
        registerObjectWriter(clz, new NObjectWriterMapperFromSPI() {
            @Override
            NObjectWriterSPI toSpi(Object o) {
                return mapper.apply(o);
            }
        });
    }

    private void registerObjectWriter(Class clz, NObjectWriterMapper mapper) {
        if (mapper == null) {
            this.writerMappers.remove(clz);
        } else {
            this.writerMappers.put(clz, mapper);
        }
    }


}
