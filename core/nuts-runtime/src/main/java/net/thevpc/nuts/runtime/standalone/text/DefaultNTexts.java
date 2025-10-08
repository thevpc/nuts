package net.thevpc.nuts.runtime.standalone.text;

import net.thevpc.nuts.elem.NElementDescribables;
import net.thevpc.nuts.io.NContentMetadata;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.log.NMsgIntent;
import net.thevpc.nuts.runtime.standalone.text.util.NTextUtils;
import net.thevpc.nuts.spi.*;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.io.NInputSource;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.util.NRef;

import net.thevpc.nuts.reflect.NReflectUtils;
import net.thevpc.nuts.runtime.standalone.format.DefaultFormatBase;
import net.thevpc.nuts.runtime.standalone.io.path.NFormatFromSPI;
import net.thevpc.nuts.runtime.standalone.text.highlighter.CustomStyleCodeHighlighter;
import net.thevpc.nuts.runtime.standalone.text.parser.*;
import net.thevpc.nuts.runtime.standalone.text.util.DefaultNDurationFormat2;
import net.thevpc.nuts.runtime.standalone.text.util.DefaultUnitFormat;
import net.thevpc.nuts.runtime.standalone.util.BytesSizeFormat;
import net.thevpc.nuts.runtime.standalone.util.CoreStringUtils;
import net.thevpc.nuts.util.NClassMap;
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
import java.util.logging.Level;

@NComponentScope(NScopeType.SESSION)
public class DefaultNTexts implements NTexts {

    private final DefaultNTextManagerModel shared;
    private NClassMap<NTextMapper> mapper = new NClassMap<>(NTextMapper.class);

    public DefaultNTexts() {
        this.shared = NWorkspaceExt.of().getModel().textModel;
        registerDefaults();
    }

