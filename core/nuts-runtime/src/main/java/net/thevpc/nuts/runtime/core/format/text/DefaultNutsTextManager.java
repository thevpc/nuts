package net.thevpc.nuts.runtime.core.format.text;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.format.text.parser.*;
import net.thevpc.nuts.runtime.core.format.text.bloc.*;
import net.thevpc.nuts.NutsTextFormatTheme;
import net.thevpc.nuts.runtime.core.util.CoreStringUtils;

import java.io.File;
import java.io.StringReader;
import java.net.URL;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.time.temporal.Temporal;
import java.util.*;
import net.thevpc.nuts.NutsCodeFormat;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;

public class DefaultNutsTextManager implements NutsTextManager {

    private NutsWorkspace ws;
    private NutsSession session;
    private DefaultNutsTextManagerModel shared;

    public DefaultNutsTextManager(NutsWorkspace ws, DefaultNutsTextManagerModel shared) {
        this.ws = ws;
        this.shared = shared;
    }

    @Override
    public NutsTextNodeBuilder builder() {
        checkSession();
        return new DefaultNutsTextNodeBuilder(getSession());
    }

    @Override
    public NutsText parse(String t) {
        return t == null ? forBlank() : parser().parse(new StringReader(t));
    }

    @Override
    public NutsTextParser parser() {
        checkSession();
        return new DefaultNutsTextNodeParser(getSession());
    }
    

    private void checkSession() {
        NutsWorkspaceUtils.checkSession(ws, getSession());
    }

    @Override
    public NutsSession getSession() {
        return session;
    }

    @Override
    public DefaultNutsTextManager setSession(NutsSession session) {
        this.session = session;
        return this;
    }

    @Override
    public NutsText forBlank() {
        return forPlain("");
    }

    @Override
    public NutsText toText(Object t) {
        if (t == null) {
            return forBlank();
        }
        if (t instanceof NutsText) {
            return (NutsText) t;
        }
        if (t instanceof NutsFormattable) {
            return parse(((NutsFormattable) t).formatter().format());
        }
        if (t instanceof NutsMessage) {
            return _NutsFormattedMessage_toString((NutsMessage) t);
        }
        if (t instanceof NutsString) {
            return ((NutsString) t).toNode();
        }
        if (t instanceof Number) {
            return forStyled(t.toString(), NutsTextNodeStyle.number());
        }
        if (t instanceof Date || t instanceof Temporal) {
            return forStyled(t.toString(), NutsTextNodeStyle.date());
        }
        if (t instanceof Boolean) {
            return forStyled(t.toString(), NutsTextNodeStyle.bool());
        }
        if (t instanceof Path || t instanceof File || t instanceof URL) {
            return forStyled(t.toString(), NutsTextNodeStyle.path());
        }
        if (t instanceof Throwable) {
            return forStyled(
                    CoreStringUtils.exceptionToString((Throwable) t),
                    NutsTextNodeStyle.error()
            );
        }
        return forPlain(t.toString());
    }

    private NutsText _NutsFormattedMessage_toString(NutsMessage m) {
        checkSession();
        NutsTextFormatStyle style = m.getStyle();
        if (style == null) {
            style = NutsTextFormatStyle.JSTYLE;
        }
        Object[] params = m.getParams();
        if (params == null) {
            params = new Object[0];
        }
        String msg = m.getMessage();
        String sLocale = getSession() == null ? null : getSession().getLocale();
        Locale locale = CoreStringUtils.isBlank(sLocale) ? null : new Locale(sLocale);
        Object[] args2 = new Object[params.length];
        NutsTextManager txt = getSession().getWorkspace().formats().text();
        for (int i = 0; i < args2.length; i++) {
            Object a = params[i];
            if (a==null) {
                //do nothing, support format pattern
                args2[i] = null;
            }else if (a instanceof Number || a instanceof Date || a instanceof Temporal) {
                //do nothing, support format pattern
                args2[i] = a;
            } else {
                args2[i] = txt.toText(a).toString();
            }
        }
        switch (style) {
            case CSTYLE: {
                StringBuilder sb = new StringBuilder();
                new Formatter(sb, locale).format(msg, args2);
                return txt.parse(sb.toString());
            }
            case JSTYLE: {
                return txt.parse(MessageFormat.format(msg, args2));
            }
        }
        throw new NutsUnsupportedEnumException(getSession(), style);
    }

