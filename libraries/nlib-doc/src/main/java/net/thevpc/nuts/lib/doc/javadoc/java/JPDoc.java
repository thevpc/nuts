/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <br>
 *
 * Copyright [2020] [thevpc]
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License");
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
*/
package net.thevpc.nuts.lib.doc.javadoc.java;

import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.JavadocBlockTag;
import com.github.javaparser.javadoc.description.JavadocDescriptionElement;
import net.thevpc.nuts.lib.md.MdElement;
import net.thevpc.nuts.lib.doc.javadoc.JDDoc;
import net.thevpc.nuts.lib.doc.javadoc.util.DocReader;

/**
 *
 * @author thevpc
 */
public class JPDoc implements JDDoc {

    Javadoc jd;

    public JPDoc(Javadoc jd) {
        this.jd = jd;
    }

    @Override
    public String getTag(String tag) {
        for (JavadocBlockTag blockTag : jd.getBlockTags()) {
            if (blockTag.getTagName().equals(tag)) {
                return blockTag.getContent().toText().trim();
            }
        }
        return null;
    }

    @Override
    public MdElement getDescription() {
        if (jd.getDescription() == null) {
            return null;
        }
        DocReader dr = new DocReader();
        for (JavadocDescriptionElement element : jd.getDescription().getElements()) {
            dr.add(element);
        }
        return dr.parse();
//        return new JPDocElementList(jd.getDescription().getElements().stream().map(x -> _JDDocElement(x))
//                .toArray(JDDocElement[]::new));
    }

//    private static JDDocElement _JDDocElement(JavadocDescriptionElement e) {
//        if (e instanceof JavadocSnippet) {
//            return _JDDocElement(e.toText());
//        }
//        if (e instanceof JavadocInlineTag) {
//            return new JPDocElementTag((JavadocInlineTag) e);
//        }
//        throw new IllegalArgumentException("Unsupported " + e);
//    }
//    public static JDDocElement _JDDocElement(String str) {
//        List<JDDocElement> all = new ArrayList<JDDocElement>();
//        CharReader in = new CharReader(str);
//        while (!in.isEmpty()) {
//            JDDocElement a = readAny(in);
//            if (a == null) {
//                System.err.println("Unable to read");
//                break;
//            } else {
//                all.add(a);
//            }
//        }
//        if (all.size() == 0) {
//            return new JPDocElementString("");
//        }
//        if (all.size() == 1) {
//            return all.get(0);
//        }
//        return new JPDocElementList(all.toArray(new JDDocElement[0]));
//    }



//    private static String readHtmlTagEnd(StringBuilder in, String name) {
//        StringBuilder sb = new StringBuilder();
//        int y = name.length() + 3;
//        String end = "</" + name + ">";
//        while (in.length() > y) {
//            if (in.substring(0, y).equals(end)) {
//                in.delete(0, y);
//                return sb.toString();
//            }
//            sb.append(in.charAt(0));
//            in.delete(0, 1);
//        }
//        while (in.length() > 0) {
//            sb.append(in.charAt(0));
//            in.delete(0, 1);
//        }
//        return sb.toString();
//    }

    /*
    
    for (String line : c.getRawCommentText().split("\n")) {
            line = line.trim();
            if (line.startsWith("%category ")) {
                String cat = line.substring("%category ".length()).trim();
                if (cat.length() > 0) {
                    System.out.println("found " + cat);
                    return cat;
                }
            }
        }
        return "Other";
    
     */
}
