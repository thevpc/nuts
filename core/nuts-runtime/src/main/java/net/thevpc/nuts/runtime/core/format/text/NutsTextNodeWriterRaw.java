package net.thevpc.nuts.runtime.core.format.text;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.format.text.parser.*;
import net.thevpc.nuts.runtime.core.util.CoreStringUtils;

import java.util.ArrayList;
import java.util.List;

public class NutsTextNodeWriterRaw {

    private final NutsSession session;
    private final List<NutsText> items = new ArrayList<>();
    private NutsTextWriteConfiguration config;

    public NutsTextNodeWriterRaw(NutsSession session) {
        this.session = session;
    }

    public NutsTextWriteConfiguration getWriteConfiguration() {
        return config;
    }

    public NutsTextNodeWriterRaw setWriteConfiguration(NutsTextWriteConfiguration config) {
        this.config = config;
        return this;
    }

    public List<NutsText> getItems() {
        return items;
    }

    public void flattenNode(NutsText node) {
        flattenNode(node, getWriteConfiguration());
    }

    public void flattenNode(NutsText node, NutsTextWriteConfiguration ctx) {
        flattenNode(node, ctx, null);
    }

    private void flattenNode(NutsText node, NutsTextWriteConfiguration ctx, NutsTextStyles style) {
        if (node == null) {
            return;
        }
        if (ctx == null) {
            ctx = new NutsTextWriteConfiguration();
        }
        switch (node.getType()) {
            case PLAIN:
                NutsTextPlain p = (NutsTextPlain) node;
                String text = p.getText();
                for (String line : CoreStringUtils.splitOnNewlines(text)) {
                    boolean newline = line.startsWith("\n")
                            || line.startsWith("\r");
                    if (newline || ctx.isFiltered() || style == null || style.isPlain()) {
                        items.add(NutsTexts.of(session).ofPlain(line));
                    } else {
                        items.add(NutsTexts.of(session).ofStyled(line, style));
                    }
                }
                break;
            case LIST: {
                NutsTextList s = (NutsTextList) node;
                for (NutsText n : s) {
                    flattenNode(n, ctx, style);
                }
                break;
            }
            case STYLED: {
                DefaultNutsTextStyled s = (DefaultNutsTextStyled) node;
                if (ctx.isFiltered()) {
                    flattenNode(s.getChild(), ctx, style);
                } else {
                    NutsTextStyles ss = s.getStyles();
                    if (style == null) {
                        style = ss;
                    }else{style=style.append(ss);}
                    flattenNode(s.getChild(), ctx, style);
                }
                break;
            }
            case TITLE: {
                DefaultNutsTextTitle s = (DefaultNutsTextTitle) node;
                if (ctx.isTitleNumberEnabled()) {
                    NutsTextNumbering seq = ctx.getTitleNumberSequence();
                    if (seq == null) {
                        seq = NutsTexts.of(session).ofNumbering();
                        ctx.setTitleNumberSequence(seq);
                    }
                    NutsTextNumbering a = seq.newLevel(s.getTextStyleCode().length());
                    String ts = a.toString() + " ";
                    flattenNode(NutsTexts.of(session).ofPlain(ts), ctx, NutsTextStyles.of(NutsTextStyle.title(s.getLevel())));
                }
                flattenNode(s.getChild(), ctx, NutsTextStyles.of(NutsTextStyle.title(s.getLevel())));
                break;
            }
            case COMMAND: {
                //not supported
                break;
            }
            case ANCHOR: {
                DefaultNutsTextAnchor s = (DefaultNutsTextAnchor) node;
                //not supported
                break;
            }
            case LINK: {
                DefaultNutsTextLink s = (DefaultNutsTextLink) node;
                if (!ctx.isFiltered()) {
                    flattenNode(s.getChild(), ctx, NutsTextStyles.of(NutsTextStyle.primary1()));//
                } else {
                    flattenNode(s.getChild(), ctx, style);//
                }
                break;
            }
            case CODE: {
                DefaultNutsTextCode s = (DefaultNutsTextCode) node;
                if (!ctx.isFiltered()) {
                    flattenNode(s.highlight(session), ctx, null);
                } else {
                    flattenNode(NutsTexts.of(session).ofPlain(s.getText()), ctx, style);
                }
                break;
            }
            default:
                throw new UnsupportedOperationException("invalid node type : " + node.getClass().getSimpleName());
        }
    }
}
