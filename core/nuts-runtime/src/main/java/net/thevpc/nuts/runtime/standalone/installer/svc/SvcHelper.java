package net.thevpc.nuts.runtime.standalone.installer.svc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class SvcHelper {

    static List<String> splitLines(String cmd) {
        ArrayList<String> lines = new ArrayList<>();
        if (cmd == null || cmd.isEmpty()) {
            return lines;
        }
        BufferedReader br2 = new BufferedReader(new StringReader(cmd));
        String line2 = null;
        while (true) {
            try {
                if (!((line2 = br2.readLine()) != null)) break;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            lines.add(line2);
        }
        return lines;
    }

    static String[] splitKeyValue(String item) {
        int i = item.indexOf('=');
        if (i >= 0) {
            return new String[]{
                    item.substring(0, i),
                    item.substring(i + 1),
            };
        } else {
            return new String[]{
                    item,
                    null,
            };
        }
    }

}
