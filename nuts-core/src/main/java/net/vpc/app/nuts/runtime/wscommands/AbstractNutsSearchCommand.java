/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 * Copyright (C) 2016-2020 thevpc
 * <br>
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * <br>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <br>
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.runtime.wscommands;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.runtime.DefaultNutsQueryBaseOptions;
import net.vpc.app.nuts.runtime.ext.DefaultNutsWorkspaceExtensionManager;
import net.vpc.app.nuts.main.wscommands.DefaultNutsFetchCommand;
import net.vpc.app.nuts.runtime.util.common.CoreCommonUtils;
import net.vpc.app.nuts.runtime.util.common.CoreStringUtils;

import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * @author vpc
 */
public abstract class AbstractNutsSearchCommand extends DefaultNutsQueryBaseOptions<NutsSearchCommand> implements NutsSearchCommand {

    protected Comparator comparator;
    protected NutsDependencyFilter dependencyFilter;
    protected NutsDescriptorFilter descriptorFilter;
    protected NutsIdFilter idFilter;
    protected NutsRepositoryFilter repositoryFilter;
    protected boolean latest = false;
    protected boolean distinct = false;
    protected boolean includeBasePackage = true;
    protected boolean sorted = false;
    protected final List<String> arch = new ArrayList<>();
    protected final List<NutsId> ids = new ArrayList<>();
    protected final List<NutsId> lockedIds = new ArrayList<>();
    protected final List<String> scripts = new ArrayList<>();
    protected final List<String> packaging = new ArrayList<>();
    protected Boolean defaultVersions = null;
    protected String execType = null;
    protected String targetApiVersion = null;
    protected boolean printResult = false;
    protected List<Set<NutsInstallStatus>> installStatus = new ArrayList<>();

    public AbstractNutsSearchCommand(NutsWorkspace ws) {
        super(ws, "search");
    }

    /**
     * @return
     * @since 0.5.5
     */
    @Override
    public Boolean getDefaultVersions() {
        return defaultVersions;
    }

    /**
     * @param acceptDefaultVersion
     * @return
     * @since 0.5.5
     */
    @Override
    public NutsSearchCommand setDefaultVersions(Boolean acceptDefaultVersion) {
        this.defaultVersions = acceptDefaultVersion;
        return this;
    }

    @Override
    public NutsSearchCommand clearScripts() {
        scripts.clear();
        return this;
    }

    @Override
    public NutsSearchCommand addScripts(Collection<String> value) {
        if (value != null) {
            addScripts(value.toArray(new String[0]));
        }
        return this;
    }

    @Override
    public NutsSearchCommand removeScript(String value) {
        scripts.remove(value);
        return this;
    }

    @Override
    public NutsSearchCommand addScript(String value) {
        if (value != null) {
            scripts.add(value);
        }
        return this;
    }

    @Override
    public NutsSearchCommand addScripts(String... value) {
        if (value != null) {
            scripts.addAll(Arrays.asList(value));
        }
        return this;

    }

    @Override
    public NutsSearchCommand addIds(String... values) {
        if (values != null) {
            for (String s : values) {
                if (!CoreStringUtils.isBlank(s)) {
                    ids.add(ws.id().parser().setLenient(false).parse(s));
                }
            }
        }
        return this;
    }

    @Override
    public NutsSearchCommand addIds(NutsId... value) {
        if (value != null) {
            for (NutsId s : value) {
                if (s != null) {
                    ids.add(s);
                }
            }
        }
        return this;
    }

    @Override
    public NutsSearchCommand clearIds() {
        ids.clear();
        return this;
    }

    @Override
    public NutsSearchCommand addLockedIds(String... values) {
        if (values != null) {
            for (String s : values) {
                if (!CoreStringUtils.isBlank(s)) {
                    lockedIds.add(ws.id().parser().setLenient(false).parse(s));
                }
            }
        }
        return this;
    }

    @Override
    public NutsSearchCommand addLockedIds(NutsId... values) {
        if (values != null) {
            for (NutsId s : values) {
                if (s != null) {
                    lockedIds.add(s);
                }
            }
        }
        return this;
    }

