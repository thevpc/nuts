package net.thevpc.nuts.toolbox.ndiff.jar;

public interface DiffItemCreateContext {
    DiffEvalContext getEvalContext();

    DiffCommand getDiff();

    DiffKey getKey();

    DiffStatus getStatus();

    String getSourceValue();

    String getTargetValue();
}
