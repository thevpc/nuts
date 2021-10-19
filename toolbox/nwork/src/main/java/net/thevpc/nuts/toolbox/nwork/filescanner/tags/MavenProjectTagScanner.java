package net.thevpc.nuts.toolbox.nwork.filescanner.tags;

import net.thevpc.nuts.toolbox.nwork.filescanner.DefaultTagInfo;
import net.thevpc.nuts.toolbox.nwork.filescanner.FileScanner;
import net.thevpc.nuts.toolbox.nwork.filescanner.TagInfo;
import net.thevpc.nuts.toolbox.nwork.filescanner.TagScanner;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MavenProjectTagScanner implements TagScanner {
    @Override
    public String[] supportedTags() {
        return new String[]{FileScanner.MAVEN_PROJECT, FileScanner.PROJECT_ROOT};
    }

    @Override
    public TagInfo[] scanTags(Path path, Set<String> tags, Map<String, Object> shared) {
        List<TagInfo> ti = new ArrayList<>();
        if (Files.isDirectory(path)) {
            if (Files.isRegularFile(path.resolve("pom.xml"))) {
                ti.add(new DefaultTagInfo(FileScanner.MAVEN_PROJECT));
                ti.add(new DefaultTagInfo(FileScanner.PROJECT_ROOT));
            }else if(path.getFileName().toString().equals("target")
                    && Files.isRegularFile(path.resolveSibling("pom.xml"))
            ){
                ti.add(new DefaultTagInfo("maven-target"));
                ti.add(new DefaultTagInfo("compiler-build"));
            }else if(path.getFileName().toString().equals("src")
                    && Files.isRegularFile(path.resolveSibling("pom.xml"))
            ){
                ti.add(new DefaultTagInfo("maven-src"));
                ti.add(new DefaultTagInfo("compiler-src"));
            }
        }
        return ti.toArray(new TagInfo[0]);
    }
}
