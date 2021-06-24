package net.thevpc.nuts.lib.ssh;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.LinkedHashMap;
import java.util.Map;

public class _StringUtils {
    public static Map<String, String> parseMap(String text, String entrySeparators) {
        return parseMap(text, "=", entrySeparators);
    }
    public static boolean isBlank(String string) {
        return string == null || string.trim().isEmpty();
    }

    public static Map<String, String> parseMap(String text, String eqSeparators, String entrySeparators) {
        Map<String, String> m = new LinkedHashMap<>();
        StringReader reader = new StringReader(text == null ? "" : text);
        while (true) {
            StringBuilder key = new StringBuilder();
            int r = 0;
            try {
                r = readToken(reader, eqSeparators + entrySeparators, key);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            String t = key.toString();
            if (r == -1) {
                if (!t.isEmpty()) {
                    m.put(t, null);
                }
                break;
            } else {
                char c = (char) r;
                if (eqSeparators.indexOf(c) >= 0) {
                    StringBuilder value = new StringBuilder();
                    try {
                        r = readToken(reader, entrySeparators, value);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    m.put(t, value.toString());
                    if (r == -1) {
                        break;
                    }
                } else {
                    //
                }
            }
        }
        return m;
    }

    public static int readToken(Reader reader, String stopTokens, StringBuilder result) throws IOException {
        while (true) {
            int r = reader.read();
            if (r == -1) {
                return -1;
            }
            if (r == '\"' || r == '\'') {
                char s = (char) r;
                while (true) {
                    r = reader.read();
                    if (r == -1) {
                        throw new RuntimeException("Expected " + '\"');
                    }
                    if (r == s) {
                        break;
                    }
                    if (r == '\\') {
                        r = reader.read();
                        if (r == -1) {
                            throw new RuntimeException("Expected " + '\"');
                        }
                        switch ((char) r) {
                            case 'n': {
                                result.append('\n');
                                break;
                            }
                            case 'r': {
                                result.append('\r');
                                break;
                            }
                            case 'f': {
                                result.append('\f');
                                break;
                            }
                            default: {
                                result.append((char) r);
                            }
                        }
                    } else {
                        char cr = (char) r;
                        result.append(cr);
                    }
                }
            } else {
                char cr = (char) r;
                if (stopTokens != null && stopTokens.indexOf(cr) >= 0) {
                    return cr;
                }
                result.append(cr);
            }
        }
    }
}
