package net.thevpc.nuts.runtime.standalone.io.path.spi.htmlfs;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.util.XmlEscaper;
import net.thevpc.nuts.util.NMsg;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TomcatWebServerHtmlfsParser extends AbstractHtmlfsParser {
    @Override
    public NCallableSupport<List<String>> parseHtmlTomcat(byte[] bytes, NSession session) {
        boolean expectTomcat = false;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(bytes)))) {
            String line = null;
            while ((line = br.readLine()) != null) {
                if (line.contains("<hr class=\"line\"><h3>Apache Tomcat")) {
                    expectTomcat = true;
                }
            }
        } catch (Exception e) {
            //ignore
        }
        if (!expectTomcat) {
            Function<NSession, NMsg> msg = s -> NMsg.ofInvalidValue("tomcat repo");
            return NCallableSupport.invalid(msg);
        }
        //<a href="/maven/net/"><tt>net/</tt></a></td>
        Pattern pattern = Pattern.compile("<a href=\"(?<href>[^\"]+)\"><tt>(?<title>[^<]+)</tt></a></td>");
        List<String> found = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(bytes)))) {
            String line = null;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                Matcher m = pattern.matcher(line);
                if (m.find()) {
                    found.add(XmlEscaper.escapeToUnicode(m.group("title"),session));
                }
            }
        } catch (Exception e) {
            //ignore
        }
        return toSupported(5,found);
    }
}