    @Override
    public NutsTextPlain forPlain(String t) {
        checkSession();
        return new DefaultNutsTextPlain(getSession(), t);
    }

    @Override
    public NutsTextList forList(NutsText... nodes) {
        return forList(Arrays.asList(nodes));
    }

    @Override
    public NutsTextList forList(Collection<NutsText> nodes) {
        checkSession();
        if (nodes == null) {
            return new DefaultNutsTextList(getSession(), new NutsText[0]);
        }
        return new DefaultNutsTextList(getSession(), nodes.toArray(new NutsText[0]));
    }

    @Override
    public NutsTextStyled forStyled(String other, NutsTextNodeStyle decorations) {
        return forStyled(forPlain(other), decorations);
    }
    

    @Override
    public NutsTextStyled forStyled(NutsString other, NutsTextNodeStyle decorations) {
        return forStyled(other.toString(), decorations);
    }

    @Override
    public NutsTextStyled forStyled(String other, NutsTextNodeStyles decorations) {
        return forStyled(forPlain(other), decorations);
    }

    @Override
    public NutsTextStyled forStyled(NutsString other, NutsTextNodeStyles decorations) {
        return forStyled(ws.formats().text().parse(other.toString()), decorations);
    }

    @Override
    public NutsTextStyled forStyled(NutsText other, NutsTextNodeStyles styles) {
        return createStyled(other, styles, true);
    }

    @Override
    public NutsTextCommand forCommand(NutsTerminalCommand command) {
        checkSession();
        return new DefaultNutsTextCommand(getSession(), "```!", command, "", "```");
    }


    @Override
    public NutsTextAnchor forAnchor(String anchorName) {
        return createAnchor(
                "```!",
                "", "```", anchorName
        );
    }

    @Override
    public NutsTextLink forLink(NutsText value) {
        return createLink(
                "```!",
                "", "```", value
        );
    }


    @Override
    public NutsTextCode forCode(String lang, String text) {
        checkSession();
        if (text == null) {
            text = "";
        }
        DefaultNutsTextManager factory0 = (DefaultNutsTextManager) ws.formats().text().setSession(session);
        if (text.indexOf('\n') >= 0) {
            return factory0.createCode("```",
                    lang, "\n", "```", text
            );
        } else {
            return factory0.createCode("```",
                    lang, "", "```", text
            );
        }
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
        return fg(forPlain(t), level);
    }

    public NutsText fg(NutsText t, int level) {
        NutsTextNodeStyle textStyle = NutsTextNodeStyle.primary(level);
        return createStyled("##:p" + level + ":", "##", t, NutsTextNodeStyles.of(textStyle), true);
    }

    public NutsText bg(String t, int level) {
        return bg(forPlain(t), level);
    }

