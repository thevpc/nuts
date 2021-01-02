package net.thevpc.nuts.runtime.core.format.text;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.format.text.parser.*;
import net.thevpc.nuts.runtime.core.format.text.bloc.*;
import net.thevpc.nuts.runtime.core.format.text.stylethemes.DefaultNutsTextStyleTheme;
import net.thevpc.nuts.runtime.core.format.text.stylethemes.NutsTextStyleTheme;

import java.io.StringReader;
import java.util.Arrays;
import java.util.Collection;

public class DefaultNutsTextNodeFactory implements NutsTextNodeFactory {
    private NutsWorkspace ws;
    private NutsTextStyleTheme styleTheme= DefaultNutsTextStyleTheme.DEFAULT;

    public DefaultNutsTextNodeFactory(NutsWorkspace ws) {
        this.ws = ws;
    }

    @Override
    public NutsTextNode formatted(NutsFormattable t) {
        return formatted(ws.formats().of(t).format());
    }

    @Override
    public NutsTextNode formatted(String t) {
        return ws.formats().text().parser().parse(new StringReader(t));
    }

    @Override
    public NutsTextNode plain(String t) {
        return new DefaultNutsTextNodePlain(t);
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
    public NutsTextNode command(String command) {
        return command(command,"");
    }

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
                "```!",
                command, "", "```", args
        );
    }

    @Override
    public NutsTextNode code(String lang, String text) {
        if (text == null) {
            text = "";
        }
        DefaultNutsTextNodeFactory factory0 = (DefaultNutsTextNodeFactory) ws.formats().text().factory();
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

    @Override
    public NutsTextNode parseBloc(String lang, String text) {
        BlocTextFormatter t = resolveBlocTextFormatter(lang);
        return t.toNode(text);
    }

    public NutsTextNode title(NutsTextNode t, int level) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < level; i++) {
            sb.append("#");
        }
        sb.append(")");
        return createTitle(sb.toString(), level,t,true);
    }

    public NutsTextNode fg(String t, int level) {
        return fg(plain(t), level);
    }

    public NutsTextNode fg(NutsTextNode t, int level) {
        NutsTextNodeStyle textStyle=NutsTextNodeStyle.primary(level);
        return createStyled("##:p"+level+":", "##", t, textStyle, true);
    }

    public NutsTextNode bg(String t, int level) {
        return bg(plain(t), level);
    }

    public NutsTextNode bg(NutsTextNode t, int level) {
        NutsTextNodeStyle textStyle=NutsTextNodeStyle.primary(level);
        return createStyled("##:s"+level+":", "##", t, textStyle, true);
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

    /**
     * this is the default theme!
     * @param other other
     * @param textNodeStyle textNodeStyle
     * @return NutsTextNode
     */
    public NutsTextNode styled(NutsTextNode other, NutsTextNodeStyle textNodeStyle) {
        if (other == null) {
            return plain("");
        }
        if (textNodeStyle == null) {
            return other;
        }
        switch (textNodeStyle.getType()) {
            case FORE_COLOR:{
                return createStyled("##:f"+ textNodeStyle.getVariant()+":", "##", other, textNodeStyle, true);
            }
            case BACK_COLOR:{
                return createStyled("##:b"+ textNodeStyle.getVariant()+":", "##", other, textNodeStyle, true);
            }
            case FORE_TRUE_COLOR:{
                String s = Integer.toString(0, textNodeStyle.getVariant());
                while(s.length()<8){
                    s="0"+s;
                }
                return createStyled("##:fx"+ s +":", "##", other, textNodeStyle, true);
            }
            case BACK_TRUE_COLOR:{
                String s = Integer.toString(0, textNodeStyle.getVariant());
                while(s.length()<8){
                    s="0"+s;
                }
                return createStyled("##:bx"+ textNodeStyle.getVariant()+":", "##", other, textNodeStyle, true);
            }
            case UNDERLINED: {
                return createStyled("##:_:", "##", other, textNodeStyle, true);
            }
            case ITALIC: {
                return createStyled("##:/:", "##", other, textNodeStyle, true);
            }
            case STRIKED: {
                return createStyled("##:-:", "##", other, textNodeStyle, true);
            }
            case REVERSED: {
                return createStyled("##:!:", "##", other, textNodeStyle, true);
            }
            case BOLD: {
                return createStyled("##:+:", "##", other, textNodeStyle, true);
            }
            case BLINK: {
                return createStyled("##:%:", "##", other, textNodeStyle, true);
            }
            case PRIMARY:{
                return createStyled("##:p:", "##", other, textNodeStyle, true);
            }
            case SECONDARY:{
                return createStyled("##:s:", "##", other, textNodeStyle, true);
            }
            default:{
                return createStyled("##:"+
                        textNodeStyle.getType().toString().toUpperCase()
                        +":", "##", other, textNodeStyle, true);
            }
        }
    }

    private BlocTextFormatter resolveBlocTextFormatter(String kind) {
        if (kind == null) {
            kind = "";
        }
        if (kind.length() > 0) {
            switch (kind.toLowerCase()) {
                case "sh": {
                    return new ShellBlocTextFormatter(ws);
                }
                case "json": {
                    return new JsonBlocTextFormatter(ws);
                }
                case "xml": {
                    return new XmlBlocTextFormatter(ws);
                }
                case "java": {
                    return new JavaBlocTextFormatter(ws);
                }

                //Default styles...
                default: {
                    try {
                        String cc = kind.toUpperCase();
                        int x=cc.length();
                        while(Character.isDigit(cc.charAt(x-1))){
                            x--;
                        }
                        if(x<cc.length()){
                            NutsTextNodeStyle found = NutsTextNodeStyle.of(NutsTextNodeStyleType.valueOf(expandAlias(kind.toUpperCase().substring(0, x))),
                                    Integer.parseInt(kind.substring(x))
                                    );
                            return new CustomStyleBlocTextFormatter(found, ws);
                        }else {
                            NutsTextNodeStyle found = NutsTextNodeStyle.of(NutsTextNodeStyleType.valueOf(expandAlias(kind.toUpperCase())));
                            return new CustomStyleBlocTextFormatter(found, ws);
                        }
                    } catch (Exception ex) {
                        //ignore
                    }
                }
            }
        }
        return new PlainBlocTextFormatter(ws);
    }

    private String expandAlias(String ss) {
        switch (ss.toUpperCase()){
            case "BOOL":{
                ss ="BOOLEAN";
                break;
            }
            case "KW":{
                ss ="KEYWORD";
                break;
            }
        }
        return ss;
    }

    public NutsTextNode createStyled(NutsTextNode child, NutsTextNodeStyle textStyle, boolean completed) {
        String svar = textStyle.getVariant() == 0 ? "" : (""+textStyle.getVariant());
        switch (textStyle.getType()){
            case PRIMARY:{
                return createStyled("##:p"+ svar +":","##",child,textStyle,completed);
            }
            case SECONDARY:{
                return createStyled("##:s"+ svar +":","##",child,textStyle,completed);
            }
            case UNDERLINED:{
                return createStyled("##:_:","##",child,textStyle,completed);
            }
            case BLINK:{
                return createStyled("##:%:","##",child,textStyle,completed);
            }
            case ITALIC:{
                return createStyled("##:/:","##",child,textStyle,completed);
            }
            case BOLD:{
                return createStyled("##:+:","##",child,textStyle,completed);
            }
            case REVERSED:{
                return createStyled("##:!:","##",child,textStyle,completed);
            }
            case FORE_COLOR:{
                return createStyled("##:f"+svar+":","##",child,textStyle,completed);
            }
            case BACK_COLOR:{
                return createStyled("##:b"+svar+":","##",child,textStyle,completed);
            }
            default:{
                return createStyled("##:"+textStyle.getType().toString().toUpperCase()+":","##",child,textStyle,completed);
            }
        }
    }
    public NutsTextNode createStyled(String start, String end, NutsTextNode child, NutsTextNodeStyle textStyle, boolean completed) {
        if (textStyle == null) {
            throw new NutsIllegalArgumentException(ws,"missing textStyle");
        }
        return new DefaultNutsTextNodeStyled(start, end, child, completed, textStyle);
    }

    public NutsTextNode createCode(String start, String kind, String separator, String end, String text) {
        return new DefaultNutsTextNodeCode(start, kind, separator, end, text);
    }

    public NutsTextNode createCommand(String start, String command, String separator, String end, String text) {
        return new DefaultNutsTextNodeCommand(start, command, separator, end, text);
    }

    public NutsTextNode createLink(String start, String command, String separator, String end, String value) {
        return new DefaultNutsTextNodeLink(start, command, separator, end, value);
    }

    public NutsTextNode createAnchor(String start, String command, String separator, String end, String value) {
        return new DefaultNutsTextNodeAnchor(start, command, separator, end, value);
    }

    public NutsTextNode createTitle(String start, int level,NutsTextNode child,boolean complete) {
        NutsTextNodeStyle title = NutsTextNodeStyle.title(level);
        return new DefaultNutsTextNodeTitle(start,level,child);
    }

    @Override
    public NutsTextNodeStyle[] toBasicStyles(NutsTextNodeStyle style) {
        NutsTextNodeStyle[] styled = styleTheme.toBasicStyles(style, ws);
        for (NutsTextNodeStyle nutsTextNodeStyle : styled) {
            if(!nutsTextNodeStyle.getType().basic()){
                throw new NutsIllegalArgumentException(ws,"invalid theme processing");
            }
        }
        return styled;
    }
}
