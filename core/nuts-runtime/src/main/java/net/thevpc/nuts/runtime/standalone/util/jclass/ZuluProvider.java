package net.thevpc.nuts.runtime.standalone.util.jclass;

public class ZuluProvider extends AbstractDiscoJavaProvider {
    @Override
    public String getName() {
        return "zulu";
    }

    @Override
    protected String getDiscoDistributionName() {
        return "zulu";
    }
}