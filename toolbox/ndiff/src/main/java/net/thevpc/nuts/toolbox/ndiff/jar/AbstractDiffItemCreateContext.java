package net.thevpc.nuts.toolbox.ndiff.jar;

public abstract class AbstractDiffItemCreateContext implements DiffItemCreateContext{
    private DiffKey key;
    private DiffStatus status;
    private String sourceValue;
    private String targetValue;
    private DiffCommand diffCommand;
    private DiffEvalContext diffEvalContext;

    public AbstractDiffItemCreateContext(DiffKey key, DiffStatus status, String sourceValue, String targetValue, DiffCommand diffCommand, DiffEvalContext diffEvalContext) {
        this.key = key;
        this.status = status;
        this.sourceValue = sourceValue;
        this.targetValue = targetValue;
        this.diffCommand = diffCommand;
        this.diffEvalContext = diffEvalContext;
    }

    @Override
    public DiffEvalContext getEvalContext() {
        return diffEvalContext;
    }

    @Override
    public DiffCommand getDiff() {
        return diffCommand;
    }

    @Override
    public DiffKey getKey() {
        return key;
    }

    @Override
    public DiffStatus getStatus() {
        return status;
    }

    @Override
    public String getSourceValue() {
        return sourceValue;
    }

    @Override
    public String getTargetValue() {
        return targetValue;
    }
}
