package net.thevpc.nuts.core.test;

import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.runtime.standalone.bridges.maven.LuceneIndexImporter;
import net.thevpc.nuts.runtime.standalone.index.ArtifactsIndexDB;
import net.thevpc.nuts.runtime.standalone.repos.WebHtmlListParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

public class Test26_RepoListParserTest {
    static String spring_repo = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 3.2 Final//EN\">\n" +
            "<html>\n" +
            "<head><title>NanoDBIndexDefinition of release/</title>\n" +
            "</head>\n" +
            "<body>\n" +
            "<h1>NanoDBIndexDefinition of release/</h1>\n" +
            "<pre>Name                       Last modified      Size</pre><hr/>\n" +
            "<pre><a href=\"com/\">com/</a>                        23-Jul-2013 15:07    -\n" +
            "<a href=\"io/\">io/</a>                         12-Jun-2014 16:26    -\n" +
            "<a href=\"org/\">org/</a>                        05-May-2013 13:51    -\n" +
            "<a href=\"samples/\">samples/</a>                    07-Jan-2015 21:08    -\n" +
            "<a href=\"spring-session-build/\">spring-session-build/</a>       07-Jan-2015 21:08    -\n" +
            "<a href=\"archetype-catalog.xml\">archetype-catalog.xml</a>       13-May-2014 11:39  8.07 KB\n" +
            "<a href=\"archetype-catalog.xml.asc\">archetype-catalog.xml.asc</a>   13-May-2014 11:39  183 bytes\n" +
            "<a href=\"build.gradle\">build.gradle</a>                13-Nov-2017 16:18  1.08 KB\n" +
            "</pre>\n" +
            "<hr/><address style=\"font-size:small;\">Artifactory Online Server at repo.spring.io Port 443</address></body></html>";

