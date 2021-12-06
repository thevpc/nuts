package net.thevpc.nuts.runtime.standalone.io.path.spi.htmlfs;

import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsSupported;
import net.thevpc.nuts.runtime.standalone.io.util.CoreIOUtils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MavenCentralHtmlfsParser extends AbstractHtmlfsParser {
    @Override
    public NutsSupported<List<String>> parseHtmlTomcat(byte[] bytes, NutsSession session) {
        List<String> found = new ArrayList<>();
//        Pattern pattern = Pattern.compile("<tr><td class=\"link\"><a href=\"(?<href>[^\"]+)\" title=\"(?<title>[^\"]+)\">(?<title2>[^<>]+)</a></td><td class=\"size\">(?<size>[^<>]+)</td><td class=\"date\">(?<date>[^<>]+)</td></tr>");
        Pattern pattern = Pattern.compile("<a href=\"(?<href>[^\"]+)\" title=\"(?<title>[^\"]+)\">(?<title2>[^<>]+)</a>.*");
        try (BufferedReader br = CoreIOUtils.bufferedReaderOf(bytes)) {
            String line = null;
            while ((line = br.readLine()) != null) {
                if(line.trim().equals("<pre id=\"contents\">")){
                    break;
                }
            }
            while ((line = br.readLine()) != null) {
                if(line.trim().equals("</pre>")){
                    break;
                }
                Matcher m = pattern.matcher(line);
                if(m.find()){
                    found.add(m.group("href"));
                }
            }
        } catch (Exception e) {
            //ignore
        }
        return toSupported(1,found);
    }
}
