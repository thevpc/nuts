package net.thevpc.nuts.runtime.standalone.util.jclass;

public class CorrettoProvider extends AbstractDiscoJavaProvider {
    @Override
    public String getName() {
        return "corretto";
    }

    @Override
    protected String getDiscoDistributionName() {
        return "corretto";
    }
}