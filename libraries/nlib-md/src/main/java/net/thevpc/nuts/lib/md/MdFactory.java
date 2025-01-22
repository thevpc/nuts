/**
 * ====================================================================
 * thevpc-common-md : Simple Markdown Manipulation Library
 * <br>
 * <p>
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
package net.thevpc.nuts.lib.md;

import net.thevpc.nuts.lib.md.base.DefaultMdProvider;
import net.thevpc.nuts.util.NRef;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author thevpc
 */
public class MdFactory {
    public static final String MIMETYPE_DOCUSAURUS = "text/markdown-docusaurus";
    public static final String MIMETYPE_ASCIIDOCTOR = "text/markdown-asciidoctor";
    public static final String MIMETYPE_DEFAULT = "text/markdown";

    private static boolean loaded = false;
    private static Map<String, MdProvider> providers = new HashMap<>();

    private MdFactory() {

    }

    public static MdElement[] toArray(MdElement e) {
        if (e == null) {
            return new MdElement[0];
        }
        if (e instanceof MdParent) {
            return ((MdParent) e).getChildren();
        }
        return new MdElement[]{e};
    }

    public static MdParser createParser(String mimeType, InputStream inputStream) {
        return createParser(mimeType,new InputStreamReader(inputStream));
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


    public static MdElement seq(Collection<MdElement> arr) {
        return seq(false, arr);
    }

    public static MdElement seqInline(Collection<MdElement> arr) {
        return seq(true, arr);
    }


    public static MdElement codeBacktick1(String lang, String code) {
        return new MdCode("`", lang, code, code.indexOf('\n') < 0);
    }

    public static MdElement codeBacktick3(String lang, String code) {
        return new MdCode("```", lang, code, code.indexOf('\n') < 0);
    }

    public static MdElement codeBacktick3Paragraph(String lang, String code) {
        return new MdCode("```", lang, code, false);
    }

    public static MdElement codeBacktick3(String lang, String code, boolean inline) {
        return new MdCode("```", lang, code, inline);
    }

    public static MdElement title(int depth, String e) {
        return new MdTitle("", MdText.phrase(e), depth, new MdElement[0]);
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
        return ofListOrEmpty(all.toArray(new MdElement[0]));
    }

    public static MdBody asBody(MdElement a) {
        if (a == null) {
            return new MdBody(new MdElement[0]);
        }
        if (a instanceof MdBody) {
            return (MdBody) a;
        }
        return new MdBody(new MdElement[]{a});
    }


    public static MdElement unwrapSeq(MdElement a) {
        if (a == null) {
            return null;
        }
        if (a instanceof MdParent) {
            MdElement[] elements = ((MdParent) a).getChildren();
            if (elements.length == 0) {
                return MdText.empty();
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
            mimeType = MIMETYPE_DEFAULT;
        } else if (mimeType.equals("markdown")) {
            mimeType = MIMETYPE_DEFAULT;
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
                    providers.put(MIMETYPE_DEFAULT, new DefaultMdProvider());
                    ServiceLoader<MdProvider> serviceLoader = ServiceLoader.load(MdProvider.class);
                    for (MdProvider mdProvider : serviceLoader) {
                        providers.put(mdProvider.getMimeType(), mdProvider);
                    }
                }
            }
        }
        return providers.get(mimeType);
    }

    public static boolean isBlank(MdElement[] e) {
        for (MdElement m : e) {
            if (!isBlank(m)) {
                return false;
            }
        }
        return true;
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
        if (e instanceof MdTitle) {
            MdTitle ti = (MdTitle) e;
            return isBlank(ti.getValue())
                    && isBlank(ti.getChildren());
        }
        if (e instanceof MdParent) {
            MdParent li = (MdParent) e;
            return isBlank(li.getChildren());
        }
        return false;
    }

    public static MdElement unpack(MdElement e) {
        if (e == null) {
            return null;
        }
        if (e instanceof MdParent) {
            MdParent li = (MdParent) e;
            MdElement[] t = li.getChildren();
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
        return MdText.phrase(s);
    }

    public static MdElement newLine() {
        return MdText.paragraph("");
    }

    public static MdElement endParagraph() {
        return MdText.paragraph("");
    }

    public static MdElement ul(int depth, MdElement elem) {
        return new MdUnNumberedItem("", depth, elem, new MdElement[0]);
    }

    public static MdElement ol(int number, int depth, MdElement elem) {
        return new MdNumberedItem(number, depth, ".", elem, new MdElement[0]);
    }

    public static MdDocumentBuilder document() {
        return new MdDocumentBuilder();
    }

    public static MdTableBuilder table() {
        return new MdTableBuilder();
    }


    public static MdElementBuilder element(MdElement e) {
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

    public static boolean detectXml(MdElement[] content) {
        for (MdElement mdElement : content) {
            if (mdElement.isXml()) {
                return true;
            }
        }
        return false;
    }


    public static MdElement ofListOrNull(MdElement[] content) {
        return ofList(content, true);
    }

    public static MdElement ofListOrEmpty(MdElement[] content) {
        return ofList(content, false);
    }

    public static MdElement ofList(MdElement[] content, boolean noneIsNull) {
        if (content != null) {
            content = Arrays.stream(content).filter(Objects::nonNull).toArray(MdElement[]::new);
        } else {
            content = new MdElement[0];
        }
        if (content.length == 0) {
            if (noneIsNull) {
                return null;
            }
            return MdText.phrase("");
        }
        if (content.length == 1) {
            return content[0];
        }
        if (detectXml(content)) {
            return new MdBody(content);
        }
        if (MdPhrase.acceptPhrase(content)) {
            List<MdElement> all = new ArrayList<>();
            NRef<MdElement> last = NRef.of(null);
            for (MdElement e : content) {
                processNextPhraseITem(e, last, all);
            }
            if (!last.isNull()) {
                all.add(last.get());
                last.set(null);
            }
            if (all.size() == 1) {
                return all.get(0);
            }
            MdPhrase p = new MdPhrase(all.toArray(new MdElement[0]));
            return p;
        }
        Set<MdElementTypeGroup> ss = Arrays.stream(content).map(x -> x.type().group()).collect(Collectors.toSet());
        if (ss.size() == 1) {
            if (ss.contains(MdElementTypeGroup.UNNUMBERED_LIST)) {
                return new MdUnNumberedList(Arrays.asList(content).toArray(new MdUnNumberedItem[0]));
            }
            if (ss.contains(MdElementTypeGroup.NUMBERED_LIST)) {
                return new MdNumberedList(Arrays.asList(content).toArray(new MdNumberedItem[0]));
            }
        }
        return new MdBody(content);
    }

    private static void processNextPhraseITem(MdElement e, NRef<MdElement> last, List<MdElement> all) {
        if (e instanceof MdPhrase && (last.isNull() || ((MdPhrase) e).isInline())) {
            for (MdElement ee : ((MdPhrase) e).getChildren()) {
                processNextPhraseITem(ee, last, all);
            }
        } else if (last.isNull()) {
            last.set(e);
        } else if (e instanceof MdText && last.get() instanceof MdText && ((MdText) last.get()).isInline() && ((MdText) e).isInline()) {
            last.set(new MdText(
                    ((MdText) last.get()).getText() +
                            ((MdText) e).getText()
                    , true
            ));
        } else  {
            all.add(last.get());
            last.set(e);
        }
    }
}
