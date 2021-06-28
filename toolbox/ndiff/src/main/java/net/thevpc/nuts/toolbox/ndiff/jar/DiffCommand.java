package net.thevpc.nuts.toolbox.ndiff.jar;

import java.io.InputStream;
import java.util.Map;

public interface DiffCommand {
    String getId();

    DiffResult eval(DiffEvalContext context);

    boolean acceptDiffKey(DiffKey key);

    int acceptInput(Object input);

    DiffItem createChildItem(DiffKey name, DiffStatus status, String sourceValue, String targetValue, DiffEvalContext diffEvalContext);

    Map<DiffKey, String> map(Object item, DiffEvalContext diffEvalContext);

    String hash(InputStream source);

    DiffItem createContentDiffItem(DiffItemCreateContext context);
}
