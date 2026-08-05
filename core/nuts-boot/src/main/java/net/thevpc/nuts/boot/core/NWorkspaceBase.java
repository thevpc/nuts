package net.thevpc.nuts.boot.core;

import net.thevpc.nuts.boot.NBootCompleteCmdlineRequest;

public interface NWorkspaceBase {
    void runBootCommand();
    void completeBootCommand(NBootCompleteCmdlineRequest completeRequest);
}
