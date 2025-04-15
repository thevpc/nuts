package net.thevpc.nuts.runtime.standalone.text.theme;

import net.thevpc.nuts.*;


import net.thevpc.nuts.NStoreType;
import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.xtra.web.DefaultNWebCli;
import net.thevpc.nuts.runtime.standalone.xtra.expr.StringReaderExt;
import net.thevpc.nuts.text.NTextFormatTheme;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTextStyleType;
import net.thevpc.nuts.text.NTextStyles;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NLiteral;
import net.thevpc.nuts.util.NMsg;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import java.util.UUID;

public class NTextFormatPropertiesTheme implements NTextFormatTheme {
    private final Properties props = new Properties();
    private final NWorkspace workspace;

    public NTextFormatPropertiesTheme(String name, ClassLoader cls, NWorkspace workspace) {
        this.workspace = workspace;
        if (name.indexOf('/') >= 0 || name.indexOf('\\') >= 0) {
            try (InputStream is = NPath.of(name).getInputStream()) {
                props.load(is);
            } catch (IOException e) {
                throw new NIllegalArgumentException(NMsg.ofC("invalid theme: %s", name), e);
            }
        } else {
            if (cls == null) {
                cls = getClass().getClassLoader();//Thread.currentThread().getContextClassLoader();
            }
            URL u = cls.getResource("META-INF/ntf-themes/" + name + ".ntf-theme");
            if (u != null) {
                try {
                    InputStream inStream = null;
                    inStream = DefaultNWebCli.prepareGlobalOpenStream(u);
                    if (inStream == null) {
                        throw new NIllegalArgumentException(NMsg.ofC("invalid theme: %s", name));
                    }
                    try {
                        props.load(inStream);
                    } finally {
                        inStream.close();
                    }
                } catch (IOException e) {
                    throw new NIOException(e);
                }
            } else {
                NPath themeFile = NWorkspace.of().getStoreLocation(
                        NId.getRuntime("SHARED").get(),
                        NStoreType.CONF
                ).resolve("themes").resolve(name);
                if (themeFile.isRegularFile()) {
                    try (InputStream inStream = themeFile.getInputStream()) {
                        props.load(inStream);
                    } catch (IOException e) {
                        throw new NIllegalArgumentException(NMsg.ofC("invalid theme: %s", name), e);
                    }
                } else {
                    throw new NIllegalArgumentException(NMsg.ofC("invalid theme: %s", name));
                }
            }
        }
    }

    @Override
    public String getName() {
        String themeName = props.getProperty("theme-name");
        if (NBlankable.isBlank(themeName)) {
            themeName = UUID.randomUUID().toString();
            props.put("theme-name", themeName);
        }
        return themeName;
    }

    @Override
    public NTextStyles toBasicStyles(NTextStyles styles, boolean basicTrueStyles) {
        NTextStyles ret = NTextStyles.PLAIN;
        if (styles != null) {
            for (NTextStyle style : styles) {
                ret = ret.append(toBasicStyles(style, basicTrueStyles));
            }
        }
        return ret;
    }

