package net.thevpc.nuts.runtime.standalone.text.theme;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.xtra.expr.StringReaderExt;
import net.thevpc.nuts.text.NTextFormatTheme;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTextStyleType;
import net.thevpc.nuts.text.NTextStyles;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import java.util.UUID;

public class NTextFormatPropertiesTheme implements NTextFormatTheme {
    private final Properties props = new Properties();
    private final NSession session;

    public NTextFormatPropertiesTheme(String name, ClassLoader cls, NSession session) {
        this.session = session;
        if (name.indexOf('/') >= 0 || name.indexOf('\\') >= 0) {
            try (InputStream is = NPath.of(name, session).getInputStream()) {
                props.load(is);
            } catch (IOException e) {
                throw new NIllegalArgumentException(session, NMsg.ofC("invalid theme: %s", name), e);
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
                        throw new NIllegalArgumentException(session, NMsg.ofC("invalid theme: %s", name));
                    }
                    try {
                        props.load(inStream);
                    } finally {
                        inStream.close();
                    }
                } catch (IOException e) {
                    throw new NIOException(session, e);
                }
            } else {
                NPath themeFile = NLocations.of(session).getStoreLocation(
                        NId.ofRuntime("SHARED").get(session),
                        NStoreType.CONF
                ).resolve("themes").resolve(name);
                if (themeFile.isRegularFile()) {
                    try (InputStream inStream = themeFile.getInputStream()) {
                        props.load(inStream);
                    } catch (IOException e) {
                        throw new NIllegalArgumentException(session, NMsg.ofC("invalid theme: %s", name), e);
                    }
                } else {
                    throw new NIllegalArgumentException(session, NMsg.ofC("invalid theme: %s", name));
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
    public NTextStyles toBasicStyles(NTextStyles styles, NSession session) {
        NTextStyles ret = NTextStyles.PLAIN;
        if (styles != null) {
            for (NTextStyle style : styles) {
                ret = ret.append(toBasicStyles(style, session));
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
                        r.nextChar();
                        r.nextChar();
                        StringBuilder mod = new StringBuilder();
                        while (r.hasNext() && r.peekChar() >= '0' && r.peekChar() <= '9') {
                            mod.append(r.nextChar());
                        }
                        sb.append(variant % (NLiteral.of(mod.toString()).asInt().orElse(1)));
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

    private int getVarValAsInt(String n) {
        return NLiteral.of(props.getProperty(n)).asInt().orElse(0);
    }

    private String getProp(NTextStyleType t, String variant) {
        String name = t.name();
        String s = props.getProperty(name + "(" + variant + ")");
        if (s == null) {
            s = props.getProperty(name.toLowerCase() + "(" + variant + ")");
        }
        return s;
    }

    public NTextStyles toBasicStyles(NTextStyle style, NSession session) {
        return toBasicStyles(style, session, 20);
    }

    public NTextStyles toBasicStyles(NTextStyle style, NSession session, int maxLoop) {
        if (maxLoop <= 0) {
            throw new NIllegalArgumentException(session,
                    NMsg.ofC("invalid ntf theme for %s(%s). infinite loop", style.getType(), style.getVariant()));
        }
        if (style.getType().basic()) {
            return NTextStyles.of(style);
        }
        String s = getProp(style.getType(), style.getVariant());
        if (s == null) {
            return NTextStyles.PLAIN;
        }
        NTextStyles ret = NTextStyles.PLAIN;
        for (String v : s.split(",")) {
            NTextStyles ss = toBasicStyles(v, style.getVariant(), session, maxLoop - 1);
            ret = ret.append(ss);
        }
        return ret;
    }

    public NTextStyles toBasicStyles(String v, int defaultVariant, NSession session, int maxLoop) {
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
                    return toBasicStyles(z, defaultVariant, session, maxLoop - 1);
                }
                return NTextStyles.PLAIN;
            }
        }
        switch (st) {
            case PLAIN: {
                return NTextStyles.of(NTextStyle.of(st, 0));
            }
            case FORE_COLOR:
            case FORE_TRUE_COLOR:
            case BACK_COLOR:
            case BACK_TRUE_COLOR: {
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
                return toBasicStyles(NTextStyle.of(st, ii), session, maxLoop);
            }
        }
    }
}