    public NutsText bg(NutsText t, int variant) {
        NutsTextNodeStyle textStyle = NutsTextNodeStyle.primary(variant);
        return createStyled("##:s" + variant + ":", "##", t, NutsTextNodeStyles.of(textStyle), true);
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

    /**
     * this is the default theme!
     *
     * @param other other
     * @param textNodeStyle textNodeStyle
     * @return NutsText
     */
    public NutsTextStyled forStyled(NutsText other, NutsTextNodeStyle textNodeStyle) {
        return createStyled(other, NutsTextNodeStyles.of(textNodeStyle), true);
    }

    public NutsCodeFormat resolveBlocTextFormatter(String kind) {
        if (kind == null) {
            kind = "";
        }
        NutsCodeFormat format = getCodeFormat(kind);
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
                    NutsTextNodeStyle found = NutsTextNodeStyle.of(NutsTextNodeStyleType.valueOf(expandAlias(kind.toUpperCase().substring(0, x))),
                            Integer.parseInt(kind.substring(x))
                    );
                    return new CustomStyleBlocTextFormatter(found, ws);
                } else {
                    NutsTextNodeStyle found = NutsTextNodeStyle.of(NutsTextNodeStyleType.valueOf(expandAlias(kind.toUpperCase())));
                    return new CustomStyleBlocTextFormatter(found, ws);
                }
            } catch (Exception ex) {
                //ignore
            }
        }
        return getCodeFormat("plain");
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

    public NutsTextStyled createStyled(NutsText child, NutsTextNodeStyles textStyles, boolean completed) {
        NutsText curr = child;
        if (textStyles == null || textStyles.isNone()) {
            return createStyled("", "", child, textStyles, completed);
        }
        for (int i = textStyles.size() - 1; i >= 0; i--) {
            NutsTextNodeStyle textStyle = textStyles.get(i);
            NutsTextNodeStyles textStyle2 = NutsTextNodeStyles.of(textStyle);
            String svar = textStyle.getVariant() == 0 ? "" : ("" + textStyle.getVariant());
            switch (textStyle.getType()) {
                case PRIMARY: {
                    curr = createStyled("##:p" + svar + ":", "##", curr, textStyle2, completed);
                    break;
                }
                case SECONDARY: {
                    curr = createStyled("##:s" + svar + ":", "##", curr, textStyle2, completed);
                    break;
                }
                case UNDERLINED: {
                    curr = createStyled("##:_:", "##", curr, textStyle2, completed);
                    break;
                }
                case BLINK: {
                    curr = createStyled("##:%:", "##", curr, textStyle2, completed);
                    break;
                }
                case ITALIC: {
                    curr = createStyled("##:/:", "##", curr, textStyle2, completed);
                    break;
                }
                case BOLD: {
                    curr = createStyled("##:+:", "##", curr, textStyle2, completed);
                    break;
                }
                case REVERSED: {
                    curr = createStyled("##:!:", "##", curr, textStyle2, completed);
                    break;
                }
                case FORE_COLOR: {
                    String s = Integer.toString(textStyle.getVariant(),16);
                    while (s.length() < 8) {
                        s = "0" + s;
                    }
                    curr = createStyled("##:f" + s + ":", "##", curr, textStyle2, completed);
                    break;
                }
                case BACK_COLOR: {
                    String s = Integer.toString(textStyle.getVariant(),16);
                    while (s.length() < 8) {
                        s = "0" + s;
                    }
                    curr = createStyled("##:b" + svar + ":", "##", curr, textStyle2, completed);
                    break;
                }
                default: {
                    curr = createStyled("##:" + textStyle.getType().toString().toUpperCase() + ":", "##", curr, textStyle2, completed);
                }
            }
        }
        return (NutsTextStyled) curr;
    }

    public NutsTextStyled createStyled(String start, String end, NutsText child, NutsTextNodeStyles textStyle, boolean completed) {
        if (textStyle == null) {
            textStyle = NutsTextNodeStyles.NONE;
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

    public NutsTextLink createLink(String start, String separator, String end, NutsText value) {
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
    public NutsTextNumbering forNumbering() {
        checkSession();
        return new DefaultNutsTitleNumberSequence("");
    }

    @Override
    public NutsTextNumbering forTitleNumberSequence(String pattern) {
        checkSession();
        return new DefaultNutsTitleNumberSequence((pattern == null || pattern.isEmpty()) ? "1.1.1.a.1" : pattern);
    }

    @Override
    public NutsTextFormatTheme getTheme() {
        checkSession();
        return shared.getTheme(getSession());
    }

    @Override
    public NutsTextManager setTheme(NutsTextFormatTheme theme) {
        checkSession();
        shared.setTheme(theme,getSession());
        return this;
    }

    @Override
    public NutsCodeFormat getCodeFormat(String kind) {
        checkSession();
        return shared.getCodeFormat(kind,getSession());
    }

    @Override
    public NutsTextManager addCodeFormat(NutsCodeFormat format) {
        checkSession();
        shared.addCodeFormat(format, getSession());
        return this;
    }

    @Override
    public NutsTextManager removeCodeFormat(NutsCodeFormat format) {
        checkSession();
        shared.removeCodeFormat(format, getSession());
        return this;
    }

    @Override
    public NutsCodeFormat[] getCodeFormats() {
        checkSession();
        return shared.getCodeFormats(getSession());
    }
    
}
