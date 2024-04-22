package net.thevpc.nuts.build.util;

import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.util.NRef;
import net.thevpc.nuts.util.NStringUtils;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ConfReader {
    public static List<Map.Entry<String,String>> readEntries(NPath conf) {
        NRef<Boolean> wrap = NRef.of(false);
        NRef<StringBuilder> lastValue = NRef.of(new StringBuilder());
        NRef<Integer> lineNumber = NRef.of(0);
        List<Map.Entry<String,String>> result=new ArrayList<>();
        conf.getLines().forEach(line -> {
            lineNumber.set(lineNumber.get() + 1);
            String toProcessLine = null;
            if (wrap.get()) {
                if (line.endsWith("\\")) {
                    lastValue.get().append(line.substring(0, line.length() - 1));
                } else {
                    lastValue.get().append(line);
                    wrap.set(false);
                    toProcessLine = lastValue.get().toString();
                }
            } else {
                String tline = line.trim();
                if (tline.length() > 0 && !tline.startsWith("#")) {
                    if (line.endsWith("\\")) {
                        String timmedStart = NStringUtils.trimLeft(line);
                        String remainingLine = timmedStart.substring(0, timmedStart.length() - 1);
                        if (timmedStart.length() > 0) {
                            lastValue.set(new StringBuilder(remainingLine));
                            wrap.set(true);
                        } else {
                            lastValue.set(new StringBuilder(""));
                            wrap.set(true);
                        }
                    } else {
                        toProcessLine = line.trim();
                    }
                }
            }
            if (toProcessLine != null && toProcessLine.length() > 0) {
                int eq = toProcessLine.indexOf("=");
                if (eq >= 0) {
                    String k = toProcessLine.substring(0, eq).trim();
                    String v = toProcessLine.substring(eq + 1);
                    result.add(new AbstractMap.SimpleEntry<>(k,v));
                } else {
                    throw new IllegalArgumentException("[line:" + lineNumber.get() + "] expected key=value : " + toProcessLine);
                }
            }
        });
        return result;
    }
}
