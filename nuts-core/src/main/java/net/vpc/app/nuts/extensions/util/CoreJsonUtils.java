package net.vpc.app.nuts.extensions.util;

import net.vpc.app.nuts.util.JsonUtils;

import javax.json.Json;
import javax.json.JsonStructure;
import java.io.*;

public class CoreJsonUtils {
    public static <T> T deserialize(String s, Class<T> t) {
        return deserialize(new ByteArrayInputStream(s.getBytes()), t);
    }

    public static <T> T deserialize(InputStream s, Class<T> t) {
        JsonStructure jsonObject = Json.createReader(s).read();
        return JsonUtils.deserialize(jsonObject, t);
    }

    public static <T> T loadJson(String jsonText, Class<T> cls) throws IOException {
        try {
            if (jsonText == null) {
                jsonText = "";
            }
            Reader reader = null;
            try {
                reader = new StringReader(jsonText);
                return (T) JsonUtils.deserialize(Json.createReader(reader).read(), cls);
            } finally {
                if (reader != null) {
                    reader.close();
                }
            }
        } catch (Exception ex) {
            throw new IOException("Error Parsing file " + jsonText, ex);
        }
    }

    public static JsonStructure loadJsonStructure(String jsonText) throws IOException {
        try {
            if (jsonText == null) {
                jsonText = "";
            }
            Reader reader = null;
            try {
                reader = new StringReader(jsonText);
                return Json.createReader(reader).read();
            } finally {
                if (reader != null) {
                    reader.close();
                }
            }
        } catch (Exception ex) {
            throw new IOException("Error Parsing string " + jsonText, ex);
        }
    }

    public static void readJsonPartialString(String str, JsonStatus s) {
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (s.openSimpleQuotes) {
                if (s.openAntiSlash) {
                    s.openAntiSlash = false;
                } else if (c == '\'') {
                    s.openSimpleQuotes = false;
                }
            } else if (s.openDoubleQuotes) {
                if (s.openAntiSlash) {
                    s.openAntiSlash = false;
                } else if (c == '\"') {
                    s.openDoubleQuotes = false;
                }
            } else if (s.openAntiSlash) {
                s.openAntiSlash = false;
            } else {
                switch (c) {
                    case '\\': {
                        s.openAntiSlash = true;
                        break;
                    }
                    case '\'': {
                        s.openSimpleQuotes = true;
                        break;
                    }
                    case '\"': {
                        s.openDoubleQuotes = true;
                        break;
                    }
                    case '{': {
                        s.openBraces++;
                        s.countBraces++;
                        break;
                    }
                    case '}': {
                        s.openBraces--;
                        break;
                    }
                    case '[': {
                        s.openBrackets++;
                        break;
                    }
                    case ']': {
                        s.openBrackets--;
                        break;
                    }
                }
            }
        }
    }
}
