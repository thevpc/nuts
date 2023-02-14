package net.thevpc.nuts.runtime.standalone.text;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NIO;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.runtime.standalone.io.path.NFormatFromSPI;
import net.thevpc.nuts.runtime.standalone.session.NSessionUtils;
import net.thevpc.nuts.runtime.standalone.util.collections.ClassMap;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.text.highlighter.CustomStyleCodeHighlighter;
import net.thevpc.nuts.runtime.standalone.text.parser.*;
import net.thevpc.nuts.runtime.standalone.util.CoreStringUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceUtils;
import net.thevpc.nuts.spi.NFormatSPI;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.text.*;
import net.thevpc.nuts.util.*;

import java.io.*;
import java.lang.reflect.Array;
import java.net.URL;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAmount;
import java.util.*;
import java.util.logging.Level;

public class DefaultNTexts implements NTexts {

    private final NWorkspace ws;
    private final DefaultNTextManagerModel shared;
    private NSession session;
    private ClassMap<NTextMapper> textMapper = new ClassMap<>(NTextMapper.class);

    public DefaultNTexts(NSession session) {
        this.session = session;
        this.ws = session.getWorkspace();
        this.shared = NWorkspaceExt.of(ws).getModel().textModel;
        registerDefaults();
    }

    private void registerDefaults() {
        register(NFormattable.class, (o, t, s) -> (((NFormattable) o).formatter(session).setSession(getSession()).setNtf(true).format()).toText());
        register(NMsg.class, (o, t, s) -> _NMsg_toString((NMsg) o));
        register(NString.class, (o, t, s) -> ((NString) o).toText());
        register(InputStream.class, (o, t, s) -> t.ofStyled(NIO.of(s).ofInputSource((InputStream) o).getInputMetaData().getName().orElse(o.toString()), NTextStyle.path()));
        register(OutputStream.class, (o, t, s) -> t.ofStyled(o.toString(), NTextStyle.path()));
        register(NPrintStream.class, (o, t, s) -> t.ofStyled(o.toString(), NTextStyle.path()));
        register(Writer.class, (o, t, s) -> t.ofStyled(o.toString(), NTextStyle.path()));
        register(NEnum.class, (o, t, s) -> t.ofStyled(((NEnum) o).id(), NTextStyle.option()));
        register(Enum.class, (o, t, s) -> (o instanceof NEnum) ? t.ofStyled(((NEnum) o).id(), NTextStyle.option()) : ofStyled(((Enum<?>) o).name(), NTextStyle.option()));
        register(Number.class, (o, t, s) -> t.ofStyled(o.toString(), NTextStyle.number()));
        register(Date.class, (o, t, s) -> t.ofStyled(o.toString(), NTextStyle.date()));
        register(Temporal.class, (o, t, s) -> t.ofStyled(o.toString(), NTextStyle.date()));
        register(TemporalAmount.class, (o, t, s) -> t.ofStyled(o.toString(), NTextStyle.date()));
        register(Boolean.class, (o, t, s) -> t.ofStyled(o.toString(), NTextStyle.bool()));
        register(Path.class, (o, t, s) -> t.ofStyled(o.toString(), NTextStyle.path()));
        register(File.class, (o, t, s) -> t.ofStyled(o.toString(), NTextStyle.path()));
        register(URL.class, (o, t, s) -> t.ofStyled(o.toString(), NTextStyle.path()));
        register(Level.class, (o, t, s) -> {
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
        register(Throwable.class, (o, t, s) -> t.ofStyled(
                ofText(CoreStringUtils.exceptionToMessage((Throwable) o)),
                NTextStyle.error()
        ));
        register(Collection.class, (o, t, s) -> {
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
                b.append(t.ofText(v));
            }
            b.append("]", NTextStyle.separator());
            return b.toText();
        });
        register(Map.Entry.class, (o, t, s) -> {
            NTextBuilder b = ofBuilder();
            Map.Entry e = (Map.Entry) o;
            b.append(t.ofText(e.getKey()));
            b.append(":", NTextStyle.separator());
            b.append(" ");
            b.append(t.ofText(e.getValue()));
            return b.toText();
        });
        register(Map.class, (o, t, s) -> {
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
                b.append(t.ofText(v));
            }
            b.append("}", NTextStyle.separator());
            return b.toText();
        });
    }

