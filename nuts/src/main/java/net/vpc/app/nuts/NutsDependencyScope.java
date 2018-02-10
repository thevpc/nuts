package net.vpc.app.nuts;

public enum NutsDependencyScope {
    /**
     * dependencies needed for running/executing the nuts : includes
     * 'compile,system,runtime' witch are NOT optional
     */
    RUN,
    /**
     * dependencies needed for running/executing unit tests the nuts : includes
     * 'test,compile,system,runtime' witch are NOT optional
     */
    TEST,
    /**
     * all dependencies (no restriction)
     */
    ALL,
}
