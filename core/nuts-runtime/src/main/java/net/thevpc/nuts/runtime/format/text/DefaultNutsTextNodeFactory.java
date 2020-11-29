package net.thevpc.nuts.runtime.format.text;

import net.thevpc.nuts.NutsTextNode;
import net.thevpc.nuts.NutsTextNodeFactory;
import net.thevpc.nuts.NutsTextNodeStyle;
import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.runtime.format.text.parser.*;
import net.thevpc.nuts.runtime.format.text.special.*;

import java.util.Arrays;
import java.util.Collection;

public class DefaultNutsTextNodeFactory implements NutsTextNodeFactory {
    private static TextFormat[] FOREGROUNDS = new TextFormat[]{
            TextFormats.FG_BLUE,
            TextFormats.FG_GREEN,
            TextFormats.FG_YELLOW,
            TextFormats.FG_CYAN,
            TextFormats.FG_YELLOW,
            TextFormats.FG_MAGENTA,
            TextFormats.FG_RED,
            TextFormats.FG_GREY,
            TextFormats.FG_BLACK,
            TextFormats.FG_WHITE
    };
    private static TextFormat[] BACKGROUNDS = new TextFormat[]{
            TextFormats.BG_BLUE,
            TextFormats.BG_GREEN,
            TextFormats.BG_YELLOW,
            TextFormats.BG_CYAN,
            TextFormats.BG_YELLOW,
            TextFormats.BG_MAGENTA,
            TextFormats.BG_RED,
            TextFormats.BG_GREY,
            TextFormats.BG_BLACK,
            TextFormats.BG_WHITE
    };
    private static TextFormat[] STYLES = new TextFormat[]{
            TextFormats.UNDERLINED,
            TextFormats.ITALIC,
            TextFormats.STRIKED,
            TextFormats.REVERSED,
    };
    private NutsWorkspace ws;

    public DefaultNutsTextNodeFactory(NutsWorkspace ws) {
        this.ws = ws;
    }

    public TextFormat styleFormat(int index) {
        index--;
        if (index < 0) {
            index = 0;
        }
        if (index >= STYLES.length) {
            index = STYLES.length - 1;
        }
        return STYLES[index];
    }

    public TextFormat foregroundFormat(int index) {
        index--;
        if (index < 0) {
            index = 0;
        }
        if (index >= FOREGROUNDS.length) {
            index = FOREGROUNDS.length - 1;
        }
        return FOREGROUNDS[index];
    }

    public TextFormat backgroundFormat(int index) {
        index--;
        if (index < 0) {
            index = 0;
        }
        if (index >= BACKGROUNDS.length) {
            index = BACKGROUNDS.length - 1;
        }
        return BACKGROUNDS[index];
    }

    @Override
    public NutsTextNode plain(String t) {
        return new DefaultNutsTextNodePlain(t);
    }

    @Override
    public NutsTextNode title(String t, int level) {
        return title(plain(t), level);
    }

    @Override
    public NutsTextNode list(NutsTextNode... nodes) {
        return list(Arrays.asList(nodes));
    }

    @Override
    public NutsTextNode list(Collection<NutsTextNode> nodes) {
        if (nodes == null || nodes.isEmpty()) {
            return plain("");
        }
        if (nodes.size() == 1) {
            return (NutsTextNode) nodes.toArray()[0];
        }
        return new DefaultNutsTextNodeList(nodes.toArray(new NutsTextNode[0]));
    }

    @Override
    public NutsTextNode error(String image) {
        return list(
                bg("error:", 7),
                plain(" "),
                fg(image, 7)
        );
    }

    @Override
    public NutsTextNode warn(String image) {
        return list(
                bg("warning:", 5),
                plain(" "),
                fg(image, 5)
        );
    }

    @Override
    public NutsTextNode styled(String other, NutsTextNodeStyle... decorations) {
        return styled(plain(other), decorations);
    }

