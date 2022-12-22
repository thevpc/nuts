package net.thevpc.nuts.runtime.standalone.text;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NutsIO;
import net.thevpc.nuts.runtime.standalone.io.path.NutsFormatFromSPI;
import net.thevpc.nuts.runtime.standalone.session.NutsSessionUtils;
import net.thevpc.nuts.runtime.standalone.util.collections.ClassMap;
import net.thevpc.nuts.runtime.standalone.workspace.NutsWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.text.highlighter.CustomStyleCodeHighlighter;
import net.thevpc.nuts.runtime.standalone.text.parser.*;
import net.thevpc.nuts.runtime.standalone.util.CoreStringUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NutsWorkspaceUtils;
import net.thevpc.nuts.spi.NutsFormatSPI;
import net.thevpc.nuts.spi.NutsSupportLevelContext;
import net.thevpc.nuts.text.*;
import net.thevpc.nuts.util.*;

import java.io.*;
import java.lang.reflect.Array;
import java.net.URL;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.time.temporal.Temporal;
import java.util.*;
import java.util.logging.Level;

public class DefaultNutsTexts implements NutsTexts {

    private final NutsWorkspace ws;
    private final DefaultNutsTextManagerModel shared;
    private NutsSession session;
    private ClassMap<NutsTextMapper> textMapper = new ClassMap<>(NutsTextMapper.class);

    public DefaultNutsTexts(NutsSession session) {
        this.session = session;
        this.ws = session.getWorkspace();
        this.shared = NutsWorkspaceExt.of(ws).getModel().textModel;
        registerDefaults();
    }

    private void registerDefaults() {
        register(NutsFormattable.class, (o, t, s) -> (((NutsFormattable) o).formatter(session).setSession(getSession()).setNtf(true).format()).toText());
        register(NutsMessage.class, (o, t, s) -> _NutsMessage_toString((NutsMessage) o));
        register(NutsString.class, (o, t, s) -> ((NutsString) o).toText());
        register(InputStream.class, (o, t, s) -> t.ofStyled(NutsIO.of(s).createInputSource((InputStream) o).getInputMetaData().getName().orElse(o.toString()), NutsTextStyle.path()));
        register(OutputStream.class, (o, t, s) -> t.ofStyled(o.toString(), NutsTextStyle.path()));
        register(Writer.class, (o, t, s) -> t.ofStyled(o.toString(), NutsTextStyle.path()));
        register(NutsEnum.class, (o, t, s) -> t.ofStyled(((NutsEnum) o).id(), NutsTextStyle.option()));
        register(Enum.class, (o, t, s) -> (o instanceof NutsEnum) ? t.ofStyled(((NutsEnum) o).id(), NutsTextStyle.option()) : ofStyled(((Enum<?>) o).name(), NutsTextStyle.option()));
        register(Number.class, (o, t, s) -> t.ofStyled(o.toString(), NutsTextStyle.number()));
        register(Date.class, (o, t, s) -> t.ofStyled(o.toString(), NutsTextStyle.date()));
        register(Temporal.class, (o, t, s) -> t.ofStyled(o.toString(), NutsTextStyle.date()));
        register(Boolean.class, (o, t, s) -> t.ofStyled(o.toString(), NutsTextStyle.bool()));
        register(Path.class, (o, t, s) -> t.ofStyled(o.toString(), NutsTextStyle.path()));
        register(File.class, (o, t, s) -> t.ofStyled(o.toString(), NutsTextStyle.path()));
        register(URL.class, (o, t, s) -> t.ofStyled(o.toString(), NutsTextStyle.path()));
        register(Throwable.class, (o, t, s) -> t.ofStyled(
                ofText(CoreStringUtils.exceptionToMessage((Throwable) o)),
                NutsTextStyle.error()
        ));
        register(Collection.class, (o, t, s) -> {
            NutsTextBuilder b = ofBuilder();
            b.append("[", NutsTextStyle.separator());
            boolean first = true;
            for (Object v : ((Collection) o)) {
                if (!first) {
                    b.append(",", NutsTextStyle.separator());
                    b.append(" ");
                } else {
                    first = false;
                }
                b.append(t.ofText(v));
            }
            b.append("]", NutsTextStyle.separator());
            return b.toText();
        });
        register(Map.Entry.class, (o, t, s) -> {
            NutsTextBuilder b = ofBuilder();
            Map.Entry e = (Map.Entry) o;
            b.append(t.ofText(e.getKey()));
            b.append(":", NutsTextStyle.separator());
            b.append(" ");
            b.append(t.ofText(e.getValue()));
            return b.toText();
        });
        register(Map.class, (o, t, s) -> {
            NutsTextBuilder b = ofBuilder();
            b.append("{", NutsTextStyle.separator());
            boolean first = true;
            for (Map.Entry<?, ?> v : ((Map<?, ?>) o).entrySet()) {
                if (!first) {
                    b.append(",", NutsTextStyle.separator());
                    b.append(" ");
                } else {
                    first = false;
                }
                b.append(t.ofText(v));
            }
            b.append("}", NutsTextStyle.separator());
            return b.toText();
        });
    }

