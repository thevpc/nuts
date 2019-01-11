package net.vpc.app.nuts.toolbox.mvn;

import net.vpc.app.nuts.app.NutsApplication;
import net.vpc.app.nuts.app.NutsApplicationContext;
import net.vpc.common.commandline.Argument;
import net.vpc.common.commandline.CommandLine;
import org.apache.maven.cli.MavenCli;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.*;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

public class NutsMvnMain extends NutsApplication {
//    public static void main(String[] args) {
//        main0(new String[]{
//                "--json", "--get", "net.vpc.common:vpc-common-classpath:1.3", "vpc-public-maven"
//        });
//    }
    public static class Options{
        boolean json = false;

    }
    public static void main(String[] args) {
        new NutsMvnMain().launchAndExit(args);
    }

    @Override
    public int launch(NutsApplicationContext appContext) {
        String command = null;
        List<String> args2 = new ArrayList<>();
        Options o=new Options();
        CommandLine cmd=new CommandLine(appContext);
        Argument a;
        while(cmd.hasNext()){
            if(command == null) {
                if (appContext.configure(cmd)) {
                    //fo nothing
                } else if ((a = cmd.readBooleanOption("-j", "--json")) != null) {
                    o.json = a.getBooleanValue();
                } else if ((a = cmd.readNonOption("build")) != null) {
                    command = "build";
                } else if ((a = cmd.readNonOption("get")) != null) {
                    command = "get";
                } else {
                    command = "default";
                    args2.add(cmd.read().getStringExpression());
                }
            }else{
                args2.add(cmd.read().getStringExpression());
            }
        }
        if (command == null) {
            command = "build";
        }
        if(cmd.isExecMode()) {
            String[] args2Arr = args2.toArray(new String[0]);
            switch (command) {
                case "build":
                case "default":
                    {
                    List<String> defaultArgs = new ArrayList<>();
                    for (String ar : args2Arr) {
                        if (ar.startsWith("-D")) {
                            String[] as = ar.substring(2).split("=");
                            System.setProperty(as[0], as[1]);
                        } else {
                            defaultArgs.add(ar);
                        }
                    }
                    boolean r = callMvn(o, ".", defaultArgs.toArray(new String[0]));
                    return(r ? 0 : 1);
                }
                case "get": {
                    System.setProperty("artifact", args2Arr[0].replaceFirst("#", ":"));
                    String repo = null;
                    if (args2Arr.length > 1) {
                        repo = args2Arr[1];
                    }
                    if ("central".equals(repo)) {
                        repo = null;
                    }
                    if ("vpc-public-maven".equals(repo)) {
                        repo = "https://raw.github.com/thevpc/vpc-public-maven/master";
                    }
                    if (repo != null) {
                        System.setProperty("repoUrl", repo);
                    }
                    File dir = createTempPom();
                    boolean r = callMvn(o, dir.getPath(), "dependency:get");
                    try {
                        delete(dir);
                    } catch (IOException ex) {
                        throw new IllegalArgumentException(ex);
                    }
                    return(r ? 0 : 1);
                }
            }
        }
        return 0;
    }

    private static boolean callMvn(Options options,String path,String ... args) {
        MavenCli cli = new MavenCli();
        if (options.json) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            PrintStream out = new PrintStream(bos);
            try {
                cli.doMain(args, path, out, out);
                String s = new String(bos.toByteArray());
                if (s.contains("BUILD SUCCESS")) {
                    System.out.println("{'result':'success'}");
                    return true;
                } else {
                    System.out.println("{'result':'error'}");
                }
            } catch (Exception ex) {
                System.out.println("{'result':'error'}");
            }
            return false;
        } else {
            return cli.doMain(args, path, System.out, System.err)==0;
        }
    }

    private static File createTempPom() {
        File d = createTempDirectory();
        try (PrintWriter out = new PrintWriter(new File(d,"filename.txt"))) {
            out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<project xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://maven.apache.org/POM/4.0.0\"\n" +
                    "         xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
                    "    <modelVersion>4.0.0</modelVersion>\n" +
                    "    <groupId>temp</groupId>\n" +
                    "    <artifactId>temp-nuts</artifactId>\n" +
                    "    <version>1.0.0</version>\n" +
                    "    <packaging>jar</packaging>\n" +
                    "    <properties>\n" +
                    "        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>\n" +
                    "        <maven.compiler.source>1.8</maven.compiler.source>\n" +
                    "        <maven.compiler.target>1.8</maven.compiler.target>\n" +
                    "    </properties>\n" +
                    "    <dependencies>\n" +
                    "    </dependencies>\n" +
                    "    <repositories>\n" +
                    "        <repository>\n" +
                    "            <id>vpc-public-maven</id>\n" +
                    "            <url>https://raw.github.com/thevpc/vpc-public-maven/master</url>\n" +
                    "            <snapshots>\n" +
                    "                <enabled>true</enabled>\n" +
                    "                <updatePolicy>always</updatePolicy>\n" +
                    "            </snapshots>\n" +
                    "        </repository>\n" +
                    "    </repositories>\n" +
                    "    <pluginRepositories>\n" +
                    "        <pluginRepository>\n" +
                    "            <id>vpc-public-maven</id>\n" +
                    "            <url>https://raw.github.com/thevpc/vpc-public-maven/master</url>\n" +
                    "            <snapshots>\n" +
                    "                <enabled>true</enabled>\n" +
                    "                <updatePolicy>always</updatePolicy>\n" +
                    "            </snapshots>\n" +
                    "        </pluginRepository>\n" +
                    "    </pluginRepositories>\n" +
                    "</project>\n");
        } catch (FileNotFoundException ex) {
            throw new RuntimeException(ex);
        }
        return d;
    }

    public static int[] delete(File file) throws IOException {
        final int[] deleted = new int[]{0, 0};
        Files.walkFileTree(file.toPath(), new FileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
//                log.log(Level.FINEST, "Delete file " + file);
                deleted[1]++;
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
//                log.log(Level.FINEST, "Delete folder " + dir);
                deleted[0]++;
                return FileVisitResult.CONTINUE;
            }
        });
        return deleted;
    }

    public static File createTempDirectory() throws RuntimeException {
        final File temp;
        try {
            temp = File.createTempFile("temp", Long.toString(System.nanoTime()));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        if (!(temp.delete())) {
            throw new RuntimeException("Could not delete temp file: " + temp.getAbsolutePath());
        }

        if (!(temp.mkdir())) {
            throw new RuntimeException("Could not create temp directory: " + temp.getAbsolutePath());
        }

        return (temp);
    }
}
