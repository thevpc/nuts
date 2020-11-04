/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.ncode.sources;

import net.thevpc.nuts.toolbox.ncode.Source;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author vpc
 */
public class JavaTypeSource extends SourceAdapter {

    private static final Map<String, String> versions = new HashMap<String, String>();

    static {
        versions.put("45.3", "1.1");
        versions.put("46.0", "1.2");
        versions.put("47.0", "1.3");
        versions.put("48.0", "1.4");
        versions.put("49.0", "5.0");
        versions.put("50.0", "6.0");
        versions.put("51.0", "7.0");
        versions.put("52.0", "8.0");
        versions.put("53.0", "9.0");
        versions.put("54.0", "10.0");
        versions.put("55.0", "11.0");
        versions.put("56.0", "12.0");
        versions.put("57.0", "13.0");
        versions.put("58.0", "14.0");
        versions.put("59.0", "15.0");
    }

    public JavaTypeSource(Source source) {
        super(source);
    }

    public String getClassVersion(boolean convert) {
        String v = "?";
        try (InputStream m = openStream()) {
            return getClassVersion(m, convert);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static String getClassVersion(InputStream filename, boolean convert) throws IOException {
        DataInputStream in = new DataInputStream(filename);

        int magic = in.readInt();
        if (magic != 0xcafebabe) {
            throw new IOException("Not a Java Class file");
        }
        int minor = in.readUnsignedShort();
        int major = in.readUnsignedShort();
        in.close();
        String s = major + "." + minor;
        if (convert) {
            String g = versions.get(s);
            if (g != null) {
                return g;
            }
            if (minor != 0) {
                String gg = versions.get(s);
                if (gg != null) {
                    return gg + "u" + minor;
                }
            }
            return "#" + s;
        } else {
            return s;
        }
    }

    public String getClassName() {
        String ii = getInternalPath();
        return ii.substring(0, ii.length() - ".class".length()).replace("/", ".");
    }

}