    private void registerDefaults() {
        register(NFormattable.class, (o, t) -> (((NFormattable) o).formatter().setNtf(true).format()));
        register(NFormatted.class, (o, t) -> (((NFormatted) o).format()));
        register(NTextFormattable.class, (o, t) -> (NText) o);
        register(NMsgFormattable.class, (o, t) -> _NMsg_toString((((NMsgFormattable) o).toMsg())));
        register(NMsg.class, (o, t) -> _NMsg_toString((NMsg) o));
        register(NText.class, (o, t) -> (NText) o);
        register(InputStream.class, (o, t) -> {
            NContentMetadata metaData = NInputSource.of((InputStream) o).getMetaData();
            return t.ofStyled(metaData.getName().orElse(o.toString()), NTextStyle.path());
        });
        register(OutputStream.class, (o, t) -> t.ofStyled(o.toString(), NTextStyle.path()));
        register(NPrintStream.class, (o, t) -> t.ofStyled(o.toString(), NTextStyle.path()));
        register(Writer.class, (o, t) -> t.ofStyled(o.toString(), NTextStyle.path()));
        register(NEnum.class, (o, t) -> t.ofStyled(((NEnum) o).id(), NTextStyle.option()));
        register(Enum.class, (o, t) -> (o instanceof NEnum) ? t.ofStyled(((NEnum) o).id(), NTextStyle.option()) : ofStyled(((Enum<?>) o).name(), NTextStyle.option()));
        register(Number.class, (o, t) -> t.ofStyled(o.toString(), NTextStyle.number()));
        register(Date.class, (o, t) -> t.ofStyled(o.toString(), NTextStyle.date()));
        register(Temporal.class, (o, t) -> t.ofStyled(o.toString(), NTextStyle.date()));
        register(TemporalAmount.class, (o, t) -> t.ofStyled(o.toString(), NTextStyle.date()));
        register(Boolean.class, (o, t) -> t.ofStyled(o.toString(), NTextStyle.bool()));
        register(Path.class, (o, t) -> t.ofStyled(o.toString(), NTextStyle.path()));
        register(File.class, (o, t) -> t.ofStyled(o.toString(), NTextStyle.path()));
        register(URL.class, (o, t) -> t.ofStyled(o.toString(), NTextStyle.path()));
        register(NTreeNode.class, (o, t) -> NTextArt.of().getTreeRenderer().get().render((NTreeNode) o));
        register(NTableModel.class, (o, t) -> NTextArt.of().getTableRenderer().get().render((NTableModel) o));
        register(Class.class, (o, t) -> {
            Class cc = (Class) o;
            Class dc = cc.getDeclaringClass();
            if (dc != null) {
                NText p = t.of(dc);
                NTextBuilder tb = new DefaultNTextBuilder();
                tb.append(p);
                tb.append(t.ofStyled(".", NTextStyle.comments()));
                tb.append(t.ofStyled(cc.getSimpleName(), NTextStyle.option()));
                return tb.build();
            } else {
                NTextBuilder tb = new DefaultNTextBuilder();
                Package p = cc.getPackage();
                if (p != null) {
                    tb.append(t.ofStyled(p.getName(), NTextStyle.comments()));
                    tb.append(t.ofStyled(".", NTextStyle.comments()));
                }
                tb.append(t.ofStyled(cc.getSimpleName(), NTextStyle.info()));
                return tb.build();
            }
        });
        register(Level.class, (o, t) -> {
            switch (((Level) o).getName()) {
                case "OFF":
                    return t.ofStyled(o.toString(), NTextStyle.pale());
                case "SEVERE":
                    return t.ofStyled(o.toString(), NTextStyle.error());
                case "WARNING":
                    return t.ofStyled(o.toString(), NTextStyle.warn());
                case "INFO":
                    return t.ofStyled(o.toString(), NTextStyle.info());
                case "CONFIG":
                    return t.ofStyled(o.toString(), NTextStyle.config());
                case "FINE":
                case "FINER":
                case "FINEST":
                    return t.ofStyled(o.toString(), NTextStyle.pale());
                case "ALL":
                    return t.ofStyled(o.toString(), NTextStyle.success());
                default:
                    return t.ofStyled(o.toString(), NTextStyle.bold());
            }
        });
        register(Throwable.class, (o, t) -> t.ofStyled(
                of(CoreStringUtils.exceptionToMessage((Throwable) o)),
                NTextStyle.error()
        ));
        register(Collection.class, (o, t) -> {
            NTextBuilder b = ofBuilder();
            b.append("[", NTextStyle.separator());
            boolean first = true;
            for (Object v : ((Collection) o)) {
                if (!first) {
                    b.append(",", NTextStyle.separator());
                    b.append(" ");
                } else {
                    first = false;
                }
                b.append(t.of(v));
            }
            b.append("]", NTextStyle.separator());
            return b.build();
        });
        register(Map.Entry.class, (o, t) -> {
            NTextBuilder b = ofBuilder();
            Map.Entry e = (Map.Entry) o;
            b.append(t.of(e.getKey()));
            b.append(":", NTextStyle.separator());
            b.append(" ");
            b.append(t.of(e.getValue()));
            return b.build();
        });
        register(Map.class, (o, t) -> {
            NTextBuilder b = ofBuilder();
            b.append("{", NTextStyle.separator());
            boolean first = true;
            for (Map.Entry<?, ?> v : ((Map<?, ?>) o).entrySet()) {
                if (!first) {
                    b.append(",", NTextStyle.separator());
                    b.append(" ");
                } else {
                    first = false;
                }
                b.append(t.of(v));
            }
            b.append("}", NTextStyle.separator());
            return b.build();
        });
    }

    private void register(Class clz, NTextMapper mapper) {
        if (mapper == null) {
            this.mapper.remove(clz);
        } else {
            this.mapper.put(clz, mapper);
        }
    }


    private NText _NMsg_toString(NMsg m) {
        NTextFormatType format = m.getFormat();
        if (format == null) {
            format = NTextFormatType.JFORMAT;
        }
        Object msg = m.getMessage();
        switch (format) {
            case CFORMAT: {
                return new NMsgCFormatHelper(m, this).format();
            }
            case JFORMAT: {
                return new NMsgJFormatHelper(m, this).format();
            }
            case VFORMAT: {
                return new NMsgVFormatHelper(m, this).format();
            }
            case PLAIN: {
                return this.ofPlain((String) msg);
            }
            case NTF: {
                if (msg instanceof String) {
                    return this.of((String) msg);
                }
                return this.of(msg);
            }
            case STYLED: {
                return this.ofStyled(this.of(msg), m.getStyles());
            }
            case CODE: {
                return this.ofCodeOrCommand(m.getCodeLang(), (String) msg);
            }
        }
        throw new NUnsupportedEnumException(format);
    }


    public NText fg(String t, int level) {
        return fg(ofPlain(t), level);
    }

    public NText fg(NText t, int level) {
        NTextStyle textStyle = NTextStyle.primary(level);
        return ofStyled(t, NTextStyles.of(textStyle));
    }