    @Override
    public NutsSearchCommand clearLockedIds() {
        lockedIds.clear();
        return this;
    }

    @Override
    public NutsSearchCommand addArch(String value) {
        if (!CoreStringUtils.isBlank(value)) {
            this.arch.add(value);
        }
        return this;
    }

    @Override
    public NutsSearchCommand removeArch(String value) {
        this.arch.remove(value);
        return this;
    }

    @Override
    public NutsSearchCommand clearArchs() {
        this.arch.clear();
        return this;
    }

    @Override
    public NutsSearchCommand addArchs(Collection<String> values) {
        if (values != null) {
            addArchs(values.toArray(new String[0]));
        }
        return this;
    }

    @Override
    public NutsSearchCommand addArchs(String... values) {
        if (values != null) {
            arch.addAll(Arrays.asList(values));
        }
        return this;
    }

    @Override
    public NutsSearchCommand addPackaging(String value) {
        if (value != null) {
            packaging.add(value);
        }
        return this;
    }

    @Override
    public NutsSearchCommand removePackaging(String value) {
        packaging.remove(value);
        return this;
    }

    @Override
    public NutsSearchCommand clearPackagings() {
        packaging.clear();
        return this;
    }

    @Override
    public NutsSearchCommand addPackagings(Collection<String> values) {
        if (values != null) {
            addPackagings(values.toArray(new String[0]));
        }
        return this;
    }

    @Override
    public NutsSearchCommand addPackagings(String... values) {
        if (values != null) {
            this.packaging.addAll(Arrays.asList(values));
        }
        return this;
    }

    @Override
    public NutsSearchCommand copyFrom(NutsFetchCommand other) {
        super.copyFromDefaultNutsQueryBaseOptions((DefaultNutsQueryBaseOptions) other);
        return this;
    }

    @Override
    public NutsSearchCommand copyFrom(NutsSearchCommand other) {
        super.copyFromDefaultNutsQueryBaseOptions((DefaultNutsQueryBaseOptions) other);
        if (other != null) {
            NutsSearchCommand o = other;
            this.comparator = o.getComparator();
            this.dependencyFilter = o.getDependencyFilter();
            this.descriptorFilter = o.getDescriptorFilter();
            this.idFilter = o.getIdFilter();
            this.latest = o.isLatest();
            this.distinct=(o.isDistinct());
            this.includeBasePackage = o.isBasePackage();
            this.sorted = o.isSorted();
            this.arch.clear();
            this.arch.addAll(Arrays.asList(o.getArch()));
            this.ids.clear();
            this.ids.addAll(Arrays.asList(o.getIds()));
            this.scripts.clear();
            this.scripts.addAll(Arrays.asList(o.getScripts()));
            this.packaging.clear();
            this.packaging.addAll(Arrays.asList(o.getPackaging()));
            this.repositoryFilter = o.getRepositoryFilter();
            this.printResult = o.isPrintResult();
            this.installStatus = new ArrayList<Set<NutsInstallStatus>>(Arrays.asList(other.getInstallStatus()));
        }
        return this;
    }

    @Override
    public boolean isSorted() {
        return sorted;
    }

    @Override
    public NutsSearchCommand sort(Comparator comparator) {
        this.comparator = comparator;
        this.sorted = true;
        return this;
    }

    @Override
    public NutsSearchCommand setSorted(boolean sort) {
        this.sorted = sort;
        return this;
    }

    @Override
    public boolean isLatest() {
        return latest;
    }

    @Override
    public NutsSearchCommand setLatest(boolean enable) {
        this.latest = enable;
        return this;
    }

    @Override
    public NutsSearchCommand setLib(boolean enable) {
        this.execType = enable ? "lib" : null;
        return this;
    }

    @Override
    public NutsSearchCommand setExec(boolean enable) {
        this.execType = enable ? "exec" : null;
        return this;
    }


    @Override
    public NutsSearchCommand setExtension(boolean enable) {
        this.execType = enable ? "extension" : null;
        return this;
    }

    @Override
    public NutsSearchCommand setCompanion(boolean enable) {
        this.execType = enable ? "companion" : null;
        return this;
    }

