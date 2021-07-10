/**
 * ====================================================================
 *            thevpc-common-md : Simple Markdown Manipulation Library
 * <br>
 *
 * Copyright [2020] [thevpc]
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts.lib.md;

import java.io.Reader;
import java.io.Writer;
import java.util.*;

/**
 * @author thevpc
 */
public class MdFactory {
    private static boolean loaded = false;
    private static Map<String, MdProvider> providers = new HashMap<>();

    private MdFactory() {

    }

    public static MdElement[] toArray(MdElement e) {
        if (e == null) {
            return new MdElement[0];
        }
        if (e instanceof MdSequence) {
            return ((MdSequence) e).getElements();
        }
        return new MdElement[]{e};
    }

    public static MdParser createParser(String mimeType, Reader reader) {
        MdParser p = getProvider(mimeType).createParser(reader);
        if (p == null) {
            throw new NoSuchElementException("no markdown parser for : " + mimeType);
        }
        return p;
    }

    public static MdWriter createWriter(String mimeType, Writer reader) {
        MdWriter w = getProvider(mimeType).createWriter(reader);
        if (w == null) {
            throw new NoSuchElementException("no markdown writer for : " + mimeType);
        }
        return w;
    }

    public static MdSequenceBuilder seq() {
        return new MdSequenceBuilder().setInline(false);
    }


    public static MdElement seq(Collection<MdElement> arr) {
        return seq(false, arr);
    }

    public static MdElement seqInline(Collection<MdElement> arr) {
        return seq(true, arr);
    }

    public static MdElement code(String lang, String code) {
        return new MdCode(lang,code,false);
    }
    public static MdElement codeInline(String code) {
        return new MdCode("",code,true);
    }

    public static MdElement title(int depth, String e) {
        return new MdTitle("", new MdText(e), depth);
    }

    public static MdElement seq(MdElement... arr) {
        return seq(false, arr);
    }

    public static MdElement seqInline(MdElement... arr) {
        return seq(true, arr);
    }

    public static MdElement seq(boolean inline, Collection<MdElement> arr) {
        if (arr == null) {
            return seq(inline, new MdElement[0]);
        }
        return seq(inline, arr.toArray(new MdElement[0]));
    }

    public static MdElement seq(boolean inline, MdElement... arr) {
        List<MdElement> all = new ArrayList<MdElement>();
        if (arr != null) {
            for (MdElement mdElement : arr) {
                if (mdElement != null) {
                    all.add(mdElement);
                }
            }
        }
        if (all.isEmpty()) {
            return new MdText("");
        }
        if (all.size() == 1) {
            return all.get(0);
        }
        return new MdSequence("", all.toArray(new MdElement[0]), inline);
    }

    public static MdSequence asSeq(MdElement a) {
        if (a == null) {
            return new MdSequence("", new MdElement[0], false);
        }
        if (a instanceof MdSequence) {
            return (MdSequence) a;
        }
        return new MdSequence("", new MdElement[]{a}, false);
    }

    public static MdSequence asInlineSeq(MdElement a) {
        if (a == null) {
            return new MdSequence("", new MdElement[0], true);
        }
        if (a instanceof MdSequence) {
            return (MdSequence) a;
        }
        return new MdSequence("", new MdElement[]{a}, true);
    }

    public static MdElement unwrapSeq(MdElement a) {
        if (a == null) {
            return null;
        }
        if (a instanceof MdSequence) {
            MdElement[] elements = ((MdSequence) a).getElements();
            if (elements.length == 0) {
                return new MdText("");
            }
            if (elements.length == 1) {
                return unwrapSeq(elements[0]);
            }
            return a;
        }
        return a;
    }

    public static MdProvider getProvider(String mimeType) {
        MdProvider provider = findProvider(mimeType);
        if (provider == null) {
            throw new NoSuchElementException("no markdown provider for : " + mimeType);
        }
        return provider;
    }

    public static MdProvider findProvider(String mimeType) {
        if (mimeType == null) {
            mimeType = "text/markdown";
        } else if (mimeType.equals("markdown")) {
            mimeType = "text/markdown";
        } else if (mimeType.indexOf('/') < 0) {
            if (mimeType.startsWith("markdown-")) {
                mimeType = "text/" + mimeType;
            } else {
                mimeType = "text/markdown-" + mimeType;
            }
        }
        if (!loaded) {
            synchronized (MdFactory.class) {
                if (!loaded) {
                    providers.put("text/markdown", new DefaultMdProvider());
                    ServiceLoader<MdProvider> serviceLoader = ServiceLoader.load(MdProvider.class);
                    for (MdProvider mdProvider : serviceLoader) {
                        providers.put(mdProvider.getMimeType(), mdProvider);
                    }
                }
            }
        }
        return providers.get(mimeType);
    }

    public static boolean isBlank(MdElement e) {
        e = unpack(e);
        if (e == null) {
            return true;
        }
        if (e instanceof MdText) {
            MdText s = (MdText) e;
            if (s.getText().trim().isEmpty()) {
                return true;
            }
        }
        if (e instanceof MdSequence) {
            MdSequence li = (MdSequence) e;
            MdElement[] t = li.getElements();
            for (MdElement jDDocElement : t) {
                if (!isBlank(jDDocElement)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public static MdElement unpack(MdElement e) {
        if (e == null) {
            return null;
        }
        if (e instanceof MdSequence) {
            MdSequence li = (MdSequence) e;
            MdElement[] t = li.getElements();
            if (t.length == 0) {
                return null;
            }
            if (t.length == 1) {
                return unpack(t[0]);
            }
        }
        return e;
    }

    public static boolean isXmlTag(MdElement e, String tag) {
        e = unpack(e);
        if (e == null) {
            return false;
        }
        if (e instanceof MdXml) {
            MdXml s = (MdXml) e;
            return s.getTag().equals(tag);
        }
        return false;
    }

    public static MdElement br() {
        return new MdBr();
    }

    public static MdElement bold(MdElement e) {
        return new MdBold(e);
    }

    public static MdTableBuilder.MdColumnBuilder column() {
        return new MdTableBuilder.MdColumnBuilder();
    }
    public static MdTableBuilder.MdRowBuilder row() {
        return new MdTableBuilder.MdRowBuilder();
    }
    public static MdElement text(String s) {
        return new MdText(s);
    }

    public static MdElement ul(int depth, MdElement elem) {
        return new MdUnNumberedItem("",depth,elem,new MdElement[0]);
    }
    public static MdElement ol(int number,int depth, MdElement elem) {
        return new MdNumberedItem(number,depth,".",elem,new MdElement[0]);
    }

    public static MdDocumentBuilder document() {
        return new MdDocumentBuilder();
    }

    private static class DefaultMdProvider implements MdProvider {
        @Override
        public String getMimeType() {
            return "text/markdown";
        }

        @Override
        public MdParser createParser(Reader reader) {
            return null;
        }

        @Override
        public MdWriter createWriter(Writer writer) {
            return new DefaultMdWriter(writer);
        }
    }

    public static MdTableBuilder table(){
        return new MdTableBuilder();
    }


    public static MdElementBuilder element(MdElement e){
        return new MdElementAsBuilder(e);
    }

    private static class MdElementAsBuilder implements MdElementBuilder {
        private MdElement e;

        public MdElementAsBuilder(MdElement e) {
            this.e = e;
        }

        @Override
        public MdElement build() {
            return e;
        }
    }
}
