package net.thevpc.nuts.toolbox.nsh.bundles;

import java.net.URL;

public class _URLUtils {
    public static String getURLName(URL url) {
        String path = url.getFile();
        String name;
        int index = path.lastIndexOf('/');
        if (index < 0) {
            name = path;
        } else {
            name = path.substring(index + 1);
        }
        index = name.indexOf('?');
        if (index >= 0) {
            name = name.substring(0, index);
        }
        name = name.trim();
        return name;
    }
}
