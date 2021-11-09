/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts;


/**
 * @author thevpc
 */
public class NutsPathInputStreamMetadata implements NutsInputStreamMetadata {

    private String userKind;
    private final NutsPath path;

    public NutsPathInputStreamMetadata(NutsPath path) {
        this.path = path;
//        this(path.getName(),
//                path.getFormattedName(),
//                path.getContentLength(),
//                path.getContentType(),
//                path.getUserKind());
    }

    @Override
    public long getContentLength() {
        return path.getContentLength();
    }

    @Override
    public NutsString getFormattedName() {
        return path.getFormattedName();
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

    public NutsInputStreamMetadata setUserKind(String userKind) {
        this.userKind = userKind;
        return this;
    }
}
