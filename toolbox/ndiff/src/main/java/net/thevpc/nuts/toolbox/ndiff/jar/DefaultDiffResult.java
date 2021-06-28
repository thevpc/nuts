package net.thevpc.nuts.toolbox.ndiff.jar;

import java.util.TreeMap;

public class DefaultDiffResult extends AbstractDiffResult {
    public DefaultDiffResult(DiffCommand diff, DiffEvalContext diffEvalContext, TreeMap<DiffKey, String> added, TreeMap<DiffKey, String> removed, TreeMap<DiffKey, String[]> changed) {
        super(diff, diffEvalContext, added, removed, changed);
    }
}