    @Override
    public NutsTextNode styled(NutsTextNode other, NutsTextNodeStyle... decorations) {
        switch (decorations.length) {
            case 0:
                return other;
            case 1:
                return styled(other, decorations[0]);
        }

        NutsTextNode n = other;
        for (int i = decorations.length - 1; i >= 0; i--) {
            n = styled(n, decorations[i]);
        }
        return n;
    }

    @Override
    public NutsTextNode command(String command, String args) {
        switch (command) {
            case "anchor": {
                return createAnchor(
                        "```!",
                        command, "", "```", args
                );
            }
            case "link": {
                return createLink(
                        "```!",
                        command, "", "```", args
                );
            }
        }
        return createCommand(
                "```",
                command, "", "```", args,
                null
        );
    }

    @Override
    public NutsTextNode code(String lang, String text) {
        if (text == null) {
            text = "";
        }
        DefaultNutsTextNodeFactory factory0 = (DefaultNutsTextNodeFactory) ws.formats().text().factory();
        if (text.indexOf('\n') >= 0) {
            return factory0.createCommand("```",
                    lang, "\n", text, "```"
            );
        } else {
            return factory0.createCommand("```",
                    lang, "", text, "```"
            );
        }
    }

    @Override
    public NutsTextNode parseCode(String lang, String text) {
        SpecialTextFormatter t = resolveFormatter(lang);
        return t.toNode(text);
    }

