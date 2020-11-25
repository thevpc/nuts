/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.util.fprint.parser;

import net.thevpc.nuts.runtime.util.fprint.FPrintCommands;
import net.thevpc.nuts.runtime.util.fprint.TextFormat;
import net.thevpc.nuts.runtime.util.fprint.TextFormats;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author vpc
 */
public class FDocNodeHelper {

    public static TextNode convert(FDocNode n) {
        if (n != null) {
            if (n instanceof FDocNode.Plain) {
                FDocNode.Plain p = (FDocNode.Plain) n;
                return new TextNodeEscaped(p.getValue());
            } else if (n instanceof FDocNode.Escaped) {
                FDocNode.Escaped p = (FDocNode.Escaped) n;
                switch (p.getStart()) {
                    case "\"":
                    case "\"\"":
                    case "\"\"\"":
                    case "'":
                    case "''":
                    case "'''": {
                        return wrap(new TextNodeEscaped(p.getValue()), p.getStart(), p.getEnd(), TextFormats.FG_GREEN);
                    }
                    case "``":
                    case "%%": {
                        // this stays un-styled
                        return new TextNodeUnStyled(p.getStart(), p.getValue(), new TextNodeEscaped(p.getValue()));
                    }
                    case "```": {
                        //this is a comment ?
                        return wrap(new TextNodeEscaped(p.getValue()), p.getStart(), p.getEnd(), TextFormats.FG_GREEN);
                    }
                    case "`": {
                        //this might be a command !!
                        String v = p.getValue().trim();
                        switch (v) {
                            case FPrintCommands.MOVE_LINE_START: {
                                return new TextNodeCommand(p.getStart(), p.getEnd(), p.getValue(), TextFormats.MOVE_LINE_START);
                            }
                            case FPrintCommands.LATER_RESET_LINE: {
                                return new TextNodeCommand(p.getStart(), p.getEnd(), p.getValue(), TextFormats.LATER_RESET_LINE);
                            }
                            case FPrintCommands.MOVE_UP: {
                                return new TextNodeCommand(p.getStart(), p.getEnd(), p.getValue(), TextFormats.MOVE_UP);
                            }
                            default: {
                                return wrap(new TextNodeEscaped(p.getValue()), p.getStart(), p.getEnd(), TextFormats.FG_GREEN);
                            }
                        }
                    }
                }

                return new TextNodeEscaped(p.getValue());
            } else if (n instanceof FDocNode.List) {
                FDocNode.List p = (FDocNode.List) n;
                FDocNode[] children = p.getValues();
                if (children.length == 1) {
                    return convert(children[0]);
                }
                return convert(Arrays.asList(children));
            } else if (n instanceof FDocNode.Typed) {
                FDocNode.Typed p = (FDocNode.Typed) n;
                switch (p.getStart()) {
                    case "(":
                    case "{":
                    case "<": {
                        return new TextNodeUnStyled(p.getStart(), p.getEnd(), convert(p.getNode()));
                    }
                    case "[": {
                        if (p.getNode() instanceof FDocNode.Plain) {
                            String s = ((FDocNode.Plain) p.getNode()).getValue();
                            if (s.startsWith("#") && s.length() > 1 && s.indexOf('#', 1) < 0) {
                                return new TextNodeAnchor(
                                        p.getStart() + "#",
                                        p.getEnd(),
                                        s.substring(1));
                            }
                        }
                        return new TextNodeUnStyled(p.getStart(), p.getEnd(), convert(p.getNode()));
                    }
                    case "__":
                    case "___":
                    case "____":
                    case "//":
                    case "///":
                    case "////":
                    case "~~":
                    case "~~~":
                    case "~~~~":
                    case "%%":
                    case "%%%":
                    case "%%%%":
                    case "==":
                    case "===":
                    case "====":
                    case "**":
                    case "***":
                    case "****":
                    case "##":
                    case "###":
                    case "####":
                    case "@@":
                    case "@@@":
                    case "@@@@":
                    case "[[":
                    case "[[[":
                    case "[[[[":
                    case "{{":
                    case "{{{":
                    case "{{{{":
                    case "++":
                    case "+++":
                    case "++++":
                    case "^^":
                    case "^^^":
                    case "^^^^":
                    case "((":
                    case "(((":
                    case "((((":
                    case "<<":
                    case "<<<":
                    case "<<<<":
                    case "$$":
                    case "$$$":
                    case "$$$$":
                    case "££":
                    case "£££":
                    case "££££":
                    case "§§":
                    case "§§§":
                    case "§§§§": {
                        return new TextNodeStyled(p.getStart(), p.getEnd(), createStyle(p.getStart()), convert(p.getNode()));
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
            } else if (n instanceof FDocNode.Title) {
                FDocNode.Title p = (FDocNode.Title) n;
                String sc = p.getStyleCode();
                return new TextNodeTitle(p.getStart(), createStyle(sc), convert(p.getNode()));
            }

        }
        throw new UnsupportedOperationException("Unsupported type " + n.getClass().getSimpleName());
    }

    private static TextNode wrap(TextNode t, String prefix, String suffix, TextFormat format) {
        if (t instanceof TextNodePlain) {
            TextNodePlain y = new TextNodePlain(
                    prefix +
                            ((TextNodePlain) t).getValue()
                            + suffix
            );
            if (format == null) {
                return y;
            }
            return new TextNodeStyled(prefix, suffix, format, y);
        }
        TextNodeList y = new TextNodeList(
                new TextNodePlain(prefix),
                t,
                new TextNodePlain(suffix)
        );
        if (format == null) {
            return y;
        }
        return new TextNodeStyled(prefix, suffix, format, y);
    }

    private static TextFormat createStyle(String code) {
        switch (code) {
            case "__":
            case "___":
            case "____": {
                return TextFormats.UNDERLINED;
            }
            case "//":
            case "///":
            case "////": {
                return TextFormats.ITALIC;
            }
            case "~~":
            case "~~~":
            case "~~~~": {
                return TextFormats.STRIKED;
            }
            case "%%":
            case "%%%":
            case "%%%%": {
                return TextFormats.REVERSED;
            }
            case "==": {
                return TextFormats.FG_BLUE;
            }
            case "===": {
                return TextFormats.BG_BLUE;
            }
            case "====": {
                return TextFormats.FG_BLUE;
            }
            case "**":
            case "***":
            case "****": {
                return TextFormats.FG_CYAN;
            }
            case "##": {
                return TextFormats.FG_GREEN;
            }
            case "###": {
                return TextFormats.BG_GREEN;
            }
            case "####": {
                return TextFormats.FG_GREEN;
            }
            case "@@": {
                return TextFormats.FG_RED;
            }
            case "@@@": {
                return TextFormats.BG_RED;
            }
            case "@@@@": {
                return TextFormats.FG_RED;
            }
            case "[[": {
                return TextFormats.FG_MAGENTA;
            }
            case "[[[": {
                return TextFormats.BG_MAGENTA;
            }
            case "[[[[": {
                return TextFormats.FG_MAGENTA;
            }

            case "{{": {
                return TextFormats.FG_YELLOW;
            }
            case "{{{": {
                return TextFormats.BG_YELLOW;
            }
            case "{{{{": {
                return TextFormats.FG_YELLOW;
            }

            case "++":
            case "+++":
            case "++++": {
                return TextFormats.BG_GREEN;
            }

            case "^^": {
                return TextFormats.FG_BLUE;
            }
            case "^^^": {
                return TextFormats.BG_BLUE;
            }
            case "^^^^": {
                return TextFormats.FG_BLUE;
            }
            case "((": {
                return TextFormats.FG_BLUE;
            }
            case "(((": {
                return TextFormats.BG_CYAN;
            }
            case "((((": {
                return TextFormats.FG_BLUE;
            }
            case "<<": {
                return TextFormats.FG_GREY;
            }
            case "<<<": {
                return TextFormats.BG_GREY;
            }
            case "<<<<": {
                return TextFormats.FG_GREY;
            }
            case "$$": {
                return TextFormats.FG_MAGENTA;
            }
            case "$$$": {
                return TextFormats.BG_MAGENTA;
            }
            case "$$$$": {
                return TextFormats.BG_MAGENTA;
            }
            case "££": {
                return TextFormats.FG_RED;
            }
            case "£££": {
                return TextFormats.BG_RED;
            }
            case "££££": {
                return TextFormats.FG_RED;
            }
            case "§§": {
                return TextFormats.FG_WHITE;
            }
            case "§§§": {
                return TextFormats.BG_WHITE;
            }
            case "§§§§": {
                return TextFormats.FG_WHITE;
            }
        }
        throw new UnsupportedOperationException("Unsupported format " + code);
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
