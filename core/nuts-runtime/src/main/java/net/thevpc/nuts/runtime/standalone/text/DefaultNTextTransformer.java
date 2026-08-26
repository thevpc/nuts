package net.thevpc.nuts.runtime.standalone.text;

import net.thevpc.nuts.internal.rpi.NTextRPI;
import net.thevpc.nuts.util.NIllegalArgumentException;
import net.thevpc.nuts.text.NMsg;
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
    private final NTextTransformConfig config;
    private final NWorkspaceVarExpansionFunction d;

    public DefaultNTextTransformer(NTextTransformConfig config) {
        this.config = config;
        d = NWorkspaceVarExpansionFunction.of();
    }

    @Override
    public NText preTransform(NText text, NTextTransformerContext context) {
        return text;
    }

    private String transformText(String t) {
        if (config.isProcessVars()) {
            Function<String, String> r = config.varProvider();
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
        switch (text.type()) {
            case LIST: {
                if (config.isFlatten()) {
                    NTextList t = (NTextList) text;
                    List<NText> all = t.children();
                    List<NText> all2 = new ArrayList<>();
                    for (NText a : all) {
                        if (a instanceof NTextList) {
                            all2.addAll(((NTextList) a).children());
                        } else if (a instanceof NTextBuilder) {
                            all2.addAll(((NTextBuilder) a).children());
                        } else {
                            all2.add(a);
                        }
                    }
//                    if (all.size() == all2.size()) {
//                        return text;
//                    }
                    text= NText.ofList(all2);
                }
                return mapApplyThemeAndFilter(text);
            }
            case BUILDER: {
                if (config.isFlatten()) {
                    NTextBuilder t = (NTextBuilder) text;
                    List<NText> all = t.children();
                    List<NText> all2 = new ArrayList<>();
                    for (NText a : all) {
                        if (a instanceof NTextList) {
                            all2.addAll(((NTextList) a).children());
                        } else if (a instanceof NTextBuilder) {
                            all2.addAll(((NTextBuilder) a).children());
                        } else {
                            all2.add(a);
                        }
                    }
                    if (all.size() == all2.size()) {
                        return text;
                    }
                    return NText.ofList(all2);
                } else {
                    return text;
                }
            }
            case PLAIN: {
                NTextPlain t = (NTextPlain) text;
                String str = transformText(t.value());
                if (config.isFlatten()) {
                    text = flatten(str);
                } else {
                    text = NText.ofPlain(str);
                }
                return text;
            }
            case STYLED: {
                NTextStyled t = (NTextStyled) text;
                NText child = t.child();
                List<NText> cc = new ArrayList<>();
                boolean filtered = config.isFiltered();
                if (config.isFlatten()) {
                    if (child instanceof NTextList) {
                        for (NText x : ((NTextList) child).children()) {
                            if (isNewline(x)) {
                                cc.add(x);
                            } else {
                                if(filtered){
                                    cc.add(x);
                                }else {
                                    cc.add(NText.ofStyled(x, t.styles()));
                                }
                            }
                        }
                    }else {
                        cc.add(mapTxt(child, x -> {
                            if (isNewline(x)) {
                                return x;
                            }
                            if(filtered){
                                return x;
                            }else {
                                return NText.ofStyled(x, t.styles());
                            }
                        }));
                    }
                }else{
                    if(filtered) {
                        cc.add(child);
                    }else{
                        cc.add(text);
                    }
                }
                if (filtered) {
                    return compressTxt(cc.stream().map(x->mapTxt(x, y -> {
                        if (y.type() == NTextType.STYLED) {
                            return ((NTextStyled) y).child();
                        }
                        return y;
                    })).collect(Collectors.toList()));
                }else{
                    if (config.isApplyTheme()) {
                        NTextTheme theme = NTextTheme.get(config.themeName()).orElse(NTextRPI.of().currentTheme());
                        NTextStyles basicStyles = theme.toBasicStyles(t.styles(),config.isBasicTrueStyles());
                        return compressTxt(cc.stream().map(x->mapTxt(x, y -> {
                            if(y.type() == NTextType.STYLED){
                                return NText.ofStyled(((NTextStyled) y).child(), basicStyles);
                            }
                            if(y.type() == NTextType.PLAIN){
                                //newline
                                return y;
                            }
                            throw new IllegalArgumentException("unexpected...");
                        })).collect(Collectors.toList()));
                    }else{
                        return compressTxt(cc);
                    }
                }
            }
            case LINK: {
                NTextLink t = (NTextLink) text;
                String str = transformText(t.value());
                if (config.isFlatten()) {
                    text = mapTxt(flatten(str), x -> {
                        NTextPlain p = (NTextPlain) x;
                        if (isNewline(p)) {
                            return p;
                        }
                        return NText.ofLink(p.value());
                    });
                }
                if (config.isNormalize()) {
                    text = mapTxt(text, x -> {
                        if (x.type() == NTextType.PLAIN) {
                            return x;
                        }
                        String lnk = ((NTextLink) x).value();
                        return NText.ofStyled(lnk, NTextStyle.underlined());
                    });
                }
                return mapApplyThemeAndFilter(text);
            }
            case TITLE: {
                NTextTitle t = (NTextTitle) text;
                String prefix = null;
                int level = t.level();
                if (config.isFlatten() || config.isNormalize() || config.isProcessTitleNumbers()) {
                    if (config.isProcessTitleNumbers()) {
                        NTitleSequence n = context.titleSequence();
                        if (n == null) {
                            n = config.titleNumberSequence();
                            if (n == null) {
                                n = NText.ofNumbering();
                            }
                            context.titleSequence(n);
                        }
                        n = n.next(level);
                        context.titleSequence(n);
                        prefix = n.toString() + " ";
                    } else {
                        prefix = CoreStringUtils.fillString('#', level) + ") ";
                    }
                    List<NText> li = new ArrayList<>();
                    li.add(NText.ofPlain(prefix + " "));
                    if (config.isFlatten()) {
                        li.addAll(asList(t.child()));
                    } else {
                        li.add(t.child());
                    }
                    li.add(NText.ofNewLine());
                    text = NText.ofStyled(NText.ofList(li), NTextStyle.primary(level));
                }
                return mapApplyThemeAndFilter(text);
            }
            case INCLUDE: {
                NTextInclude t = (NTextInclude) text;
                if (config.isProcessIncludes()) {
                    NCmdLine cmd = NCmdLine.parseDefault(
                            t.text()
                    ).orNull();
                    if (cmd != null && cmd.length() > 0) {
                        String p = cmd.next().flatMap(NArg::asString).orNull();
                        NPath newP = resolveRelativePath(p, config.currentDir());
                        NText n = NTextParser.of().parse(newP);
                        //do not continue
                        return NText.transform(n, config.copy()
                                .processIncludes(true)
                                .currentDir(newP.parent())
                                .importClassLoader(config.importClassLoader())
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
                    text = NText.transform(text, context.config().copy()
                            .flatten(true)
                            .normalize(config.isNormalize())
                            .processVars(config.isProcessVars())
                            .varProvider(config.varProvider())
                    );
                }
                if (config.isApplyTheme()) {
                    NTextTheme theme = NTextTheme.get(config.themeName()).orElse(NTextTheme.of());
                    text = mapTxt(text, x -> {
                        if (x.type() == NTextType.STYLED) {
                            NTextStyled y = (NTextStyled) x;
                            NTextStyles basicStyles = theme.toBasicStyles(y.styles(),config.isBasicTrueStyles());
                            if(!y.styles().equals(basicStyles)){
                                return NText.ofStyled(y.child(), basicStyles);
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
                if (x.type() == NTextType.STYLED) {
                    return ((NTextStyled) x).child();
                }
                return x;
            });
        }else{
            if (config.isApplyTheme()) {
                NTextTheme theme = NTextTheme.get(config.themeName()).orElse(NTextTheme.of());
                text = mapTxt(text, x -> {
                    if (x.type() == NTextType.STYLED) {
                        NTextStyled y = (NTextStyled) x;
                        NTextStyles basicStyles = theme.toBasicStyles(y.styles(),config.isBasicTrueStyles());
                        if(!y.styles().equals(basicStyles)){
                            return NText.ofStyled(y.child(), basicStyles);
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
        return li.isEmpty() ? null : li.size() == 1 ? li.get(0) : NText.ofList(li);
    }

    private boolean isNewline(String c) {
        return c != null && (c.startsWith("\n")
                || c.startsWith("\r"));
    }

    private boolean isNewline(NText c) {
        return c instanceof NTextPlain && isNewline(((NTextPlain) c).value());
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
            li.add(NText.ofStyled(c, styles));
        }
        return compressTxt(li);
    }

    private List<NText> asList(NText text) {
        if (text == null) {
            return Collections.emptyList();
        }
        if (text instanceof NTextList) {
            return ((NTextList) text).children();
        }
        return Arrays.asList(text);
    }

    private NText flatten(NTextPlain tt) {
        return flatten(tt.value());
    }

    private NText flatten(String tt) {
        List<NText> li = new ArrayList<>();
        for (String line : CoreStringUtils.splitOnNewlines(tt)) {
            li.add(NText.ofPlain(line));
        }
        return compressTxt(li);
    }
}