    @Override
    public NTextBuilder ofBuilder() {
        return new DefaultNTextBuilder();
    }

    @Override
    public NText ofBlank() {
        return ofPlain("");
    }

    @Override
    public NText of(NMsg t) {
        return _NMsg_toString(t);
    }

    @Override
    public NText of(Object t) {
        if (t == null) {
            return ofBlank();
        }
        if (t instanceof NText) {
            return (NText) t;
        }
        Class<?> c = t.getClass();
        if (c.isArray()) {
            NTextBuilder b = ofBuilder();
            b.append("[", NTextStyle.separator());
            int max = Array.getLength(t);
            if (max > 0) {
                b.append(of(Array.get(t, 0)));
                for (int i = 1; i < max; i++) {
                    b.append(",", NTextStyle.separator());
                    b.append(" ");
                    b.append(of(Array.get(t, i)));
                }
            }
            b.append("]", NTextStyle.separator());
            return b.build();
        }
        NTextMapper e = mapper.get(c);
        if (e != null) {
            return e.ofText(t, this);
        }
        NFormat nFormat = NFormats.of().ofFormat(t).orNull();
        if (nFormat != null) {
            return (nFormat.setNtf(true).format());
        }
        return ofPlain(t.toString());
    }

    @Override
    public NTextPlain ofPlain(String t) {
        return new DefaultNTextPlain(t);
    }

    @Override
    public NTextList ofList(NText... nodes) {
        return ofList(Arrays.asList(nodes));
    }

    @Override
    public NTextList ofList(Collection<NText> nodes) {
        if (nodes == null) {
            return new DefaultNTextList();
        }
        return new DefaultNTextList(nodes.toArray(new NText[0]));
    }

    @Override
    public NText ofStyled(String other, NTextStyles styles) {
        return ofStyled(other == null ? null : ofPlain(other), styles);
    }


    @Override
    public NText ofStyled(NText other, NTextStyles styles) {
        if (other == null) {
            return ofBlank();
        }
        if (styles == null || styles.isPlain()) {
            return other;
        }
        return new DefaultNTextStyled(
                "##:" + styles.id() + ":", "##",
                other, true, styles);
    }


    @Override
    public NText ofStyled(String plainText, NTextStyle style) {
        return ofStyled(ofPlain(plainText), style);
    }

    @Override
    public NText ofStyled(NMsg other, NTextStyles styles) {
        return ofStyled(of(other), styles);
    }

    @Override
    public NText ofStyled(NMsg other, NTextStyle style) {
        return ofStyled(of(other), style);
    }

    /**
     * this is the default theme!
     *
     * @param other other
     * @param style textNodeStyle
     * @return NutsText
     */
    public NText ofStyled(NText other, NTextStyle style) {
        return ofStyled(other, NTextStyles.of(style));
    }

    @Override
    public NTextCmd ofCommand(NTerminalCmd command) {
        return new DefaultNTextCommand("```!", command, "", "```");
    }

    @Override
    public NText ofCodeOrCommand(String lang, String text) {
        return ofCodeOrCommand(lang, text, " ");
    }

