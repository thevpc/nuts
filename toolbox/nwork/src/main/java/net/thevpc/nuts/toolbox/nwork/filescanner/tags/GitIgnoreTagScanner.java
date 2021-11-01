package net.thevpc.nuts.toolbox.nwork.filescanner.tags;

import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.toolbox.nwork.filescanner.DefaultTagInfo;
import net.thevpc.nuts.toolbox.nwork.filescanner.TagInfo;
import net.thevpc.nuts.toolbox.nwork.filescanner.TagScanner;

import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;

public class GitIgnoreTagScanner implements TagScanner {
    @Override
    public String[] supportedTags() {
        return new String[]{"git-ignored", ".gitignore"};
    }

    @Override
    public TagInfo[] scanTags(Path path, Set<String> tags, Map<String, Object> shared) {
        List<TagInfo> ti = new ArrayList<>();
        GitIgnoreFile ii = (GitIgnoreFile) shared.get(GitIgnoreFile.class.getName());
        if (path.getFileName().toString().equalsIgnoreCase(".gitignore")) {
            if (ii == null) {
                ii = new GitIgnoreFile(ii.session);
            }
            shared.put(GitIgnoreFile.class.getName(), ii);
            ii.loadFrom(path);
            ti.add(new DefaultTagInfo(".gitignore"));
        } else {
            if (ii != null) {
                if (ii.accept(path)) {
                    if(path.toString().endsWith(".java") && !path.toString().contains("/target/")){
                        ii.accept(path);
//                        System.out.println(ii.accept(path));
                    }
                    ti.add(new DefaultTagInfo("git-ignored-all"));
                    if (ii.markIgnored(path)) {
                        ti.add(new DefaultTagInfo("git-ignored"));
                    }
                }
            }
        }
        return ti.toArray(new TagInfo[0]);
    }


    public static class GitIgnoreFile extends PathFilterList {
        public GitIgnoreFile(NutsSession session) {
            super(session);
        }
    }
}
