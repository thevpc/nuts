package net.vpc.app.nuts.toolbox.mvn;

import org.apache.maven.cli.MavenCli;

import java.io.*;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

public class NutsMvnMain {
//    public static void main(String[] args) {
//        main0(new String[]{
//                "--json", "--get", "net.vpc.common:vpc-common-classpath:1.3", "vpc-public-maven"
//        });
//    }

    public static void main(String[] args) {
        boolean json = false;
        String command = null;
        List<String> args2 = new ArrayList<>();
        for (int i = 0; i < args.length; i++) {
            if (command == null) {
                switch (args[i]) {
                    case "-j":
                    case "--json": {
                        json = true;
                        break;
                    }
                    case "-v":
                    case "--version": {
                        System.out.println("1.0.0");
                        return;
                    }
                    case "build": {
                        command = "build";
                        break;
                    }
                    case "get": {
                        command = "get";
                        break;
                    }
                    default: {
                        command = "default";
                        args2.add(args[i]);
                    }
                }
            } else {
                args2.add(args[i]);
            }
        }
        if (command == null) {
            command = "build";
        }
        String[] args2Arr = args2.toArray(new String[args2.size()]);
        switch (command) {
            case "build": {
                List<String> defaultArgs=new ArrayList<>();
                for (String a : args2Arr) {
                    if(a.startsWith("-D")){
                        String[] as=a.substring(2).split("=");
                        System.setProperty(as[0],as[1]);
                    }else{
                        defaultArgs.add(a);
                    }
                }
                boolean r=callMvn(json,".",defaultArgs.toArray(new String[defaultArgs.size()]));
                System.exit(r?0:1);
                break;
            }
            case "get": {
                MavenCli cli = new MavenCli();
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
                boolean r=callMvn(json,dir.getPath(),"dependency:get");
                try {
                    delete(dir);
                } catch (IOException ex) {
                    throw new IllegalArgumentException(ex);
                }
                System.exit(r?0:1);
                break;
            }
        }
    }

    private static boolean callMvn(boolean json,String path,String ... args) {
        MavenCli cli = new MavenCli();
        if (json) {
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