    static String tomcat_repo = "<html>\n" +
            "<head>\n" +
            "<title>Directory Listing For /net/thevpc/</title>\n" +
            "<STYLE><!--h1 {font-family:Tahoma,Arial,sans-serif;color:white;background-color:#525D76;font-size:22px;} h2 {font-family:Tahoma,Arial,sans-serif;color:white;background-color:#525D76;font-size:16px;} h3 {font-family:Tahoma,Arial,sans-serif;color:white;background-color:#525D76;font-size:14px;} body {font-family:Tahoma,Arial,sans-serif;color:black;background-color:white;} b {font-family:Tahoma,Arial,sans-serif;color:white;background-color:#525D76;} p {font-family:Tahoma,Arial,sans-serif;background:white;color:black;font-size:12px;} a {color:black;} a.name {color:black;} .line {height:1px;background-color:#525D76;border:none;}--></STYLE> </head>\n" +
            "<body><h1>Directory Listing For /net/thevpc/ - <a href=\"/maven/net/\"><b>Up To /net</b></a></h1><HR size=\"1\" noshade=\"noshade\"><table width=\"100%\" cellspacing=\"0\" cellpadding=\"5\" align=\"center\">\n" +
            "<tr>\n" +
            "<td align=\"left\"><font size=\"+1\"><strong>Filename</strong></font></td>\n" +
            "<td align=\"center\"><font size=\"+1\"><strong>Size</strong></font></td>\n" +
            "<td align=\"right\"><font size=\"+1\"><strong>Last Modified</strong></font></td>\n" +
            "</tr><tr>\n" +
            "<td align=\"left\">&nbsp;&nbsp;\n" +
            "<a href=\"/maven/net/thevpc/jshell/\"><tt>jshell/</tt></a></td>\n" +
            "<td align=\"right\"><tt>&nbsp;</tt></td>\n" +
            "<td align=\"right\"><tt>Sat, 09 Jan 2021 12:08:48 GMT</tt></td>\n" +
            "</tr>\n" +
            "<tr bgcolor=\"#eeeeee\">\n" +
            "<td align=\"left\">&nbsp;&nbsp;\n" +
            "<a href=\"/maven/net/thevpc/app/\"><tt>app/</tt></a></td>\n" +
            "<td align=\"right\"><tt>&nbsp;</tt></td>\n" +
            "<td align=\"right\"><tt>Sat, 09 Jan 2021 12:08:11 GMT</tt></td>\n" +
            "</tr>\n" +
            "<tr>\n" +
            "<td align=\"left\">&nbsp;&nbsp;\n" +
            "<a href=\"/maven/net/thevpc/scholar/\"><tt>scholar/</tt></a></td>\n" +
            "<td align=\"right\"><tt>&nbsp;</tt></td>\n" +
            "<td align=\"right\"><tt>Sat, 09 Jan 2021 12:31:51 GMT</tt></td>\n" +
            "</tr>\n" +
            "<tr bgcolor=\"#eeeeee\">\n" +
            "<td align=\"left\">&nbsp;&nbsp;\n" +
            "<a href=\"/maven/net/thevpc/nuts/\"><tt>nuts/</tt></a></td>\n" +
            "<td align=\"right\"><tt>&nbsp;</tt></td>\n" +
            "<td align=\"right\"><tt>Sat, 09 Jan 2021 12:08:23 GMT</tt></td>\n" +
            "</tr>\n" +
            "<tr>\n" +
            "<td align=\"left\">&nbsp;&nbsp;\n" +
            "<a href=\"/maven/net/thevpc/common/\"><tt>common/</tt></a></td>\n" +
            "<td align=\"right\"><tt>&nbsp;</tt></td>\n" +
            "<td align=\"right\"><tt>Sat, 09 Jan 2021 12:18:20 GMT</tt></td>\n" +
            "</tr>\n" +
            "<tr bgcolor=\"#eeeeee\">\n" +
            "<td align=\"left\">&nbsp;&nbsp;\n" +
            "<a href=\"/maven/net/thevpc/maven/\"><tt>maven/</tt></a></td>\n" +
            "<td align=\"right\"><tt>&nbsp;</tt></td>\n" +
            "<td align=\"right\"><tt>Sat, 09 Jan 2021 12:08:10 GMT</tt></td>\n" +
            "</tr>\n" +
            "<tr>\n" +
            "<td align=\"left\">&nbsp;&nbsp;\n" +
            "<a href=\"/maven/net/thevpc/netbeans-launcher/\"><tt>netbeans-launcher/</tt></a></td>\n" +
            "<td align=\"right\"><tt>&nbsp;</tt></td>\n" +
            "<td align=\"right\"><tt>Sat, 09 Jan 2021 12:26:35 GMT</tt></td>\n" +
            "</tr>\n" +
            "<tr bgcolor=\"#eeeeee\">\n" +
            "<td align=\"left\">&nbsp;&nbsp;\n" +
            "<a href=\"/maven/net/thevpc/upa/\"><tt>upa/</tt></a></td>\n" +
            "<td align=\"right\"><tt>&nbsp;</tt></td>\n" +
            "<td align=\"right\"><tt>Sat, 09 Jan 2021 12:08:18 GMT</tt></td>\n" +
            "</tr>\n" +
            "<tr>\n" +
            "<td align=\"left\">&nbsp;&nbsp;\n" +
            "<a href=\"/maven/net/thevpc/tson/\"><tt>tson/</tt></a></td>\n" +
            "<td align=\"right\"><tt>&nbsp;</tt></td>\n" +
            "<td align=\"right\"><tt>Sat, 09 Jan 2021 12:08:53 GMT</tt></td>\n" +
            "</tr>\n" +
            "<tr bgcolor=\"#eeeeee\">\n" +
            "<td align=\"left\">&nbsp;&nbsp;\n" +
            "<a href=\"/maven/net/thevpc/jeep/\"><tt>jeep/</tt></a></td>\n" +
            "<td align=\"right\"><tt>&nbsp;</tt></td>\n" +
            "<td align=\"right\"><tt>Sat, 09 Jan 2021 12:08:10 GMT</tt></td>\n" +
            "</tr>\n" +
            "<tr>\n" +
            "<td align=\"left\">&nbsp;&nbsp;\n" +
            "<a href=\"/maven/net/thevpc/hl/\"><tt>hl/</tt></a></td>\n" +
            "<td align=\"right\"><tt>&nbsp;</tt></td>\n" +
            "<td align=\"right\"><tt>Sat, 09 Jan 2021 12:32:37 GMT</tt></td>\n" +
            "</tr>\n" +
            "</table>\n" +
            "<HR size=\"1\" noshade=\"noshade\"><h3>Apache Tomcat/8.5.14</h3></body>\n" +
            "</html>\n";