    @Override
    public NText ofCodeOrCommand(String text) {
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
            return ofCodeOrCommand(cmd, value);
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
            return ofCodeOrCommand(cmd, value, String.valueOf(sep));
        }
    }

    @Override
    public NText ofCodeOrCommand(String name, String text, String sep) {
        checkValidSeparator(sep);
        if (name != null && name.startsWith("!")) {
            switch (name) {
                case "!anchor": {
                    return ofAnchor(text.trim(), sep);
                }
                case "!link": {
                    return ofLink(text.trim(), sep);
                }
                case "!include": {
                    return ofInclude(text, sep);
                }
            }
            return ofCommand(NTerminalCmd.of(name.substring(1), text));
        }
        return ofCode(text, name, sep);
    }

    private void checkValidSeparator(String sep) {
        for (char c : sep.toCharArray()) {
            if (c != ':' && !Character.isWhitespace(c)) {
                throw new NIllegalArgumentException(NMsg.ofC("invalid separator '%s'", c));
            }
        }
    }

    @Override
    public NTextCode ofCode(String lang, String text) {
        return ofCode(text, lang, " ");
    }

    @Override
    public NTextCode ofCode(String text, String lang, String sep) {
        checkValidSeparator(sep);
        if (text == null) {
            text = "";
        }
        DefaultNTexts factory0 = (DefaultNTexts) NTexts.of();
        return factory0.createCode("```",
                lang, "" + sep, "```", text
        );
    }

    @Override
    public NTitleSequence ofNumbering() {
        return new DefaultNTitleSequence("");
    }

    @Override
    public NTitleSequence ofNumbering(String pattern) {
        return new DefaultNTitleSequence((pattern == null || pattern.isEmpty()) ? "1.1.1.a.1" : pattern);
    }

    @Override
    public NTextAnchor ofAnchor(String anchorName) {
        return ofAnchor(anchorName, " ");
    }

    @Override
    public NTextAnchor ofAnchor(String anchorName, String sep) {
        checkValidSeparator(sep);
        return createAnchor(
                "```!",
                "" + sep, "```", anchorName
        );
    }

    @Override
    public NTextLink ofLink(String value) {
        return ofLink(value, " ");
    }

    @Override
    public NTextLink ofLink(String value, String sep) {
        checkValidSeparator(sep);
        return new DefaultNTextLink("" + sep, value);
    }

    @Override
    public NTextInclude ofInclude(String value) {
        return ofInclude(value, " ");
    }

    @Override
    public NTextInclude ofInclude(String value, String sep) {
        checkValidSeparator(sep);
        return new DefaultNTextInclude("" + sep, value);
    }

    public NOptional<NTextFormatTheme> getTheme(String name) {
        return shared.getTheme(name);
    }

    @Override
    public NTextFormatTheme getTheme() {
        return shared.getTheme();
    }

    @Override
    public NTexts setTheme(NTextFormatTheme theme) {
        shared.setTheme(theme);
        return this;
    }

    @Override
    public NTexts setTheme(String theme) {
        shared.setTheme(theme);
        return this;
    }

    @Override
    public NCodeHighlighter getCodeHighlighter(String kind) {
        return shared.getCodeHighlighter(kind);
    }

    @Override
    public NTexts addCodeHighlighter(NCodeHighlighter format) {
        shared.addCodeHighlighter(format);
        return this;
    }

    @Override
    public NTexts removeCodeHighlighter(String id) {
        shared.removeCodeHighlighter(id);
        return this;
    }

    @Override
    public List<NCodeHighlighter> getCodeHighlighters() {
        return Arrays.asList(shared.getCodeHighlighters());
    }

    @Override
    public NText of(String t) {
        return t == null ? ofBlank() : parser().parse(new StringReader(t));
    }

    @Override
    public NTextParser parser() {
        return AbstractNTextNodeParserDefaults.createDefault();
    }

    public NText bg(String t, int level) {
        return bg(ofPlain(t), level);
    }

    public NText bg(NText t, int variant) {
        NTextStyle textStyle = NTextStyle.secondary(variant);
        return ofStyled(t, NTextStyles.of(textStyle));
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
        NCodeHighlighter format = getCodeHighlighter(kind);
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
        return getCodeHighlighter("plain");
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
    public NTextTitle ofTitle(NText other, int level) {
        String prefix = CoreStringUtils.fillString('#', level) + ")";
        return new DefaultNTextTitle(
                prefix, level, other
        );
    }

    @Override
    public NTextTitle ofTitle(String other, int level) {
        return ofTitle(ofPlain(other), level);
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
    public int getScore(NScorableContext context) {
        return DEFAULT_SCORE;
    }

    @Override
    public NNormalizedText normalize(NText text) {
        return normalize(text, null, null);
    }

    @Override
    public NNormalizedText normalize(NText text, NTextTransformConfig config) {
        return normalize(text, null, config);
    }

    @Override
    public NNormalizedText normalize(NText text, NTextTransformer transformer, NTextTransformConfig config) {
        List<NNormalizedText> li = normalizeStream(text, transformer, config).toList();
        if (li.isEmpty()) {
            return (NNormalizedText) NText.ofBlank();
        }
        if (li.size() == 1) {
            return li.get(0);
        }
        return NText.ofList(li.toArray(new NNormalizedText[0]));
    }


    public NStream<NNormalizedText> normalizeStream(NText text, NTextTransformer transformer, NTextTransformConfig config) {
        if (config == null) {
            config = new NTextTransformConfig();
        }
        config.setFlatten(true);
        config.setNormalize(true);
        NText z = transform(text, transformer, config);
        return NStream.ofIterator(new Iterator<NText>() {
            Deque<NText> queue = new ArrayDeque<>();

            {
                if (z != null) {
                    queue.addFirst(z);
                }
                refactorNext();
            }

            private void refactorNext() {
                while (!queue.isEmpty()) {
                    NText z = queue.peek();
                    switch (z.type()) {
                        case PLAIN:
                        case CODE:
                        case ANCHOR:
                        case LINK:
                        case COMMAND:
                        case TITLE:
                        case STYLED: {
                            return;
                        }
                        case LIST: {
                            NTextList t = (NTextList) z;
                            queue.removeFirst();
                            List<NText> children = t.getChildren();
                            if (children.size() > 0) {
                                for (int i = children.size() - 1; i >= 0; i--) {
                                    queue.addFirst(children.get(i));
                                }
                            }
                            break;
                        }
                        case BUILDER: {
                            NTextBuilder t = (NTextBuilder) z;
                            queue.removeFirst();
                            List<NText> children = t.getChildren();
                            if (children.size() > 0) {
                                for (int i = children.size() - 1; i >= 0; i--) {
                                    queue.addFirst(children.get(i));
                                }
                            }
                            break;
                        }
                        case INCLUDE:
                        default: {
                            //won't be processed!
                            queue.removeFirst();
                            break;
                        }
                    }
                }
            }

            @Override
            public boolean hasNext() {
                refactorNext();
                return !queue.isEmpty();
            }

            @Override
            public NText next() {
                refactorNext();
                return queue.remove();
            }
        }).instanceOf(NNormalizedText.class).redescribe(NElementDescribables.ofDesc("flattened text"));
    }

    @Override
    public NText transform(NText text, NTextTransformConfig config) {
        if (NBlankable.isBlank(config)) {
            return text;
        }
        return transform(text, null, config);
    }

    int resolveRootLevel(NText text) {
        NRef<Integer> level = NRef.ofNull();
        traverseDFS(text, n -> {
            if (n.type() == NTextType.TITLE) {
                int lvl = ((NTextTitle) n).getLevel();
                if (level.isNull() || level.get() > lvl) {
                    level.set(lvl);
                }
            }
        });
        return level.isNull() ? 0 : level.get();
    }

    @Override
    public NText transform(NText text, NTextTransformer transformer, NTextTransformConfig config) {
        if (text == null) {
            return null;
        }
        if (NBlankable.isBlank(config) && transformer == null) {
            return text;
        }
        if (config == null) {
            config = new NTextTransformConfig();
        }
        // start by processing includes
        if (config.isProcessIncludes()) {
            NTextTransformConfig iconfig = config.copy();
            iconfig.setProcessIncludes(true);
            iconfig.setImportClassLoader(config.getImportClassLoader());
            NTextTransformerContext c = new DefaultNTextTransformerContext(iconfig);
            text = transform(text, c.getDefaultTransformer(), c);
            config = config.copy().setProcessIncludes(false).setImportClassLoader(null);
        }

        if (NBlankable.isBlank(config) && transformer == null) {
            return text;
        }

        Integer rootLevel = config.getRootLevel();
        if (rootLevel != null) {
            config = config.copy().setRootLevel(null);
            //find root level
            int level = resolveRootLevel(text);
            if (level != rootLevel) {
                int offset = rootLevel - level;
                NTextTransformerContext c = new DefaultNTextTransformerContext(config);
                text = transform(text, (text1, context) -> {
                    if (text1.type() == NTextType.TITLE) {
                        NTextTitle t = (NTextTitle) text1;
                        return ofTitle(t.getChild(), t.getLevel() + offset);
                    }
                    return text1;
                }, c);
            }
        }

        if (NBlankable.isBlank(config) && transformer == null) {
            return text;
        }

        String anchor = config.getAnchor();
        if (anchor != null) {
            config = config.copy().setAnchor(null);
        }

        if (transformer != null || !config.isBlank()) {
            NTextTransformerContext c = new DefaultNTextTransformerContext(config);
            if (transformer == null) {
                transformer = c.getDefaultTransformer();
            }
            text = transform(text, transformer == null ? c.getDefaultTransformer() : transformer, c);
        }

        if (anchor != null) {
            List<NText> ok = new ArrayList<>();
            boolean foundAnchor = false;
            if (text.type() == NTextType.LIST) {
                for (NText o : ((NTextList) text)) {
                    if (foundAnchor) {
                        ok.add(o);
                    } else if (o.type() == NTextType.ANCHOR) {
                        if (anchor.equals(((DefaultNTextAnchor) o).getValue())) {
                            foundAnchor = true;
                        }
                    }
                }
            }
            if (foundAnchor) {
                text = ofList(ok).simplify();
            }
        }
        return text;
    }

    @Override
    public void traverseDFS(NText text, NTextVisitor visitor) {
        if (text == null) {
            return;
        }
        switch (text.type()) {
            case PLAIN:
            case CODE:
            case ANCHOR:
            case LINK:
            case COMMAND: {
                visitor.visit(text);
                break;
            }
            case TITLE: {
                NTextTitle t = (NTextTitle) text;
                NText child = t.getChild();
                if (child != null) {
                    visitor.visit(child);
                }
                visitor.visit(t);
                break;
            }
            case STYLED: {
                NTextStyled t = (NTextStyled) text;
                NText child = t.getChild();
                if (child != null) {
                    visitor.visit(child);
                }
                visitor.visit(t);
                break;
            }
            case LIST: {
                NTextList t = (NTextList) text;
                for (NText child : t.getChildren()) {
                    if (child != null) {
                        visitor.visit(child);
                    }
                }
                visitor.visit(t);
                break;
            }
            case BUILDER: {
                NTextBuilder t = (NTextBuilder) text;
                for (NText child : t.getChildren()) {
                    if (child != null) {
                        visitor.visit(child);
                    }
                }
                visitor.visit(t);
                break;
            }
            case INCLUDE: {
                NTextInclude t = (NTextInclude) text;
                visitor.visit(t);
                break;
            }
        }
    }

    @Override
    public void traverseBFS(NText text, NTextVisitor visitor) {
        Queue<NText> q = new ArrayDeque<>();
        q.add(text);
        while (!q.isEmpty()) {
            NText u = q.remove();
            switch (text.type()) {
                case PLAIN:
                case CODE:
                case ANCHOR:
                case LINK:
                case COMMAND: {
                    visitor.visit(text);
                    break;
                }
                case TITLE: {
                    NTextTitle t = (NTextTitle) text;
                    NText child = t.getChild();
                    if (child != null) {
                        q.add(child);
                    }
                    visitor.visit(t);
                    break;
                }
                case STYLED: {
                    NTextStyled t = (NTextStyled) text;
                    NText child = t.getChild();
                    if (child != null) {
                        q.add(child);
                    }
                    visitor.visit(t);
                    break;
                }
                case LIST: {
                    NTextList t = (NTextList) text;
                    for (NText child : t.getChildren()) {
                        if (child != null) {
                            q.add(child);
                        }
                    }
                    visitor.visit(t);
                    break;
                }
                case BUILDER: {
                    NTextBuilder t = (NTextBuilder) text;
                    for (NText child : t.getChildren()) {
                        if (child != null) {
                            q.add(child);
                        }
                    }
                    visitor.visit(t);
                    break;
                }
                case INCLUDE: {
                    NTextInclude t = (NTextInclude) text;
                    visitor.visit(t);
                    break;
                }
            }
        }
    }

    private NText transform(NText text, NTextTransformer transformer, NTextTransformerContext c) {
        if (text == null) {
            return null;
        }
        NText pt = transformer.preTransform(text, c);
        if (pt != text) {
            return pt;
        }
        switch (text.type()) {
            case PLAIN:
            case CODE:
            case ANCHOR:
            case LINK:
            case COMMAND: {
                return transformer.postTransform(text, c);
            }
            case TITLE: {
                NTextTitle t = (NTextTitle) text;
                NText child = t.getChild();
                if (child == null) {
                    return null;
                }
                child = transform(child, transformer, c);
                return transformer.postTransform(ofTitle(child, t.getLevel()), c);
            }
            case STYLED: {
                NTextStyled t = (NTextStyled) text;
                NText child = t.getChild();
                if (child == null) {
                    return null;
                }
                child = transform(child, transformer, c);
                return transformer.postTransform(ofStyled(child, t.getStyles()), c);
            }
            case LIST: {
                NTextList t = (NTextList) text;
                List<NText> li = new ArrayList<>();
                for (NText child : t.getChildren()) {
                    if (child != null) {
                        child = transform(child, transformer, c);
                        if (child != null) {
                            li.add(child);
                        }
                    }
                }
                if (li.size() > 0) {
                    if (li.size() == 1) {
                        return transformer.postTransform(li.get(0), c);
                    }
                    return transformer.postTransform(ofList(li), c);
                }
                return null;
            }
            case BUILDER: {
                NTextBuilder t = (NTextBuilder) text;
                List<NText> li = new ArrayList<>();
                for (NText child : t.getChildren()) {
                    if (child != null) {
                        child = transform(child, transformer, c);
                        if (child != null) {
                            li.add(child);
                        }
                    }
                }
                if (li.size() > 0) {
                    if (li.size() == 1) {
                        return transformer.postTransform(li.get(0), c);
                    }
                    return transformer.postTransform(ofList(li), c);
                }
                return null;
            }
            case INCLUDE: {
                return null;
            }
        }
        return null;
    }

    @Override
    public String escapeText(String str) {
        return NTextUtils.escapeText0(str);
    }

    private void writeFilteredText(NText t, ByteArrayOutputStream out) {
        if (t != null) {
            if (t instanceof NTextPlain) {
                try {
                    out.write(((NTextPlain) t).getValue().getBytes());
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            } else if (t instanceof NTextList) {
                for (NText child : ((NTextList) t).getChildren()) {
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
            NText parsed = this.parser().parse(new StringReader(text));
            parsed = NTexts.of().transform(parsed, new NTextTransformConfig().setFiltered(true));
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

    @Override
    public NFormat createFormat(NFormatSPI format) {
        return new NFormatFromSPI(format);
    }

    @Override
    public <T> NFormat createFormat(T object, NTextFormat<T> format) {
        return new DefaultFormatBase<NFormat>("NTextFormat") {
            @Override
            public void print(NPrintStream out) {
                NText u = format.toText(object);
                out.print(u);
            }

            @Override
            public boolean configureFirst(NCmdLine cmdLine) {
                return false;
            }

            @Override
            public int getScore(NScorableContext context) {
                return DEFAULT_SCORE;
            }

        };
    }

    @Override
    public NOptional<NTextFormat<Number>> createNumberTextFormat(String type, String pattern) {
        return createTextFormat(type, pattern, Number.class);
    }

    @Override
    public NOptional<NStringFormat<Number>> createNumberStringFormat(String type, String pattern) {
        return createStringFormat(type, pattern, Number.class);
    }

    @Override
    public <T> NOptional<NStringFormat<T>> createStringFormat(String type, String pattern, Class<T> expectedType) {
        NOptional<NTextFormat<T>> e = createTextFormat(type, pattern, expectedType);
        if (e.isEmpty()) {
            return NOptional.ofEmpty(() -> NMsg.ofC("unknown %s format with type %s. Expected %s.", type, expectedType, "Double"));
        }
        return e.map(x -> x);
    }

    public <T> NOptional<NTextFormat<T>> createTextFormat(String type, String pattern, Class<T> expectedType) {
        Class<T> finalExpectedType = expectedType;
        NAssert.requireNonNull(type, "type");
        NAssert.requireNonNull(expectedType, "expectedType");
        NAssert.requireNonNull(pattern, "pattern");
        if (expectedType.isPrimitive()) {
            expectedType = (Class) NReflectUtils.toBoxedType(expectedType).get();
        }
        switch (type.toLowerCase().trim()) {
            case "duration": {
                DefaultNDurationFormat2 d = new DefaultNDurationFormat2(pattern);
                if (NDuration.class.equals(expectedType)) {
                    return NOptional.of(
                            (NTextFormat<T>) new NTextFormat<NDuration>() {
                                @Override
                                public NText toText(NDuration object) {
                                    return d.format(object);
                                }
                            }
                    );
                }
                if (Duration.class.equals(expectedType)) {
                    return NOptional.of(
                            (NTextFormat<T>) new NTextFormat<Duration>() {
                                @Override
                                public NText toText(Duration object) {
                                    return d.format(object);
                                }
                            }
                    );
                }
                return NOptional.ofEmpty(() -> NMsg.ofC("unknown duration format with type %s. Expected Duration or NDuration.", finalExpectedType));
            }
            case "double":
            case "decimal":
            case "number": {
                if (pattern.endsWith("%")) {
                    DecimalFormat d = new DecimalFormat(pattern.substring(0, pattern.length() - 1));
                    if (Number.class.isAssignableFrom(expectedType)) {
                        return NOptional.of(
                                (NTextFormat<T>) new NTextFormat<Number>() {
                                    @Override
                                    public NText toText(Number object) {
                                        if (object == null) {
                                            return NTextBuilder.of().build();
                                        }
                                        return NTextBuilder.of()
                                                .append(d.format(object.doubleValue() * 100.0), NTextStyle.number())
                                                .append("%", NTextStyle.separator())
                                                .build()
                                                ;
                                    }
                                }
                        );
                    }
                    return NOptional.ofEmpty(() -> NMsg.ofC("unknown %s format with type %s. Expected .", type, finalExpectedType, "Number"));
                } else if (pattern.endsWith("'°'")) {
                    DecimalFormat d = new DecimalFormat(pattern.substring(0, pattern.length() - 3));
                    if (Number.class.isAssignableFrom(expectedType)) {
                        return NOptional.of(
                                (NTextFormat<T>) new NTextFormat<Number>() {
                                    @Override
                                    public NText toText(Number object) {
                                        if (object == null) {
                                            return NTextBuilder.of().build();
                                        }
                                        return NTextBuilder.of()
                                                .append(d.format(object), NTextStyle.number())
                                                .append("°", NTextStyle.separator())
                                                .build()
                                                ;
                                    }
                                }
                        );
                    }
                    return NOptional.ofEmpty(() -> NMsg.ofC("unknown %s format with type %s. Expected .", type, finalExpectedType, "Number"));
                } else {
                    DecimalFormat d = new DecimalFormat(pattern);
                    if (Number.class.isAssignableFrom(expectedType)) {
                        return NOptional.of(
                                (NTextFormat<T>) new NTextFormat<Number>() {
                                    @Override
                                    public NText toText(Number object) {
                                        if (object == null) {
                                            return NTextBuilder.of().build();
                                        }
                                        return NTextBuilder.of()
                                                .append(d.format(object), NTextStyle.number())
                                                .build()
                                                ;
                                    }
                                }
                        );
                    }
                    return NOptional.ofEmpty(() -> NMsg.ofC("unknown %s format with type %s. Expected .", type, finalExpectedType, "Number"));
                }
            }
            case "m":
            case "meter":
            case "metric": {
                String p = NStringUtils.trim(pattern);
                DefaultUnitFormat d = new DefaultUnitFormat("m " + (p.isEmpty() ? "M-3 M3 I2 D2" : p));
                if (Number.class.isAssignableFrom(expectedType)) {
                    return NOptional.of(
                            (NTextFormat<T>) new NTextFormat<Number>() {
                                @Override
                                public NText toText(Number object) {
                                    return d.format(object.doubleValue());
                                }
                            }
                    );
                }
                return NOptional.ofEmpty(() -> NMsg.ofC("unknown %s format with type %s. Expected .", type, finalExpectedType, "Number"));
            }
            case "memory":
            case "bytes":
            case "size": {
                String p = NStringUtils.trim(pattern);
                BytesSizeFormat d = new BytesSizeFormat(null);
                if (Number.class.isAssignableFrom(expectedType)) {
                    return NOptional.of(
                            (NTextFormat<T>) new NTextFormat<Number>() {
                                @Override
                                public NText toText(Number object) {
                                    return d.formatText(object.longValue());
                                }
                            }
                    );
                }
                return NOptional.ofEmpty(() -> NMsg.ofC("unknown %s format with type %s. Expected .", type, finalExpectedType, "Number"));
            }
            case "freq":
            case "frequency":
            case "hz": {
                String p = NStringUtils.trim(pattern);
                DefaultUnitFormat d = new DefaultUnitFormat("Hz " + (p.isEmpty() ? "M1 M12 I2 D3" : p));
                if (Number.class.isAssignableFrom(expectedType)) {
                    return NOptional.of(
                            (NTextFormat<T>) new NTextFormat<Number>() {
                                @Override
                                public NText toText(Number object) {
                                    return d.format(object.longValue());
                                }
                            }
                    );
                }
                return NOptional.ofEmpty(() -> NMsg.ofC("unknown %s format with type %s. Expected .", type, finalExpectedType, "Number"));
            }
            default: {
                String p = NStringUtils.trim(pattern);
                DefaultUnitFormat d = new DefaultUnitFormat(type + " " + (p.isEmpty() ? "M-6 M12 I2 D3" : p));
                if (Number.class.isAssignableFrom(expectedType)) {
                    return NOptional.of(
                            (NTextFormat<T>) new NTextFormat<Number>() {
                                @Override
                                public NText toText(Number object) {
                                    return d.format(object.doubleValue());
                                }
                            }
                    );
                }
                return NOptional.ofEmpty(() -> NMsg.ofC("unknown %s format with type %s. Expected %s.", type, finalExpectedType, "Number"));
            }
        }
    }

    private interface NTextMapper {
        NText ofText(Object t, NTexts texts);
    }
}
