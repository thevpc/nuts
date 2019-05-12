/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.util.fprint.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.vpc.app.nuts.core.util.fprint.FPrintCommands;
import net.vpc.app.nuts.core.util.fprint.TextFormat;
import net.vpc.app.nuts.core.util.fprint.TextFormats;

/**
 *
 * @author vpc
 */
public class FDocNodeHelper {

    public static TextNode convert(FDocNode n) {
        if (n != null) {
            if (n instanceof FDocNode.Plain) {
                FDocNode.Plain p = (FDocNode.Plain) n;
                return new TextNodePlain(p.getValue());
            }
            if (n instanceof FDocNode.Escaped) {
                FDocNode.Escaped p = (FDocNode.Escaped) n;
                switch (p.getStart()) {
                    case "\"":
                    case "\"\"":
                    case "\"\"\"":
                    case "'":
                    case "''":
                    case "'''": {
                        return wrap(convert(new FDocNode.Plain(p.getValue())), p.getStart(), p.getEnd(), TextFormats.FG_GREEN);
                    }
                    case "``": {
                        // this a plain text!
                        return new TextNodePlain(p.getValue());
                    }
                    case "%%": {
                        // this a plain text!
                        return new TextNodePlain(p.getValue());
                    }
                    case "```": {
                        //this is a comment ?
                        return wrap(convert(new FDocNode.Plain(p.getValue())), p.getStart(), p.getEnd(), TextFormats.FG_GREEN);
                    }
                    case "`": {
                        //this might be a command !!
                        String v = p.getValue().trim();
                        switch(v){
                            case FPrintCommands.MOVE_LINE_START:{
                                return new TextNodeCommand(TextFormats.MOVE_LINE_START);
                            }
                            case FPrintCommands.MOVE_UP:{
                                return new TextNodeCommand(TextFormats.MOVE_UP);
                            }
                            default:{
                                return wrap(convert(new FDocNode.Plain(p.getValue())), p.getStart(), p.getEnd(), TextFormats.FG_GREEN);
                            }
                        }
                    }
                }

                return new TextNodePlain(p.getValue());
            }
            if (n instanceof FDocNode.List) {
                FDocNode.List p = (FDocNode.List) n;
                FDocNode[] children = p.getValues();
                if (children.length == 1) {
                    return convert(children[0]);
                }
                return convert(Arrays.asList(children));
            }
            if (n instanceof FDocNode.Typed) {
                FDocNode.Typed p = (FDocNode.Typed) n;
                switch (p.getStart()) {
                    case "(": {
                        return wrap(convert(p.getNode()), "(", ")", null);
                    }
                    case "[": {
                        return wrap(convert(p.getNode()), "[", "]", null);
                    }
                    case "{": {
                        return wrap(convert(p.getNode()), "{", "}", null);
                    }
                    case "<": {
                        return wrap(convert(p.getNode()), "<", ">", null);
                    }
                    case "__":
                    case "___":
                    case "____": {
                        return new TextNodeStyled(TextFormats.UNDERLINED, convert(p.getNode()));
                    }
                    case "//":
                    case "///":
                    case "////": {
                        return new TextNodeStyled(TextFormats.ITALIC, convert(p.getNode()));
                    }
                    case "~~":
                    case "~~~":
                    case "~~~~": {
                        return new TextNodeStyled(TextFormats.STRIKED, convert(p.getNode()));
                    }
                    case "%%":
                    case "%%%":
                    case "%%%%": {
                        return new TextNodeStyled(TextFormats.REVERSED, convert(p.getNode()));
                    }
                    case "==": {
                        return new TextNodeStyled(TextFormats.FG_BLUE, convert(p.getNode()));
                    }
                    case "===": {
                        return new TextNodeStyled(TextFormats.BG_BLUE, convert(p.getNode()));
                    }
                    case "====": {
                        return new TextNodeStyled(TextFormats.FG_BLUE, convert(p.getNode()));
                    }
                    case "**":
                    case "***":
                    case "****": {
                        return new TextNodeStyled(TextFormats.FG_CYAN, convert(p.getNode()));
                    }
                    case "##": {
                        return new TextNodeStyled(TextFormats.FG_GREEN, convert(p.getNode()));
                    }
                    case "###": {
                        return new TextNodeStyled(TextFormats.BG_GREEN, convert(p.getNode()));
                    }
                    case "####": {
                        return new TextNodeStyled(TextFormats.FG_GREEN, convert(p.getNode()));
                    }
                    case "@@": {
                        return new TextNodeStyled(TextFormats.FG_RED, convert(p.getNode()));
                    }
                    case "@@@": {
                        return new TextNodeStyled(TextFormats.BG_RED, convert(p.getNode()));
                    }
                    case "@@@@": {
                        return new TextNodeStyled(TextFormats.FG_RED, convert(p.getNode()));
                    }
                    case "[[": {
                        return new TextNodeStyled(TextFormats.FG_MAGENTA, convert(p.getNode()));
                    }
                    case "[[[": {
                        return new TextNodeStyled(TextFormats.BG_MAGENTA, convert(p.getNode()));
                    }
                    case "[[[[": {
                        return new TextNodeStyled(TextFormats.FG_MAGENTA, convert(p.getNode()));
                    }

                    case "{{": {
                        return new TextNodeStyled(TextFormats.FG_YELLOW, convert(p.getNode()));
                    }
                    case "{{{": {
                        return new TextNodeStyled(TextFormats.BG_YELLOW, convert(p.getNode()));
                    }
                    case "{{{{": {
                        return new TextNodeStyled(TextFormats.FG_YELLOW, convert(p.getNode()));
                    }

                    case "++":
                    case "+++":
                    case "++++": {
                        return new TextNodeStyled(TextFormats.BG_GREEN, convert(p.getNode()));
                    }

                    case "^^": {
                        return new TextNodeStyled(TextFormats.FG_BLUE, convert(p.getNode()));
                    }
                    case "^^^": {
                        return new TextNodeStyled(TextFormats.BG_BLUE, convert(p.getNode()));
                    }
                    case "^^^^": {
                        return new TextNodeStyled(TextFormats.FG_BLUE, convert(p.getNode()));
                    }
                    case "((": {
                        return new TextNodeStyled(TextFormats.FG_BLUE, convert(p.getNode()));
                    }
                    case "(((": {
                        return new TextNodeStyled(TextFormats.BG_CYAN, convert(p.getNode()));
                    }
                    case "((((": {
                        return new TextNodeStyled(TextFormats.FG_BLUE, convert(p.getNode()));
                    }
                    case "<<": {
                        return new TextNodeStyled(TextFormats.FG_GREY, convert(p.getNode()));
                    }
                    case "<<<": {
                        return new TextNodeStyled(TextFormats.BG_GREY, convert(p.getNode()));
                    }
                    case "<<<<": {
                        return new TextNodeStyled(TextFormats.FG_GREY, convert(p.getNode()));
                    }
                    case "$$": {
                        return new TextNodeStyled(TextFormats.FG_MAGENTA, convert(p.getNode()));
                    }
                    case "$$$": {
                        return new TextNodeStyled(TextFormats.BG_MAGENTA, convert(p.getNode()));
                    }
                    case "$$$$": {
                        return new TextNodeStyled(TextFormats.BG_MAGENTA, convert(p.getNode()));
                    }
                    case "££": {
                        return new TextNodeStyled(TextFormats.FG_RED, convert(p.getNode()));
                    }
                    case "£££": {
                        return new TextNodeStyled(TextFormats.BG_RED, convert(p.getNode()));
                    }
                    case "££££": {
                        return new TextNodeStyled(TextFormats.FG_RED, convert(p.getNode()));
                    }
                    case "§§": {
                        return new TextNodeStyled(TextFormats.FG_WHITE, convert(p.getNode()));
                    }
                    case "§§§": {
                        return new TextNodeStyled(TextFormats.BG_WHITE, convert(p.getNode()));
                    }
                    case "§§§§": {
                        return new TextNodeStyled(TextFormats.FG_WHITE, convert(p.getNode()));
                    }
                }
                TextNode convert = convert(p.getNode());
                return convert;
//                if (convert instanceof TextNodePlain) {
//                    return new TextNodePlain(((TextNodePlain) convert).getValue());
//                } else {
//                    return new TextNodePlain(convert.toString());
//                }
//                return new TextNodePlain(String.valueOf(n.toString()));
            }
        }
        return new TextNodePlain(String.valueOf(n == null ? null : n.toString()));
    }

    private static TextNode wrap(TextNode t, String prefix, String suffix, TextFormat format) {
        TextNodeList y = new TextNodeList(
                new TextNodePlain(prefix),
                t,
                new TextNodePlain(suffix)
        );
        if (format == null) {
            return y;
        }
        return new TextNodeStyled(format, y);
    }

    private static TextNode convert(List<FDocNode> n) {
        if (n.size() == 1) {
            return convert(n.get(0));
        }
        List<TextNode> children = new ArrayList<>(n.size());
        for (FDocNode node : n) {
            children.add(convert(node));
        }
        return new TextNodeList(children.toArray(new TextNode[0]));
    }

}
