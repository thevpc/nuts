/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
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
package net.thevpc.nuts.lib.doc.javadoc.util;

import com.github.javaparser.javadoc.description.JavadocDescriptionElement;
import com.github.javaparser.javadoc.description.JavadocInlineTag;
import com.github.javaparser.javadoc.description.JavadocSnippet;
import net.thevpc.nuts.lib.md.*;

import java.util.*;

/**
 * @author thevpc
 */
public class DocReader {
    private static Set<String> unclosableTags=new HashSet<>(Arrays.asList("p","img","br","hr"));

    private List<Object> all = new ArrayList<>();

    public DocReader add(JavadocDescriptionElement e) {
        if (e instanceof JavadocSnippet) {
            all.add(new CharReader(e.toText()));
        } else if (e instanceof JavadocInlineTag) {
            JavadocInlineTag ee = (JavadocInlineTag) e;
            switch (ee.getType()) {
                case CODE: {
                    all.add(MdFactory.codeBacktick3(ee.getName(), ee.getContent(),true));
                    break;
                }
                case LINK: {
                    all.add(new MdCodeLink(ee.getName(), ee.getContent()));
                    break;
                }
                case LINKPLAIN: {
                    all.add(new MdCodeLink(ee.getName(), ee.getContent()));
                    break;
                }
                case LITERAL: {
                    all.add(MdFactory.codeBacktick3(ee.getName(), ee.getContent(),true));
                    break;
                }
                case VALUE: {
                    all.add(new MdCodeLink(ee.getName(), ee.getContent()));
                    break;
                }
                case SYSTEM_PROPERTY: {
                    all.add(MdFactory.codeBacktick3(ee.getName(), ee.getContent(),true));
                    break;
                }
                case DOC_ROOT: {
                    //ignore
                    break;
                }
                default: {
                    throw new IllegalArgumentException("Unsupported");
                }
            }
        }
        return this;
    }

    public boolean isEmpty() {
        if (all.isEmpty()) {
            return true;
        }
//        Object a = all.get(0);
//        if (a instanceof CharReader) {
//            return ((CharReader) a).isEmpty();
//        }
        return false;
    }

    public MdElement parse() {
        List<MdElement> result = new ArrayList<MdElement>();
        while (!isEmpty()) {
            MdElement a = readAny();
            if (a == null) {
                System.err.println("Unable to read");
                break;
            } else {
                result.add(a);
            }
        }
        if (result.size() == 0) {
            return MdText.empty();
        }
        if (result.size() == 1) {
            return result.get(0);
        }
        return MdFactory.ofListOrEmpty( result.toArray(new MdElement[0]));
    }

    private MdElement readAny() {
        if (isEmpty()) {
            return null;
        }
        List<MdElement> result = new ArrayList<>();
        while (!isEmpty()) {
            if (isCurrTag()) {
                result.add(currTag());
                this.all.remove(0);
            } else {
                CharReader in = currText();
                if (in.isEmpty()) {
                    this.all.remove(0);
                } else {
                    if (in.peek("</")) {
                        break;
                    } else if (in.peek("<")) {
                        String[] na = readHtmlTagStart();
                        if (na[1].endsWith("/>") || isNoClosingTag(na[0])) {
                            result.add(prepareXml(new MdXml(MdXml.XmlTagType.OPEN, na[0], (Map)null, null)));
                        } else {
                            MdElement content = readAny();
                            if (!isCurrText() || !currText().read("</" + na[0] + ">")) {
                                if(!unclosableTags.contains(na[0])) {
                                    System.err.println("missing " + "</" + na[0] + ">");
                                }
                                //ignore.
                            } else {
                                result.add(prepareXml(new MdXml(MdXml.XmlTagType.OPEN,na[0], (Map)null, content)));
                            }
                        }
                    } else {
                        result.add(readHtmlText());
                    }
                }
            }
        }
        if (result.isEmpty()) {
            return null;
        }
        if (result.size() == 1) {
            return result.get(0);
        }
        return MdFactory.ofListOrEmpty( result.toArray(new MdElement[0]));
    }

    protected MdElement prepareXml(MdXml xml) {
        switch (xml.getTag()) {
            case "p": {
                return MdFactory.seq(
                        new MdBr(),
                        xml.getContent(),
                        new MdBr()
                );
            }
            case "strong": {
                return new MdBold(xml.getContent());
            }
            case "i": {
                return new MdItalic(xml.getContent());
            }
            case "pre": {
                return MdFactory.codeBacktick3("", xml.getContent().toString(),false);
            }
            case "code": {
                return MdFactory.codeBacktick3("", xml.getContent().toString(),false);
            }
            case "tt": {
                return MdFactory.codeBacktick3("", xml.getContent().toString(),true);
            }
            case "br": {
                return new MdBr();
            }
            case "hr": {
                return new MdHr();
            }
            case "ul": {
                List<MdElement> items = new ArrayList<>();
                items.add(new MdBr());
                for (MdElement a : MdFactory.toArray(xml.getContent())) {
                    if (MdFactory.isBlank(a)) {
                        //ignore
                    } else if (MdFactory.isXmlTag(a, "li")) {
                        items.add(((MdXml) a).getContent());
                    } else {
                        items.add(a);
                    }
                }
                return MdFactory.seq(items);
            }
            case "ol": {
                List<MdElement> items = new ArrayList<>();
                items.add(new MdBr());
                for (MdElement a : MdFactory.toArray(xml.getContent())) {
                    if (MdFactory.isBlank(a)) {
                        //ignore
                    } else if (MdFactory.isXmlTag(a, "li")) {
                        items.add(((MdXml) a).getContent());
                    } else {
                        items.add(a);
                    }
                }
                return MdFactory.seq(items);
            }
            default: {
                return xml;
//                throw new IllegalArgumentException("Unsupported xml tag <" + xml.getTag() + ">");
            }
        }

    }

    private boolean isNoClosingTag(String s) {
        return s.equals("br") || s.equals("hr");
    }

    private MdElement readHtmlText() {
        CharReader in = currText();
        StringBuilder s = new StringBuilder();
        while (!in.isEmpty() && in.peek() != '<') {
            s.append(in.read());
        }
        return MdText.phrase(s.toString());
    }

    private String[] readHtmlTagStart() {
        StringBuilder sb = new StringBuilder();
        CharReader in = currText();
        char e = in.read();
        boolean acceptName = true;
        sb.append(e);
        StringBuilder n = new StringBuilder();
        while (!in.isEmpty()) {
            e = in.read();
            sb.append(e);
            if (e == '>') {
                return new String[]{n.toString(), sb.toString()};
            } else if (e == ' ') {
                acceptName = false;
            } else {
                if (acceptName) {
                    n.append(e);
                }
            }
        }
        return new String[]{n.toString(), sb.toString()};
    }

    public boolean isCurrTag() {
        return all.size() > 0 && all.get(0) instanceof MdElement;
    }

    public boolean isCurrText() {
        return all.size() > 0 && all.get(0) instanceof CharReader;
    }

    public CharReader currText() {
        return (CharReader) this.all.get(0);
    }

    public MdElement currTag() {
        return (MdElement) this.all.get(0);
    }

}
