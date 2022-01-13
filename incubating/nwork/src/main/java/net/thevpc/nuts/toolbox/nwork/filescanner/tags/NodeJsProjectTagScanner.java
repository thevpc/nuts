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

public class NodeJsProjectTagScanner implements TagScanner {


    @Override
    public String[] supportedTags() {
        return new String[]{FileScanner.NODEJS_PROJECT, FileScanner.PROJECT_ROOT};
    }

    @Override
    public TagInfo[] scanTags(Path path, Set<String> tags, Map<String, Object> shared) {
        List<TagInfo> ti = new ArrayList<>();
        if (Files.isDirectory(path)) {
            if (Files.isRegularFile(path.resolve("package.json"))) {
                ti.add(new DefaultTagInfo(FileScanner.NODEJS_PROJECT));
                ti.add(new DefaultTagInfo(FileScanner.PROJECT_ROOT));
            }
        }
        return ti.toArray(new TagInfo[0]);
    }
}
