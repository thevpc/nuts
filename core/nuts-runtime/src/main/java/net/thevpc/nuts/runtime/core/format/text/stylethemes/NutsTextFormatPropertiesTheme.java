package net.thevpc.nuts.runtime.core.format.text.stylethemes;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.bundles.parsers.StringReaderExt;
import net.thevpc.nuts.runtime.core.util.CoreIOUtils;
import net.thevpc.nuts.runtime.core.util.CoreNumberUtils;
import net.thevpc.nuts.runtime.core.util.CoreStringUtils;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.UUID;

public class NutsTextFormatPropertiesTheme implements NutsTextFormatTheme {
    private Properties props = new Properties();
    private NutsWorkspace ws;

    public NutsTextFormatPropertiesTheme(String name, ClassLoader cls, NutsWorkspace ws) {
        this.ws = ws;
        if (name.indexOf('/') >= 0 || name.indexOf('\\') >= 0) {
            if (name.startsWith("classpath://")) {
                if (cls == null) {
                    cls = getClass().getClassLoader();//Thread.currentThread().getContextClassLoader();
                }
                String r = name.substring("classpath://".length());
                if (!r.startsWith("/")) {
                    r = "/" + r;
                }
                URL u = cls.getResource(r);
                if (u == null) {
                    if (!r.endsWith(".ntf-theme")) {
                        r += ".ntf-theme";
                        u = cls.getResource(r);
                    }
                }
                if (u == null) {
                    throw new IllegalArgumentException("invalid theme: " + name);
                }
                try {
                    props.load(u.openStream());
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
            if (CoreIOUtils.isURL(name)) {
                try {
                    URL uu = new URL(name);
                    InputStream inStream = null;
                    inStream = uu.openStream();
                    if (inStream == null) {
                        throw new IllegalArgumentException("invalid theme: " + name);
                    }
                    try {
                        props.load(inStream);
                    } finally {
                        inStream.close();
                    }
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            } else {
                try (InputStream inStream = new FileInputStream(new File(name))) {
                    props.load(inStream);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
        } else {
            if (cls == null) {
                cls = getClass().getClassLoader();//Thread.currentThread().getContextClassLoader();
            }
            URL u = cls.getResource("META-INF/ntf-themes/" + name + ".ntf-theme");
            if (u != null) {
                try {
                    InputStream inStream = null;
                    inStream = u.openStream();
                    if (inStream == null) {
                        throw new IllegalArgumentException("invalid theme: " + name);
                    }
                    try {
                        props.load(inStream);
                    } finally {
                        inStream.close();
                    }
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            } else {
                Path themeFile = Paths.get(ws.locations().getStoreLocation(
                        ws.id().parser().parse("net.thevpc.nuts:nuts-runtime#SHARED"),
                        NutsStoreLocation.CONFIG
                )).resolve("themes").resolve(name);
                if (Files.isRegularFile(themeFile)) {
                    try (InputStream inStream = Files.newInputStream(themeFile)) {
                        props.load(inStream);
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                } else {
                    throw new IllegalArgumentException("invalid theme: " + name);
                }
            }
        }
    }

    @Override
    public String getName() {
        String themeName = props.getProperty("theme-name");
        if (CoreStringUtils.isBlank(themeName)) {
            themeName = UUID.randomUUID().toString();
            props.put("theme-name", themeName);
        }
        return themeName;
    }

    @Override
    public NutsTextStyles toBasicStyles(NutsTextStyles styles, NutsSession session) {
        NutsTextStyles ret = NutsTextStyles.NONE;
        if (styles != null) {
            for (NutsTextStyle style : styles) {
                ret = ret.append(toBasicStyles(style, session));
            }
        }
        return ret;
    }

    private String getProp(NutsTextStyleType t, int variant) {
        String v = getProp(t, "" + variant);
        if (v != null) {
            return v;
        }
        v = getProp(t, "*");
        if (v != null) {
            StringReaderExt r = new StringReaderExt(v);
            StringBuilder sb = new StringBuilder();
            while (r.hasNext()) {
                if (r.peekChar() == '*') {
                    if (r.hasNext(1) && r.peekChar(1) == '%') {
                        r.nextChar();
                        r.nextChar();
                        StringBuilder mod = new StringBuilder();
                        while (r.hasNext() && r.peekChar() >= '0' && r.peekChar() <= '9') {
                            mod.append(r.nextChar());
                        }
                        sb.append(variant % (Integer.parseInt(mod.toString())));
                    } else {
                        r.nextChar();
                        sb.append(variant);
                    }
                } else {
                    sb.append(r.nextChar());
                }
            }
            return sb.toString();
        }
        return v;
    }

    private int getVarVal(String n) {
        String z = props.getProperty(n);
        if (z != null) {
            try {
                return Integer.parseInt(z);
            } catch (Exception ex) {
                return 0;
            }
        }
        return 0;
    }

    private String getProp(NutsTextStyleType t, String variant) {
        String name = t.name();
        String s = props.getProperty(name + "(" + variant + ")");
        if (s == null) {
            s = props.getProperty(name.toLowerCase() + "(" + variant + ")");
        }
        return s;
    }

    public NutsTextStyles toBasicStyles(NutsTextStyle style, NutsSession session) {
        return toBasicStyles(style, session, 20);
    }

    public NutsTextStyles toBasicStyles(NutsTextStyle style, NutsSession session, int maxLoop) {
        if (maxLoop <= 0) {
            throw new NutsIllegalArgumentException(session,
                    NutsMessage.cstyle("invalid ntf theme for %s(%s). infinite loop", style.getType(), style.getVariant()));
        }
        if (style.getType().basic()) {
            return NutsTextStyles.of(style);
        }
        String s = getProp(style.getType(), style.getVariant());
        if (s == null) {
            return NutsTextStyles.NONE;
        }
        NutsTextStyles ret = NutsTextStyles.NONE;
        for (String v : s.split(",")) {
            NutsTextStyles ss = toBasicStyles(v, style.getVariant(), session, maxLoop - 1);
            ret = ret.append(ss);
        }
        return ret;
    }

    public NutsTextStyles toBasicStyles(String v, int defaultVariant, NutsSession session, int maxLoop) {
        v = v.trim();
        int a = v.indexOf('(');
        if (a > 0) {
            int b = v.indexOf(')', a);
            if (b > 0) {
                String n = v.substring(a + 1, b);
                String k = v.substring(0, a);
                if (n.equals("*")) {
                    n = "" + defaultVariant;
                }
                switch (k.toLowerCase()) {
                    case "foreground":
                    case "foregroundcolor": {
                        Integer ii = CoreNumberUtils.convertToInteger(n, null);
                        if (ii == null) {
                            ii = getVarVal(n);
                        }
                        return NutsTextStyles.of(
                                NutsTextStyle.foregroundColor(ii)
                        );
                    }
                    case "plain": {
                        return NutsTextStyles.NONE;
                    }
                    case "foregroundtruecolor": {
                        Integer ii = CoreNumberUtils.convertToInteger(n, null);
                        if (ii == null) {
                            ii = getVarVal(n);
                        }
                        return NutsTextStyles.of(
                                NutsTextStyle.foregroundTrueColor(ii)
                        );
                    }
                    case "background":
                    case "backgroundcolor": {
                        Integer ii = CoreNumberUtils.convertToInteger(n, null);
                        if (ii == null) {
                            ii = getVarVal(n);
                        }
                        return NutsTextStyles.of(
                                NutsTextStyle.backgroundColor(ii)
                        );
                    }
                    case "backgroundtruecolor": {
                        Integer ii = CoreNumberUtils.convertToInteger(n, null);
                        if (ii == null) {
                            ii = getVarVal(n);
                        }
                        return NutsTextStyles.of(
                                NutsTextStyle.backgroundTrueColor(ii)
                        );
                    }

                    case "primary": {
                        Integer ii = CoreNumberUtils.convertToInteger(n, null);
                        if (ii == null) {
                            ii = getVarVal(n);
                        }
                        return toBasicStyles(NutsTextStyle.primary(ii), session, maxLoop);
                    }
                    case "secondary": {
                        Integer ii = CoreNumberUtils.convertToInteger(n, null);
                        if (ii == null) {
                            ii = getVarVal(n);
                        }
                        return toBasicStyles(NutsTextStyle.secondary(ii), session, maxLoop);
                    }
                    case "underlined": {
                        Integer ii = CoreNumberUtils.convertToInteger(n, null);
                        if (ii == null) {
                            ii = getVarVal(n);
                        }
                        return toBasicStyles(NutsTextStyle.underlined(), session, maxLoop);
                    }
                    case "bold": {
                        Integer ii = CoreNumberUtils.convertToInteger(n, null);
                        if (ii == null) {
                            ii = getVarVal(n);
                        }
                        return toBasicStyles(NutsTextStyle.bold(), session, maxLoop);
                    }
                    case "bool": {
                        Integer ii = CoreNumberUtils.convertToInteger(n, null);
                        if (ii == null) {
                            ii = getVarVal(n);
                        }
                        return toBasicStyles(NutsTextStyle.bool(ii), session, maxLoop);
                    }
                    case "blink": {
                        Integer ii = CoreNumberUtils.convertToInteger(n, null);
                        if (ii == null) {
                            ii = getVarVal(n);
                        }
                        return toBasicStyles(NutsTextStyle.blink(), session, maxLoop);
                    }
                    case "comments": {
                        Integer ii = CoreNumberUtils.convertToInteger(n, null);
                        if (ii == null) {
                            ii = getVarVal(n);
                        }
                        return toBasicStyles(NutsTextStyle.comments(ii), session, maxLoop);
                    }
                    case "config": {
                        Integer ii = CoreNumberUtils.convertToInteger(n, null);
                        if (ii == null) {
                            ii = getVarVal(n);
                        }
                        return toBasicStyles(NutsTextStyle.config(ii), session, maxLoop);
                    }
                    case "danger": {
                        Integer ii = CoreNumberUtils.convertToInteger(n, null);
                        if (ii == null) {
                            ii = getVarVal(n);
                        }
                        return toBasicStyles(NutsTextStyle.danger(ii), session, maxLoop);
                    }
                    case "date": {
                        Integer ii = CoreNumberUtils.convertToInteger(n, null);
                        if (ii == null) {
                            ii = getVarVal(n);
                        }
                        return toBasicStyles(NutsTextStyle.date(ii), session, maxLoop);
                    }
                    case "number": {
                        Integer ii = CoreNumberUtils.convertToInteger(n, null);
                        if (ii == null) {
                            ii = getVarVal(n);
                        }
                        return toBasicStyles(NutsTextStyle.number(ii), session, maxLoop);
                    }
                    case "error": {
                        Integer ii = CoreNumberUtils.convertToInteger(n, null);
                        if (ii == null) {
                            ii = getVarVal(n);
                        }
                        return toBasicStyles(NutsTextStyle.error(ii), session, maxLoop);
                    }
                    case "warn": {
                        Integer ii = CoreNumberUtils.convertToInteger(n, null);
                        if (ii == null) {
                            ii = getVarVal(n);
                        }
                        return toBasicStyles(NutsTextStyle.warn(ii), session, maxLoop);
                    }
                    case "version": {
                        Integer ii = CoreNumberUtils.convertToInteger(n, null);
                        if (ii == null) {
                            ii = getVarVal(n);
                        }
                        return toBasicStyles(NutsTextStyle.version(ii), session, maxLoop);
                    }
                    case "variable": {
                        Integer ii = CoreNumberUtils.convertToInteger(n, null);
                        if (ii == null) {
                            ii = getVarVal(n);
                        }
                        return toBasicStyles(NutsTextStyle.variable(ii), session, maxLoop);
                    }
                    case "input": {
                        Integer ii = CoreNumberUtils.convertToInteger(n, null);
                        if (ii == null) {
                            ii = getVarVal(n);
                        }
                        return toBasicStyles(NutsTextStyle.input(ii), session, maxLoop);
                    }
                    case "title": {
                        Integer ii = CoreNumberUtils.convertToInteger(n, null);
                        if (ii == null) {
                            ii = getVarVal(n);
                        }
                        return toBasicStyles(NutsTextStyle.title(ii), session, maxLoop);
                    }
                    case "success": {
                        Integer ii = CoreNumberUtils.convertToInteger(n, null);
                        if (ii == null) {
                            ii = getVarVal(n);
                        }
                        return toBasicStyles(NutsTextStyle.success(ii), session, maxLoop);
                    }
                    case "string": {
                        Integer ii = CoreNumberUtils.convertToInteger(n, null);
                        if (ii == null) {
                            ii = getVarVal(n);
                        }
                        return toBasicStyles(NutsTextStyle.string(ii), session, maxLoop);
                    }
                    case "striked": {
                        Integer ii = CoreNumberUtils.convertToInteger(n, null);
                        if (ii == null) {
                            ii = getVarVal(n);
                        }
                        return toBasicStyles(NutsTextStyle.striked(ii), session, maxLoop);
                    }
                    case "separator": {
                        Integer ii = CoreNumberUtils.convertToInteger(n, null);
                        if (ii == null) {
                            ii = getVarVal(n);
                        }
                        return toBasicStyles(NutsTextStyle.separator(ii), session, maxLoop);
                    }
                    case "reversed": {
                        Integer ii = CoreNumberUtils.convertToInteger(n, null);
                        if (ii == null) {
                            ii = getVarVal(n);
                        }
                        return toBasicStyles(NutsTextStyle.reversed(ii), session, maxLoop);
                    }
                    case "path": {
                        Integer ii = CoreNumberUtils.convertToInteger(n, null);
                        if (ii == null) {
                            ii = getVarVal(n);
                        }
                        return toBasicStyles(NutsTextStyle.path(ii), session, maxLoop);
                    }
                    case "option": {
                        Integer ii = CoreNumberUtils.convertToInteger(n, null);
                        if (ii == null) {
                            ii = getVarVal(n);
                        }
                        return toBasicStyles(NutsTextStyle.option(ii), session, maxLoop);
                    }
                    case "pale": {
                        Integer ii = CoreNumberUtils.convertToInteger(n, null);
                        if (ii == null) {
                            ii = getVarVal(n);
                        }
                        return toBasicStyles(NutsTextStyle.pale(ii), session, maxLoop);
                    }
                    case "operator": {
                        Integer ii = CoreNumberUtils.convertToInteger(n, null);
                        if (ii == null) {
                            ii = getVarVal(n);
                        }
                        return toBasicStyles(NutsTextStyle.operator(ii), session, maxLoop);
                    }
                    case "keyword": {
                        Integer ii = CoreNumberUtils.convertToInteger(n, null);
                        if (ii == null) {
                            ii = getVarVal(n);
                        }
                        return toBasicStyles(NutsTextStyle.keyword(ii), session, maxLoop);
                    }
                    case "italic": {
                        Integer ii = CoreNumberUtils.convertToInteger(n, null);
                        if (ii == null) {
                            ii = getVarVal(n);
                        }
                        return toBasicStyles(NutsTextStyle.italic(ii), session, maxLoop);
                    }
                    case "info": {
                        Integer ii = CoreNumberUtils.convertToInteger(n, null);
                        if (ii == null) {
                            ii = getVarVal(n);
                        }
                        return toBasicStyles(NutsTextStyle.info(ii), session, maxLoop);
                    }
                    case "fail": {
                        Integer ii = CoreNumberUtils.convertToInteger(n, null);
                        if (ii == null) {
                            ii = getVarVal(n);
                        }
                        return toBasicStyles(NutsTextStyle.fail(ii), session, maxLoop);
                    }
                }
            }
        }
        return NutsTextStyles.NONE;
    }
}
