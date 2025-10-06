package net.thevpc.nuts.runtime.standalone.io.path.spi.htmlfs;

import net.thevpc.nuts.concurrent.NCallableSupport;
import net.thevpc.nuts.runtime.standalone.util.XmlEscaper;
import net.thevpc.nuts.util.NMsg;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TomcatWebServerHtmlfsParser extends AbstractHtmlfsParser {
    public TomcatWebServerHtmlfsParser() {
        super();
    }

    @Override
    public NCallableSupport<List<String>> parseHtmlTomcat(byte[] bytes) {
        boolean expectDirListing = false;
        boolean expectTomcat = false;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(bytes)))) {
            String line = null;
            while ((line = br.readLine()) != null) {
                String trimmedLine = line.trim();
                if (!expectTomcat) {
                    if(trimmedLine.matches("<hr class=\"line\"><h3>[^<>]+</h3></body>")) {
                        expectTomcat = true;
                    }
                    if(trimmedLine.matches("<hr class=\"line\">")) {
                        expectTomcat = true;
                    }
                    if(trimmedLine.matches("<th [^<>]+Filename[^<>]+</th>")) {
                        expectTomcat = true;
                    }
                }
                if (!expectDirListing && trimmedLine.matches("<title>Directory Listing For.*")) {
                    expectDirListing = true;
                }
            }
        } catch (Exception e) {
            //ignore
        }
        if (!expectTomcat || !expectDirListing) {
            Supplier<NMsg> msg = () -> NMsg.ofInvalidValue("tomcat repo");
            return NCallableSupport.ofInvalid(msg);
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
                    found.add(XmlEscaper.escapeToUnicode(m.group("title")));
                }
            }
        } catch (Exception e) {
            //ignore
        }
        return toSupported(5,found);
    }
}
