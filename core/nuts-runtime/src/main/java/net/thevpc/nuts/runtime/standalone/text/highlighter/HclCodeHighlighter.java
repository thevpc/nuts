package net.thevpc.nuts.runtime.standalone.text.highlighter;

import net.thevpc.nuts.reflect.NScore;
import net.thevpc.nuts.reflect.NScorableContext;

import java.util.Arrays;

public class HclCodeHighlighter extends AbstractCodeHighlighter {

    public HclCodeHighlighter() {
        super("hcl");
        keywords.addAll(Arrays.asList(
                "resource", "data", "variable", "output", "module", "provider", "terraform", "locals",
                "dynamic", "content", "lifecycle", "provisioner", "connection", "backend", "required_providers",
                "for_each", "count", "depends_on", "source", "version", "when", "condition", "error_message",
                "validation", "sensitive", "nullable", "default", "description", "type", "expression",
                "for", "in", "if", "else", "endif", "can", "try", "ephemeral"
        ));
        types.addAll(Arrays.asList(
                "string", "number", "bool", "list", "set", "map", "object", "tuple", "any", "dynamic"
        ));
        booleanLiterals.addAll(Arrays.asList("true", "false"));
        nullLiterals.add("null");
        lineComments.add("//");
        lineComments.add("#");
        blockCommentOpen = "/*";
        blockCommentClose = "*/";
        interpolation = true;
        numberSuffixChars = "";
        separatorChars = "()[]{}.,;";
        operatorChars = "=+-*/%<>!&|^~?:";
    }

    @NScore
    public static int getScore(NScorableContext context) {
        return NCodeHighlighterHelper.score(context,
                "hcl", "terraform", "tf", "tfvars",
                "text/x-hcl", "text/hcl", "application/x-hcl", "application/hcl");
    }
}
