package net.thevpc.nuts.runtime.standalone.io.path.spi.htmlfs;

import net.thevpc.nuts.*;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class TomcatWebServerHtmlfsParser implements HtmlfsParser {
    @Override
    public NutsSupported<List<String>> parseHtmlTomcat(byte[] bytes, NutsSession session) {
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
            return toSupported(null);
        }
        InputStream html = new ByteArrayInputStream(bytes);
        List<String> found = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(html));
            State s = State.EXPECT_DOCTYPE;
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                switch (s) {
                    case EXPECT_DOCTYPE: {
                        if (!line.isEmpty()) {
                            if (line.toLowerCase().startsWith("<!DOCTYPE html".toLowerCase())) {
                                s = State.EXPECT_BODY;
                            } else if (
                                    line.toLowerCase().startsWith("<html>".toLowerCase())
                                            || line.toLowerCase().startsWith("<html ".toLowerCase())
                            ) {
                                s = State.EXPECT_BODY;
                            } else {
                                return toSupported(found);
                            }
                        }
                        break;
                    }
                    case EXPECT_BODY: {
                        if (!line.isEmpty()) {
                            if (
                                    line.toLowerCase()
                                            .startsWith("<body>".toLowerCase())
                                            || line.toLowerCase()
                                            .startsWith("<body ".toLowerCase())
                            ) {
                                s = State.EXPECT_PRE;
                            }
                        }
                        break;
                    }
                    case EXPECT_PRE: {
                        if (!line.isEmpty()) {
                            String lowLine = line;
                            if (
                                    lowLine.toLowerCase()
                                            .startsWith("<pre>".toLowerCase())
                                            || lowLine.toLowerCase()
                                            .startsWith("<pre ".toLowerCase())
                            ) {
                                //spring.io
                                if (lowLine.toLowerCase().startsWith("<pre>") && lowLine.toLowerCase().matches("<pre>name[ ]+last modified[ ]+size</pre>(<hr/>)?")) {
                                    //just ignore
                                } else if (lowLine.toLowerCase().startsWith("<pre>") && lowLine.toLowerCase().matches("<pre>[ ]*<a href=.*")) {
                                    lowLine = lowLine.substring("<pre>".length()).trim();
                                    if (lowLine.toLowerCase().startsWith("<a href=\"")) {
                                        int i0 = "<a href=\"".length();
                                        int i1 = lowLine.indexOf('\"', i0);
                                        if (i1 > 0) {
                                            found.add(lowLine.substring(i0, i1));
                                            s = State.EXPECT_HREF;
                                        } else {
                                            return toSupported(found);
                                        }
                                    }
                                } else if (lowLine.toLowerCase().startsWith("<pre ")) {
                                    s = State.EXPECT_HREF;
                                } else {
                                    //ignore
                                }
                            } else if (lowLine.toLowerCase().matches("<td .*<strong>last modified</strong>.*</td>")) {
                                s = State.EXPECT_HREF;
                            }
                        }
                        break;
                    }
                    case EXPECT_HREF: {
                        if (!line.isEmpty()) {
                            String lowLine = line;
                            if (lowLine.toLowerCase().startsWith("</pre>".toLowerCase())) {
                                return toSupported(found);
                            }
                            if (lowLine.toLowerCase().startsWith("</html>".toLowerCase())) {
                                return toSupported(found);
                            }
                            if (lowLine.toLowerCase().startsWith("<a href=\"")) {
                                int i0 = "<a href=\"".length();
                                int i1 = lowLine.indexOf('\"', i0);
                                if (i1 > 0) {
                                    found.add(lowLine.substring(i0, i1));
                                } else {
                                    //ignore
                                }
                            }
                        }
                        break;
                    }
                }
            }
        } catch (Exception ex) {
            NutsLoggerOp.of(HtmlfsPath.class, session)
                    .verb(NutsLogVerb.FAIL)
                    .level(Level.FINE)
                    .error(ex)
                    .log(NutsMessage.cstyle("unable to parse html"));

            //System.err.println(ex);
            //ignore
        }
        return toSupported(found);
    }

    private enum State {
        EXPECT_DOCTYPE,
        EXPECT_BODY,
        EXPECT_PRE,
        EXPECT_HREF,
    }
    private NutsSupported<List<String>> toSupported(List<String> li) {
        if (li == null || li.isEmpty()) {
            return NutsSupported.invalid();
        }
        return NutsSupported.of(1, () -> li);
    }

}