    private void register(Class clz, NTextMapper mapper) {
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
                case NConstants.Ntf.SILENT:
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
        NSessionUtils.checkSession(ws, getSession());
    }

    private boolean isSpecialLiteral(Object m) {
        if (m == null) {
            return true;
        }
        if (m instanceof Number) {
            return true;
        }
        if (m instanceof Temporal) {
            return true;
        }
        if (m instanceof Date) {
            return true;
        }
        if (m instanceof Boolean) {
            return true;
        }
        if (m instanceof String) {
            return true;
        }
        if (m instanceof StringBuilder) {
            return true;
        }
        return false;
    }

    private NTextStyles getSpecialLiteralType(Object m) {
        if (m == null) {
            return NTextStyles.of(NTextStyle.warn());
        }
        if (m instanceof Number) {
            return NTextStyles.of(NTextStyle.number());
        }
        if (m instanceof Temporal) {
            return NTextStyles.of(NTextStyle.date());
        }
        if (m instanceof Date) {
            return NTextStyles.of(NTextStyle.date());
        }
        if (m instanceof Boolean) {
            return NTextStyles.of(NTextStyle.bool());
        }
        return NTextStyles.of();
    }

    private NText asLiteralOrText(Object m, String format, NTexts txt) {
        if (m == null) {
            return txt.ofStyled("null", NTextStyle.danger());
        }
        if (m instanceof Number) {
            return txt.ofStyled(String.valueOf(m), NTextStyle.number());
        }
        if (m instanceof Temporal) {
            return txt.ofStyled(String.valueOf(m), NTextStyle.date());
        }
        if (m instanceof Date) {
            return txt.ofStyled(String.valueOf(m), NTextStyle.date());
        }
        if (m instanceof Boolean) {
            return txt.ofStyled(String.valueOf(m), NTextStyle.keyword());
        }
        return txt.ofText(m);
    }


