package net.thevpc.nuts.toolbox.docusaurus;

import net.thevpc.nuts.NutsContentType;
import net.thevpc.nuts.NutsElement;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.lib.md.MdElement;
import net.thevpc.nuts.lib.md.docusaurus.DocusaurusMdParser;
import net.thevpc.nuts.lib.md.docusaurus.DocusaurusUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

public class DocusaurusContentFile extends DocusaurusFile{
    private final MdElement tree;
    public static DocusaurusFile ofTreeFile(Reader reader, String partialPath, String location, NutsSession session,boolean loadContent) {
//        int from = root.getNameCount();
//        int to = path.getNameCount() - 1;
//        String partialPath = from == to ? "" : path.subpath(from, to).toString();
        try {
            BufferedReader br = (reader instanceof BufferedReader)?(BufferedReader) reader:new BufferedReader(reader);
            int MAX_LINE_SIZE = 4096;
            br.mark(MAX_LINE_SIZE);
            String line1 = br.readLine();
            if ("---".trim().equals(line1)) {
                Map<String, String> props = new HashMap<>();
                NutsElement config=null;
                while (true) {
                    line1 = br.readLine();
                    if (line1 == null) {
                        break;
                    }
                    if (line1.trim().equals("---")) {
                        while(true){
                            br.mark(MAX_LINE_SIZE);
                            line1 = br.readLine();
                            if(line1==null){
                                break;
                            }
                            if (line1.trim().startsWith("import ")) {
                                //continue;
                            }else{
                                br.reset();
                                break;
                            }
                        }
                        break;
                    }else if (line1.matches("[a-z_]+:.*")) {
                        int colon = line1.indexOf(':');
                        String value = line1.substring(colon + 1).trim();
                        String key = line1.substring(0, colon).trim();
                        props.put(key, value);
                        if("type".equals(key) && value.length()>0){
                            try {
                                config = session.getWorkspace().elem().setContentType(NutsContentType.JSON).parse(value);
                            }catch (Exception ex){
                                throw new IllegalArgumentException("invalid json for type in "+location);
                            }
                        }
                    }
                }
                String id = props.get("id");
                Integer menu_order = DocusaurusUtils.parseInt(props.get("order"));
                if (menu_order != null) {
                    if (menu_order.intValue() <= 0) {
                        throw new IllegalArgumentException("invalid order in " + location);
                    }
                } else {
                    menu_order = 0;
                }
                MdElement content=null;
                if(loadContent) {
                    DocusaurusMdParser p = new DocusaurusMdParser(reader);
                    content = p.parse();
                }
                return new DocusaurusContentFile(
                        id,(partialPath == null || partialPath.isEmpty()) ? id : (partialPath + "/" + id),
                        props.get("title"),
                        content,menu_order,config
                );
            }else{
                br.reset();
                MdElement content=null;
                if(loadContent) {
                    DocusaurusMdParser p = new DocusaurusMdParser(br);
                    content = p.parse();
                }
                return new DocusaurusContentFile(
                        null,(partialPath == null || partialPath.isEmpty()) ? "*" : (partialPath + "/" + "*"),
                        null,
                        content,0,null
                );
            }
        } catch (IOException iOException) {
            //
        }
        return null;
    }

    public DocusaurusContentFile(String shortId, String longId, String title, MdElement tree, int order, NutsElement config) {
        super(shortId, longId, title, order, config);
        this.tree=tree;
    }

    public MdElement getContent(NutsSession session) {
        return this.tree;
    }
}
