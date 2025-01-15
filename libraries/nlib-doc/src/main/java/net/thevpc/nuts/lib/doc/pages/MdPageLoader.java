package net.thevpc.nuts.lib.doc.pages;

import net.thevpc.nuts.Nuts;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.lib.md.MdBody;
import net.thevpc.nuts.lib.md.MdElement;
import net.thevpc.nuts.lib.md.docusaurus.DocusaurusMdParser;
import net.thevpc.nuts.util.NLiteral;
import net.thevpc.nuts.util.NStringUtils;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.util.*;

public class MdPageLoader {
    public static void main(String[] args) {
        Nuts.openInheritedWorkspace(null, null).setSharedInstance();

        try (Reader r = NPath.of("/home/vpc/xprojects/nuts/nuts-community/documentation/website/src/include/pages/01-intro/.folder-info.md").getReader()) {
            DocusaurusMdParser p = new DocusaurusMdParser(r);
            MdElement md = p.parse();
            System.out.println(md);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public static MdPage load(NPath path) {
        if (path.isDirectory()) {
            NPath u = path.resolve(".folder-info.md");
            if (u.isRegularFile()) {
                MdPage mdPage = loadFile(u);
                mdPage.setPath(path.toString());
                mdPage.setPathName(path.getName());
                return mdPage;
            }
        } else if (path.getName().endsWith(".md") && ! path.getName().endsWith(".folder-info.md")) {
            return loadFile(path);
        }
        return null;
    }

    private static MdPage loadFile(NPath path) {
        try (Reader r = path.getReader()) {
            DocusaurusMdParser p = new DocusaurusMdParser(r);
            MdElement md = p.parse();
            List<MdElement> props = new ArrayList<>();
            List<MdElement> body = new ArrayList<>();
            MdPage g = new MdPage()
                    .setPath(path.toString())
                    .setTitle(path.getName())
                    .setPathName(path.getName());
            Object frontMatter = md.getFrontMatter();
            if (frontMatter instanceof Map) {
                for (Map.Entry<?, ?> fe : ((Map<?, ?>) frontMatter).entrySet()) {
                    if (fe.getKey() instanceof String) {
                        switch ((String) fe.getKey()) {
                            case "id": {
                                g.setId(NLiteral.of(fe.getValue()).asString().orNull());
                                break;
                            }
                            case "title": {
                                g.setTitle(NLiteral.of(fe.getValue()).asString().orNull());
                                break;
                            }
                            case "order": {
                                g.setOrder(NLiteral.of(fe.getValue()).asInt().orElse(0));
                                break;
                            }
                            case "sort": {
                                g.setSortAsc(NLiteral.of(fe.getValue()).asBoolean().orElse(!"desc".equalsIgnoreCase(String.valueOf(fe.getValue()))));
                                break;
                            }
                        }
                    }
                }
            }
            return g.setMarkdown(md);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
