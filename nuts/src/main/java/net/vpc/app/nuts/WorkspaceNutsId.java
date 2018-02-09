package net.vpc.app.nuts;

class WorkspaceNutsId {
    String groupId;
    String artifactId;
    String version;

    public WorkspaceNutsId(String groupId, String artifactId, String version) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
    }

    static WorkspaceNutsId parse(String id){
        String[] splittedBootId = id.split("[:#]");
        if(splittedBootId.length==3){
            return new WorkspaceNutsId(splittedBootId[0],splittedBootId[1],splittedBootId[2]);
        }
        if(splittedBootId.length==2){
            return new WorkspaceNutsId(splittedBootId[0],splittedBootId[1],null);
        }
        throw new NutsParseException("Unable to parse "+id);
    }

    @Override
    public String toString() {
        if(version==null){
            return groupId+":"+artifactId;
        }
        return groupId+":"+artifactId +"#"+version;
    }

    public String getVersion() {
        return version;
    }
}
