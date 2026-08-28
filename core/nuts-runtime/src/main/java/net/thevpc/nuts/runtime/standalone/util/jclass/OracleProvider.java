package net.thevpc.nuts.runtime.standalone.util.jclass;

public class OracleProvider extends AbstractDiscoJavaProvider {
    @Override
    public String getName() {
        return "oracle";
    }

    @Override
    protected String getDiscoDistributionName() {
        // "oracle" for Oracle OpenJDK / Oracle JDK Free GA builds
        return "oracle";
    }
}