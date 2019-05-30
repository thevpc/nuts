/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.io;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Locale;
import net.vpc.app.nuts.core.util.fprint.util.FormattedPrintStreamUtils;
import net.vpc.app.nuts.NutsTerminalFormat;
import net.vpc.app.nuts.core.util.fprint.ExtendedFormatAware;
import net.vpc.app.nuts.core.util.fprint.ExtendedFormatAwarePrintStream;
import net.vpc.app.nuts.core.util.fprint.ExtendedFormatAwarePrintWriter;

/**
 *
 * @author vpc
 */
public class DefaultNutsTerminalFormat implements NutsTerminalFormat {

    @Override
    public int textLength(String value) {
        return filterText(value).length();
    }

    @Override
    public String filterText(String value) {
        return FormattedPrintStreamUtils.filterText(value);
    }

    @Override
    public String escapeText(String str) {
        if (str == null) {
            return "";
        }
        return FormattedPrintStreamUtils.escapeText(str);
    }

    /**
     *
     * @param locale
     * @param format
     * @param args
     * @return
     */
    @Override
    public String formatText(Locale locale, String format, Object... args) {
        return FormattedPrintStreamUtils.format(locale, format, args);
    }

    @Override
    public String formatText(String format, Object... args) {
        return FormattedPrintStreamUtils.format(Locale.getDefault(), format, args);
    }

    @Override
    public PrintStream prepare(PrintStream out) {
        if (out instanceof ExtendedFormatAware) {
            return out;
        }
        return new ExtendedFormatAwarePrintStream(out);
    }

    @Override
    public PrintWriter prepare(PrintWriter out) {
        if (out instanceof ExtendedFormatAware) {
            return out;
        }
        return new ExtendedFormatAwarePrintWriter(out);
    }
}
