package net.thevpc.nuts.runtime.standalone.util.jclass;

public class GraalVMProvider extends AbstractDiscoJavaProvider {
    @Override
    public String getName() {
        return "graalvm";
    }

    @Override
    protected String getDiscoDistributionName() {
        // "graalvm_ce" is not a Foojay distribution id (the API answers 400 "unsupported
        // distribution"). The legacy CE builds are versioned ("graalvm_ce8", ... "graalvm_ce19");
        // "graalvm_community" is the current GraalVM Community Edition (recent JDKs, e.g. 21).
        return "graalvm_community";
    }
}