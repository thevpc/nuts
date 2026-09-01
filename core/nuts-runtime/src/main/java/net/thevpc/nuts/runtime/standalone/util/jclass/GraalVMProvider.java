package net.thevpc.nuts.runtime.standalone.util.jclass;

public class GraalVMProvider extends AbstractDiscoJavaProvider {
    @Override
    public String getName() {
        return "graalvm";
    }

    @Override
    protected String getDiscoDistributionName() {
        // Foojay identifies GraalVM Community Edition as "graalvm_ce"
        return "graalvm_ce";
    }
}