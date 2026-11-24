package net.thevpc.nuts.runtime.standalone.util.jclass;

public class GraalVMProvider extends AbstractDiscoJavaProvider {
    @Override
    public String getName() {
        return "graalvm";
    }

    @Override
    protected String getDiscoDistributionName() {
        return "graalvm_community";
    }

    @Override
    protected String getDiscoDistributionName(int version) {
        // foojay uses dedicated distribution ids for the older GraalVM CE
        // releases; plain "graalvm_ce" is not valid and returns HTTP 400.
        switch (version) {
            case 8:
                return "graalvm_ce8";
            case 11:
                return "graalvm_ce11";
            case 16:
                return "graalvm_ce16";
            case 17:
                return "graalvm_ce17";
            case 19:
                return "graalvm_ce19";
            default:
                return "graalvm_community";
        }
    }
}