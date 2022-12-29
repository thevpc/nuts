package net.thevpc.nuts.runtime.standalone.io.path.spi.htmlfs;

import net.thevpc.nuts.NSession;
import net.thevpc.nuts.NSupported;
import net.thevpc.nuts.runtime.standalone.util.XmlEscaper;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JettyWebServerHtmlfsParser extends AbstractHtmlfsParser {
    @Override
    public NSupported<List<String>> parseHtmlTomcat(byte[] bytes, NSession session) {
        List<String> found = new ArrayList<>();
        Pattern pattern = Pattern.compile("tr><td class=\"name\"><a href=\"(?<href>[^\"]+)\">(?<title>[^\"]+)</a></td><td class=\"lastmodified\">(?<date>[^\"]+)</td><td class=\"size\">(?<size>[^\"]+)</td></tr>");
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(bytes)))) {
            String line = null;
            while ((line = br.readLine()) != null) {
                if(line.trim().equals("<table class=\"listing\">")){
                    break;
                }
            }
            while ((line = br.readLine()) != null) {
                if(line.trim().equals("</table>")){
                    break;
                }
                Matcher m = pattern.matcher(line);
                if(m.find()){
                    found.add(XmlEscaper.escapeToUnicode(m.group("title").trim(),session).trim());
                }
            }
        } catch (Exception e) {
            //ignore
        }
        return toSupported(1,found);
    }
}