    @Override
    public NutsSearchCommand setRuntime(boolean enable) {
        this.execType = enable ? "runtime" : null;
        return this;
    }

    @Override
    public NutsSearchCommand setApplication(boolean enable) {
        this.execType = enable ? "app" : null;
        return this;
    }

    @Override
    public boolean isRuntime() {
        return "runtime".equals(execType);
    }

    @Override
    public boolean isCompanion() {
        return "companion".equals(execType);
    }

    @Override
    public boolean isExtension() {
        return "extension".equals(execType);
    }

    @Override
    public boolean isExec() {
        return "exec".equals(execType);
    }

    @Override
    public boolean isApplication() {
        return "app".equals(execType);
    }

    @Override
    public boolean isLib() {
        return "lib".equals(execType);
    }

    @Override
    public NutsSearchCommand addId(NutsId id) {
        if (id != null) {
            addId(id.toString());
        }
        return this;
    }

    @Override
    public NutsSearchCommand removeId(NutsId id) {
        if (id != null) {
            removeId(id.toString());
        }
        return this;
    }

    @Override
    public NutsSearchCommand removeId(String id) {
        ids.remove(ws.id().parser().parse(id));
        return this;
    }

    @Override
    public NutsSearchCommand addId(String id) {
        if (!CoreStringUtils.isBlank(id)) {
            ids.add(ws.id().parser().setLenient(false).parse(id));
        }
        return this;
    }

    @Override
    public NutsId[] getIds() {
        return this.ids.toArray(new NutsId[0]);
    }


    @Override
    public NutsSearchCommand addLockedId(NutsId id) {
        if (id != null) {
            addLockedId(id.toString());
        }
        return this;
    }

    @Override
    public NutsSearchCommand removeLockedId(NutsId id) {
        if (id != null) {
            removeLockedId(id.toString());
        }
        return this;
    }


    @Override
    public NutsSearchCommand removeLockedId(String id) {
        lockedIds.remove(ws.id().parser().parse(id));
        return this;
    }

    @Override
    public NutsSearchCommand addLockedId(String id) {
        if (!CoreStringUtils.isBlank(id)) {
            lockedIds.add(ws.id().parser().setLenient(false).parse(id));
        }
        return this;
    }

    @Override
    public NutsId[] getLockedIds() {
        return this.lockedIds.toArray(new NutsId[0]);
    }

    //    public NutsQuery setDependencyFilter(TypedObject filter) {
//        if (filter == null) {
//            this.dependencyFilter = null;
//        } else if (NutsDependencyFilter.class.equals(filter.getType()) || String.class.equals(filter.getType())) {
//            this.dependencyFilter = filter;
//        } else {
//            throw new IllegalArgumentException("Invalid Object");
//        }
//        return this;
//    }
//
    @Override
    public NutsSearchCommand setDependencyFilter(NutsDependencyFilter filter) {
        this.dependencyFilter = filter;
        return this;
    }

    @Override
    public NutsDependencyFilter getDependencyFilter() {
        return dependencyFilter;
    }

    @Override
    public NutsSearchCommand setDependencyFilter(String filter) {
        this.dependencyFilter = ws.dependency().filter().byExpression(filter);
        return this;
    }

    @Override
    public NutsSearchCommand setRepositoryFilter(NutsRepositoryFilter filter) {
        this.repositoryFilter = filter;
        return this;
    }

    @Override
    public NutsRepositoryFilter getRepositoryFilter() {
        return repositoryFilter;
    }

    @Override
    public NutsSearchCommand setRepository(String filter) {
        this.repositoryFilter = ws.repos().filter().byName(filter);
        return this;
    }

    @Override
    public NutsSearchCommand setDescriptorFilter(NutsDescriptorFilter filter) {
        this.descriptorFilter = filter;
        return this;
    }

    @Override
    public NutsDescriptorFilter getDescriptorFilter() {
        return descriptorFilter;
    }

    @Override
    public NutsSearchCommand setDescriptorFilter(String filter) {
        this.descriptorFilter =ws.descriptor().filter().byExpression(filter);
        return this;
    }

