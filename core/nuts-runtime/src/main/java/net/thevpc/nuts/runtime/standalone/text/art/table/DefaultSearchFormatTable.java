/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.text.art.table;

import java.util.Arrays;

import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.core.NSession;
import net.thevpc.nuts.text.*;
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

    private NMutableTableModel model;

    public DefaultSearchFormatTable(NPrintStream writer, NFetchDisplayOptions options) {
        super(writer, NContentType.TABLE, options);
    }

    public NMutableTableModel getTableModel() {
        if (model == null) {
            model = NMutableTableModel.of();
            NSession session = NSession.of();

            if (session.outputFormatOptions() != null) {
                for (String outputFormatOption : session.outputFormatOptions()) {
                    if (outputFormatOption != null) {
                        //table.configure(true, NCmdLine.of(outputFormatOption, NShellFamily.BASH).setExpandSimpleOptions(false));
                    }
                }
            }
        }
        return model;
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
                .addHeaderRow(
                        Arrays.stream(getDisplayOptions().getDisplayProperties())
                                .map(x -> NTableCell.of(NText.of(CoreEnumUtils.getEnumString(x)))).toArray(NTableCell[]::new)
                );
    }

    @Override
    public void next(Object object, long index) {
        NIdFormatHelper fid = NIdFormatHelper.of(object);
        if (fid != null) {
            formatElement(fid, index);
        } else {
            getTableModel().newRow().addCell(NTableCell.of(NText.of(object)));
        }
        getWriter().flush();
    }

    public void formatElement(NIdFormatHelper id, long index) {
        getTableModel().newRow().addCells(
                Arrays.stream(id.getMultiColumnRow(getDisplayOptions()))
                        .map(x-> NTableCell.of(x))
                        .toArray(NTableCell[]::new)
        );
    }

    @Override
    public void complete(long count) {
        getWriter().println(NTextArt.of().tableRenderer().get().render(getTableModel()));
    }

}
