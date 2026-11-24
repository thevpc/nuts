package net.thevpc.nuts.runtime.standalone.text.highlighter;

import net.thevpc.nuts.reflect.NScore;
import net.thevpc.nuts.reflect.NScorableContext;
import net.thevpc.nuts.runtime.standalone.xtra.expr.StringReaderExt;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextStyle;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SwiftCodeHighlighter extends AbstractCodeHighlighter {

    public SwiftCodeHighlighter() {
        super("swift");
        keywords.addAll(Arrays.asList(
                "associatedtype", "class", "deinit", "enum", "extension", "fileprivate", "func", "import",
                "init", "inout", "internal", "let", "open", "operator", "private", "precedencegroup",
                "protocol", "public", "rethrows", "static", "struct", "subscript", "typealias", "var",
                "break", "case", "catch", "continue", "default", "defer", "do", "else", "fallthrough",
                "for", "guard", "if", "in", "repeat", "return", "throw", "switch", "where", "while",
                "as", "Any", "await", "false", "is", "nil", "self", "Self", "super", "throws", "true",
                "try", "async", "nonisolated", "some", "any"
        ));
        types.addAll(Arrays.asList(
                "Bool", "Character", "Double", "Float", "Int", "Int8", "Int16", "Int32", "Int64",
                "String", "UInt", "UInt8", "UInt16", "UInt32", "UInt64", "Void",
                "Array", "Dictionary", "Set", "Optional", "Result", "Error", "AnyObject",
                "CGFloat", "CGPoint", "CGSize", "CGRect", "URL", "UUID", "Date", "Data"
        ));
        booleanLiterals.addAll(Arrays.asList("true", "false"));
        nullLiterals.add("nil");
        lineComments.add("//");
        blockCommentOpen = "/*";
        blockCommentClose = "*/";
        nestedBlockComments = true;
        tripleQuotes = true;
        tripleStringsUseEscapes = true;
        interpolation = true;
        annotations = true;
        backtickIdentifiers = true;
        numberSuffixChars = "";
    }

    @NScore
    public static int getScore(NScorableContext context) {
        return NCodeHighlighterHelper.score(context,
                "swift", "swift5", "text/x-swift", "text/swift", "application/x-swift");
    }

    @Override
    protected boolean handleSpecial(StringReaderExt ar, List<NText> all) {
        // compiler directives: #if, #else, #endif, #available, #selector ...
        if (ar.peekChar() == '#' && ar.hasNext(1) && isIdentifierStart(ar.peekChar(1))) {
            StringBuilder sb = new StringBuilder();
            sb.append(ar.readChar());
            while (ar.hasNext() && isIdentifierPart(ar.peekChar())) {
                sb.append(ar.readChar());
            }
            all.add(NText.ofStyled(sb.toString(), NTextStyle.annotation()));
            return true;
        }
        return false;
    }

    @Override
    protected boolean isInterpolationStart(StringReaderExt ar) {
        return interpolation && ar.peekChar() == '\\' && ar.hasNext(1) && ar.peekChar(1) == '(';
    }

    @Override
    protected List<NText> readInterpolation(StringReaderExt ar) {
        StringBuilder sb = new StringBuilder();
        sb.append(ar.readChar()); // backslash
        sb.append(ar.readChar()); // (
        int depth = 1;
        while (ar.hasNext() && depth > 0) {
            char c = ar.readChar();
            sb.append(c);
            if (c == '(') {
                depth++;
            } else if (c == ')') {
                depth--;
            }
        }
        return Collections.singletonList(NText.ofStyled(sb.toString(), NTextStyle.variable()));
    }
}