    static String central_repo = "<!DOCTYPE html>\n" +
            "<html>\n" +
            "\n" +
            "<head>\n" +
            "\t<title>Central Repository: </title>\n" +
            "\t<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
            "\t<style>\n" +
            "body {\n" +
            "\tbackground: #fff;\n" +
            "}\n" +
            "\t</style>\n" +
            "</head>\n" +
            "\n" +
            "<body>\n" +
            "\t<header>\n" +
            "\t\t<h1></h1>\n" +
            "\t</header>\n" +
            "\t<hr/>\n" +
            "\t<main>\n" +
            "\t\t<pre id=\"contents\">\n" +
            "<a href=\"../\">../</a>\n" +
            "<a href=\"HTTPClient/\" title=\"HTTPClient/\">HTTPClient/</a>                                                      -         -      \n" +
            "<a href=\"abbot/\" title=\"abbot/\">abbot/</a>                                                           -         -      \n" +
            "<a href=\"academy/\" title=\"academy/\">academy/</a>                                                         -         -      \n" +
            "<a href=\"acegisecurity/\" title=\"acegisecurity/\">acegisecurity/</a>                                                   -         -      \n" +
            "<a href=\"activation/\" title=\"activation/\">activation/</a>                                                      -         -      \n" +
            "<a href=\"activecluster/\" title=\"activecluster/\">activecluster/</a>                                                   -         -      \n" +
            "<a href=\"activeio/\" title=\"activeio/\">activeio/</a>                                                        -         -      \n" +
            "<a href=\"activemq/\" title=\"activemq/\">activemq/</a>                                                        -         -      \n" +
            "<a href=\"activemq-jaxb/\" title=\"activemq-jaxb/\">activemq-jaxb/</a>                                                   -         -      \n" +
            "<a href=\"activesoap/\" title=\"activesoap/\">activesoap/</a>                                                      -         -      \n" +
            "<a href=\"activespace/\" title=\"activespace/\">activespace/</a>                                                     -         -      \n" +
            "<a href=\"adarwin/\" title=\"adarwin/\">adarwin/</a>                                                         -         -      \n" +
            "<a href=\"xsddoc/\" title=\"xsddoc/\">xsddoc/</a>                                                          -         -      \n" +
            "<a href=\"xsdlib/\" title=\"xsdlib/\">xsdlib/</a>                                                          -         -      \n" +
            "<a href=\"xstream/\" title=\"xstream/\">xstream/</a>                                                         -         -      \n" +
            "<a href=\"xtc/\" title=\"xtc/\">xtc/</a>                                                             -         -      \n" +
            "<a href=\"xtiff-jai/\" title=\"xtiff-jai/\">xtiff-jai/</a>                                                       -         -      \n" +
            "<a href=\"xxl/\" title=\"xxl/\">xxl/</a>                                                             -         -      \n" +
            "<a href=\"xyz/\" title=\"xyz/\">xyz/</a>                                                             -         -      \n" +
            "<a href=\"yan/\" title=\"yan/\">yan/</a>                                                             -         -      \n" +
            "<a href=\"ymsg/\" title=\"ymsg/\">ymsg/</a>                                                            -         -      \n" +
            "<a href=\"yom/\" title=\"yom/\">yom/</a>                                                             -         -      \n" +
            "<a href=\"za/\" title=\"za/\">za/</a>                                                              -         -      \n" +
            "<a href=\"zone/\" title=\"zone/\">zone/</a>                                                            -         -      \n" +
            "<a href=\"zw/\" title=\"zw/\">zw/</a>                                                              -         -      \n" +
            "<a href=\"archetype-catalog.xml\" title=\"archetype-catalog.xml\">archetype-catalog.xml</a>                             2021-01-04 21:16  10505056      \n" +
            "<a href=\"archetype-catalog.xml.md5\" title=\"archetype-catalog.xml.md5\">archetype-catalog.xml.md5</a>                         2021-01-04 21:16        32      \n" +
            "<a href=\"archetype-catalog.xml.sha1\" title=\"archetype-catalog.xml.sha1\">archetype-catalog.xml.sha1</a>                        2021-01-04 21:16        40      \n" +
            "<a href=\"deploy-output.txt\" title=\"deploy-output.txt\">deploy-output.txt</a>                                 2018-09-06 20:36       853      \n" +
            "<a href=\"last_updated.txt\" title=\"last_updated.txt\">last_updated.txt</a>                                  2021-01-07 11:59        29      \n" +
            "<a href=\"robots.txt\" title=\"robots.txt\">robots.txt</a>                                        2016-01-01 19:20        26      \n" +
            "\t\t</pre>\n" +
            "\t</main>\n" +
            "\t<hr/>\n" +
            "</body>\n" +
            "\n" +
            "</html>";

    @Test
    public void testSpringRepo() {
        try (InputStream in = new ByteArrayInputStream(spring_repo.getBytes())) {
            List<String> parse = new WebHtmlListParser().parse(in);
            Assertions.assertNotNull(parse);
            for (String s : parse) {
                System.out.println(s);
            }
        } catch (Exception ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    @Test
    public void testCentralRepo() {
        try (InputStream in = new ByteArrayInputStream(central_repo.getBytes())) {
            List<String> parse = new WebHtmlListParser().parse(in);
            Assertions.assertNotNull(parse);
            for (String s : parse) {
                System.out.println(s);
            }
        } catch (Exception ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    @Test
    public void testTomcatRepo() {
        try (InputStream in = new ByteArrayInputStream(tomcat_repo.getBytes())) {
            List<String> parse = new WebHtmlListParser().parse(in);
            Assertions.assertNotNull(parse);
            for (String s : parse) {
                System.out.println(s);
            }
        } catch (Exception ex) {
            throw new IllegalArgumentException(ex);
        }
    }
}