    public NutsTextNode tilde(NutsTextNode t, int level) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i <= level; i++) {
            sb.append("~");
        }
        return createStyled(sb.toString(), sb.toString(), t,true);
    }

    public NutsTextNode title(NutsTextNode t, int level) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < level; i++) {
            sb.append("#");
        }
        sb.append(")");
        return createTitle(sb.toString(), t, foregroundFormat(level));
    }

    public NutsTextNode fg(String t, int level) {
        return fg(plain(t), level);
    }

    public NutsTextNode fg(NutsTextNode t, int level) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < level + 1; i++) {
            sb.append("#");
        }
        return createStyled(sb.toString(), sb.toString(), t, true);
    }

    public NutsTextNode bg(String t, int level) {
        return bg(plain(t), level);
    }

    public NutsTextNode bg(NutsTextNode t, int level) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < level + 1; i++) {
            sb.append("@");
        }
        return createStyled(sb.toString(), sb.toString(), t, true);
    }

    public NutsTextNode comments(String image) {
        return fg(image, 4);
    }

    public NutsTextNode literal(String image) {
        return fg(image, 1);
    }

    public NutsTextNode stringLiteral(String image) {
        return fg(image, 3);
    }

    public NutsTextNode numberLiteral(String image) {
        return fg(image, 1);
    }

    public NutsTextNode reservedWord(String image) {
        return fg(image, 1);
    }

    public NutsTextNode annotation(String image) {
        return fg(image, 3);
    }

    public NutsTextNode separator(String image) {
        return fg(image, 6);
    }

    public NutsTextNode commandName(String image) {
        return fg(image, 1);
    }

    public NutsTextNode subCommand1Name(String image) {
        return fg(image, 2);
    }

    public NutsTextNode subCommand2Name(String image) {
        return fg(image, 3);
    }

    public NutsTextNode optionName(String image) {
        return fg(image, 4);
    }

    public NutsTextNode userInput(String image) {
        return fg(image, 8);
    }

    public NutsTextNode styled(NutsTextNode other, NutsTextNodeStyle decoration) {
        if (decoration == null) {
            if (other == null) {
                return plain("");
            }
            return other;
        }
        switch (decoration) {
            case PRIMARY1:
            case PRIMARY2:
            case PRIMARY3:
            case PRIMARY4:
            case PRIMARY5:
            case PRIMARY6:
            case PRIMARY7:
            case PRIMARY8:
            case PRIMARY9: {
                return fg(other, decoration.ordinal() - NutsTextNodeStyle.PRIMARY1.ordinal() + 1);
            }
            case SECONDARY1:
            case SECONDARY2:
            case SECONDARY3:
            case SECONDARY4:
            case SECONDARY5:
            case SECONDARY6:
            case SECONDARY7:
            case SECONDARY8:
            case SECONDARY9: {
                return bg(other, decoration.ordinal() - NutsTextNodeStyle.SECONDARY1.ordinal() + 1);
            }
            case UNDERLINED: {
                return tilde(other, 1);
            }
            case ITALIC: {
                return tilde(other, 2);
            }
            case STRIKED: {
                return tilde(other, 3);
            }
            case REVERSED: {
                return tilde(other, 4);
            }
            case KEYWORD1: {
                return styled(other, NutsTextNodeStyle.PRIMARY1);
            }
            case KEYWORD2: {
                return styled(other, NutsTextNodeStyle.PRIMARY2);
            }
            case KEYWORD3: {
                return styled(other, NutsTextNodeStyle.PRIMARY3);
            }
            case KEYWORD4: {
                return styled(other, NutsTextNodeStyle.PRIMARY4);
            }
            case OPTION1: {
                return styled(other, NutsTextNodeStyle.PRIMARY3);
            }
            case OPTION2: {
                return styled(other, NutsTextNodeStyle.PRIMARY4);
            }
            case OPTION3: {
                return styled(other, NutsTextNodeStyle.PRIMARY5);
            }
            case OPTION4: {
                return styled(other, NutsTextNodeStyle.PRIMARY6);
            }
            case ERROR1: {
                return styled(other, NutsTextNodeStyle.PRIMARY7);
            }
            case ERROR2: {
                return list(
                        styled(plain("error:"), NutsTextNodeStyle.SECONDARY7),
                        plain(" "),
                        styled(other, NutsTextNodeStyle.PRIMARY7)
                );
            }
            case WARNING1: {
                return styled(other, NutsTextNodeStyle.PRIMARY3);
            }
            case WARNING2: {
                return list(
                        styled(plain("warning:"), NutsTextNodeStyle.SECONDARY3),
                        plain(" "),
                        styled(other, NutsTextNodeStyle.PRIMARY3)
                );
            }
            case HIDDEN1:
            case HIDDEN2: {
                return styled(other, NutsTextNodeStyle.PRIMARY8);
            }
            case NUMBER1:
            case NUMBER2:
            case BOOLEAN1:
            case BOOLEAN2: {
                return styled(other, NutsTextNodeStyle.PRIMARY1);
            }
            case STRING1:
            case STRING2: {
                return styled(other, NutsTextNodeStyle.PRIMARY2);
            }
            case COMMENTS1:
            case COMMENTS2: {
                return styled(other, NutsTextNodeStyle.PRIMARY4);
            }
            case SEPARATOR1:
            case SEPARATOR2:
            case SEPARATOR3:
            case SEPARATOR4: {
                return styled(other, NutsTextNodeStyle.PRIMARY7);
            }
            case USER_INPUT1:
            case USER_INPUT2:
            case USER_INPUT3:
            case USER_INPUT4: {
                return styled(other, NutsTextNodeStyle.PRIMARY8);
            }
            case SUCCESS1:
            case SUCCESS2: {
                return styled(other, NutsTextNodeStyle.PRIMARY3);
            }
            case FAIL1:
            case FAIL2: {
                return styled(other, NutsTextNodeStyle.PRIMARY7);
            }
            case VAR1:
            case VAR2:
            case VAR3:
            case VAR4: {
                return styled(other, NutsTextNodeStyle.PRIMARY5);
            }
        }
        throw new IllegalArgumentException("Invalid " + decoration);
    }

    private SpecialTextFormatter resolveFormatter(String kind) {
        if (kind == null) {
            kind = "";
        }
        if (kind.length() > 0) {
            switch (kind.toLowerCase()) {
                case "shell": {
                    return new ShellSpecialTextFormatter(ws);
                }
                case "json": {
                    return new JsonSpecialTextFormatter(ws);
                }
                case "xml": {
                    return new XmlSpecialTextFormatter(ws);
                }
                case "java": {
                    return new JavaSpecialTextFormatter(ws);
                }

                //special renaming...
                case "warn":
                case "warn1": {
                    return new CustomStyleSpecialTextFormatter(NutsTextNodeStyle.WARNING1, ws);
                }
                case "bool":
                case "bool1": {
                    return new CustomStyleSpecialTextFormatter(NutsTextNodeStyle.BOOLEAN1, ws);
                }
                case "bool2": {
                    return new CustomStyleSpecialTextFormatter(NutsTextNodeStyle.BOOLEAN2, ws);
                }
                case "kw":
                case "kw1": {
                    return new CustomStyleSpecialTextFormatter(NutsTextNodeStyle.KEYWORD1, ws);
                }
                case "kw2": {
                    return new CustomStyleSpecialTextFormatter(NutsTextNodeStyle.KEYWORD2, ws);
                }
                case "kw3": {
                    return new CustomStyleSpecialTextFormatter(NutsTextNodeStyle.KEYWORD3, ws);
                }
                case "kw4": {
                    return new CustomStyleSpecialTextFormatter(NutsTextNodeStyle.KEYWORD4, ws);
                }

                //Default styles...
                default: {
                    try {
                        NutsTextNodeStyle found = NutsTextNodeStyle.valueOf(kind.toUpperCase());
                        return new CustomStyleSpecialTextFormatter(found, ws);
                    } catch (Exception ex) {
                        //ignore
                    }
                    if (!Character.isDigit(kind.charAt(kind.length() - 1))) {
                        try {
                            NutsTextNodeStyle found = NutsTextNodeStyle.valueOf(kind.toUpperCase() + "1");
                            return new CustomStyleSpecialTextFormatter(found, ws);
                        } catch (Exception ex) {
                            //ignore
                        }
                    }
                }
            }
        }
        return new PlainSpecialTextFormatter(ws);
    }

    public NutsTextNode createStyled(String start, String end, NutsTextNode child, boolean completed) {
        TextFormat style1 = createStyle(start);
        if (style1 == null) {
            return child;
        }
        return new DefaultNutsTextNodeStyled(start, end, style1, child, completed);
    }

    public NutsTextNode createCommand(String start, String kind, String separator, String end, String text) {
        return new DefaultNutsTextNodeCode(start, kind, separator, end, text);
    }

    public NutsTextNode createCommand(String start, String command, String separator, String end, String text, TextFormat style) {
        return new DefaultNutsTextNodeCommand(start, command, separator, end, text, style);
    }

    public NutsTextNode createLink(String start, String command, String separator, String end, String value) {
        return new DefaultNutsTextNodeLink(start, command, separator, end, value);
    }

    public NutsTextNode createAnchor(String start, String command, String separator, String end, String value) {
        return new DefaultNutsTextNodeAnchor(start, command, separator, end, value);
    }

    public NutsTextNode createTitle(String start, NutsTextNode child, TextFormat style) {
        if (style == null) {
            return child;
        }
        return new DefaultNutsTextNodeTitle(start, style, child);
    }

    public TextFormat createStyle(String code) {
        switch (code) {
            case "~~":
            case "~~~":
            case "~~~~":
            case "~~~~~": {
                return styleFormat(code.length() - 1);
            }
            case "##":
            case "###":
            case "####":
            case "#####":
            case "######":
            case "#######":
            case "########":
            case "#########":
            case "##########": {
                return foregroundFormat(code.length() - 1);
            }
            case "#)":
            case "##)":
            case "###)":
            case "####)":
            case "#####)":
            case "######)":
            case "#######)":
            case "########)":
            case "#########)": {
                return foregroundFormat(code.length() - 1);
            }

            case "@@":
            case "@@@":
            case "@@@@":
            case "@@@@@":
            case "@@@@@@":
            case "@@@@@@@":
            case "@@@@@@@@":
            case "@@@@@@@@@":
            case "@@@@@@@@@@": {
                return backgroundFormat(code.length() - 1);
            }
        }
        throw new UnsupportedOperationException("Unsupported format " + code);
    }

}
