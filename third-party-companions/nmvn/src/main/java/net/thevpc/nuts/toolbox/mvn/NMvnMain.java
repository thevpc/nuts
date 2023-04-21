package net.thevpc.nuts.toolbox.mvn;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.spi.NPaths;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NMvnMain implements NApplication {
    private static final Logger LOG= Logger.getLogger(NMvnMain.class.getName());
//    public static void main(String[] args) {
//        main0(new String[]{
//                "--json", "--get", "test:classpath:1.3", "vpc-public-maven"
//        });
//    }

    public static class Options {

        boolean json = false;

    }

    public static void main(String[] args) {
        new NMvnMain().runAndExit(args);
    }

    @Override
    public void run(NSession session) {
        String command = null;
        List<String> args2 = new ArrayList<>();
        Options o = new Options();
        NCmdLine cmd = session.getAppCommandLine();
        NArg a;
        while (cmd.hasNext()) {
            if (command == null) {
                if (session.configureFirst(cmd)) {
                    //fo nothing
                } else if ((a = cmd.nextFlag("-j", "--json").orNull()) != null) {
                    o.json = a.getBooleanValue().get(session);
                } else if ((a = cmd.next("build").orNull()) != null) {
                    command = "build";
                } else if ((a = cmd.next("get").orNull()) != null) {
                    command = "get";
                } else {
                    command = "default";
                    args2.add(cmd.next().flatMap(NLiteral::asString).get(session));
                }
            } else {
                args2.add(cmd.next().flatMap(NLiteral::asString).get(session));
            }
        }
        if (command == null) {
            command = "build";
        }
        if (cmd.isExecMode()) {
            MavenCli2 cli = new MavenCli2(session);

            String[] args2Arr = args2.toArray(new String[0]);
            switch (command) {
                case "build":
                case "default": {
                    List<String> defaultArgs = new ArrayList<>();
                    for (String ar : args2Arr) {
                        if (ar.startsWith("-D")) {
                            String[] as = ar.substring(2).split("=");
                            cli.setProperty(as[0], as[1]);
                        } else {
                            defaultArgs.add(ar);
                        }
                    }
                    int r = callMvn(cli,session, o, defaultArgs.toArray(new String[0]));
                    if (r == 0) {
                        return;
                    } else {
                        throw new NExecutionException(session, NMsg.ofC("Maven Call exited with code %d", r), r);
                    }
                }
                case "get": {
                    cli.setArtifactId(args2Arr[0]);
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
                        cli.setRepoUrl(repo);
                    }
                    Path dir = createTempPom(session);
                    cli.setWorkingDirectory(dir.toString());
                    int r = callMvn(cli,session, o,  "dependency:get");
                    try {
                        delete(dir);
                    } catch (IOException ex) {
                        throw new IllegalArgumentException(ex);
                    }
                    if (r == 0) {
                        return;
                    } else {
                        throw new NExecutionException(session, NMsg.ofC("Maven Call exited with code %s", r), r);
                    }
                }
            }
        }
    }

//    public void prepareM2Home(NSession session){
//        Path configFolder = session.getConfigFolder();
//        if(!Files.isRegularFile(configFolder.resolve(".mvn/maven.config"))){
//            if(!Files.isDirectory(configFolder.resolve(".mvn"))){
//                Files.createDirectories(configFolder.resolve(".mvn"));
//            }
//            Files.
//        }
//        maven.multiModuleProjectDirectory
//    }
    private static int callMvn(MavenCli2 cli, NSession session, Options options, String... args) {
       if (options.json) {
            try {
                cli.setGrabString(true);
                int r = cli.doMain(args);
                String s = cli.getResultString();
                if (s.contains("BUILD SUCCESS")) {
                    session.out().println("{'result':'success'}");
                    return 0;
                } else {
                    if (r == 0) {
                        r = 1;
                    }
                    session.out().println("{'result':'error'}");
                }
                return r;
            } catch (Exception ex) {
                LOG.log(Level.FINE,"error executing mvn command "+ Arrays.toString(args),ex);//e.printStackTrace();
                session.out().println("{'result':'error'}");
                return 1;
            }
        } else {
            return cli.doMain(args);
        }
    }

    private static Path createTempPom(NSession session) {
        Path d = NPath.ofTempFolder(session).toFile();
        try (Writer out = Files.newBufferedWriter(d.resolve("pom.xml"))) {
            out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                    + "<project xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://maven.apache.org/POM/4.0.0\"\n"
                    + "         xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n"
                    + "    <modelVersion>4.0.0</modelVersion>\n"
                    + "    <groupId>temp</groupId>\n"
                    + "    <artifactId>temp-nuts</artifactId>\n"
                    + "    <version>1.0.0</version>\n"
                    + "    <packaging>jar</packaging>\n"
                    + "    <properties>\n"
                    + "        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>\n"
                    + "        <maven.compiler.source>1.8</maven.compiler.source>\n"
                    + "        <maven.compiler.target>1.8</maven.compiler.target>\n"
                    + "    </properties>\n"
                    + "    <dependencies>\n"
                    + "    </dependencies>\n"
                    + "    <repositories>\n"
                    + "        <repository>\n"
                    + "            <id>vpc-public-maven</id>\n"
                    + "            <url>https://raw.github.com/thevpc/vpc-public-maven/master</url>\n"
                    + "            <snapshots>\n"
                    + "                <enabled>true</enabled>\n"
                    + "                <updatePolicy>always</updatePolicy>\n"
                    + "            </snapshots>\n"
                    + "        </repository>\n"
                    + "    </repositories>\n"
                    + "    <pluginRepositories>\n"
                    + "        <pluginRepository>\n"
                    + "            <id>vpc-public-maven</id>\n"
                    + "            <url>https://raw.github.com/thevpc/vpc-public-maven/master</url>\n"
                    + "            <snapshots>\n"
                    + "                <enabled>true</enabled>\n"
                    + "                <updatePolicy>always</updatePolicy>\n"
                    + "            </snapshots>\n"
                    + "        </pluginRepository>\n"
                    + "    </pluginRepositories>\n"
                    + "</project>\n");
            out.write(System.getProperty("line.separator"));
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        return d;
    }

    public static int[] delete(Path file) throws IOException {
        final int[] deleted = new int[]{0, 0};
        Files.walkFileTree(file, new FileVisitor<Path>() {
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
}
