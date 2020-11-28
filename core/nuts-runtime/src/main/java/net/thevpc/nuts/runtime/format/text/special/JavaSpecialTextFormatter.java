package net.thevpc.nuts.runtime.format.text.special;

import net.thevpc.nuts.NutsTextNode;
import net.thevpc.nuts.NutsTextNodeFactory;
import net.thevpc.nuts.NutsTextNodeStyle;
import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.runtime.util.common.StreamTokenizerExt;
import net.thevpc.nuts.runtime.format.text.parser.SpecialTextFormatter;

import java.io.StringReader;
import java.util.*;

public class JavaSpecialTextFormatter implements SpecialTextFormatter {
    private static Set<String> reservedWords = new LinkedHashSet<>(
            Arrays.asList(
                    "abstract", "assert", "boolean", "break", "byte", "case",
                    "catch", "char", "class", "const", "continue", "default",
                    "double", "do", "else", "enum", "extends", "false",
                    "final", "finally", "float", "for", "goto", "if",
                    "implements", "import", "instanceof", "int", "interface", "long",
                    "native", "new", "null", "package", "private", "protected",
                    "public", "return", "short", "static", "strictfp", "super",
                    "switch", "synchronized", "this", "throw", "throws", "transient",
                    "true", "try", "void", "volatile", "while"
            )
    );
    private NutsWorkspace ws;

    public JavaSpecialTextFormatter(NutsWorkspace ws) {
        this.ws = ws;
    }

    @Override
    public NutsTextNode toNode(String text) {
        StreamTokenizerExt st = new StreamTokenizerExt(new StringReader(text));
        List<NutsTextNode> nodes = new ArrayList<>();
        int s;
        boolean wasAnnotationStart=false;
        NutsTextNodeFactory factory = ws.formats().text().factory();
        while ((s = st.nextToken()) != StreamTokenizerExt.TT_EOF) {
            switch (s) {
                case StreamTokenizerExt.TT_INTEGER:
                case StreamTokenizerExt.TT_DOUBLE: {
                    nodes.add(factory.styled(st.image, NutsTextNodeStyle.NUMBER1));
                    wasAnnotationStart=false;
                    break;
                }
                case StreamTokenizerExt.TT_WORD: {
                    if (reservedWords.contains(st.image)) {
                        nodes.add(factory.styled(st.image,NutsTextNodeStyle.KEYWORD1));
                    } else {
                        if(wasAnnotationStart){
                            nodes.add(factory.styled(st.image,NutsTextNodeStyle.OPTION1));
                        }else{
                            nodes.add(factory.plain(st.image));
                        }
                    }
                    wasAnnotationStart=false;
                    break;
                }
                case '\"':
                case '\'': {
                    wasAnnotationStart=false;
                    nodes.add(factory.styled(st.image,NutsTextNodeStyle.STRING1));
                    break;
                }
                case '@':{
                    wasAnnotationStart=true;
                    nodes.add(factory.styled(st.image,NutsTextNodeStyle.OPTION1));
                    break;
                }
                case '{':
                case '}':
                case '[':
                case ']': {
                    wasAnnotationStart=false;
                    nodes.add(factory.styled(st.image,NutsTextNodeStyle.SEPARATOR1));
                    break;
                }
            }
        }
        return factory.list(nodes);
    }
}
