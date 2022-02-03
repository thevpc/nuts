package net.thevpc.nuts.installer.model;

public class NutsId {
    private String groupId;
    private String artifactId;
    private String version;

    public NutsId(String id) {
        int a = id.indexOf(':');
        if(a>0) {
            int b = id.indexOf('#', a + 1);
            if(b>0){
                this.groupId = id.substring(0,a);
                this.artifactId = id.substring(a+1,b);
                this.version = id.substring(b+1);
            }else{
                this.groupId = id.substring(0,a);
                this.artifactId = id.substring(a+1);
                this.version = "";
            }
            return;
        }
        throw new IllegalArgumentException("invalid id format");
    }

    public String getGroupId() {
        return groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getVersion() {
        return version;
    }
}