    @Override
    public NutsSearchCommand setIdFilter(NutsIdFilter filter) {
        this.idFilter = filter;
        return this;
    }

    @Override
    public NutsIdFilter getIdFilter() {
        return idFilter;
    }

    @Override
    public NutsSearchCommand setIdFilter(String filter) {
        this.idFilter = ws.id().filter().byExpression(filter);
        return this;
    }

//    @Override
//    public String toString() {
//        StringBuilder sb = new StringBuilder("NutsSearch{");
//        sb.append(getScope());
//        if (ids.size() > 0) {
//            sb.append(",ids=").append(ids);
//        }
//        if (lockedIds.size() > 0) {
//            sb.append(",lockedIds=").append(lockedIds);
//        }
//        if (idFilter != null) {
//            sb.append(",idFilter=").append(idFilter);
//        }
//        if (dependencyFilter != null) {
//            sb.append(",dependencyFilter=").append(dependencyFilter);
//        }
//        if (repositoryFilter != null) {
//            sb.append(",repositoryFilter=").append(repositoryFilter);
//        }
//        if (descriptorFilter != null) {
//            sb.append(",descriptorFilter=").append(descriptorFilter);
//        }
//        sb.append('}');
//        return sb.toString();
//    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "failFast=" + isFailFast() +
                ", optional=" + getOptional() +
                ", scope=" + getScope() +
                ", content=" + isContent() +
                ", inlineDependencies=" + isInlineDependencies() +
                ", dependencies=" + isDependencies() +
                ", dependenciesTree=" + isDependenciesTree() +
                ", effective=" + isEffective() +
                ", location=" + getLocation() +
                ", repos=" + Arrays.toString(getRepositories()) +
                ", displayOptions=" + getDisplayOptions() +
                ", comparator=" + getComparator() +
                ", dependencyFilter=" + getDependencyFilter() +
                ", descriptorFilter=" + getDescriptorFilter() +
                ", idFilter=" + getIdFilter() +
                ", repositoryFilter=" + getRepositoryFilter() +
                ", latest=" + isLatest() +
                ", distinct=" + isDistinct() +
                ", includeMain=" + isBasePackage() +
                ", sorted=" + isSorted() +
                ", arch=" + Arrays.toString(getArch()) +
                ", ids=" + Arrays.toString(getIds()) +
                ", lockedIds=" + Arrays.toString(getLockedIds()) +
                ", scripts=" + Arrays.toString(getScripts()) +
                ", packaging=" + Arrays.toString(getPackaging()) +
                ", defaultVersions=" + getDefaultVersions() +
                ", execType='" + getExecType() + '\'' +
                ", targetApiVersion='" + getTargetApiVersion() + '\'' +
                '}';
    }

    public String getExecType() {
        return execType;
    }

    @Override
    public String[] getScripts() {
        return scripts.toArray(new String[0]);
    }

    @Override
    public String[] getArch() {
        return arch.toArray(new String[0]);
    }

    @Override
    public String[] getPackaging() {
        return this.packaging.toArray(new String[0]);
    }

    @Override
    public NutsFetchCommand toFetch() {
        NutsFetchCommand t = new DefaultNutsFetchCommand(ws).copyFromDefaultNutsQueryBaseOptions(this)
                .setSession(evalSession(true));
        if (getDisplayOptions().isRequireDefinition()) {
            t.setContent(true);
        }
        return t;
    }

    private NutsSession evalSession(boolean create) {
        NutsSession s = getSession();
        if (create) {
            if (s == null) {
                s = ws.createSession();
            }
        }
        return s;
//        if (mode != null) {
//            if (s == null) {
//                s = ws.createSession();
//            }
//            s.setFetchMode(mode);
//            return s;
//        } else {
//            return s;
//        }
    }


    @Override
    public boolean isDistinct() {
        return distinct;
    }

    @Override
    public NutsSearchCommand setDistinct(boolean distinct) {
        this.distinct = distinct;
        return this;
    }

    @Override
    public Comparator getComparator() {
        return comparator;
    }

    @Override
    public String getTargetApiVersion() {
        return targetApiVersion;
    }

    @Override
    public NutsSearchCommand setTargetApiVersion(String targetApiVersion) {
        this.targetApiVersion = targetApiVersion;
        return this;
    }

    @Override
    public boolean isBasePackage() {
        return includeBasePackage;
    }

    @Override
    public NutsSearchCommand setBasePackage(boolean includeBasePackage) {
        this.includeBasePackage = includeBasePackage;
        return this;
    }

    @Override
    public ClassLoader getResultClassLoader() {
        return getResultClassLoader(null);
    }

    @Override
    public ClassLoader getResultClassLoader(ClassLoader parent) {
        List<NutsDefinition> nutsDefinitions = getResultDefinitions().list();
        URL[] allURLs = new URL[nutsDefinitions.size()];
        NutsId[] allIds = new NutsId[nutsDefinitions.size()];
        for (int i = 0; i < allURLs.length; i++) {
            allURLs[i] = nutsDefinitions.get(i).getURL();
            allIds[i] = nutsDefinitions.get(i).getId();
        }
        return ((DefaultNutsWorkspaceExtensionManager) ws.extensions()).getNutsURLClassLoader(allURLs, allIds,parent);
    }

    @Override
    public boolean configureFirst(NutsCommandLine cmdLine) {
        NutsArgument a = cmdLine.peek();
        if (a == null) {
            return false;
        }
        boolean enabled = a.isEnabled();
        switch (a.getStringKey()) {
            case "--inline-dependencies": {
                boolean val = cmdLine.nextBoolean().getBooleanValue();
                if(enabled) {
                    this.setInlineDependencies(val);
                }
                return true;
            }
            case "-L":
            case "--latest":
            case "--latest-versions": {
                cmdLine.skip();
                if(enabled) {
                    this.setLatest(true);
                }
                return true;
            }
            case "--distinct": {
                boolean val = cmdLine.nextBoolean().getBooleanValue();
                if(enabled) {
                    this.setDistinct(val);
                }
                return true;
            }
            case "--default":
            case "--default-versions": {
                Boolean val = cmdLine.nextBoolean().getBoolean(null);
                if(enabled) {
                    this.setDefaultVersions(val);
                }
                return true;
            }
            case "--duplicates": {
                boolean val = !cmdLine.nextBoolean().getBooleanValue();
                if(enabled) {
                    this.setDistinct(val);
                }
                return true;
            }
            case "-s":
            case "--sort": {
                boolean val = cmdLine.nextBoolean().getBooleanValue();
                if(enabled) {
                    this.setSorted(val);
                }
                return true;
            }
            case "--base": {
                boolean val = cmdLine.nextBoolean().getBooleanValue();
                if(enabled) {
                    this.includeBasePackage = val;
                }
                return true;
            }
            case "--libs": {
                boolean val = cmdLine.nextBoolean().getBooleanValue();
                if(enabled) {
                    this.setLib(val);
                }
                return true;
            }
            case "--apps": {
                boolean val = cmdLine.nextBoolean().getBooleanValue();
                if(enabled) {
                    this.setExec(val);
                }
                return true;
            }
            case "--companions": {
                boolean val = cmdLine.nextBoolean().getBooleanValue();
                if(enabled) {
                    this.setCompanion(val);
                }
                return true;
            }
            case "--extensions": {
                boolean val = cmdLine.nextBoolean().getBooleanValue();
                if(enabled) {
                    this.setExtension(val);
                }
                return true;
            }
            case "--runtime": {
                boolean val = cmdLine.nextBoolean().getBooleanValue();
                if(enabled) {
                    this.setRuntime(val);
                }
                return true;
            }
            case "--api-version": {
                String val = cmdLine.nextBoolean().getStringValue();
                if(enabled) {
                    this.setTargetApiVersion(val);
                }
                return true;
            }
            case "--nuts-apps": {
                boolean val = cmdLine.nextBoolean().getBooleanValue();
                if(enabled) {
                    this.setApplication(val);
                }
                return true;
            }
            case "--arch": {
                String val = cmdLine.nextString().getStringValue();
                if(enabled) {
                    this.addArch(val);
                }
                return true;
            }
            case "--packaging": {
                String val = cmdLine.nextString().getStringValue();
                if(enabled) {
                    this.addPackaging(val);
                }
                return true;
            }
            case "--optional": {
                NutsArgument val = cmdLine.nextString();
                if(enabled) {
                    this.setOptional(CoreCommonUtils.parseBoolean(val.getStringValue(), null));
                }
                return true;
            }
            case "--script": {
                String val = cmdLine.nextString().getStringValue();
                if(enabled) {
                    this.addScript(val);
                }
                return true;
            }
            case "--id": {
                String val = cmdLine.nextString().getStringValue();
                if(enabled) {
                    this.addId(val);
                }
                return true;
            }
            case "--locked-id": {
                String val = cmdLine.nextString().getStringValue();
                if(enabled) {
                    this.addLockedId(val);
                }
                return true;
            }
            case "--print": {
                boolean val = cmdLine.nextBoolean().getBooleanValue();
                if(enabled) {
                    this.setPrintResult(val);
                }
                return true;
            }
            case "--installed-or-required": {
                cmdLine.skip();
                if(enabled) {
                    this.addInstallStatus(NutsInstallStatus.INSTALLED);
                    this.addInstallStatus(NutsInstallStatus.REQUIRED);
                }
                return true;
            }
            case "--not-installed": {
                cmdLine.skip();
                if(enabled) {
                    this.addInstallStatus(NutsInstallStatus.NOT_INSTALLED);
                }
                return true;
            }
            case "-i":
            case "--installed": {
                cmdLine.skip();
                if(enabled) {
                    this.addInstallStatus(NutsInstallStatus.INSTALLED);
                }
                return true;
            }
            case "--required": {
                cmdLine.skip();
                if(enabled) {
                    this.addInstallStatus(NutsInstallStatus.REQUIRED);
                }
                return true;
            }
            case "--obsolete": {
                cmdLine.skip();
                if(enabled) {
                    this.addInstallStatus(NutsInstallStatus.OBSOLETE);
                }
                return true;
            }
            case "--status": {
                NutsArgument aa = cmdLine.nextString();
                if(enabled) {
                    String sv = aa.getStringValue();
                    if(sv==null|| sv.isEmpty()){
                        throw new NutsIllegalArgumentException(getWorkspace(),"Invalid status");
                    }
                    List<NutsInstallStatus> ss=new ArrayList<>();
                    for (String s : sv.split("[&+]")) {
                        s=s.trim();
                        if(s.length()>0){
                            s=s.toUpperCase();
                            ss.add(NutsInstallStatus.valueOf(s));
                        }
                    }
                    this.addInstallStatus(ss.toArray(new NutsInstallStatus[0]));
                }
                return true;
            }
            default: {
                if (super.configureFirst(cmdLine)) {
                    return true;
                }
                if (a.isOption()) {
                    return false;
                } else {
                    cmdLine.skip();
                    addId(a.getString());
                    return true;
                }
            }
        }
    }

    @Override
    public boolean isPrintResult() {
        return printResult;
    }

//    @Override
//    public NutsSearchCommand printResult() {
//        return printResult(true);
//    }
//
//    @Override
//    public NutsSearchCommand printResult(boolean printResult) {
//        return setPrintResult(printResult);
//    }

    @Override
    public NutsSearchCommand setPrintResult(boolean printResult) {
        this.printResult = printResult;
        return this;
    }

    @Override
    public Set<NutsInstallStatus>[] getInstallStatus() {
        return installStatus.toArray(new Set[0]);
    }

    @Override
    public NutsSearchCommand addInstallStatus(NutsInstallStatus... installStatus) {
        if(installStatus!=null && installStatus.length>0){
            this.installStatus.add(EnumSet.copyOf(Arrays.asList(installStatus)));
        }
        return this;
    }

    @Override
    public NutsSearchCommand removeInstallStatus(NutsInstallStatus... installStatus) {
        if(installStatus!=null && installStatus.length>0){
            this.installStatus.remove(EnumSet.copyOf(Arrays.asList(installStatus)));
        }
        return this;
    }

}
