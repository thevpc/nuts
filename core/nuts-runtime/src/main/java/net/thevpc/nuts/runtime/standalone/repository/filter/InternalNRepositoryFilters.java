package net.thevpc.nuts.runtime.standalone.repository.filter;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.util.filters.InternalNTypedFilters;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import net.thevpc.nuts.runtime.standalone.repository.impl.main.DefaultNInstalledRepository;
import net.thevpc.nuts.spi.NSupportLevelContext;

public class InternalNRepositoryFilters extends InternalNTypedFilters<NRepositoryFilter>
        implements NRepositoryFilters {

    public InternalNRepositoryFilters(NSession session) {
        super(session, NRepositoryFilter.class);
    }

    @Override
    public NRepositoryFilter always() {
        checkSession();
        return new NRepositoryFilterTrue(getSession());
    }

    @Override
    public NRepositoryFilter never() {
        checkSession();
        return new NRepositoryFilterFalse(getSession());
    }

    @Override
    public NRepositoryFilter not(NFilter other) {
        checkSession();
        return new NRepositoryFilterNone(getSession(), (NRepositoryFilter) other);
    }

    @Override
    public NRepositoryFilter installedRepo() {
        checkSession();
        return new DefaultNRepositoryFilter(getSession(), Arrays.asList(DefaultNInstalledRepository.INSTALLED_REPO_UUID));
    }

    @Override
    public NRepositoryFilter byName(String[] names) {
        checkSession();
        if (names == null || names.length == 0) {
            return always();
        }
        List<String> namesList = Arrays.asList(names).stream()
                .filter(x -> !NBlankable.isBlank(x))
                .map(x -> "=" + x.trim()).collect(Collectors.toList());
        if (namesList.isEmpty()) {
            return always();
        }
        return new DefaultNRepositoryFilter(getSession(), namesList);
    }

    @Override
    public NRepositoryFilter byNameSelector(String... names) {
        checkSession();
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
        return new DefaultNRepositoryFilter(getSession(), namesList);
    }

    @Override
    public NRepositoryFilter byUuid(String... uuids) {
        checkSession();
        if (uuids == null || uuids.length == 0) {
            return always();
        }
        //TODO should create another class for uuids!
        return new DefaultNRepositoryFilter(getSession(), Arrays.asList(uuids));
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
        checkSession();
        if (a == null) {
            return null;
        }
        NRepositoryFilter t = as(a);
        if (t == null) {
            throw new NIllegalArgumentException(getSession(), NMsg.ofPlain("not a RepositoryFilter"));
        }
        return t;
    }

    @Override
    public NRepositoryFilter all(NFilter... others) {
        checkSession();
        List<NRepositoryFilter> all = convertList(others);
        if (all.isEmpty()) {
            return always();
        }
        if (all.size() == 1) {
            return all.get(0);
        }
        return new NRepositoryFilterAnd(getSession(), all.toArray(new NRepositoryFilter[0]));
    }

    @Override
    public NRepositoryFilter any(NFilter... others) {
        checkSession();
        List<NRepositoryFilter> all = convertList(others);
        if (all.isEmpty()) {
            return always();
        }
        if (all.size() == 1) {
            return all.get(0);
        }
        return new NRepositoryFilterOr(getSession(), all.toArray(new NRepositoryFilter[0]));
    }

    @Override
    public NRepositoryFilter none(NFilter... others) {
        checkSession();
        List<NRepositoryFilter> all = convertList(others);
        if (all.isEmpty()) {
            return always();
        }
        return new NRepositoryFilterNone(getSession(), all.toArray(new NRepositoryFilter[0]));
    }

    @Override
    public NRepositoryFilter parse(String expression) {
        checkSession();
        return new NRepositoryFilterParser(expression, getSession()).parse();
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return DEFAULT_SUPPORT;
    }
}
