package net.thevpc.nuts.runtime.standalone.io.util;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.xtra.expr.StringTokenizerUtils;
import net.thevpc.nuts.text.NTextBuilder;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.util.NMsg;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class NPathParts {
    private final Type type;
    private final String protocol;
    private final String authority;
    private final String location;
    private final String ref;
    private final String query;

    public NPathParts(Type type, String protocol, String authority, String location, String query, String ref, NSession session) {
        switch (type) {
            case REF: {
                this.type = type;
                this.protocol = ensureNull(protocol, "protocol");
                this.authority = ensureNull(authority, "authority");
                this.location = ensureNull(location, "location");
                this.query = ensureNull(query, "query");
                this.ref = ensureNonNull(ref, "ref");
                break;
            }
            case FILE: {
                this.type = type;
                this.protocol = ensureNull(protocol, "protocol");
                this.authority = ensureNull(authority, "authority");
                this.location = ensureNonNull(location, "location");
                this.query = ensureNull(query, "query");
                this.ref = ensureNull(ref, "ref");
                break;
            }
            case FILE_URL: {
                if (!"file".equals(protocol)) {
                    throw new IllegalArgumentException("protocol must not be 'file'");
                }
                this.type = type;
                this.protocol = ensureTrimmed(protocol, "protocol");
                this.authority = ensureNull(authority, "authority");
                this.location = ensureNonNull(location, "location");
                this.query = ensureNull(query, "query");
                this.ref = ensureNull(ref, "ref");
                break;
            }
            case EMPTY: {
                this.type = type;
                this.protocol = ensureNull(protocol, "protocol");
                this.authority = ensureNull(authority, "authority");
                this.location = ensureNull(location, "location");
                this.query = ensureNull(query, "query");
                this.ref = ensureNull(ref, "ref");
                break;
            }
            case URL: {
                String p = ensureNonNull(protocol, "protocol");
                if (p.equals("file")) {
                    this.type = Type.FILE_URL;
                    this.protocol = ensureTrimmed(protocol, "protocol");
                    this.authority = ensureNull(authority, "authority");
                    this.location = ensureNonNull(location, "location");
                    this.query = ensureNull(query, "query");
                    this.ref = ensureNull(ref, "ref");
                } else if (p.equals("jar") || p.equals("zip")) {
                    this.type = type;
                    this.protocol = ensureTrimmed(protocol, "protocol");
                    this.authority = ensureNull(authority, "authority");
                    this.location = ensureNonNull(location, "location");
                    this.query = ensureNull(query, "query");
                    this.ref = ensureNull(ref, "ref");
                } else {
                    this.type = type;
                    this.protocol = p;
                    this.authority = ensureNonNull(authority, "authority");
                    this.location = ensureOk(location, "location");
                    this.query = ensureOk(query, "query");
                    this.ref = ensureOk(ref, "ref");
                }
                break;
            }
            default: {
                throw new NIllegalArgumentException(session, NMsg.ofPlain("unsupported NutsPathParts"));
            }
        }
    }

    public NPathParts(String path, NSession session) {
        if (path == null || path.trim().isEmpty()) {
            type = Type.EMPTY;
            protocol = "";
            authority = "";
            location = "";
            query = "";
            ref = "";
        } else {
            int wiredProtocolIndex = path.indexOf("://");
            int wiredProtocolIndex2 = wiredProtocolIndex > 0 ? wiredProtocolIndex + 3 : -1;
            String protocolToBe = wiredProtocolIndex2 < 0 ? null : path.substring(0, wiredProtocolIndex);
            boolean wiredProtocol = protocolToBe != null && protocolToBe.matches("[a-zA-Z]+");
            if (wiredProtocol) {
                type = Type.URL;
                protocol = protocolToBe;
                int firstInterr = path.indexOf('?', wiredProtocolIndex2);
                int firstSlash = path.indexOf('/', wiredProtocolIndex2);
                int firstSharp = path.indexOf('#', wiredProtocolIndex2);
                if (firstSharp >= 0) {
                    if (firstInterr >= 0 && firstInterr > firstSharp) {
                        firstInterr = -1;
                    }
                }
                if (firstSlash >= 0) {
                    if (firstInterr >= 0 && firstSlash > firstInterr) {
                        firstSlash = -1;
                    } else if (firstSharp >= 0 && firstSlash > firstSharp) {
                        firstSlash = -1;
                    }
                }
                if (firstSlash >= 0) {
                    authority = path.substring(wiredProtocolIndex2, firstSlash);
                    if (firstInterr >= 0) {
                        location = path.substring(firstSlash, firstInterr);
                        if (firstSharp >= 0) {
                            query = path.substring(firstInterr + 1, firstSharp);
                            ref = path.substring(firstSharp + 1);
                        } else {
                            query = path.substring(firstInterr + 1);
                            ref = "";
                        }
                    } else {
                        if (firstSharp >= 0) {
                            location = path.substring(firstSlash, firstSharp);
                            query = "";
                            ref = path.substring(firstSharp + 1);
                        } else {
                            location = path.substring(firstSlash);
                            query = "";
                            ref = "";
                        }
                    }
                } else {
                    if (firstInterr >= 0) {
                        authority = path.substring(wiredProtocolIndex2, firstInterr);
                        location = "";
                        if (firstSharp >= 0) {
                            query = path.substring(firstInterr + 1, firstSharp);
                            ref = path.substring(firstSharp + 1);
                        } else {
                            query = path.substring(firstInterr + 1);
                            ref = "";
                        }
                    } else {
                        if (firstSharp >= 0) {
                            authority = path.substring(wiredProtocolIndex2, firstSharp);
                            location = "";
                            query = "";
                            ref = path.substring(firstSharp + 1);
                        } else {
                            authority = path.substring(wiredProtocolIndex2);
                            location = "";
                            query = "";
                            ref = "";
                        }
                    }
                }
            } else if (path.startsWith("#")) {
                this.type = Type.REF;
                this.protocol = "";
                this.location = "";
                this.authority = "";
                this.query = "";
                this.ref = path.substring(1);
            } else if (path.startsWith("file:")) {
                this.type = Type.FILE_URL;
                this.protocol = path.substring(0, 4);
                this.location = path.substring(5);
                this.authority = "";
                this.query = "";
                this.ref = "";
            } else {
                this.type = Type.FILE;
                this.location = path;
                this.authority = "";
                this.protocol = "";
                this.query = "";
                this.ref = "";
            }
        }
    }

    public static String compressLocalPath(String path) {
        return compressLocalPath(path, 2, 2);
    }

    public static String compressLocalPath(String path, int left, int right) {
        String p = System.getProperty("user.home");
        if (path.startsWith(p + File.separator) || path.startsWith(p + "/") || path.startsWith(p + "\\")) {
            path = "~" + path.substring(p.length());
        }
        List<String> a = new ArrayList<>(StringTokenizerUtils.splitFileSlash(path));
        int min = left + right + 1;
        if (a.size() > 0 && a.get(0).equals("")) {
            left += 1;
            min += 1;
        }
        if (a.size() > min) {
            a.add(left, "...");
            int len = a.size() - right - left - 1;
            for (int i = 0; i < len; i++) {
                a.remove(left + 1);
            }
        }
        return String.join("/", a);
    }

    public Type getType() {
        return type;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getAuthority() {
        return authority;
    }

    public String getFile() {
        return location;
    }

    public String getName() {
        if (location == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder(location);
        while (sb.length() > 0 && (sb.charAt(sb.length() - 1) == '/' || sb.charAt(sb.length() - 1) == '\\')) {
            sb.delete(sb.length() - 1, sb.length());
        }
        if (sb.length() == 0) {
            return "";
        }
        if (sb.length() == 1) {
            return sb.toString();
        }
        int x = sb.length() - 1;
        while (x - 1 > 0 && (sb.charAt(x - 1) != '/' && sb.charAt(x - 1) != '\\')) {
            x--;
        }
        return sb.substring(x);
    }

    public String getQuery() {
        return query;
    }

    public String getRef() {
        return ref;
    }

    public enum Type {
        EMPTY,
        FILE,
        FILE_URL,
        URL,
        REF
    }

    public String toString() {
        // pre-compute length of StringBuffer
        int len = protocol.length() + 1;
        if (authority != null && authority.length() > 0) {
            len += 2 + authority.length();
        }
        if (location != null) {
            len += location.length();
        }
        if (query != null) {
            len += 1 + query.length();
        }
        if (ref != null) {
            len += 1 + ref.length();
        }

        StringBuilder result = new StringBuilder(len);
        result.append(protocol);
        result.append(":");
        if (authority != null && authority.length() > 0) {
            result.append("//");
            result.append(authority);
        }
        if (location != null) {
            result.append(location);
        }
        if (query != null) {
            result.append(query);
        }
        if (ref != null) {
            result.append(ref);
        }
        return result.toString();
    }

    public static NString toNutsString(NString protocol, NString authority, NString path, NString query, NString ref, NSession session) {
        NTexts txt = NTexts.of(session);
        NTextBuilder result = txt.ofBuilder();
        result.append(protocol);
        if (authority != null && authority.textLength() > 0) {
            result.append("://", NTextStyle.path());
            result.append(authority);
        } else {
            result.append(":", NTextStyle.path());
        }
        if (path != null) {
            result.append(path);
        }
        if (query != null) {
            result.append(query);
        }
        if (ref != null) {
            result.append(ref);
        }
        return result.build();
    }

    private static String ensureNonNull(String s, String name) {
        if (s == null || s.trim().length() == 0) {
            throw new IllegalArgumentException(name + " must not be null");
        }
        return s.trim();
    }

    private static String ensureTrimmed(String s, String name) {
        if (s == null) {
            return "";
        }
        return s.trim();
    }

    private static String ensureOk(String s, String name) {
        if (s == null) {
            return "";
        }
        return s.trim();
    }

    private static String ensureNull(String s, String name) {
        if (!(s == null || s.length() == 0)) {
            throw new IllegalArgumentException(name + " must be null or empty");
        }
        return "";
    }

    public static NString compressPath(String path, NSession session) {
        return compressPath(path, 2, 2, session);
    }

    public static NString compressPath(String path, int left, int right, NSession session) {
        NTexts txt = NTexts.of(session);
        NPathParts p = new NPathParts(path, session);
        switch (p.getType()) {
            case FILE_URL:
            case URL: {
                return NPathParts.toNutsString(
                        txt.ofStyled(p.getProtocol(), NTextStyle.path()),
                        NBlankable.isBlank(p.getAuthority()) ? null : txt.ofStyled(p.getAuthority(), NTextStyle.path()),
                        NBlankable.isBlank(p.getFile()) ? null : txt.ofStyled(NPathParts.compressLocalPath(p.getFile(), 0, 2), NTextStyle.path()),
                        NBlankable.isBlank(p.getQuery()) ? null : txt.ofStyled("...", NTextStyle.path()),
                        NBlankable.isBlank(p.getRef()) ? null : txt.ofStyled("...", NTextStyle.path()),
                        session
                );
            }
            case REF: {
                return NBlankable.isBlank(p.getRef()) ? null : txt.ofStyled("...", NTextStyle.path());
            }
            case FILE: {
                return txt.ofStyled(NPathParts.compressLocalPath(p.getFile(), 2, 2), NTextStyle.path());
            }
            case EMPTY:
                return txt.ofBlank();
        }
        throw new NUnsupportedEnumException(session, p.getType());
    }
}
