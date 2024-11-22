package net.thevpc.nuts.toolbox.mvn;

import net.thevpc.nuts.NApp;
import net.thevpc.nuts.NSession;
import org.apache.maven.cli.MavenCli;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

public class MavenCli2 {
    ByteArrayOutputStream bos;
    private NSession session;
    private String workingDirectory;
    private String multiModuleProjectDirectory;
    private String artifactId;
    private String repoUrl;
    private boolean grabString;
    private Map<String, String> options = new HashMap<>();


    public MavenCli2(NSession session) {
        this.session = session;
    }

    public boolean isGrabString() {
        return grabString;
    }

    public MavenCli2 setGrabString(boolean grabString) {
        this.grabString = grabString;
        return this;
    }

    public String getWorkingDirectory() {
        return workingDirectory;
    }

    public MavenCli2 setWorkingDirectory(String workingDirectory) {
        this.workingDirectory = workingDirectory;
        return this;
    }

    public String getMultiModuleProjectDirectory() {
        return multiModuleProjectDirectory;
    }

    public MavenCli2 setMultiModuleProjectDirectory(String multiModuleProjectDirectory) {
        this.multiModuleProjectDirectory = multiModuleProjectDirectory;
        return this;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public MavenCli2 setArtifactId(String artifactId) {
        this.artifactId = artifactId;
        return this;
    }

    public String getRepoUrl() {
        return repoUrl;
    }

    public MavenCli2 setRepoUrl(String repoUrl) {
        this.repoUrl = repoUrl;
        return this;
    }

    public int doMain(String[] args) {
        if (multiModuleProjectDirectory == null) {
            System.setProperty("maven.multiModuleProjectDirectory", NApp.of().getConfFolder().toString());
        } else {
            System.setProperty("maven.multiModuleProjectDirectory", multiModuleProjectDirectory);
        }
        if (artifactId != null) {
            System.setProperty("artifact", artifactId.replaceFirst("#", ":"));
        }
        for (Map.Entry<String, String> ss : options.entrySet()) {
            System.setProperty(ss.getKey(), ss.getValue());
        }
        MavenCli cli = new MavenCli();
        String wd = this.workingDirectory;
        if(wd==null){
            wd=".";
        }
        if (grabString) {
            bos = new ByteArrayOutputStream();
            PrintStream out = new PrintStream(bos);
            int t = cli.doMain(args, wd, out, out);
            out.flush();
            return t;
        } else {
            return cli.doMain(args, wd, session.out().asPrintStream(), session.err().asPrintStream());
        }
    }

    public String getResultString(){
        return bos.toString();
    }

    public void setProperty(String a, String a1) {
        options.put(a, a1);
    }
}