    private void register(Class clz, NutsTextMapper mapper) {
        if (mapper == null) {
            textMapper.remove(clz);
        } else {
            textMapper.put(clz, mapper);
        }
    }

    /**
     * transform plain text to formatted text so that the result is rendered as
     * is
     *
     * @param str str
     * @return escaped text
     */
    public static String escapeText0(String str) {
        if (str == null) {
            return str;
        }
        StringBuilder sb = new StringBuilder(str.length());
        for (char c : str.toCharArray()) {
            switch (c) {
                case '`':
                case '#':
                case NutsConstants.Ntf.SILENT:
                case '\\': {
                    sb.append('\\').append(c);
                    break;
                }
                default: {
                    sb.append(c);
                }
            }
        }
        return sb.toString();
    }

    private void checkSession() {
        NutsSessionUtils.checkSession(ws, getSession());
    }

    private NutsText _NutsMessage_toString(NutsMessage m) {
        checkSession();
        NutsTextFormatStyle format = m.getFormat();
        if (format == null) {
            format = NutsTextFormatStyle.JSTYLE;
        }
        Object[] params = m.getParams();
        if (params == null) {
            params = new Object[0];
        }
        Object msg = m.getMessage();
        String sLocale = getSession() == null ? null : getSession().getLocale();
        Locale locale = NutsBlankable.isBlank(sLocale) ? null : new Locale(sLocale);
        Object[] args2 = new Object[params.length];
        NutsTexts txt = NutsTexts.of(getSession());
        for (int i = 0; i < args2.length; i++) {
            Object a = params[i];
            if (a == null) {
                //do nothing, support format pattern
                args2[i] = null;
            } else if (a instanceof Number || a instanceof Date || a instanceof Temporal) {
                //do nothing, support format pattern
                args2[i] = a;
            } else {
                args2[i] = txt.ofText(a).toString();
            }
        }
        switch (format) {
            case CSTYLE: {
                StringBuilder sb = new StringBuilder();
                new Formatter(sb, locale).format((String) msg, args2);
                return txt.parse(sb.toString());
            }
            case JSTYLE: {
                return txt.parse(MessageFormat.format((String) msg, args2));
            }
            case VSTYLE: {
                Object[] finalParams = params;
                String a = NutsStringUtils.replaceDollarString((String) msg,
                        s -> {
                            Map<String, ?> mm =
                                    (finalParams == null) ? new LinkedHashMap<>() :
                                            (Map<String, ?>) finalParams[0];
                            Object v = mm.get(s);
                            if (v != null) {
                                return String.valueOf(v);
                            }
                            return "${" + s + "}";
                        }
                );
                return txt.parse(a);
            }
            case PLAIN: {
                return txt.ofPlain((String) msg);
            }
            case NTF: {
                if (msg instanceof String) {
                    return txt.parse((String) msg);
                }
                return txt.ofText(msg);
            }
            case STYLED: {
                return txt.ofStyled(txt.ofText(msg), m.getStyles());
            }
            case CODE: {
                return txt.ofCodeOrCommand(m.getCodeLang(), (String) msg);
            }
        }
        throw new NutsUnsupportedEnumException(getSession(), format);
    }


