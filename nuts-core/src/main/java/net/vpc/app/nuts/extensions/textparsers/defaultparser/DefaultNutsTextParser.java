/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 *
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * Copyright (C) 2016-2017 Taha BEN SALAH
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.extensions.textparsers.defaultparser;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import net.vpc.app.nuts.NutsTextFormat;
import net.vpc.app.nuts.NutsTextFormats;
import net.vpc.app.nuts.extensions.core.NutsTextList;
import net.vpc.app.nuts.extensions.core.NutsTextNode;
import net.vpc.app.nuts.extensions.core.NutsTextPlain;
import net.vpc.app.nuts.extensions.core.NutsTextStyled;
import net.vpc.app.nuts.extensions.textparsers.NutsTextFallbackParser;

/**
 * Created by vpc on 5/23/17.
 */
public class DefaultNutsTextParser {

    public static final DefaultNutsTextParser INSTANCE = new DefaultNutsTextParser();

    public static void main(String[] args) {
//        String str = "''\\\"a''";
//        String str = "''a''";
//        String str = "a";
        String str = "''aa''";
        NutsTextNode t = INSTANCE.parse(str);
        System.out.println(t);
    }

    private NutsTextNode convert(List<TextNode> n) {
        if (n.size() == 1) {
            return convert(n.get(0));
        }
        List<NutsTextNode> children = new ArrayList<>();
        for (TextNode node : n) {
            children.add(convert(node));
        }
        return new NutsTextList(children.toArray(new NutsTextNode[children.size()]));
    }

    private NutsTextNode convert(TextNode n) {
        if (n != null) {
            switch (n.getType()) {
                case "list": {
                    return convert(n.getNodes());
                }
                case "phrase": {
                    return new NutsTextPlain(n.getValue());
                }
                case "__":
                case "___":
                case "____": {
                    return new NutsTextStyled(NutsTextFormats.UNDERLINED, convert(n.getNodes()));
                }
                case "//":
                case "///":
                case "////": {
                    return new NutsTextStyled(NutsTextFormats.ITALIC, convert(n.getNodes()));
                }
                case "~~":
                case "~~~":
                case "~~~~": {
                    return new NutsTextStyled(NutsTextFormats.STRIKED, convert(n.getNodes()));
                }
                case "%%":
                case "%%%":
                case "%%%%": {
                    return new NutsTextStyled(NutsTextFormats.REVERSED, convert(n.getNodes()));
                }
                case "==":
                case "===":
                case "====": {
                    return new NutsTextStyled(NutsTextFormats.FG_BLUE, convert(n.getNodes()));
                }
                case "**":
                case "***":
                case "****": {
                    return new NutsTextStyled(NutsTextFormats.FG_CYAN, convert(n.getNodes()));
                }
                case "##":
                case "###":
                case "####": {
                    return new NutsTextStyled(NutsTextFormats.FG_GREEN, convert(n.getNodes()));
                }
                case "@@":
                case "@@@":
                case "@@@@": {
                    return new NutsTextStyled(NutsTextFormats.FG_RED, convert(n.getNodes()));
                }
                case "[":
                case "[[":
                case "[[[":
                case "[[[[": {
                    return new NutsTextStyled(NutsTextFormats.FG_MAGENTA, convert(n.getNodes()));
                }

                case "{{":
                case "{{{":
                case "{{{{": {
                    return new NutsTextStyled(NutsTextFormats.FG_YELLOW, convert(n.getNodes()));
                }

                case "++":
                case "+++":
                case "++++": {
                    return new NutsTextStyled(NutsTextFormats.BG_GREEN, convert(n.getNodes()));
                }

                case "^^":
                case "^^^":
                case "^^^^": {
                    return new NutsTextStyled(NutsTextFormats.BG_BLUE, convert(n.getNodes()));
                }
                case "(": {
                    return wrap(convert(n.getNodes()), "(", ")", null);
                }
                case "((":
                case "(((":
                case "((((": {
                    return new NutsTextStyled(NutsTextFormats.BG_CYAN, convert(n.getNodes()));
                }
                case "{": {
                    return wrap(convert(n.getNodes()), "{", "}", NutsTextFormats.BG_GREEN);
                }
                case "<<":
                case "<<<":
                case "<<<<": {
                    return new NutsTextStyled(NutsTextFormats.BG_YELLOW, convert(n.getNodes()));
                }
                case "$$":
                case "$$$":
                case "$$$$": {
                    return new NutsTextStyled(NutsTextFormats.BG_MAGENTA, convert(n.getNodes()));
                }
                case "££":
                case "£££":
                case "££££": {
                    return new NutsTextStyled(NutsTextFormats.BG_RED, convert(n.getNodes()));
                }
                case "§§":
                case "§§§":
                case "§§§§": {
                    return new NutsTextStyled(NutsTextFormats.BG_WHITE, convert(n.getNodes()));
                }
                case "\"":
                case "\"\"":
                case "\"\"\"": {
                    return wrap(convert(n.getNodes()), n.getType(), n.getType(), NutsTextFormats.FG_GREEN);
                }
                case "'":
                case "''":
                case "'''": {
                    return wrap(convert(n.getNodes()), n.getType(), n.getType(), NutsTextFormats.FG_YELLOW);
                }
                case "``": {
                    // this a plain text!
                    return new NutsTextPlain(n.getValue());
                }
                case "```": {
                    //this is a comment
                    return new NutsTextPlain("");
                }
                case "`": {
                    //this a command !!
                    //should be interpreted as
                    String v = n.getValue().trim();
                    List<NutsTextNode> nodes=new ArrayList<NutsTextNode>();
                    for (String cmd : v.split(";")) {
                        if ("move-line-start".endsWith(cmd)) {
                            nodes.add(new NutsTextStyled(NutsTextFormats.MOVE_LINE_START, new NutsTextPlain("")));
                        }else if ("move-up".endsWith(cmd)) {
                            nodes.add(new NutsTextStyled(NutsTextFormats.MOVE_UP, new NutsTextPlain("")));
                        }
                    }
                    return new NutsTextList(nodes.toArray(new NutsTextNode[nodes.size()]));
                }
            }
        }
        return new NutsTextPlain(String.valueOf(n.getValue()));
    }

    private NutsTextNode wrap(NutsTextNode t, String prefix, String suffix, NutsTextFormat format) {
        NutsTextList y = new NutsTextList(
                new NutsTextPlain(prefix),
                t,
                new NutsTextPlain(suffix)
        );
        if (format == null) {
            return y;
        }
        return new NutsTextStyled(format, y);
    }

    public NutsTextNode parse(String text) {
        NutsDefaultParserImpl d = new NutsDefaultParserImpl(new StringReader(text));
        try {
            TextNode tn = d.parseList();
            return convert(tn);
        } catch (Exception ex) {
            ex.printStackTrace();
            return NutsTextFallbackParser.INSTANCE.parse(text);
        }
    }
}
