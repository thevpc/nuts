package net.thevpc.nuts.toolbox.nwork.filescanner.tags;

import net.thevpc.nuts.toolbox.nwork.filescanner.DefaultTagInfo;
import net.thevpc.nuts.toolbox.nwork.filescanner.TagInfo;
import net.thevpc.nuts.toolbox.nwork.filescanner.TagScanner;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BackupIgnoreTagScanner implements TagScanner {
    @Override
    public String[] supportedTags() {
        return new String[]{"backup-ignored", ".backup-ignore"};
    }

    @Override
    public TagInfo[] scanTags(Path path, Set<String> tags, Map<String, Object> shared) {
        List<TagInfo> ti = new ArrayList<>();
        BackupIgnoreFile ii = (BackupIgnoreFile) shared.get(BackupIgnoreFile.class.getName());
        if (path.getFileName().toString().equalsIgnoreCase(".backup-ignore")) {
            if (ii == null) {
                ii = new BackupIgnoreFile();
            }
            shared.put(BackupIgnoreFile.class.getName(), ii);
            ii.loadFrom(path);
            ti.add(new DefaultTagInfo(".backup-ignore"));
        } else {
            if (ii != null) {
                if (ii.accept(path)) {
                    ti.add(new DefaultTagInfo("backup-ignored-all"));
                    if (ii.markIgnored(path)) {
                        ti.add(new DefaultTagInfo("backup-ignored"));
                    }
                }
            }
        }
        return ti.toArray(new TagInfo[0]);
    }


    public static class BackupIgnoreFile extends PathFilterList {

    }
}