    public NutsText fg(String t, int level) {
        return fg(ofPlain(t), level);
    }

    public NutsText fg(NutsText t, int level) {
        NutsTextStyle textStyle = NutsTextStyle.primary(level);
        return ofStyled(t, NutsTextStyles.of(textStyle));
    }

    @Override
    public NutsSession getSession() {
        return session;
    }

    @Override
    public DefaultNutsTexts setSession(NutsSession session) {
        this.session = NutsWorkspaceUtils.bindSession(ws, session);
        return this;
    }

    @Override
    public NutsTextBuilder ofBuilder() {
        checkSession();
        return new DefaultNutsTextNodeBuilder(getSession());
    }

    @Override
    public NutsText ofBlank() {
        return ofPlain("");
    }

    @Override
    public NutsText ofText(Object t) {
        checkSession();
        if (t == null) {
            return ofBlank();
        }
        if (t instanceof NutsText) {
            return (NutsText) t;
        }
        Class<?> c = t.getClass();
        if (c.isArray()) {
            NutsTextBuilder b = ofBuilder();
            b.append("[", NutsTextStyle.separator());
            int max = Array.getLength(t);
            if (max > 0) {
                b.append(ofText(Array.get(t, 0)));
                for (int i = 1; i < max; i++) {
                    b.append(",", NutsTextStyle.separator());
                    b.append(" ");
                    b.append(ofText(Array.get(t, i)));
                }
            }
            b.append("]", NutsTextStyle.separator());
            return b.toText();
        }
        NutsTextMapper e = textMapper.get(c);
        if (e != null) {
            return e.ofText(t, this, session);
        }
        if (c.isArray()) {

        }
        return ofPlain(t.toString());
    }

    @Override
    public NutsTextPlain ofPlain(String t) {
        checkSession();
        return new DefaultNutsTextPlain(getSession(), t);
    }

    @Override
    public NutsTextList ofList(NutsText... nodes) {
        return ofList(Arrays.asList(nodes));
    }

    @Override
    public NutsTextList ofList(Collection<NutsText> nodes) {
        checkSession();
        if (nodes == null) {
            return new DefaultNutsTextList(getSession());
        }
        return new DefaultNutsTextList(getSession(), nodes.toArray(new NutsText[0]));
    }

    @Override
    public NutsText ofStyled(String other, NutsTextStyles styles) {
        return ofStyled(other == null ? null : ofPlain(other), styles);
    }

    @Override
    public NutsText ofStyled(NutsString other, NutsTextStyles styles) {
        checkSession();
        return ofStyled(other == null ? null : other.toText(), styles);
    }

    @Override
    public NutsText ofStyled(NutsText other, NutsTextStyles styles) {
        if (other == null) {
            return ofPlain("");
        }
        if (styles == null || styles.isPlain()) {
            return other;
        }
        checkSession();
        return new DefaultNutsTextStyled(getSession(),
                "##:" + styles.id() + ":", "##",
                other, true, styles);
    }


    @Override
    public NutsText ofStyled(String other, NutsTextStyle style) {
        return ofStyled(ofPlain(other), style);
    }

    @Override
    public NutsText ofStyled(NutsString other, NutsTextStyle style) {
        return ofStyled(other.toText(), style);
    }

    @Override
    public NutsText ofStyled(NutsMessage other, NutsTextStyles styles) {
        return ofStyled(ofText(other), styles);
    }

    @Override
    public NutsText ofStyled(NutsMessage other, NutsTextStyle style) {
        return ofStyled(ofText(other), style);
    }

    /**
     * this is the default theme!
     *
     * @param other other
     * @param style textNodeStyle
     * @return NutsText
     */
    public NutsText ofStyled(NutsText other, NutsTextStyle style) {
        return ofStyled(other, NutsTextStyles.of(style));
    }