    private String getProp(NTextStyleType t, int variant) {
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
                        r.readChar();
                        r.readChar();
                        StringBuilder mod = new StringBuilder();
                        while (r.hasNext() && r.peekChar() >= '0' && r.peekChar() <= '9') {
                            mod.append(r.readChar());
                        }
                        sb.append(variant % (NLiteral.of(mod.toString()).asInt().orElse(1)));
                    } else {
                        r.readChar();
                        sb.append(variant);
                    }
                } else {
                    sb.append(r.readChar());
                }
            }
            return sb.toString();
        }
        return v;
    }

    private int getVarValAsInt(String n) {
        return NLiteral.of(props.getProperty(n)).asInt().orElse(0);
    }

    private Color getVarValAsColor(String n) {
        String b = props.getProperty(n);
        if (b != null) {
            b = b.trim();
            if (b.startsWith("#")) {
                return new Color(Integer.parseInt(b.substring(1), 16));
            }
        }
        return null;
    }

    private String getProp(NTextStyleType t, String variant) {
        String name = t.name();
        String s = props.getProperty(name + "(" + variant + ")");
        if (s == null) {
            s = props.getProperty(name.toLowerCase() + "(" + variant + ")");
        }
        return s;
    }

    public NTextStyles toBasicStyles(NTextStyle style, boolean basicTrueStyles) {
        if (style == null) {
            return NTextStyles.PLAIN;
        }
        if(style.getType().isBasic(basicTrueStyles)) {
            return NTextStyles.of(style);
        }
        return toBasicStyles(style, basicTrueStyles, 20);
    }

    public NTextStyles toBasicStyles(NTextStyle style, boolean basicTrueStyles, int maxLoop) {
        if (style == null) {
            return NTextStyles.PLAIN;
        }
        if(style.getType().isBasic(basicTrueStyles)) {
            return NTextStyles.of(style);
        }
        if (maxLoop <= 0) {
            throw new NIllegalArgumentException(
                    NMsg.ofC("invalid ntf theme for %s(%s). infinite loop", style.getType(), style.getVariant()));
        }
        String s = getProp(style.getType(), style.getVariant());
        if (s == null) {
            switch (style.getType()){
                case FORE_COLOR:{
                    //basicTrue is true!!
                    return NTextStyles.of(NTextStyle.foregroundTrueColor(DefaultNTextFormatTheme.foregroundSimpleToTrueColor(style.getVariant())));
                }
                case BACK_COLOR:{
                    //basicTrue is true!!
                    return NTextStyles.of(NTextStyle.foregroundTrueColor(DefaultNTextFormatTheme.backgroundSimpleToTrueColor(style.getVariant())));
                }
            }
            return NTextStyles.PLAIN;
        }
        NTextStyles ret = NTextStyles.PLAIN;
        for (String v : s.split(",")) {
            NTextStyles ss = toBasicStyles(v, basicTrueStyles, style.getVariant(), maxLoop - 1);
            ret = ret.append(ss);
        }
        return ret;
    }

    public NTextStyles toBasicStyles(String v, boolean basicTrueStyles, int defaultVariant, int maxLoop) {
        v = v.trim();
        int a = v.indexOf('(');
        String n = "";
        String k = v;
        if (a > 0) {
            int b = v.indexOf(')', a);
            if (b > 0) {
                n = v.substring(a + 1, b);
                k = v.substring(0, a);
            }
        }
        n = n.trim();
        k = k.trim();
        if (n.equals("*") || n.isEmpty()) {
            n = "" + defaultVariant;
        }
        NTextStyleType st = NTextStyleType.parse(k).orNull();
        if (st == null) {
            if (NBlankable.isBlank(n)) {
                return NTextStyles.PLAIN;
            } else {
                String z = props.getProperty(n);
                if (z != null) {
                    if (maxLoop < 0) {
                        return null;
                    }
                    return toBasicStyles(z, basicTrueStyles, defaultVariant, maxLoop - 1);
                }
                return NTextStyles.PLAIN;
            }
        }
        switch (st) {
            case PLAIN: {
                return NTextStyles.of(NTextStyle.of(st, 0));
            }
            case FORE_COLOR: {
                Color c = getVarValAsColor(n);
                if(c!=null){
                    return NTextStyles.of(NTextStyle.of(st, c.getRGB()));
                }
                Integer ii = NLiteral.of(n).asInt().orNull();
                if (ii == null) {
                    ii = getVarValAsInt(n);
                }
                if (basicTrueStyles) {
                    return toBasicStyles(NTextStyles.of(NTextStyle.foregroundTrueColor(DefaultNTextFormatTheme.foregroundSimpleToTrueColor(ii))), basicTrueStyles);
                }
            }
            case BACK_COLOR: {
                Color c = getVarValAsColor(n);
                if(c!=null){
                    return NTextStyles.of(NTextStyle.of(st, c.getRGB()));
                }
                Integer ii = NLiteral.of(n).asInt().orNull();
                if (ii == null) {
                    ii = getVarValAsInt(n);
                }
                if (basicTrueStyles) {
                    return toBasicStyles(NTextStyles.of(NTextStyle.backgroundTrueColor(DefaultNTextFormatTheme.backgroundSimpleToTrueColor(ii))), basicTrueStyles);
                }
            }
            case FORE_TRUE_COLOR:
            case BACK_TRUE_COLOR: {
                Color c = getVarValAsColor(n);
                if(c!=null){
                    return NTextStyles.of(NTextStyle.of(st, c.getRGB()));
                }
                Integer ii = NLiteral.of(n).asInt().orNull();
                if (ii == null) {
                    ii = getVarValAsInt(n);
                }
                return NTextStyles.of(NTextStyle.of(st, ii));
            }
            default: {
                Integer ii = NLiteral.of(n).asInt().orNull();
                if (ii == null) {
                    ii = getVarValAsInt(n);
                }
                return toBasicStyles(NTextStyle.of(st, ii), basicTrueStyles, maxLoop);
            }
        }
    }
}