    private NText _NMsg_toString(NMsg m) {
        checkSession();
        NTextFormatType format = m.getFormat();
        if (format == null) {
            format = NTextFormatType.JFORMAT;
        }
        Object[] params = m.getParams();
        if (params == null) {
            params = new Object[0];
        }
        Object msg = m.getMessage();
        String sLocale = getSession() == null ? null : getSession().getLocale();
        Locale locale = NBlankable.isBlank(sLocale) ? null : new Locale(sLocale);
        NTexts txt = NTexts.of(getSession());
        switch (format) {
            case CFORMAT: {
                String smsg = (String) msg;
                NFormattedTextParts r = NFormattedTextParts.parseCFormat(smsg);
                StringBuilder sb = new StringBuilder();
                int paramIndex = 0;
                for (NFormattedTextPart part : r.getParts()) {
                    if (part.isFormat()) {
                        if (part.getValue().equals("%%")) {
                            sb.append("%");
                        } else if (part.getValue().equals("%n")) {
                            sb.append("\n");
                        } else {
                            if (paramIndex < 0 || paramIndex >= params.length) {
                                throw new NIllegalArgumentException(session, NMsg.ofPlain("invalid index in message"));
                            }
                            Object a = params[paramIndex];
                            if (a == null) {
                                sb.append((String) null);
                            } else if (isSpecialLiteral(a)) {
                                StringBuilder sb2 = new StringBuilder();
                                new Formatter(sb2, locale).format(part.getValue(), a);
                                sb.append(txt.ofStyled(sb2.toString(), getSpecialLiteralType(a)));
                            } else {
                                StringBuilder sb2 = new StringBuilder();
                                new Formatter(sb2, locale).format(part.getValue(), txt.ofText(a));
                                sb.append(sb2);
                            }
                            paramIndex++;
                        }
                    } else {
                        sb.append(part.getValue());
                    }
                }
                return txt.parse(sb.toString());
            }
            case JFORMAT: {
                String smsg = (String) msg;
                NFormattedTextParts r = NFormattedTextParts.parseJStyle(smsg);
                StringBuilder sb = new StringBuilder();
                int gParamIndex = 0;
                for (NFormattedTextPart part : r.getParts()) {
                    if (part.isFormat()) {
                        String formatExt = "";
                        String formatPart = part.getValue();
                        int paramIndex = -1;
                        int commaPos = formatPart.indexOf(',');
                        if (commaPos >= 0) {
                            String paramIndexStr = formatPart.substring(0, commaPos).trim();
                            if (paramIndexStr.isEmpty()) {
                                paramIndex = gParamIndex;
                            } else {
                                paramIndex = NLiteral.of(paramIndexStr).asInt().get();
                            }
                            formatExt = formatPart.substring(commaPos + 1);
                        } else {
                            String paramIndexStr = formatPart.trim();
                            if (paramIndexStr.isEmpty()) {
                                paramIndex = gParamIndex;
                            } else {
                                paramIndex = NLiteral.of(paramIndexStr).asInt().get();
                            }
                        }
                        Object a = params[paramIndex];
                        if (a == null) {
                            sb.append((String) null);
                        } else if (isSpecialLiteral(a)) {
                            String sb2 = MessageFormat.format("{0" + formatExt + "}", a);
                            sb.append(txt.ofStyled(sb2, getSpecialLiteralType(a)));
                        } else {
                            sb.append(MessageFormat.format("{0" + formatExt + "}", txt.ofText(a)));
                        }
                        gParamIndex++;
                    } else {
                        sb.append(part.getValue());
                    }
                }
                return txt.parse(sb.toString());
            }
            case VFORMAT: {
                Object[] finalParams = params;
                String a = NMsg.ofV((String) msg,
                        s -> {
                            Map<String, ?> mm =
                                    (finalParams == null) ? new LinkedHashMap<>() :
                                            (Map<String, ?>) finalParams[0];
                            Object v = mm.get(s);
                            if (v != null) {
                                return asLiteralOrText(v, "", txt);
                            }
                            return "${" + s + "}";
                        }
                ).toString();
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
        throw new NUnsupportedEnumException(getSession(), format);
    }


    public NText fg(String t, int level) {
        return fg(ofPlain(t), level);
    }

    public NText fg(NText t, int level) {
        NTextStyle textStyle = NTextStyle.primary(level);
        return ofStyled(t, NTextStyles.of(textStyle));
    }

    @Override
    public NSession getSession() {
        return session;
    }

    @Override
    public DefaultNTexts setSession(NSession session) {
        this.session = NWorkspaceUtils.bindSession(ws, session);
        return this;
    }

    @Override
    public NTextBuilder ofBuilder() {
        checkSession();
        return new DefaultNTextNodeBuilder(getSession());
    }

    @Override
    public NText ofBlank() {
        return ofPlain("");
    }

    @Override
    public NText ofText(Object t) {
        checkSession();
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
                b.append(ofText(Array.get(t, 0)));
                for (int i = 1; i < max; i++) {
                    b.append(",", NTextStyle.separator());
                    b.append(" ");
                    b.append(ofText(Array.get(t, i)));
                }
            }
            b.append("]", NTextStyle.separator());
            return b.toText();
        }
        NTextMapper e = textMapper.get(c);
        if (e != null) {
            return e.ofText(t, this, session);
        }
        if (c.isArray()) {

        }
        return ofPlain(t.toString());
    }

    @Override
    public NTextPlain ofPlain(String t) {
        checkSession();
        return new DefaultNTextPlain(getSession(), t);
    }

    @Override
    public NTextList ofList(NText... nodes) {
        return ofList(Arrays.asList(nodes));
    }

    @Override
    public NTextList ofList(Collection<NText> nodes) {
        checkSession();
        if (nodes == null) {
            return new DefaultNTextList(getSession());
        }
        return new DefaultNTextList(getSession(), nodes.toArray(new NText[0]));
    }

    @Override
    public NText ofStyled(String other, NTextStyles styles) {
        return ofStyled(other == null ? null : ofPlain(other), styles);
    }

    @Override
    public NText ofStyled(NString other, NTextStyles styles) {
        checkSession();
        return ofStyled(other == null ? null : other.toText(), styles);
    }

    @Override
    public NText ofStyled(NText other, NTextStyles styles) {
        if (other == null) {
            return ofPlain("");
        }
        if (styles == null || styles.isPlain()) {
            return other;
        }
        checkSession();
        return new DefaultNTextStyled(getSession(),
                "##:" + styles.id() + ":", "##",
                other, true, styles);
    }


