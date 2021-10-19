package net.thevpc.nuts.toolbox.nwork.filescanner;

import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

class PathRichPathFunction implements Function<Path, RichPath> {
    private final Set<String> effSupportedTags = new LinkedHashSet<>();
    private final Set<TagScanner> effSupportedTagScanners = new LinkedHashSet<>();
    private FileScanner fs;
    private Map<String,Object> shared;

    public PathRichPathFunction(FileScanner fs,Map<String,Object> shared) {
        this.fs = fs;
        this.shared = shared;
        if(fs.getTagScanners()!=null) {
            for (TagScanner tagScanner : fs.getTagScanners()) {
                for (String st : tagScanner.supportedTags()) {
                    if (fs.getSupportedTags() == null || fs.getSupportedTags().test(st)) {
                        effSupportedTags.add(st);
                        effSupportedTagScanners.add(tagScanner);
                    }
                }
            }
        }
    }

    @Override
    public RichPath apply(Path path) {
        LinkedHashSet<TagInfo> tagInfo = new LinkedHashSet<>();
        for (TagScanner s : effSupportedTagScanners) {
            TagInfo[] r = s.scanTags(path, effSupportedTags,shared);
            if (r != null && r.length != 0) {
                for (TagInfo i : r) {
                    if (i != null) {
                        tagInfo.add(i);
                    }
                }
            }
        }
        return new DefaultRichPath(path, tagInfo,shared);
    }
}
