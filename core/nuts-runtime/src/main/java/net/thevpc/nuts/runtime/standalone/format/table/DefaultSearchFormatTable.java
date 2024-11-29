/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.format.table;

import java.util.Arrays;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.env.NShellFamily;
import net.thevpc.nuts.format.NContentType;
import net.thevpc.nuts.format.NMutableTableModel;
import net.thevpc.nuts.format.NTableFormat;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.runtime.standalone.format.NIdFormatHelper;
import net.thevpc.nuts.runtime.standalone.format.DefaultSearchFormatBase;
import net.thevpc.nuts.runtime.standalone.format.NFetchDisplayOptions;
import net.thevpc.nuts.runtime.standalone.util.CoreEnumUtils;

/**
 *
 * @author thevpc
 */
public class DefaultSearchFormatTable extends DefaultSearchFormatBase {

    private NTableFormat table;
    private NMutableTableModel model;

    public DefaultSearchFormatTable(NWorkspace workspace, NPrintStream writer, NFetchDisplayOptions options) {
        super(workspace, writer, NContentType.TABLE, options);
    }

    public NMutableTableModel getTableModel() {
        getTable();
        return model;
    }

    public NTableFormat getTable() {
        if (table == null) {
            table = NTableFormat.of();
            model = NMutableTableModel.of();
            table.setValue(model);
            NSession session = getWorkspace().currentSession();
            if (session.getOutputFormatOptions() != null) {
                for (String outputFormatOption : session.getOutputFormatOptions()) {
                    if (outputFormatOption != null) {
                        table.configure(true, NCmdLine.of(outputFormatOption, NShellFamily.BASH).setExpandSimpleOptions(false));
                    }
                }
            }
        }
        return table;
    }

    @Override
    public boolean configureFirst(NCmdLine cmdLine) {
        NArg a = cmdLine.peek().get();
        if (a == null) {
            return false;
        }
        if (getDisplayOptions().configureFirst(cmdLine)) {
            return true;
        }
        return false;
    }

    @Override
    public void start() {
        getTableModel()
                .addHeaderCells(
                        Arrays.stream(getDisplayOptions().getDisplayProperties())
                                .map(x -> CoreEnumUtils.getEnumString(x)).toArray()
                );
    }

    @Override
    public void next(Object object, long index) {
        NIdFormatHelper fid = NIdFormatHelper.of(object);
        if (fid != null) {
            formatElement(fid, index);
        } else {
            getTableModel().newRow().addCell(object);
        }
        getWriter().flush();
    }

    public void formatElement(NIdFormatHelper id, long index) {
        getTableModel().newRow().addCells((Object[]) id.getMultiColumnRow(getDisplayOptions()));
    }

    @Override
    public void complete(long count) {
        getTable().println(getWriter());
    }

}
