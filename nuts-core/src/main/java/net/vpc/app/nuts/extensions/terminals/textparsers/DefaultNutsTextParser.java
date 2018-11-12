/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.extensions.terminals.textparsers;

import net.vpc.app.nuts.extensions.terminals.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by vpc on 5/23/17.
 */
public class DefaultNutsTextParser {

    public static final DefaultNutsTextParser INSTANCE = new DefaultNutsTextParser();
    private static final Logger log = Logger.getLogger(DefaultNutsTextParser.class.getName());

    private NutsTextNode convert(List<NutsDocNode> n) {
        if (n.size() == 1) {
            return convert(n.get(0));
        }
        List<NutsTextNode> children = new ArrayList<>();
        for (NutsDocNode node : n) {
            children.add(convert(node));
        }
        return new NutsTextNodeList(children.toArray(new NutsTextNode[children.size()]));
    }

    private NutsTextNode convert(NutsDocNode n) {
        if (n != null) {
            if (n instanceof NutsDocNode.Plain) {
                NutsDocNode.Plain p = (NutsDocNode.Plain) n;
                return new NutsTextNodePlain(p.getValue());
            }
            if (n instanceof NutsDocNode.Escaped) {
                NutsDocNode.Escaped p = (NutsDocNode.Escaped) n;
                switch (p.getStart()) {
                    case "\"":
                    case "\"\"":
                    case "\"\"\"":
                    case "'":
                    case "''":
                    case "'''": {
                        return wrap(convert(new NutsDocNode.Plain(p.getValue())), p.getStart(), p.getEnd(), NutsTextFormats.FG_GREEN);
                    }
                    case "``": {
                        // this a plain text!
                        return new NutsTextNodePlain(p.getValue());
                    }
                    case "```": {
                        //this is a comment ?
                        return wrap(convert(new NutsDocNode.Plain(p.getValue())), p.getStart(), p.getEnd(), NutsTextFormats.FG_GREEN);
                    }
                    case "`": {
                        //this a command !!
                        //should be interpreted as
                        String v = p.getValue().trim();
                        List<NutsTextNode> nodes = new ArrayList<NutsTextNode>();
                        for (String cmd : v.split(";")) {
                            if ("move-line-start".endsWith(cmd)) {
                                nodes.add(new NutsTextNodeCommand(NutsTextFormats.MOVE_LINE_START));
                            } else if ("move-up".endsWith(cmd)) {
                                nodes.add(new NutsTextNodeCommand(NutsTextFormats.MOVE_UP));
                            }
                        }
                        return new NutsTextNodeList(nodes.toArray(new NutsTextNode[nodes.size()]));
                    }
                }

                return new NutsTextNodePlain(p.getValue());
            }
            if (n instanceof NutsDocNode.List) {
                NutsDocNode.List p = (NutsDocNode.List) n;
                NutsDocNode[] children = p.getValues();
                if (children.length == 1) {
                    return convert(children[0]);
                }
                return convert(Arrays.asList(children));
            }
            if (n instanceof NutsDocNode.Typed) {
                NutsDocNode.Typed p = (NutsDocNode.Typed) n;
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
                    case "__":
                    case "___":
                    case "____": {
                        return new NutsTextNodeStyled(NutsTextFormats.UNDERLINED, convert(p.getNode()));
                    }
                    case "//":
                    case "///":
                    case "////": {
                        return new NutsTextNodeStyled(NutsTextFormats.ITALIC, convert(p.getNode()));
                    }
                    case "~~":
                    case "~~~":
                    case "~~~~": {
                        return new NutsTextNodeStyled(NutsTextFormats.STRIKED, convert(p.getNode()));
                    }
                    case "%%":
                    case "%%%":
                    case "%%%%": {
                        return new NutsTextNodeStyled(NutsTextFormats.REVERSED, convert(p.getNode()));
                    }
                    case "==":
                    case "===":
                    case "====": {
                        return new NutsTextNodeStyled(NutsTextFormats.FG_BLUE, convert(p.getNode()));
                    }
                    case "**":
                    case "***":
                    case "****": {
                        return new NutsTextNodeStyled(NutsTextFormats.FG_CYAN, convert(p.getNode()));
                    }
                    case "##":
                    case "###":
                    case "####": {
                        return new NutsTextNodeStyled(NutsTextFormats.FG_GREEN, convert(p.getNode()));
                    }
                    case "@@":
                    case "@@@":
                    case "@@@@": {
                        return new NutsTextNodeStyled(NutsTextFormats.FG_RED, convert(p.getNode()));
                    }
                    case "[[":
                    case "[[[":
                    case "[[[[": {
                        return new NutsTextNodeStyled(NutsTextFormats.FG_MAGENTA, convert(p.getNode()));
                    }

                    case "{{":
                    case "{{{":
                    case "{{{{": {
                        return new NutsTextNodeStyled(NutsTextFormats.FG_YELLOW, convert(p.getNode()));
                    }

                    case "++":
                    case "+++":
                    case "++++": {
                        return new NutsTextNodeStyled(NutsTextFormats.BG_GREEN, convert(p.getNode()));
                    }

                    case "^^":
                    case "^^^":
                    case "^^^^": {
                        return new NutsTextNodeStyled(NutsTextFormats.BG_BLUE, convert(p.getNode()));
                    }
                    case "((":
                    case "(((":
                    case "((((": {
                        return new NutsTextNodeStyled(NutsTextFormats.BG_CYAN, convert(p.getNode()));
                    }
                    case "<<":
                    case "<<<":
                    case "<<<<": {
                        return new NutsTextNodeStyled(NutsTextFormats.BG_YELLOW, convert(p.getNode()));
                    }
                    case "$$":
                    case "$$$":
                    case "$$$$": {
                        return new NutsTextNodeStyled(NutsTextFormats.BG_MAGENTA, convert(p.getNode()));
                    }
                    case "££":
                    case "£££":
                    case "££££": {
                        return new NutsTextNodeStyled(NutsTextFormats.BG_RED, convert(p.getNode()));
                    }
                    case "§§":
                    case "§§§":
                    case "§§§§": {
                        return new NutsTextNodeStyled(NutsTextFormats.BG_WHITE, convert(p.getNode()));
                    }
                }
                return new NutsTextNodeStyled(NutsTextFormats.BG_WHITE, convert(p.getNode()));
            }
        }
        return new NutsTextNodePlain(String.valueOf(n.toString()));
    }

    private NutsTextNode wrap(NutsTextNode t, String prefix, String suffix, NutsTextFormat format) {
        NutsTextNodeList y = new NutsTextNodeList(
                new NutsTextNodePlain(prefix),
                t,
                new NutsTextNodePlain(suffix)
        );
        if (format == null) {
            return y;
        }
        return new NutsTextNodeStyled(format, y);
    }

    NutsDocNode parseTextNode(String text) {
        return DefaultNutsTextNodeParser.INSTANCE.parse(text);
    }

    private void escape(NutsDocNode tn, StringBuilder sb) {
        if (tn instanceof NutsDocNode.Plain) {
            sb.append(((NutsDocNode.Plain) tn).getValue());
        } else if (tn instanceof NutsDocNode.List) {
            for (NutsDocNode nutsDocNode : ((NutsDocNode.List) tn).getValues()) {
                escape(nutsDocNode,sb);
            }
        } else if (tn instanceof NutsDocNode.Typed) {
            escape(((NutsDocNode.Typed)tn).getNode(),sb);
        } else if (tn instanceof NutsDocNode.Escaped) {
            sb.append(((NutsDocNode.Escaped)tn).getValue());
        }else{
            throw new IllegalArgumentException("Unsupported");
        }
    }

    public String filterText(String text) {
        if(text==null){
            text="";
        }
        StringBuilder sb = new StringBuilder();
        try {
            NutsDocNode tn = DefaultNutsTextNodeParser.INSTANCE.parse(text);
            escape(tn,sb);
            return sb.toString();
        } catch (Exception ex) {
            log.log(Level.FINEST, "Error parsing : \n" + text, ex);
            return text;
        }
    }

    public NutsTextNode parse(String text) {
        try {

            NutsDocNode tn = DefaultNutsTextNodeParser.INSTANCE.parse(text);
            return convert(tn);
        } catch (Exception ex) {
            log.log(Level.FINEST, "Error parsing : \n" + text, ex);
            return new NutsTextNodePlain(text);
        }
    }
}
