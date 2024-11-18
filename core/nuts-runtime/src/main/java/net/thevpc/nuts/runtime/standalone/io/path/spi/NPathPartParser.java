package net.thevpc.nuts.runtime.standalone.io.path.spi;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class NPathPartParser {
    public static NPathPartList parseParts(String ttt) {
        PushbackReader r = new PushbackReader(new StringReader(ttt == null ? "" : ttt));
        List<NPathPart> all = new ArrayList<>();
        try {
            while (true) {
                StringBuilder prefix = new StringBuilder();
                StringBuilder value = new StringBuilder();
                while (true) {
                    int c = 0;
                    c = r.read();
                    if (c < 0) {
                        break;
                    }
                    if (c == '/' || c == '\\') {
                        prefix.append((char) c);
                    } else {
                        value.append((char) c);
                        while (true) {
                            c = r.read();
                            if (c < 0) {
                                break;
                            } else if (c == '/' || c == '\\') {
                                r.unread(c);
                                break;
                            } else {
                                value.append((char) c);
                            }
                        }
                        break;
                    }
                }
                if (prefix.length() > 0 || value.length() > 0) {
                    all.add(new NPathPart(prefix.toString(), value.toString()));
                } else {
                    break;
                }
            }
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
        return new NPathPartList(all);
    }
}
