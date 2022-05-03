package net.thevpc.nuts.runtime.standalone.text;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NutsIO;
import net.thevpc.nuts.runtime.standalone.session.NutsSessionUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NutsWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.text.highlighter.CustomStyleCodeHighlighter;
import net.thevpc.nuts.runtime.standalone.text.parser.*;
import net.thevpc.nuts.runtime.standalone.util.CoreStringUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NutsWorkspaceUtils;
import net.thevpc.nuts.spi.NutsSupportLevelContext;
import net.thevpc.nuts.text.*;

import java.io.*;
import java.net.URL;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.time.temporal.Temporal;
import java.util.*;

public class DefaultNutsTexts implements NutsTexts {

    private final NutsWorkspace ws;
    private final DefaultNutsTextManagerModel shared;
    private NutsSession session;

    public DefaultNutsTexts(NutsSession session) {
        this.session = session;
        this.ws = session.getWorkspace();
        this.shared = NutsWorkspaceExt.of(ws).getModel().textModel;
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
        String msg = m.getMessage();
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
                args2[i] = txt.toText(a).toString();
            }
        }
        switch (format) {
            case CSTYLE: {
                StringBuilder sb = new StringBuilder();
                new Formatter(sb, locale).format(msg, args2);
                return txt.parse(sb.toString());
            }
            case JSTYLE: {
                return txt.parse(MessageFormat.format(msg, args2));
            }
            case PLAIN: {
                return txt.ofPlain(msg);
            }
            case NTF: {
                return txt.parse(msg);
            }
            case STYLED: {
                return txt.ofStyled(msg, m.getStyles());
            }
            case CODE: {
                return txt.ofCodeOrCommand(m.getCodeLang(), msg);
            }
        }
        throw new NutsUnsupportedEnumException(getSession(), format);
    }

    public NutsText title(NutsText t, int level) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < level; i++) {
            sb.append("#");
        }
        sb.append(")");
        return createTitle(sb.toString(), level, t, true);
    }

    public NutsText fg(String t, int level) {
        return fg(ofPlain(t), level);
    }

    public NutsText fg(NutsText t, int level) {
        NutsTextStyle textStyle = NutsTextStyle.primary(level);
        return createStyled("##:p" + level + ":", "##", t, NutsTextStyles.of(textStyle), true);
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
    public NutsTextBuilder builder() {
        checkSession();
        return new DefaultNutsTextNodeBuilder(getSession());
    }

    @Override
    public NutsText ofBlank() {
        return ofPlain("");
    }

    @Override
    public NutsText toText(Object t) {
        checkSession();
        if (t == null) {
            return ofBlank();
        }
        if (t instanceof NutsText) {
            return (NutsText) t;
        }
        if (t instanceof NutsFormattable) {
            return (((NutsFormattable) t).formatter(session).setSession(getSession()).setNtf(true).format()).toText();
        }
        if (t instanceof NutsMessage) {
            return _NutsMessage_toString((NutsMessage) t);
        }
        if (t instanceof NutsMessageFormattable) {
            NutsMessage m = ((NutsMessageFormattable) t).formatMessage(getSession());
            return _NutsMessage_toString(m);
        }
        if (t instanceof NutsString) {
            return ((NutsString) t).toText();
        }
        if (t instanceof InputStream) {
            String q = NutsIO.of(session).createInputSource((InputStream) t).getInputMetaData().getName().orElse(t.toString());
            return ofStyled(q, NutsTextStyle.path());
        }
        if (t instanceof OutputStream || t instanceof Writer) {
            return ofStyled(t.toString(), NutsTextStyle.path());
        }
        if (t instanceof Enum) {
            if (t instanceof NutsEnum) {
                return ofStyled(((NutsEnum) t).id(), NutsTextStyle.option());
            } else {
                return ofStyled(((Enum<?>) t).name(), NutsTextStyle.option());
            }
        }
        if (t instanceof Number) {
            return ofStyled(t.toString(), NutsTextStyle.number());
        }
        if (t instanceof Date || t instanceof Temporal) {
            return ofStyled(t.toString(), NutsTextStyle.date());
        }
        if (t instanceof Boolean) {
            return ofStyled(t.toString(), NutsTextStyle.bool());
        }
        if (t instanceof Path || t instanceof File || t instanceof URL) {
            return ofStyled(t.toString(), NutsTextStyle.path());
        }
        if (t instanceof Throwable) {
            return applyStyles(
                    toText(CoreStringUtils.exceptionToMessage((Throwable) t)),
                    NutsTextStyle.error()
            );
        }
        if (t instanceof NutsEnum) {
            return ofStyled(((NutsEnum) t).id(), NutsTextStyle.option());
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
    public NutsTextStyled ofStyled(String other, NutsTextStyles styles) {
        return ofStyled(ofPlain(other), styles);
    }

    @Override
    public NutsTextStyled ofStyled(NutsString other, NutsTextStyles styles) {
        checkSession();
        return ofStyled(other.toText(), styles);
    }

    @Override
    public NutsTextStyled ofStyled(NutsText other, NutsTextStyles styles) {
        return createStyled(other, styles, true);
    }

    @Override
    public NutsText applyStyles(NutsText other, NutsTextStyles styles) {
        return createStyledOrPlain(other, styles, true);
    }

    @Override
    public NutsText applyStyles(NutsText other, NutsTextStyle... styles) {
        return applyStyles(other, NutsTextStyles.of(styles));
    }

    @Override
    public NutsText applyStyles(NutsString other, NutsTextStyles styles) {
        return createStyledOrPlain(other.toText(), styles, true);
    }

    @Override
    public NutsText applyStyles(NutsString other, NutsTextStyle... styles) {
        return applyStyles(other, NutsTextStyles.of(styles));
    }

    @Override
    public NutsTextStyled ofStyled(String other, NutsTextStyle styles) {
        return ofStyled(ofPlain(other), styles);
    }

    @Override
    public NutsTextStyled ofStyled(NutsString other, NutsTextStyle styles) {
        return ofStyled(other.toText(), styles);
    }

    /**
     * this is the default theme!
     *
     * @param other  other
     * @param styles textNodeStyle
     * @return NutsText
     */
    public NutsTextStyled ofStyled(NutsText other, NutsTextStyle styles) {
        return createStyled(other, NutsTextStyles.of(styles), true);
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
            }
            NutsTerminalCommand ntc = NutsTerminalCommand.of(name.substring(1), text);
            if (ntc == null) {
                return ofCode(null, name + sep + text);
            }
            return ofCommand(ntc);
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
    public NutsTextNumbering ofNumbering() {
        checkSession();
        return new DefaultNutsTitleNumberSequence("");
    }

    @Override
    public NutsTextNumbering ofNumbering(String pattern) {
        checkSession();
        return new DefaultNutsTitleNumberSequence((pattern == null || pattern.isEmpty()) ? "1.1.1.a.1" : pattern);
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
        return createLink(
                "```!",
                "" + sep, "```", value
        );
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
        NutsTextStyle textStyle = NutsTextStyle.primary(variant);
        return createStyled("##:s" + variant + ":", "##", t, NutsTextStyles.of(textStyle), true);
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

    public NutsText createStyledOrPlain(NutsText child, NutsTextStyles textStyles, boolean completed) {
        if (textStyles == null || textStyles.isPlain()) {
            return child;
        }
        return createStyled(child, textStyles, completed);
    }

    public NutsTextStyled createStyled(NutsText child, NutsTextStyles textStyles, boolean completed) {
        if (textStyles == null || textStyles.isPlain()) {
            return createStyled("", "", child, textStyles, completed);
        }
        return createStyled("##:" + textStyles.id() + ":", "##", child, textStyles, completed);
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

    public NutsTextStyled createStyled(String start, String end, NutsText child, NutsTextStyles textStyle, boolean completed) {
        if (textStyle == null) {
            textStyle = NutsTextStyles.PLAIN;
        }
        checkSession();
        return new DefaultNutsTextStyled(getSession(), start, end, child, completed, textStyle);
    }

    public NutsTextCode createCode(String start, String kind, String separator, String end, String text) {
        checkSession();
        return new DefaultNutsTextCode(getSession(), start, kind, separator, end, text);
    }

    public NutsTextCommand createCommand(String start, NutsTerminalCommand command, String separator, String end) {
        checkSession();
        return new DefaultNutsTextCommand(getSession(), start, command, separator, end);
    }

    public NutsTextLink createLink(String start, String separator, String end, String value) {
        checkSession();
        return new DefaultNutsTextLink(getSession(), start, separator, end, value);
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

}