    @Override
    public NutsTextCommand ofCommand(NutsTerminalCommand command) {
        checkSession();
        return new DefaultNutsTextCommand(getSession(), "```!", command, "", "```");
    }

    @Override
    public NutsText ofCodeOrCommand(String lang, String text) {
        return ofCodeOrCommand(lang, text, ' ');
    }

    @Override
    public NutsText ofCodeOrCommand(String text) {
        if (text == null) {
            text = "";
        }
        int i = 0;
        while (i < text.length() && text.charAt(i) != ':' && !Character.isWhitespace(text.charAt(i))) i++;
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
                value = text.substring(i + 1);
                sep = text.charAt(i);
                //normalize separator
                if (sep == ' ' || sep == '\t' || sep == ':') {
                    //ok
                } else if (sep == '\n') {
                    if (value.length() > 0 && value.charAt(0) == '\r') {
                        value = value.substring(1);
                    }
                } else if (sep == '\r') {
                    sep = '\n';
                } else {
                    sep = ' ';
                }
            } else {
                cmd = null;
                value = text;
            }
            return ofCodeOrCommand(cmd, value, sep);
        }
    }

    @Override
    public NutsText ofCodeOrCommand(String name, String text, char sep) {
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
            return ofCommand(NutsTerminalCommand.of(name.substring(1), text));
        }
        return ofCode(name, text, sep);
    }

    private void checkValidSeparator(char sep) {
        if (sep != ':' && !Character.isWhitespace(sep)) {
            throw new NutsIllegalArgumentException(session, NutsMessage.ofCstyle("invalid separator '%s'", sep));
        }
    }

    @Override
    public NutsTextCode ofCode(String lang, String text) {
        return ofCode(lang, text, ' ');
    }

    @Override
    public NutsTextCode ofCode(String lang, String text, char sep) {
        checkValidSeparator(sep);
        checkSession();
        if (text == null) {
            text = "";
        }
        DefaultNutsTexts factory0 = (DefaultNutsTexts) NutsTexts.of(session);
        return factory0.createCode("```",
                lang, "" + sep, "```", text
        );
    }

    @Override
    public NutsTitleSequence ofNumbering() {
        checkSession();
        return new DefaultNutsTitleSequence("");
    }

    @Override
    public NutsTitleSequence ofNumbering(String pattern) {
        checkSession();
        return new DefaultNutsTitleSequence((pattern == null || pattern.isEmpty()) ? "1.1.1.a.1" : pattern);
    }

    @Override
    public NutsTextAnchor ofAnchor(String anchorName) {
        return ofAnchor(anchorName, ' ');
    }

    @Override
    public NutsTextAnchor ofAnchor(String anchorName, char sep) {
        checkValidSeparator(sep);
        return createAnchor(
                "```!",
                "" + sep, "```", anchorName
        );
    }

    @Override
    public NutsTextLink ofLink(String value) {
        return ofLink(value, ' ');
    }

    @Override
    public NutsTextLink ofLink(String value, char sep) {
        checkValidSeparator(sep);
        return new DefaultNutsTextLink(getSession(), "" + sep, value);
    }

    @Override
    public NutsTextInclude ofInclude(String value) {
        return ofInclude(value, ' ');
    }

    @Override
    public NutsTextInclude ofInclude(String value, char sep) {
        checkValidSeparator(sep);
        checkSession();
        return new DefaultNutsTextInclude(getSession(), "" + sep, value);
    }

    @Override
    public NutsTextFormatTheme getTheme() {
        checkSession();
        return shared.getTheme(getSession());
    }

    @Override
    public NutsTexts setTheme(NutsTextFormatTheme theme) {
        checkSession();
        shared.setTheme(theme, getSession());
        return this;
    }

    @Override
    public NutsTexts setTheme(String theme) {
        checkSession();
        shared.setTheme(theme, getSession());
        return this;
    }

    @Override
    public NutsCodeHighlighter getCodeHighlighter(String kind) {
        checkSession();
        return shared.getCodeHighlighter(kind, getSession());
    }

    @Override
    public NutsTexts addCodeHighlighter(NutsCodeHighlighter format) {
        checkSession();
        shared.addCodeHighlighter(format, getSession());
        return this;
    }

    @Override
    public NutsTexts removeCodeHighlighter(String id) {
        checkSession();
        shared.removeCodeHighlighter(id, getSession());
        return this;
    }

    @Override
    public List<NutsCodeHighlighter> getCodeHighlighters() {
        checkSession();
        return Arrays.asList(shared.getCodeHighlighters(getSession()));
    }

    @Override
    public NutsText parse(String t) {
        return t == null ? ofBlank() : parser().parse(new StringReader(t));
    }

    @Override
    public NutsTextParser parser() {
        checkSession();
        return AbstractNutsTextNodeParserDefaults.createDefault(getSession());
    }

    public NutsText bg(String t, int level) {
        return bg(ofPlain(t), level);
    }

    public NutsText bg(NutsText t, int variant) {
        NutsTextStyle textStyle = NutsTextStyle.secondary(variant);
        return ofStyled(t, NutsTextStyles.of(textStyle));
    }

    public NutsText comments(String image) {
        return fg(image, 4);
    }

    public NutsText literal(String image) {
        return fg(image, 1);
    }

    public NutsText stringLiteral(String image) {
        return fg(image, 3);
    }

    public NutsText numberLiteral(String image) {
        return fg(image, 1);
    }

    public NutsText reservedWord(String image) {
        return fg(image, 1);
    }

    public NutsText annotation(String image) {
        return fg(image, 3);
    }

    public NutsText separator(String image) {
        return fg(image, 6);
    }

    public NutsText commandName(String image) {
        return fg(image, 1);
    }

    public NutsText subCommand1Name(String image) {
        return fg(image, 2);
    }

    public NutsText subCommand2Name(String image) {
        return fg(image, 3);
    }

    public NutsText optionName(String image) {
        return fg(image, 4);
    }

    public NutsText userInput(String image) {
        return fg(image, 8);
    }

    public NutsCodeHighlighter resolveCodeHighlighter(String kind) {
        checkSession();
        if (kind == null) {
            kind = "";
        }
        NutsCodeHighlighter format = getCodeHighlighter(kind);
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
                    NutsTextStyle found = NutsTextStyle.of(NutsTextStyleType.valueOf(expandAlias(kind.toUpperCase().substring(0, x))),
                            NutsValue.of(kind.substring(x)).asInt().orElse(0)
                    );
                    return new CustomStyleCodeHighlighter(found, session);
                } else {
                    NutsTextStyle found = NutsTextStyle.of(NutsTextStyleType.valueOf(expandAlias(kind.toUpperCase())));
                    return new CustomStyleCodeHighlighter(found, session);
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
    public NutsTextTitle ofTitle(NutsText other, int level) {
        String prefix = CoreStringUtils.fillString('#', level) + ")";
        return new DefaultNutsTextTitle(
                session,
                prefix, level, other
        );
    }

    @Override
    public NutsTextTitle ofTitle(String other, int level) {
        return ofTitle(ofPlain(other), level);
    }

    @Override
    public NutsTextTitle ofTitle(NutsString other, int level) {
        return ofTitle(other.toText(), level);
    }

    public NutsTextCode createCode(String start, String kind, String separator, String end, String text) {
        checkSession();
        return new DefaultNutsTextCode(getSession(), start, kind, separator, end, text);
    }

    public NutsTextCommand createCommand(String start, NutsTerminalCommand command, String separator, String end) {
        checkSession();
        return new DefaultNutsTextCommand(getSession(), start, command, separator, end);
    }

    public NutsTextAnchor createAnchor(String start, String separator, String end, String value) {
        checkSession();
        return new DefaultNutsTextAnchor(getSession(), start, separator, end, value);
    }

    public NutsText createTitle(String start, int level, NutsText child, boolean complete) {
        checkSession();
        return new DefaultNutsTextTitle(getSession(), start, level, child);
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext context) {
        return DEFAULT_SUPPORT;
    }

    @Override
    public NutsText transform(NutsText text, NutsTextTransformConfig config) {
        if (NutsBlankable.isBlank(config)) {
            return text;
        }
        return transform(text, null, config);
    }

    int resolveRootLevel(NutsText text) {
        NutsRef<Integer> level = NutsRef.ofNull();
        traverseDFS(text, n -> {
            if (n.getType() == NutsTextType.TITLE) {
                int lvl = ((NutsTextTitle) n).getLevel();
                if (level.isNull() || level.get() > lvl) {
                    level.set(lvl);
                }
            }
        });
        return level.isNull() ? 0 : level.get();
    }

    @Override
    public NutsText transform(NutsText text, NutsTextTransformer transformer, NutsTextTransformConfig config) {
        if (text == null) {
            return null;
        }
        if (NutsBlankable.isBlank(config) && transformer == null) {
            return text;
        }
        if (config == null) {
            config = new NutsTextTransformConfig();
        }
        // start by processing includes
        if (config.isProcessIncludes()) {
            NutsTextTransformConfig iconfig = new NutsTextTransformConfig();
            iconfig.setProcessIncludes(true);
            iconfig.setImportClassLoader(config.getImportClassLoader());
            NutsTextTransformerContext c = new DefaultNutsTextTransformerContext(iconfig, session);
            text = transform(text, c.getDefaultTransformer(), c);
            config = config.copy().setProcessIncludes(false).setImportClassLoader(null);
        }

        if (NutsBlankable.isBlank(config) && transformer == null) {
            return text;
        }

        Integer rootLevel = config.getRootLevel();
        if (rootLevel != null) {
            config = config.copy().setRootLevel(null);
            //find root level
            int level = resolveRootLevel(text);
            if (level != rootLevel) {
                int offset = rootLevel - level;
                NutsTextTransformerContext c = new DefaultNutsTextTransformerContext(new NutsTextTransformConfig(), session);
                text = transform(text, (text1, context) -> {
                    if (text1.getType() == NutsTextType.TITLE) {
                        NutsTextTitle t = (NutsTextTitle) text1;
                        return ofTitle(t.getChild(), t.getLevel() + offset);
                    }
                    return text1;
                }, c);
            }
        }

        if (NutsBlankable.isBlank(config) && transformer == null) {
            return text;
        }

        String anchor = config.getAnchor();
        if (anchor != null) {
            config = config.copy().setAnchor(null);
        }

        if (transformer != null || !config.isBlank()) {
            NutsTextTransformerContext c = new DefaultNutsTextTransformerContext(config, session);
            if (transformer == null) {
                transformer = c.getDefaultTransformer();
            }
            text = transform(text, transformer == null ? c.getDefaultTransformer() : transformer, c);
        }

        if (anchor != null) {
            List<NutsText> ok = new ArrayList<>();
            boolean foundAnchor = false;
            if (text.getType() == NutsTextType.LIST) {
                for (NutsText o : ((NutsTextList) text)) {
                    if (foundAnchor) {
                        ok.add(o);
                    } else if (o.getType() == NutsTextType.ANCHOR) {
                        if (anchor.equals(((DefaultNutsTextAnchor) o).getValue())) {
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
    public void traverseDFS(NutsText text, NutsTextVisitor visitor) {
        if (text == null) {
            return;
        }
        switch (text.getType()) {
            case PLAIN:
            case CODE:
            case ANCHOR:
            case LINK:
            case COMMAND: {
                visitor.visit(text);
                break;
            }
            case TITLE: {
                NutsTextTitle t = (NutsTextTitle) text;
                NutsText child = t.getChild();
                if (child != null) {
                    visitor.visit(child);
                }
                visitor.visit(t);
                break;
            }
            case STYLED: {
                NutsTextStyled t = (NutsTextStyled) text;
                NutsText child = t.getChild();
                if (child != null) {
                    visitor.visit(child);
                }
                visitor.visit(t);
                break;
            }
            case LIST: {
                NutsTextList t = (NutsTextList) text;
                for (NutsText child : t.getChildren()) {
                    if (child != null) {
                        visitor.visit(child);
                    }
                }
                visitor.visit(t);
            }
        }
    }

    @Override
    public void traverseBFS(NutsText text, NutsTextVisitor visitor) {
        Queue<NutsText> q = new ArrayDeque<>();
        q.add(text);
        while (!q.isEmpty()) {
            NutsText u = q.remove();
            switch (text.getType()) {
                case PLAIN:
                case CODE:
                case ANCHOR:
                case LINK:
                case COMMAND: {
                    visitor.visit(text);
                    break;
                }
                case TITLE: {
                    NutsTextTitle t = (NutsTextTitle) text;
                    NutsText child = t.getChild();
                    if (child != null) {
                        q.add(child);
                    }
                    visitor.visit(t);
                    break;
                }
                case STYLED: {
                    NutsTextStyled t = (NutsTextStyled) text;
                    NutsText child = t.getChild();
                    if (child != null) {
                        q.add(child);
                    }
                    visitor.visit(t);
                    break;
                }
                case LIST: {
                    NutsTextList t = (NutsTextList) text;
                    for (NutsText child : t.getChildren()) {
                        if (child != null) {
                            q.add(child);
                        }
                    }
                    visitor.visit(t);
                    break;
                }
            }
        }
    }

    private NutsText transform(NutsText text, NutsTextTransformer transformer, NutsTextTransformerContext c) {
        if (text == null) {
            return null;
        }
        NutsText pt = transformer.preTransform(text, c);
        if (pt != text) {
            return pt;
        }
        switch (text.getType()) {
            case PLAIN:
            case CODE:
            case ANCHOR:
            case LINK:
            case COMMAND: {
                return transformer.postTransform(text, c);
            }
            case TITLE: {
                NutsTextTitle t = (NutsTextTitle) text;
                NutsText child = t.getChild();
                if (child == null) {
                    return null;
                }
                child = transform(child, transformer, c);
                return transformer.postTransform(ofTitle(child, t.getLevel()), c);
            }
            case STYLED: {
                NutsTextStyled t = (NutsTextStyled) text;
                NutsText child = t.getChild();
                if (child == null) {
                    return null;
                }
                child = transform(child, transformer, c);
                return transformer.postTransform(ofStyled(child, t.getStyles()), c);
            }
            case LIST: {
                NutsTextList t = (NutsTextList) text;
                List<NutsText> li = new ArrayList<>();
                for (NutsText child : t.getChildren()) {
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
        }
        return null;
    }

    @Override
    public String escapeText(String str) {
        return escapeText0(str);
    }

    private void writeFilteredText(NutsText t, ByteArrayOutputStream out) {
        if (t != null) {
            if (t instanceof NutsTextPlain) {
                try {
                    out.write(((NutsTextPlain) t).getText().getBytes());
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            } else if (t instanceof NutsTextList) {
                for (NutsText child : ((NutsTextList) t).getChildren()) {
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
            NutsText parsed = this.parser().parse(new StringReader(text));
            parsed = NutsTexts.of(session).transform(parsed, new NutsTextTransformConfig().setFiltered(true));
            writeFilteredText(parsed, out);
            return out.toString();
        } catch (Exception ex) {
            NutsLoggerOp.of(AbstractNutsTextNodeParser.class, session)
                    .verb(NutsLoggerVerb.WARNING)
                    .level(Level.FINEST)
                    .log(NutsMessage.ofCstyle("error parsing : %s", text));
            return text;
        }
    }

    @Override
    public NutsFormat createFormat(NutsFormatSPI value) {
        return new NutsFormatFromSPI(value, getSession());
    }

    private interface NutsTextMapper {

        NutsText ofText(Object t, NutsTexts texts, NutsSession session);
    }
}
