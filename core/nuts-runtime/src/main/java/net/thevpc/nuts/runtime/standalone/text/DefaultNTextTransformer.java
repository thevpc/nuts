package net.thevpc.nuts.runtime.standalone.text;

import net.thevpc.nuts.NIllegalArgumentException;
import net.thevpc.nuts.NWorkspace;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.util.CoreStringUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceVarExpansionFunction;
import net.thevpc.nuts.runtime.standalone.xtra.expr.StringPlaceHolderParser;
import net.thevpc.nuts.text.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DefaultNTextTransformer implements NTextTransformer {
    private final NTexts txt;
    private final NTextTransformConfig config;
    private final NWorkspace workspace;
    private final NWorkspaceVarExpansionFunction d;

    public DefaultNTextTransformer(NTextTransformConfig config, NWorkspace workspace) {
        this.workspace = workspace;
        this.config = config;
        txt = NTexts.of();
        d = NWorkspaceVarExpansionFunction.of();
    }

    @Override
    public NText preTransform(NText text, NTextTransformerContext context) {
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
    public NText postTransform(NText text, NTextTransformerContext context) {
        switch (text.getType()) {
            case LIST: {
                if (config.isFlatten()) {
                    NTextList t = (NTextList) text;
                    List<NText> all = t.getChildren();
                    List<NText> all2 = new ArrayList<>();
                    for (NText a : all) {
                        if (a instanceof NTextList) {
                            all2.addAll(((NTextList) a).getChildren());
                        } else if (a instanceof NTextBuilder) {
                            all2.addAll(((NTextBuilder) a).getChildren());
                        } else {
                            all2.add(a);
                        }
                    }
//                    if (all.size() == all2.size()) {
//                        return text;
//                    }
                    text= txt.ofList(all2);
                }
                return mapApplyThemeAndFilter(text);
            }
            case BUILDER: {
                if (config.isFlatten()) {
                    NTextBuilder t = (NTextBuilder) text;
                    List<NText> all = t.getChildren();
                    List<NText> all2 = new ArrayList<>();
                    for (NText a : all) {
                        if (a instanceof NTextList) {
                            all2.addAll(((NTextList) a).getChildren());
                        } else if (a instanceof NTextBuilder) {
                            all2.addAll(((NTextBuilder) a).getChildren());
                        } else {
                            all2.add(a);
                        }
                    }
                    if (all.size() == all2.size()) {
                        return text;
                    }
                    return txt.ofList(all2);
                } else {
                    return text;
                }
            }
            case PLAIN: {
                NTextPlain t = (NTextPlain) text;
                String str = transformText(t.getText());
                if (config.isFlatten()) {
                    text = flatten(str);
                } else {
                    text = txt.ofPlain(str);
                }
                return text;
            }
            case STYLED: {
                NTextStyled t = (NTextStyled) text;
                NText child = t.getChild();
                List<NText> cc = new ArrayList<>();
                if (config.isFlatten()) {
                    if (child instanceof NTextList) {
                        for (NText x : ((NTextList) child).getChildren()) {
                            if (isNewline(x)) {
                                cc.add(x);
                            } else {
                                cc.add(txt.ofStyled(x, t.getStyles()));
                            }
                        }
                    }else {
                        cc.add(mapTxt(child, x -> {
                            if (isNewline(x)) {
                                return x;
                            }
                            return txt.ofStyled(x, t.getStyles());
                        }));
                    }
                }else{
                    cc.add(child);
                }
                if (config.isFiltered()) {
                    return compressTxt(cc.stream().map(x->mapTxt(x, y -> {
                        if (y.getType() == NTextType.STYLED) {
                            return ((NTextStyled) y).getChild();
                        }
                        return y;
                    })).collect(Collectors.toList()));
                }else{
                    if (config.isApplyTheme()) {
                        NTextFormatTheme theme = NTexts.of().getTheme(config.getThemeName()).orElse(NTexts.of().getTheme());
                        NTextStyles basicStyles = theme.toBasicStyles(t.getStyles(),config.isBasicTrueStyles());
                        return compressTxt(cc.stream().map(x->mapTxt(x, y -> {
                            if(y.getType() == NTextType.STYLED){
                                return txt.ofStyled(((NTextStyled) y).getChild(), basicStyles);
                            }
                            if(y.getType() == NTextType.PLAIN){
                                //newline
                                return y;
                            }
                            throw new IllegalArgumentException("unexpected...");
                        })).collect(Collectors.toList()));
                    }
                    return compressTxt(cc);
                }
            }
            case LINK: {
                NTextLink t = (NTextLink) text;
                String str = transformText(t.getText());
                if (config.isFlatten()) {
                    text = mapTxt(flatten(str), x -> {
                        NTextPlain p = (NTextPlain) x;
                        if (isNewline(p)) {
                            return p;
                        }
                        return txt.ofLink(p.getText());
                    });
                }
                if (config.isNormalize()) {
                    text = mapTxt(text, x -> {
                        if (x.getType() == NTextType.PLAIN) {
                            return x;
                        }
                        String lnk = ((NTextLink) x).getText();
                        return txt.ofStyled(lnk, NTextStyle.underlined());
                    });
                }
                return mapApplyThemeAndFilter(text);
            }
            case TITLE: {
                NTextTitle t = (NTextTitle) text;
                String prefix = null;
                int level = t.getLevel();
                if (config.isFlatten() || config.isNormalize() || config.isProcessTitleNumbers()) {
                    if (config.isProcessTitleNumbers()) {
                        NTitleSequence n = context.getTitleSequence();
                        if (n == null) {
                            n = config.getTitleNumberSequence();
                            if (n == null) {
                                n = NText.ofNumbering();
                            }
                            context.setTitleSequence(n);
                        }
                        n = n.next(level);
                        context.setTitleSequence(n);
                        prefix = n.toString() + " ";
                    } else {
                        prefix = CoreStringUtils.fillString('#', level) + ") ";
                    }
                    List<NText> li = new ArrayList<>();
                    li.add(txt.ofPlain(prefix + " "));
                    if (config.isFlatten()) {
                        li.addAll(asList(t.getChild()));
                    } else {
                        li.add(t.getChild());
                    }
                    li.add(txt.ofPlain("\n"));
                    text = txt.ofStyled(txt.ofList(li), NTextStyle.primary(level));
                }
                return mapApplyThemeAndFilter(text);
            }
            case INCLUDE: {
                NTextInclude t = (NTextInclude) text;
                if (config.isProcessIncludes()) {
                    NCmdLine cmd = NCmdLine.parseDefault(
                            t.getText()
                    ).orNull();
                    if (cmd != null && cmd.length() > 0) {
                        String p = cmd.next().flatMap(NArg::asString).orNull();
                        NPath newP = resolveRelativePath(p, config.getCurrentDir());
                        NText n = txt.parser().parse(newP);
                        //do not continue
                        return txt.transform(n, config.copy()
                                .setProcessIncludes(true)
                                .setCurrentDir(newP.getParent())
                                .setImportClassLoader(config.getImportClassLoader())
                        );
                    }
                }
                return t;
            }
            case CODE: {
                NTextCode t = (NTextCode) text;
                if (config.isNormalize() || config.isFlatten()) {
                    text = t.highlight();
                    // We have no insurance that highlight is not using special nodes so
                    // we enforce flattening
                    text = txt.transform(text, context.getConfig().copy()
                            .setFlatten(true)
                            .setNormalize(config.isNormalize())
                            .setProcessVars(config.isProcessVars())
                            .setVarProvider(config.getVarProvider())
                    );
                }
                if (config.isApplyTheme()) {
                    NTextFormatTheme theme = NTexts.of().getTheme(config.getThemeName()).orElse(NTexts.of().getTheme());
                    text = mapTxt(text, x -> {
                        if (x.getType() == NTextType.STYLED) {
                            NTextStyled y = (NTextStyled) x;
                            NTextStyles basicStyles = theme.toBasicStyles(y.getStyles(),config.isBasicTrueStyles());
                            if(!y.getStyles().equals(basicStyles)){
                                return txt.ofStyled(y.getChild(), basicStyles);
                            }
                            return x;
                        }
                        return x;
                    });
                }
                return text;
            }
        }
        return text;
    }

    private NText mapApplyThemeAndFilter(NText text){
        if (config.isFiltered()) {
            text = mapTxt(text, x -> {
                if (x.getType() == NTextType.STYLED) {
                    return ((NTextStyled) x).getChild();
                }
                return x;
            });
        }else{
            if (config.isApplyTheme()) {
                NTextFormatTheme theme = NTexts.of().getTheme(config.getThemeName()).orElse(NTexts.of().getTheme());
                text = mapTxt(text, x -> {
                    if (x.getType() == NTextType.STYLED) {
                        NTextStyled y = (NTextStyled) x;
                        NTextStyles basicStyles = theme.toBasicStyles(y.getStyles(),config.isBasicTrueStyles());
                        if(!y.getStyles().equals(basicStyles)){
                            return txt.ofStyled(y.getChild(), basicStyles);
                        }
                        return x;
                    }
                    return x;
                });
            }
        }
        return text;
    }

    private NText mapTxt(NText li, Function<NText, NText> f) {
        return compressTxt(asList(li).stream().map(f).collect(Collectors.toList()));
    }

    private NText compressTxt(List<NText> li) {
        return li.isEmpty() ? null : li.size() == 1 ? li.get(0) : txt.ofList(li);
    }

    private boolean isNewline(String c) {
        return c != null && (c.startsWith("\n")
                || c.startsWith("\r"));
    }

    private boolean isNewline(NText c) {
        return c instanceof NTextPlain && isNewline(((NTextPlain) c).getText());
    }

    private NPath resolveRelativePath(String path, NPath curr) {
        if (path.startsWith("classpath:")) {
            // NPath.of(path, Thread.currentThread().getContextClassLoader(), session).exists()
            NPath p = NPath.of(path, Thread.currentThread().getContextClassLoader());
            if (p.exists()) {
                return p;
            }
            p = NPath.of(path, getClass().getClassLoader());
            if (p.exists()) {
                return p;
            }
            throw new NIllegalArgumentException(NMsg.ofC("unable to resolve path %s", path));
        }
        return NPath.of(path, getClass().getClassLoader());
    }

    private NText applyFlatStyle(NText tt, NTextStyles styles) {
        List<NText> li = new ArrayList<>();
        for (NText c : asList(tt)) {
            li.add(txt.ofStyled(c, styles));
        }
        return compressTxt(li);
    }

    private List<NText> asList(NText text) {
        if (text == null) {
            return Collections.emptyList();
        }
        if (text instanceof NTextList) {
            return ((NTextList) text).getChildren();
        }
        return Arrays.asList(text);
    }

    private NText flatten(NTextPlain tt) {
        return flatten(tt.getText());
    }

    private NText flatten(String tt) {
        List<NText> li = new ArrayList<>();
        for (String line : CoreStringUtils.splitOnNewlines(tt)) {
            li.add(txt.ofPlain(line));
        }
        return compressTxt(li);
    }
}
