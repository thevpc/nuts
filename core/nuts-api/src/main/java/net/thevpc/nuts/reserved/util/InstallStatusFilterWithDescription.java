package net.thevpc.nuts.reserved.util;

import net.thevpc.nuts.NInstallStatus;
import net.thevpc.nuts.NInstallStatusFilter;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.elem.NEDesc;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.spi.base.AbstractInstallStatusFilter;
import net.thevpc.nuts.util.NFilter;
import net.thevpc.nuts.util.NFilterOp;

import java.util.List;

public class InstallStatusFilterWithDescription extends AbstractInstallStatusFilter {
    private NInstallStatusFilter base;
    private NEDesc description;

    public InstallStatusFilterWithDescription(NInstallStatusFilter base, NEDesc description) {
        super(base.getSession(), NFilterOp.CUSTOM);
        this.base = base;
        this.description = description;
    }


    @Override
    public boolean acceptInstallStatus(NInstallStatus status, NSession session) {
        return base.acceptInstallStatus(status,session );
    }

    @Override
    public NFilter withDesc(NEDesc description) {
        this.description = description;
        return this;
    }

    @Override
    public NFilterOp getFilterOp() {
        return base.getFilterOp();
    }

    @Override
    public NSession getSession() {
        return base.getSession();
    }

    @Override
    public Class<? extends NFilter> getFilterType() {
        return base.getFilterType();
    }

    @Override
    public <T extends NFilter> NFilter simplify(Class<T> type) {
        return base.simplify(type);
    }

    @Override
    public NInstallStatusFilter simplify() {
        return (NInstallStatusFilter) base.simplify();
    }

    @Override
    public <T extends NFilter> T to(Class<T> type) {
        return base.to(type);
    }

    @Override
    public List<NFilter> getSubFilters() {
        return base.getSubFilters();
    }

    @Override
    public NElement describe(NSession session) {
        return NEDesc.safeDescribeOfBase(session, description,base);
    }
}
