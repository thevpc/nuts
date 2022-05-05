package net.thevpc.nuts.runtime.standalone.text;

import net.thevpc.nuts.NutsIllegalArgumentException;
import net.thevpc.nuts.NutsMessage;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.cmdline.NutsArgument;
import net.thevpc.nuts.cmdline.NutsCommandLine;
import net.thevpc.nuts.io.NutsPath;
import net.thevpc.nuts.runtime.standalone.util.CoreStringUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NutsWorkspaceVarExpansionFunction;
import net.thevpc.nuts.runtime.standalone.xtra.expr.StringPlaceHolderParser;
import net.thevpc.nuts.text.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DefaultNutsTextTransformer implements NutsTextTransformer {
    private final NutsTexts txt;
    private final NutsTextTransformConfig config;
    private final NutsSession session;
    private final NutsWorkspaceVarExpansionFunction d;

    public DefaultNutsTextTransformer(NutsTextTransformConfig config, NutsSession session) {
        this.session = session;
        this.config = config;
        txt = NutsTexts.of(session);
        d = NutsWorkspaceVarExpansionFunction.of(session);
    }

    @Override
    public NutsText preTransform(NutsText text, NutsTextTransformerContext context) {
        return text;
    }

    private String transformText(String t) {
        if (config.isProcessVars()) {
            Function<String, String> r = config.getVarProvider();
            return StringPlaceHolderParser.replaceDollarPlaceHolders(t, s -> {
                String u = null;
                if (r != null) {
                    u = r.apply(s);
                    if (u != null) {
                        return u;
                    }
                }
                return d.apply(s);
            });
        }
        return t;
    }

    @Override
    public NutsText postTransform(NutsText text, NutsTextTransformerContext context) {
        switch (text.getType()) {
            case PLAIN: {
                NutsTextPlain t = (NutsTextPlain) text;
                String str = transformText(t.getText());
                if (config.isFlatten()) {
                    text = flatten(str);
                } else {
                    text = txt.ofPlain(str);
                }
                return text;
            }
            case STYLED: {
                NutsTextStyled t = (NutsTextStyled) text;
                NutsText child = t.getChild();
                if (config.isFlatten()) {
                    text = map(child, x -> {
                        if (isNewline(x)) {
                            return x;
                        }
                        return txt.ofStyled(x, t.getStyles());
                    });
                }
                if (config.isFiltered()) {
                    text = map(child, x -> {
                        if (isNewline(x)) {
                            return x;
                        }
                        return ((NutsTextStyled) x).getChild();
                    });
                }
                return text;
            }
            case LINK: {
                NutsTextLink t = (NutsTextLink) text;
                String str = transformText(t.getText());
                if (config.isFlatten()) {
                    text = map(flatten(str), x -> {
                        NutsTextPlain p = (NutsTextPlain) x;
                        if (isNewline(p)) {
                            return p;
                        }
                        return txt.ofLink(p.getText());
                    });
                }
                if (config.isNormalize()) {
                    text = map(text, x -> {
                        if (x.getType() == NutsTextType.PLAIN) {
                            return x;
                        }
                        String lnk = ((NutsTextLink) x).getText();
                        return txt.ofStyled(lnk, NutsTextStyle.underlined());
                    });
                }
                if (config.isFiltered()) {
                    text = map(text, x -> {
                        if (x.getType() == NutsTextType.PLAIN) {
                            return x;
                        }
                        String lnk = ((NutsTextLink) x).getText();
                        return txt.ofPlain(lnk);
                    });
                }
                return text;
            }
            case TITLE: {
                NutsTextTitle t = (NutsTextTitle) text;
                String prefix = null;
                int level = t.getLevel();
                if (config.isFlatten() || config.isNormalize() || config.isProcessTitleNumbers()) {
                    if (config.isProcessTitleNumbers()) {
                        NutsTitleSequence n = context.getTitleSequence();
                        if (n == null) {
                            n = config.getTitleNumberSequence();
                            if (n == null) {
                                n = NutsTexts.of(session).ofNumbering();
                            }
                            context.setTitleSequence(n);
                        }
                        n = n.next(level);
                        context.setTitleSequence(n);
                        prefix = n.toString() + " ";
                    } else {
                        prefix = CoreStringUtils.fillString('#', level) + ") ";
                    }
                    List<NutsText> li = new ArrayList<>();
                    li.add(txt.ofPlain(prefix + " "));
                    if (config.isFlatten()) {
                        li.addAll(asList(t.getChild()));
                    } else {
                        li.add(t.getChild());
                    }
                    li.add(txt.ofPlain("\n"));
                    text = txt.ofStyled(txt.ofList(li), NutsTextStyle.primary(level));
                }
                if (config.isFiltered()) {
                    text = map(text, x -> {
                        if (x.getType() == NutsTextType.STYLED) {
                            return ((NutsTextStyled) x).getChild();
                        }
                        return x;
                    });
                }
                return text;
            }
            case CODE: {
                NutsTextCode t = (NutsTextCode) text;
                if (config.isProcessIncludes()) {
                    if ("include".equals(t.getQualifier())) {
                        NutsCommandLine cmd = NutsCommandLine.parseDefault(
                                t.getText()
                        ).orNull();
                        if (cmd != null && cmd.length() > 0) {
                            String p = cmd.next().flatMap(NutsArgument::asString).orNull();
                            NutsPath newP = resolveRelativePath(p, config.getCurrentDir());
                            NutsText n = txt.parser().parse(newP);
                            return txt.transform(n, new NutsTextTransformConfig()
                                    .setProcessIncludes(true)
                                    .setCurrentDir(newP.getParent())
                                    .setImportClassLoader(config.getImportClassLoader())
                            );
                        }
                    }
                }
                if (config.isNormalize()) {
                    text = t.highlight(session);
                }
                if (config.isFlatten()) {
                    // We have no insurance that highlight is not using special nodes so
                    // we enforce flattening
                    text = txt.transform(text, new NutsTextTransformConfig()
                            .setFlatten(true)
                            .setNormalize(config.isNormalize())
                            .setProcessVars(config.isProcessVars())
                            .setVarProvider(config.getVarProvider())
                    );
                }
                return text;
            }
        }
        return text;
    }

    private NutsText map(NutsText li, Function<NutsText, NutsText> f) {
        return compress(asList(li).stream().map(f).collect(Collectors.toList()));
    }

    private NutsText compress(List<NutsText> li) {
        return li.isEmpty() ? null : li.size() == 1 ? li.get(0) : txt.ofList(li);
    }

    private boolean isNewline(String c) {
        return c != null && (c.startsWith("\n")
                || c.startsWith("\r"));
    }

    private boolean isNewline(NutsText c) {
        return c instanceof NutsTextPlain && isNewline(((NutsTextPlain) c).getText());
    }

    private NutsPath resolveRelativePath(String path, NutsPath curr) {
        if (path.startsWith("classpath:")) {
            // NutsPath.of(path, Thread.currentThread().getContextClassLoader(), session).exists()
            NutsPath p = NutsPath.of(path, Thread.currentThread().getContextClassLoader(), session);
            if (p.exists()) {
                return p;
            }
            p = NutsPath.of(path, getClass().getClassLoader(), session);
            if (p.exists()) {
                return p;
            }
            throw new NutsIllegalArgumentException(session, NutsMessage.ofCstyle("unable to resolve path %s", path));
        }
        return NutsPath.of(path, getClass().getClassLoader(), session);
    }

    private NutsText applyFlatStyle(NutsText tt, NutsTextStyles styles) {
        List<NutsText> li = new ArrayList<>();
        for (NutsText c : asList(tt)) {
            li.add(txt.ofStyled(c, styles));
        }
        return compress(li);
    }

    private List<NutsText> asList(NutsText text) {
        if (text == null) {
            return Collections.emptyList();
        }
        if (text instanceof NutsTextList) {
            return ((NutsTextList) text).getChildren();
        }
        return Arrays.asList(text);
    }

    private NutsText flatten(NutsTextPlain tt) {
        return flatten(tt.getText());
    }

    private NutsText flatten(String tt) {
        List<NutsText> li = new ArrayList<>();
        for (String line : CoreStringUtils.splitOnNewlines(tt)) {
            li.add(txt.ofPlain(line));
        }
        return compress(li);
    }
}
