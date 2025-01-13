package net.thevpc.nuts.lib.doc.pages;

import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.lib.md.MdBody;
import net.thevpc.nuts.lib.md.MdElement;
import net.thevpc.nuts.lib.md.PropertiesParser;
import net.thevpc.nuts.lib.md.docusaurus.DocusaurusMdParser;
import net.thevpc.nuts.util.NLiteral;
import net.thevpc.nuts.util.NStringUtils;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.util.*;

public class PageGroupMdLoader {
    public static PageGroup load(NPath path) {
        if (path.isDirectory()) {
            NPath u = path.resolve(".folder-info.md");
            if (u.isRegularFile()) {
                return loadFile(u);
            }
        } else if (path.getName().endsWith(".md")) {
            return loadFile(path);
        }
        return null;
    }

    private static PageGroup loadFile(NPath path) {
        try (Reader r = path.getReader()) {
            DocusaurusMdParser p = new DocusaurusMdParser(r);
            MdElement md = p.parse();
            List<MdElement> props = new ArrayList<>();
            List<MdElement> body = new ArrayList<>();
            PageGroup g = new PageGroup()
                    .setPath(path.toString())
                    .setTitle(path.getName())
                    .setPathName(path.getName())
                    ;
            if (md.isBody()) {
                MdElement[] children = md.asBody().getChildren();
                if (children.length > 0) {
                    if (children[0].isHr("---")) {
                        for (int i = 1; i < children.length; i++) {
                            if (children[i].isHr("---")) {
                                i++;
                                for (; i < children.length; i++) {
                                    body.add(children[i]);
                                }
                                break;
                            }
                            if(children[i].isText()) {
                                String[] text = NStringUtils.trim(children[i].asText().getText()).split("\n");
                                for (String s : text) {
                                    int ii = s.indexOf(':');
                                    if(ii>0){
                                        String name = s.substring(0, ii).trim();
                                        String value = s.substring(ii+1).trim();
                                        switch (name) {
                                            case "id": {
                                                g.setId(value);
                                                break;
                                            }
                                            case "title": {
                                                g.setTitle(value);
                                                break;
                                            }
                                            case "order": {
                                                g.setOrder(NLiteral.of(value).asInt().orElse(0));
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                            props.add(children[i]);
                        }
                    } else {
                        body = new ArrayList<>(Arrays.asList(md.asBody().getChildren()));
                    }
                }
            }
            return g
                    .setMarkdown(new MdBody(body.toArray(new MdElement[0])));
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
