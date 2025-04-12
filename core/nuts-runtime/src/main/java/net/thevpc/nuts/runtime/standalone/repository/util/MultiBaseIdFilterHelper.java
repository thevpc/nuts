package net.thevpc.nuts.runtime.standalone.repository.util;

import net.thevpc.nuts.NId;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.util.NBlankable;

import java.util.ArrayList;
import java.util.List;

public class MultiBaseIdFilterHelper {
    private List<NId> baseIds;
    private List<String> baseIdStrings;
    public List<List<String>> expectedPathNames;
    public MultiBaseIdFilterHelper(String... baseIds) {
        List<NId> ids = new ArrayList<>();
        List<String> idstr = new ArrayList<>();
        List<List<String>> names = new ArrayList<>();
        if (baseIds != null) {
            for (String baseId : baseIds) {
                if (NBlankable.isBlank(baseId)) {
                    NId longId = NId.of(baseId).getLongId();
                    ids.add(longId);
                    idstr.add(longId.toString());
                    names.add(NPath.of(longId.getMavenPath("")).getNames());
                }
            }
        }
        this.baseIds = ids;
        this.baseIdStrings = idstr;
        this.expectedPathNames = names;
    }

    public List<NId> getBaseIds() {
        return baseIds;
    }

    public List<String> getBaseIdStrings() {
        return baseIdStrings;
    }

    public List<List<String>> getExpectedPathNames() {
        return expectedPathNames;
    }

    public boolean isAcceptableBasePaths(NPath[] basePaths) {
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

    private boolean isAcceptableBasePath(List<String> names, List<String> expectedNames) {
        if (names.size() >= expectedNames.size()) {
            for (int i = 0; i < expectedNames.size(); i++) {
                if (!expectedNames.get(i).equals(names.get(i))) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    private boolean isAcceptableBasePath(NPath basePath) {
        List<String> names = basePath.getNames();
        List<List<String>> expectedNames2 = getExpectedPathNames();
        if (expectedNames2.isEmpty()) {
            return true;
        }
        for (List<String> strings : expectedNames2) {
            if (isAcceptableBasePath(names, strings)) {
                return true;
            }
        }
        return false;
    }

}
