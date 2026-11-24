package net.thevpc.nuts.runtime.standalone.text.highlighter;

import net.thevpc.nuts.reflect.NScore;
import net.thevpc.nuts.reflect.NScorableContext;

import java.util.Arrays;

public class KotlinCodeHighlighter extends AbstractCodeHighlighter {

    public KotlinCodeHighlighter() {
        super("kotlin");
        keywords.addAll(Arrays.asList(
                "as", "as?", "break", "class", "continue", "do", "else", "false", "for", "fun",
                "if", "in", "!in", "interface", "is", "!is", "null", "object", "package", "return",
                "super", "this", "throw", "true", "try", "typealias", "typeof", "val", "var", "when",
                "while", "by", "catch", "constructor", "delegate", "dynamic", "field", "file", "finally",
                "get", "import", "init", "param", "property", "receiver", "set", "setparam", "where",
                "actual", "abstract", "annotation", "companion", "const", "crossinline", "data",
                "enum", "expect", "external", "final", "infix", "inline", "inner", "internal",
                "lateinit", "noinline", "open", "operator", "out", "override", "private", "protected",
                "public", "reified", "sealed", "suspend", "tailrec", "vararg", "value", "context"
        ));
        types.addAll(Arrays.asList(
                "Any", "AnyRef", "Boolean", "Byte", "Char", "Double", "Float", "Int", "Long", "Nothing",
                "Number", "Short", "String", "Unit", "UByte", "UShort", "UInt", "ULong",
                "Array", "BooleanArray", "ByteArray", "CharArray", "DoubleArray", "FloatArray",
                "IntArray", "LongArray", "ShortArray", "List", "MutableList", "ArrayList",
                "Set", "MutableSet", "HashSet", "LinkedHashSet", "Map", "MutableMap", "HashMap",
                "LinkedHashMap", "Collection", "Iterable", "Iterator", "Sequence", "Pair", "Triple",
                "Result", "Lazy", "Regex", "Exception", "RuntimeException", "Throwable", "Comparable"
        ));
        booleanLiterals.addAll(Arrays.asList("true", "false"));
        nullLiterals.add("null");
        lineComments.add("//");
        blockCommentOpen = "/*";
        blockCommentClose = "*/";
        nestedBlockComments = true;
        stringQuotes.add('\'');
        charLiterals = true;
        tripleQuotes = true;
        interpolation = true;
        annotations = true;
        backtickIdentifiers = true;
    }

    @NScore
    public static int getScore(NScorableContext context) {
        return NCodeHighlighterHelper.score(context,
                "kotlin", "kt", "kts", "text/x-kotlin", "text/kotlin", "application/x-kotlin");
    }
}
