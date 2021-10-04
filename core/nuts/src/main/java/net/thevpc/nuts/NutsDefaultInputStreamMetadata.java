/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts;


/**
 *
 * @author thevpc
 */
public class NutsDefaultInputStreamMetadata implements NutsInputStreamMetadata {

    private long contentLength;
    private NutsString formattedName;
    private String contentType;
    private String name;
    private String userKind;

    public NutsDefaultInputStreamMetadata(NutsInputStreamMetadata other) {
        if(other!=null){
            this.contentLength =other.getContentLength();
            this.name=other.getName();
            this.formattedName=other.getFormattedName();
            this.userKind=other.getUserKind();
            this.contentType=other.getContentType();
        }else{
            this.contentLength =-1;
            this.name=null;
            this.formattedName=null;
            this.userKind=null;
            this.contentType=null;
        }
    }

    public NutsDefaultInputStreamMetadata() {
        this.contentLength =-1;
        this.name=null;
        this.formattedName=null;
        this.userKind=null;
        this.contentType=null;
    }
    public NutsDefaultInputStreamMetadata(NutsMessage message, long contentLength, String contentType, String userKind,NutsSession session) {
        this(message==null?null:message.toString(),message==null?null:message.toNutsString(session),contentLength,contentType,userKind);
    }
    public NutsDefaultInputStreamMetadata(NutsString message, long contentLength, String contentType, String userKind) {
        this(message==null?null:message.toString(),message,contentLength,contentType,userKind);
    }

    public NutsDefaultInputStreamMetadata(NutsPath path) {
        this(path.getName(),
                path.getFormattedName(),
                path.getContentLength(),
                path.getContentType(),
                path.getUserKind());
    }

    public NutsDefaultInputStreamMetadata(String name, NutsString formattedName, long contentLength, String contentType, String userKind) {
        this.contentLength = contentLength;
        this.name = name;
        this.formattedName = formattedName;
        this.userKind = userKind;
        this.contentType = contentType;
    }

    public String getUserKind() {
        return userKind;
    }

    public NutsInputStreamMetadata setUserKind(String userKind) {
        this.userKind = userKind;
        return this;
    }

    @Override
    public NutsString getFormattedName() {
        return formattedName;
    }

    @Override
    public long getContentLength() {
        return contentLength;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getContentType() {
        return contentType;
    }
}
