package net.thevpc.nuts.boot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NBootCompleteCmdlineRequest implements NBootCompleteRequestOrResult{
    private List<String> args;
    private NBootCompleteRequest request;

    public NBootCompleteCmdlineRequest(NBootCompleteRequest request, List<String> args) {
        this.args = new ArrayList<>(args);
        this.request = request;
    }

    public List<String> args() {
        return Collections.unmodifiableList(args);
    }

    public NBootCompleteRequest request() {
        return request;
    }
}
