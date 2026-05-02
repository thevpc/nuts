package net.thevpc.nuts.runtime.standalone.repository.util;

import net.thevpc.nuts.core.NConstants;
import net.thevpc.nuts.elem.NElementReader;
import net.thevpc.nuts.elem.NObjectElement;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.log.NMsgIntent;
import net.thevpc.nuts.core.NRepositorySpec;
import net.thevpc.nuts.core.NRepositoryConfig;
import net.thevpc.nuts.core.NRepositoryRef;
import net.thevpc.nuts.runtime.standalone.repository.DefaultNRepositoryDB;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.spi.*;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.util.NStringUtils;

import java.util.*;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NRepositoryUtils {
    public static NRepositoryRef optionsToRef(NRepositorySpec options) {
        return new NRepositoryRef()
                .setEnabled(options.isEnabled())
                .setFailSafe(options.isFailSafe())
                .setName(options.getName())
                .setLocation(options.getLocation())
                .setDeployWeight(options.getDeployWeight());
    }

    public static NRepositorySpec refToOptions(NRepositoryRef ref) {
        return new NRepositorySpec()
                .setEnabled(ref.isEnabled())
                .setFailSafe(ref.isFailSafe())
                .setName(ref.getName())
                .setLocation(ref.getLocation())
                .setDeployWeight(ref.getDeployWeight())
                .setTemporary(false);
    }

    public static NRepositoryLocation validateLocation(NRepositoryLocation r, NLog nLog) {
        if (NBlankable.isBlank(r.getLocationType()) /*|| NBlankable.isBlank(r.getName())*/) {
            if (r.getFullLocation() != null) {
                NPath r1 = NPath.of(r.getPath()).toAbsolute();
                if (!Objects.equals(r.getPath(),r1.toString())) {
                    r = r.setPath(r1.toString());
                }
                NPath r2 = r1.resolve(".nuts-repository");
                boolean fileExists = false;
                try {
                    if(!r2.exists()){
                        if (nLog != null) {
                            nLog.log(NMsg.ofC("unable to load %s", r2).withLevel(Level.CONFIG).withIntent(NMsgIntent.ALERT));
                        }
                    }else {
                        byte[] bytes = r2.readBytes();
                        if (bytes != null) {
                            fileExists = true;
                            NObjectElement jsonObject = NElementReader.ofJson().read(bytes).asObject().get();
                            if (NBlankable.isBlank(r.getLocationType())) {
                                String o = jsonObject.getStringValue("repositoryType").orNull();
                                if (!NBlankable.isBlank(o)) {
                                    r = r.setLocationType(String.valueOf(o));
                                }
                            }
                            if (NBlankable.isBlank(r.getName())) {
                                String o = jsonObject.getStringValue("repositoryName").orNull();
                                if (!NBlankable.isBlank(o)) {
                                    r = r.setName(String.valueOf(o));
                                }
                            }
                            if (NBlankable.isBlank(r.getName())) {
                                r = r.setName(r.getName());
                            }
                        }
                    }
                } catch (Exception e) {
                    if (nLog != null) {
                        nLog.log(NMsg.ofC("unable to load %s", r2).withLevel(Level.CONFIG).withIntent(NMsgIntent.ALERT));
                    }
                }
                if (fileExists) {
                    if (NBlankable.isBlank(r.getLocationType())) {
                        r = r.setLocationType(NConstants.RepoTypes.NUTS);
                    }
                }
                if (NBlankable.isBlank(r.getLocationType())) {
                    NPath p = NPath.of(r.getPath());
                    if (p.isLocal()) {
                        if (!p.exists() || p.isDirectory()) {
                            r = r.setLocationType(NConstants.RepoTypes.NUTS);
                        }
                    }
                }
            }
        }
        return r;
    }

    public static String getRepoType(NRepositoryConfig conf) {
        if (conf != null) {
            NRepositoryLocation loc = conf.getLocation();
            if (loc != null) {
                loc = validateLocation(loc, null);
                if (!NBlankable.isBlank(loc.getLocationType())) {
                    return loc.getLocationType();
                }
            }
        }
        return null;
    }

    public static String getRepoType(NRepositorySpec conf) {
        if (conf != null) {
            NRepositoryLocation loc = conf.getSourceLocation();
            if (loc != null) {
                loc = validateLocation(loc, null);
                if (!NBlankable.isBlank(loc.getLocationType())) {
                    return loc.getLocationType();
                }
            }
        }
        return null;
    }

    public static NOptional<NRepositorySelectorList> createRepositorySelectorList(List<String> expressions) {
        if (expressions == null) {
            return NOptional.of(new NRepositorySelectorList());
        }
        NRepositorySelectorList result = new NRepositorySelectorList();
        for (String t : expressions) {
            if (t != null) {
                t = t.trim();
                if (!t.isEmpty()) {
                    NOptional<NRepositorySelectorList> r = createRepositorySelectorList(t);
                    if (r.isNotPresent()) {
                        String finalT = t;
                        return NOptional.ofError(() -> NMsg.ofC("invalid selector list : %s", finalT));
                    }
                    result = result.merge(r.get());
                }
            }
        }
        return NOptional.of(result);
    }

    public static NOptional<NRepositorySelectorList> createRepositorySelectorList(String expression) {
        if (NBlankable.isBlank(expression)) {
            return NOptional.of(new NRepositorySelectorList());
        }
        NSelectorOp op = NSelectorOp.INCLUDE;
        List<NRepositorySelector> all = new ArrayList<>();
        for (String s : NStringUtils.split(expression, ",;", true, true)) {
            s = s.trim();
            if (s.length() > 0) {
                NOptional<NRepositorySelector> oe = createRepositorySelector(op, s);
                if (oe.isNotPresent()) {
                    return NOptional.ofError(() -> NMsg.ofC("invalid selector list : %s", expression));
                }
                NRepositorySelector e = oe.get();
                op = e.getOp();
                all.add(e);
            }
        }
        return NOptional.of(new NRepositorySelectorList(all.toArray(new NRepositorySelector[0])));
    }

//
//    /**
//     * @param repositorySelectionExpression expression in the form +a,-b,=c
//     * @param available                     available (default) locations
//     * @return repository location list from db that include available/defaults
//     * and fulfills the condition {@code repositorySelectionExpression}
//     */
//    public static NOptional<NRepositoryLocation[]> createRepositoryLocationArray(String repositorySelectionExpression, NRepositoryLocation[] available) {
//        return createRepositorySelectorList(repositorySelectionExpression).map(x -> NRepositoryUtils.resolve(x,available));
//    }


    public static NOptional<NRepositorySelector> createRepositorySelector(String location) {
        return createRepositorySelector(null,location);
    }

    public static NOptional<NRepositorySelector> createRepositorySelector(NSelectorOp op, String location) {
        location = NStringUtils.trim(location);
        if (op == null) {
            op = NSelectorOp.INCLUDE;
        }
        if (location.length() > 0) {
            if (location.startsWith("+")) {
                op = NSelectorOp.INCLUDE;
                location = location.substring(1).trim();
            } else if (location.startsWith("-")) {
                op = NSelectorOp.EXCLUDE;
                location = location.substring(1).trim();
            } else if (location.startsWith("=")) {
                op = NSelectorOp.EXACT;
                location = location.substring(1).trim();
            }
            NOptional<NRepositoryLocation> z = NRepositoryUtils.createRepositoryLocation(location);
            if (z.isPresent()) {
                return NOptional.of(new NRepositorySelector(op, z.get()));
            }
        }
        String finalLocation = location;
        return NOptional.<NRepositorySelector>ofEmpty(() -> NMsg.ofC("repository %s", finalLocation));
    }

    /**
     * Create a new NutsRepositoryLocation. When the name is null,
     * {@code fullLocation} will preserve any existing name (where
     * {@code fullLocation} is entered as a {@code locationString})
     *
     * @param locationString location string in the format
     *                       {@code name=locationType:path}
     * @return new Instance
     */
    public static NOptional<NRepositoryLocation> createRepositoryLocation(String locationString) {
        String name = null;
        String url = null;
        if (locationString == null) {
            return NOptional.<NRepositoryLocation>ofEmpty(() -> NMsg.ofPlain("repository location"));
        }
        locationString = locationString.trim();
        if (locationString.startsWith("-")
                || locationString.startsWith("+")
                || locationString.startsWith("=")
                || locationString.indexOf(',') >= 0
                || locationString.indexOf(';') >= 0) {
            String finalLocationString = locationString;
            return NOptional.<NRepositoryLocation>ofError(() -> NMsg.ofC("invalid selection syntax : %s", finalLocationString));
        }
        Matcher matcher = Pattern.compile("(?<name>[a-zA-Z-_]+)=(?<value>.+)").matcher(locationString);
        if (matcher.find()) {
            name = matcher.group("name");
            url = matcher.group("value");
        } else {
            DefaultNRepositoryDB db = NWorkspaceExt.of().getRepositoryModel().getDB();
            if (locationString.matches("[a-zA-Z][a-zA-Z0-9-_]+")) {
                name = locationString;
                NRepositorySpec ro = db.getDefinitionByName(name).orNull();
                String u = ro==null?null:ro.getSourceLocation().getFullLocation();
                if (u == null) {
                    url = name;
                } else {
                    url = u;
                }
            } else {
                url = locationString;
                NRepositorySpec ro = db.getDefinitionByPath(name).orNull();
                String n = ro==null?null:ro.getName();
                if (n == null) {
                    name = null;
                } else {
                    name = n;
                }
            }
        }
        if (url.length() > 0) {
            return NOptional.of(NRepositoryLocation.of(name, url));
        }
        return NOptional.<NRepositoryLocation>ofEmpty(() -> NMsg.ofPlain("repository location"));
    }


    public static NRepositorySpec[] resolve(NRepositorySelectorList list, NRepositorySpec[] available) {
        NRepositoryURLList current = new NRepositoryURLList();
        DefaultNRepositoryDB db = NWorkspaceExt.of().getRepositoryModel().getDB();
        if (available != null) {
            for (NRepositorySpec entry : available) {
                if(entry!=null) {
                    String k = entry.getName();
                    NRepositoryLocation sl = entry.getSourceLocation();
                    String k2 = sl ==null?null: sl.getName();
                    if(NBlankable.isBlank(k) && !NBlankable.isBlank(k2)){
                        k=k2;
                    }
                    String v = sl ==null?null: sl.getFullLocation();
                    if (NBlankable.isBlank(v) && !NBlankable.isBlank(k)) {
                        NRepositorySpec ro = db.getDefinitionByName(k).orNull();
                        String u = (ro==null ||ro.getSourceLocation()==null)?null:ro.getSourceLocation().getFullLocation();
                        if (u != null) {
                            v = u;
                        } else {
                            v = k;
                        }
                    } else if (!NBlankable.isBlank(v) && NBlankable.isBlank(k)) {
                        NRepositorySpec ro = db.getDefinitionByPath(k).orNull();
                        String n = ro==null?null:ro.getName();
                        if (n != null) {
                            k = n;
                        }
                    }
                    current.add(
                            entry.copy().setName(k).setSourceLocation(NRepositoryLocation.of(k, v))
                            );
                }
            }
        }
        List<NRepositorySpec> result = new ArrayList<>();
        Set<String> visited = new HashSet<>();

        List<NRepositorySelector> selectorsExclude = new ArrayList<>();
        List<NRepositorySelector> selectorsInclude = new ArrayList<>();
        boolean exact = false;
        for (NRepositorySelector selector : list.getSelectors()) {
            switch (selector.getOp()) {
                case EXACT: {
                    exact = true;
                    selectorsInclude.add(selector);
                    break;
                }
                case INCLUDE: {
                    selectorsInclude.add(selector);
                    break;
                }
                case EXCLUDE: {
                    selectorsExclude.add(selector);
                    break;
                }
            }
        }
        if (exact) {
            current.clear();
        }

        //now remove all excluded
        for (NRepositorySelector r : selectorsExclude) {
            Set<String> allNames = getAllNames(r);
            int i = current.indexOfNames(allNames.toArray(new String[0]), 0);
            if (i >= 0) {
                current.removeAt(i);
            }
        }

        //finally add included in the defined order
        for (NRepositorySelector r : selectorsInclude) {
            Set<String> allNames = getAllNames(r);
            if (!isVisitedFlag(allNames, visited)) {
                visited.addAll(allNames);
                result.add(new NRepositorySpec()
                        .setName(r.getName())
                        .setSourceLocation(NRepositoryLocation.of(r.getName(), r.getUrl()))
                );
            }
        }
        for (NRepositorySpec e : current.toArray()) {
            if (list.acceptExisting(e)) {
                Set<String> allNames = db.findAllNamesByName(e.getName());
                if (!isVisitedFlag(allNames, visited)) {
                    visited.addAll(allNames);
                    result.add(e);
                }
            }
        }
        return result.toArray(new NRepositorySpec[0]);
    }

    private static boolean isVisitedFlag(Set<String> allNames, Set<String> visited) {
        boolean visitedFlag = false;
        for (String allName : allNames) {
            if (visited.contains(allName)) {
                visitedFlag = true;
                break;
            }
        }
        return visitedFlag;
    }

    private static Set<String> getAllNames(NRepositorySelector r){
        DefaultNRepositoryDB db = NWorkspaceExt.of().getRepositoryModel().getDB();
        if (!NBlankable.isBlank(r.getName())) {
            return db.findAllNamesByName(r.getName());
        }else{
            String name = db.getDefinitionByPath(r.getUrl()).map(x->x.getName()).orNull();
            return db.findAllNamesByName(name);
        }
    }


}
