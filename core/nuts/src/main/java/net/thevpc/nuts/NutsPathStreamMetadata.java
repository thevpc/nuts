/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts;


/**
 * @author thevpc
 */
public class NutsPathStreamMetadata implements NutsStreamMetadata {

    private String userKind;
    private final NutsPath path;

    public NutsPathStreamMetadata(NutsPath path) {
        this.path = path;
    }

    @Override
    public long getContentLength() {
        return path.getContentLength();
    }

    @Override
    public NutsString getFormattedPath(NutsSession session) {
        return path.format(session);
    }

    @Override
    public String getContentType() {
        return path.getContentType();
    }

    @Override
    public String getName() {
        return path.getName();
    }

    public String getUserKind() {
        return userKind;
    }

    public NutsStreamMetadata setUserKind(String userKind) {
        this.userKind = userKind;
        return this;
    }
}
