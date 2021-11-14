package net.thevpc.nuts.runtime.core.format.text.stylethemes;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.expr.StringReaderExt;
import net.thevpc.nuts.runtime.core.util.CoreNumberUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import java.util.UUID;

public class NutsTextFormatPropertiesTheme implements NutsTextFormatTheme {
    private final Properties props = new Properties();
    private final NutsSession session;

    public NutsTextFormatPropertiesTheme(String name, ClassLoader cls, NutsSession session) {
        this.session = session;
        if (name.indexOf('/') >= 0 || name.indexOf('\\') >= 0) {
            try (InputStream is = NutsPath.of(name, session).getInputStream()) {
                props.load(is);
            } catch (IOException e) {
                throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("invalid theme: %s", name), e);
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
                        throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("invalid theme: %s", name));
                    }
                    try {
                        props.load(inStream);
                    } finally {
                        inStream.close();
                    }
                } catch (IOException e) {
                    throw new NutsIOException(session, e);
                }
            } else {
                NutsPath themeFile = session.locations().getStoreLocation(
                        NutsId.of("net.thevpc.nuts:nuts-runtime#SHARED", session),
                        NutsStoreLocation.CONFIG
                ).resolve("themes").resolve(name);
                if (themeFile.isRegularFile()) {
                    try (InputStream inStream = themeFile.getInputStream()) {
                        props.load(inStream);
                    } catch (IOException e) {
                        throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("invalid theme: %s", name), e);
                    }
                } else {
                    throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("invalid theme: %s", name));
                }
            }
        }
    }

    @Override
    public String getName() {
        String themeName = props.getProperty("theme-name");
        if (NutsBlankable.isBlank(themeName)) {
            themeName = UUID.randomUUID().toString();
            props.put("theme-name", themeName);
        }
        return themeName;
    }

    @Override
    public NutsTextStyles toBasicStyles(NutsTextStyles styles, NutsSession session) {
        NutsTextStyles ret = NutsTextStyles.PLAIN;
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
                        sb.append(variant % (CoreNumberUtils.convertToInteger(mod.toString(), 1)));
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
        return CoreNumberUtils.convertToInteger(props.getProperty(n), 0);
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
            return NutsTextStyles.PLAIN;
        }
        NutsTextStyles ret = NutsTextStyles.PLAIN;
        for (String v : s.split(",")) {
            NutsTextStyles ss = toBasicStyles(v, style.getVariant(), session, maxLoop - 1);
            ret = ret.append(ss);
        }
        return ret;
    }

    public NutsTextStyles toBasicStyles(String v, int defaultVariant, NutsSession session, int maxLoop) {
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
        NutsTextStyleType st = NutsTextStyleType.parseLenient(k, null);
        if (st == null) {
            if (NutsBlankable.isBlank(n)) {
                return NutsTextStyles.PLAIN;
            } else {
                String z = props.getProperty(n);
                if (z != null) {
                    if (maxLoop < 0) {
                        return null;
                    }
                    return toBasicStyles(z, defaultVariant, session, maxLoop - 1);
                }
                return NutsTextStyles.PLAIN;
            }
        }
        switch (st) {
            case PLAIN: {
                return NutsTextStyles.of(NutsTextStyle.of(st, 0));
            }
            case FORE_COLOR:
            case FORE_TRUE_COLOR:
            case BACK_COLOR:
            case BACK_TRUE_COLOR: {
                Integer ii = CoreNumberUtils.convertToInteger(n, null);
                if (ii == null) {
                    ii = getVarValAsInt(n);
                }
                return NutsTextStyles.of(NutsTextStyle.of(st, ii));
            }
            default: {
                Integer ii = CoreNumberUtils.convertToInteger(n, null);
                if (ii == null) {
                    ii = getVarValAsInt(n);
                }
                return toBasicStyles(NutsTextStyle.of(st, ii), session, maxLoop);
            }
        }
    }
}
