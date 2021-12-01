package net.thevpc.nuts.runtime.standalone.io.path.spi.htmlfs;

import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsSupported;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JettyWebServerHtmlfsParser implements HtmlfsParser {
    @Override
    public NutsSupported<List<String>> parseHtmlTomcat(byte[] bytes, NutsSession session) {
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
                    found.add(HtmlEscaper.escape(m.group("title").trim()).trim());
                }
            }
        } catch (Exception e) {
            //ignore
        }
        return toSupported(found);
    }
    private NutsSupported<List<String>> toSupported(List<String> li){
        if(li==null || li.isEmpty()){
            return NutsSupported.invalid();
        }
        return NutsSupported.of(1,()->li);
    }
}
