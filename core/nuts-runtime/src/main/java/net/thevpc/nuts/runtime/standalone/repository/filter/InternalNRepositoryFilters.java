package net.thevpc.nuts.runtime.standalone.repository.filter;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.util.filters.InternalNTypedFilters;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import net.thevpc.nuts.runtime.standalone.repository.impl.main.DefaultNInstalledRepository;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NFilter;
import net.thevpc.nuts.util.NMsg;

public class InternalNRepositoryFilters extends InternalNTypedFilters<NRepositoryFilter>
        implements NRepositoryFilters {

    public InternalNRepositoryFilters(NWorkspace workspace) {
        super(workspace, NRepositoryFilter.class);
    }

    @Override
    public NRepositoryFilter always() {
        return new NRepositoryFilterTrue(getWorkspace());
    }

    @Override
    public NRepositoryFilter never() {
        return new NRepositoryFilterFalse(getWorkspace());
    }

    @Override
    public NRepositoryFilter not(NFilter other) {
        return new NRepositoryFilterNone(getWorkspace(), (NRepositoryFilter) other);
    }

    @Override
    public NRepositoryFilter installedRepo() {
        return new DefaultNRepositoryUuidFilter(getWorkspace(), Arrays.asList(DefaultNInstalledRepository.INSTALLED_REPO_UUID));
    }

    @Override
    public NRepositoryFilter byName(String[] names) {
        if (names == null || names.length == 0) {
            return always();
        }
        List<String> namesList = Arrays.asList(names).stream()
                .filter(x -> !NBlankable.isBlank(x))
                .map(String::trim).collect(Collectors.toList());
        if (namesList.isEmpty()) {
            return always();
        }
        return new DefaultNRepositoryNameFilter(getWorkspace(), namesList);
    }

    @Override
    public NRepositoryFilter bySelector(String[] names) {
        if (names == null || names.length == 0) {
            return always();
        }
        List<String> namesList = Arrays.asList(names).stream()
                .filter(x -> !NBlankable.isBlank(x))
                .map(x -> {
                    String i = x.trim();
                    switch (i.charAt(0)) {
                        case '=':
                        case '+':
                        case '-':
                            return i;
                    }
                    return "=" + i;
                }).collect(Collectors.toList());
        if (namesList.isEmpty()) {
            return always();
        }
        return new DefaultNRepositorySelectorFilter(getWorkspace(), namesList);
    }

    @Override
    public NRepositoryFilter byNameSelector(String... names) {
        if (names == null || names.length == 0) {
            return always();
        }
        List<String> namesList = Arrays.asList(names).stream()
                .filter(x -> !NBlankable.isBlank(x))
                .map(x -> {
                    String v = x.trim();
                    char s = v.charAt(0);
                    switch (s) {
                        case '+':
                        case '-':
                        case '=': {
                            return v;
                        }
                        default: {
                            return '+' + v;
                        }
                    }
                }).collect(Collectors.toList());
        if (namesList.isEmpty()) {
            return always();
        }
        return new DefaultNRepositorySelectorFilter(getWorkspace(), namesList);
    }

    @Override
    public NRepositoryFilter byUuid(String... uuids) {
        if (uuids == null || uuids.length == 0) {
            return always();
        }
        return new DefaultNRepositoryUuidFilter(getWorkspace(), Arrays.asList(uuids));
    }

    @Override
    public NRepositoryFilter as(NFilter a) {
        if (a instanceof NRepositoryFilter) {
            return (NRepositoryFilter) a;
        }
        return null;
    }

    @Override
    public NRepositoryFilter from(NFilter a) {
        if (a == null) {
            return null;
        }
        NRepositoryFilter t = as(a);
        if (t == null) {
            throw new NIllegalArgumentException(NMsg.ofPlain("not a RepositoryFilter"));
        }
        return t;
    }

    @Override
    public NRepositoryFilter all(NFilter... others) {
        List<NRepositoryFilter> all = convertList(others);
        if (all.isEmpty()) {
            return always();
        }
        if (all.size() == 1) {
            return all.get(0);
        }
        return new NRepositoryFilterAnd(getWorkspace(), all.toArray(new NRepositoryFilter[0]));
    }

    @Override
    public NRepositoryFilter any(NFilter... others) {
        List<NRepositoryFilter> all = convertList(others);
        if (all.isEmpty()) {
            return always();
        }
        if (all.size() == 1) {
            return all.get(0);
        }
        return new NRepositoryFilterOr(getWorkspace(), all.toArray(new NRepositoryFilter[0]));
    }

    @Override
    public NRepositoryFilter none(NFilter... others) {
        List<NRepositoryFilter> all = convertList(others);
        if (all.isEmpty()) {
            return always();
        }
        return new NRepositoryFilterNone(getWorkspace(), all.toArray(new NRepositoryFilter[0]));
    }

    @Override
    public NRepositoryFilter parse(String expression) {
        return new NRepositoryFilterParser(expression, getWorkspace()).parse();
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return NConstants.Support.DEFAULT_SUPPORT;
    }
}
