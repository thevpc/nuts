package net.thevpc.nuts.runtime.standalone.repository.util;

import net.thevpc.nuts.NId;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.util.NBlankable;

import java.util.Collections;
import java.util.List;

public class SingleBaseIdFilterHelper {
    private NId baseId;
    private String baseIdString;
    public List<String> expectedPathName;

    public SingleBaseIdFilterHelper(String baseId) {
        if (NBlankable.isBlank(baseId)) {
            this.baseId = NId.BLANK;
            this.baseIdString = "";
            this.expectedPathName = Collections.emptyList();
        } else {
            this.baseId = NId.of(baseId).getShortId();
            this.baseIdString = this.baseId.toString();
            this.expectedPathName = NPath.of(this.baseId.getMavenPath("")).getNames();
        }
    }

    public boolean accept(NPath[] basePaths) {
        if (basePaths == null) {
            return true;
        }
        if (basePaths.length == 0) {
            return true;
        }
        for (NPath basePath : basePaths) {
            if (isAcceptableBasePath(basePath)) {
                return true;
            }
        }
        return false;
    }

    private boolean isAcceptableBasePath(NPath basePath) {
        List<String> names = basePath.getNames();
        int size = expectedPathName.size();
        if (size == 0) {
            return true;
        }
        if (names.size() >= size) {
            for (int i = 0; i < size; i++) {
                if (!expectedPathName.get(i).equals(names.get(i))) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public boolean accept(NId id) {
        return baseId.getShortName().equals(id.getShortName());
    }
}