    @Override
    public NText ofStyled(String other, NTextStyle style) {
        return ofStyled(ofPlain(other), style);
    }

    @Override
    public NText ofStyled(NString other, NTextStyle style) {
        return ofStyled(other.toText(), style);
    }

    @Override
    public NText ofStyled(NMsg other, NTextStyles styles) {
        return ofStyled(ofText(other), styles);
    }

    @Override
    public NText ofStyled(NMsg other, NTextStyle style) {
        return ofStyled(ofText(other), style);
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
    public NTextCommand ofCommand(NTerminalCommand command) {
        checkSession();
        return new DefaultNTextCommand(getSession(), "```!", command, "", "```");
    }

    @Override
    public NText ofCodeOrCommand(String lang, String text) {
        return ofCodeOrCommand(lang, text, ' ');
    }

    @Override
    public NText ofCodeOrCommand(String text) {
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
    public NText ofCodeOrCommand(String name, String text, char sep) {
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
            return ofCommand(NTerminalCommand.of(name.substring(1), text));
        }
        return ofCode(name, text, sep);
    }

    private void checkValidSeparator(char sep) {
        if (sep != ':' && !Character.isWhitespace(sep)) {
            throw new NIllegalArgumentException(session, NMsg.ofC("invalid separator '%s'", sep));
        }
    }

    @Override
    public NTextCode ofCode(String lang, String text) {
        return ofCode(lang, text, ' ');
    }

    @Override
    public NTextCode ofCode(String lang, String text, char sep) {
        checkValidSeparator(sep);
        checkSession();
        if (text == null) {
            text = "";
        }
        DefaultNTexts factory0 = (DefaultNTexts) NTexts.of(session);
        return factory0.createCode("```",
                lang, "" + sep, "```", text
        );
    }

    @Override
    public NTitleSequence ofNumbering() {
        checkSession();
        return new DefaultNTitleSequence("");
    }

    @Override
    public NTitleSequence ofNumbering(String pattern) {
        checkSession();
        return new DefaultNTitleSequence((pattern == null || pattern.isEmpty()) ? "1.1.1.a.1" : pattern);
    }

    @Override
    public NTextAnchor ofAnchor(String anchorName) {
        return ofAnchor(anchorName, ' ');
    }

    @Override
    public NTextAnchor ofAnchor(String anchorName, char sep) {
        checkValidSeparator(sep);
        return createAnchor(
                "```!",
                "" + sep, "```", anchorName
        );
    }

    @Override
    public NTextLink ofLink(String value) {
        return ofLink(value, ' ');
    }

    @Override
    public NTextLink ofLink(String value, char sep) {
        checkValidSeparator(sep);
        return new DefaultNTextLink(getSession(), "" + sep, value);
    }

    @Override
    public NTextInclude ofInclude(String value) {
        return ofInclude(value, ' ');
    }

    @Override
    public NTextInclude ofInclude(String value, char sep) {
        checkValidSeparator(sep);
        checkSession();
        return new DefaultNTextInclude(getSession(), "" + sep, value);
    }

    @Override
    public NTextFormatTheme getTheme() {
        checkSession();
        return shared.getTheme(getSession());
    }

    @Override
    public NTexts setTheme(NTextFormatTheme theme) {
        checkSession();
        shared.setTheme(theme, getSession());
        return this;
    }

    @Override
    public NTexts setTheme(String theme) {
        checkSession();
        shared.setTheme(theme, getSession());
        return this;
    }

    @Override
    public NCodeHighlighter getCodeHighlighter(String kind) {
        checkSession();
        return shared.getCodeHighlighter(kind, getSession());
    }

    @Override
    public NTexts addCodeHighlighter(NCodeHighlighter format) {
        checkSession();
        shared.addCodeHighlighter(format, getSession());
        return this;
    }

    @Override
    public NTexts removeCodeHighlighter(String id) {
        checkSession();
        shared.removeCodeHighlighter(id, getSession());
        return this;
    }

    @Override
    public List<NCodeHighlighter> getCodeHighlighters() {
        checkSession();
        return Arrays.asList(shared.getCodeHighlighters(getSession()));
    }

    @Override
    public NText parse(String t) {
        return t == null ? ofBlank() : parser().parse(new StringReader(t));
    }

    @Override
    public NTextParser parser() {
        checkSession();
        return AbstractNTextNodeParserDefaults.createDefault(getSession());
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
        checkSession();
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
                    return new CustomStyleCodeHighlighter(found, session);
                } else {
                    NTextStyle found = NTextStyle.of(NTextStyleType.valueOf(expandAlias(kind.toUpperCase())));
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
    public NTextTitle ofTitle(NText other, int level) {
        String prefix = CoreStringUtils.fillString('#', level) + ")";
        return new DefaultNTextTitle(
                session,
                prefix, level, other
        );
    }

    @Override
    public NTextTitle ofTitle(String other, int level) {
        return ofTitle(ofPlain(other), level);
    }

    @Override
    public NTextTitle ofTitle(NString other, int level) {
        return ofTitle(other.toText(), level);
    }

    public NTextCode createCode(String start, String kind, String separator, String end, String text) {
        checkSession();
        return new DefaultNTextCode(getSession(), start, kind, separator, end, text);
    }

    public NTextCommand createCommand(String start, NTerminalCommand command, String separator, String end) {
        checkSession();
        return new DefaultNTextCommand(getSession(), start, command, separator, end);
    }

    public NTextAnchor createAnchor(String start, String separator, String end, String value) {
        checkSession();
        return new DefaultNTextAnchor(getSession(), start, separator, end, value);
    }

    public NText createTitle(String start, int level, NText child, boolean complete) {
        checkSession();
        return new DefaultNTextTitle(getSession(), start, level, child);
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return DEFAULT_SUPPORT;
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
            if (n.getType() == NTextType.TITLE) {
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
            NTextTransformConfig iconfig = new NTextTransformConfig();
            iconfig.setProcessIncludes(true);
            iconfig.setImportClassLoader(config.getImportClassLoader());
            NTextTransformerContext c = new DefaultNTextTransformerContext(iconfig, session);
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
                NTextTransformerContext c = new DefaultNTextTransformerContext(new NTextTransformConfig(), session);
                text = transform(text, (text1, context) -> {
                    if (text1.getType() == NTextType.TITLE) {
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
            NTextTransformerContext c = new DefaultNTextTransformerContext(config, session);
            if (transformer == null) {
                transformer = c.getDefaultTransformer();
            }
            text = transform(text, transformer == null ? c.getDefaultTransformer() : transformer, c);
        }

        if (anchor != null) {
            List<NText> ok = new ArrayList<>();
            boolean foundAnchor = false;
            if (text.getType() == NTextType.LIST) {
                for (NText o : ((NTextList) text)) {
                    if (foundAnchor) {
                        ok.add(o);
                    } else if (o.getType() == NTextType.ANCHOR) {
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
            }
        }
    }

    @Override
    public void traverseBFS(NText text, NTextVisitor visitor) {
        Queue<NText> q = new ArrayDeque<>();
        q.add(text);
        while (!q.isEmpty()) {
            NText u = q.remove();
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
        switch (text.getType()) {
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
        }
        return null;
    }

    @Override
    public String escapeText(String str) {
        return escapeText0(str);
    }

    private void writeFilteredText(NText t, ByteArrayOutputStream out) {
        if (t != null) {
            if (t instanceof NTextPlain) {
                try {
                    out.write(((NTextPlain) t).getText().getBytes());
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
            parsed = NTexts.of(session).transform(parsed, new NTextTransformConfig().setFiltered(true));
            writeFilteredText(parsed, out);
            return out.toString();
        } catch (Exception ex) {
            NLogOp.of(AbstractNTextNodeParser.class, session)
                    .verb(NLogVerb.WARNING)
                    .level(Level.FINEST)
                    .log(NMsg.ofC("error parsing : %s", text));
            return text;
        }
    }

    @Override
    public NFormat createFormat(NFormatSPI value) {
        return new NFormatFromSPI(value, getSession());
    }

    private interface NTextMapper {
        NText ofText(Object t, NTexts texts, NSession session);
    }
}
