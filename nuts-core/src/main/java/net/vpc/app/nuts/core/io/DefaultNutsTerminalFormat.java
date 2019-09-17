/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.io;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Locale;

import net.vpc.app.nuts.NutsTextFormatStyle;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.core.spi.NutsWorkspaceAware;
import net.vpc.app.nuts.core.util.fprint.util.FormattedPrintStreamUtils;
import net.vpc.app.nuts.NutsTerminalFormat;
import net.vpc.app.nuts.core.util.fprint.ExtendedFormatAware;
import net.vpc.app.nuts.core.util.fprint.ExtendedFormatAwarePrintStream;
import net.vpc.app.nuts.core.util.fprint.ExtendedFormatAwarePrintWriter;
import net.vpc.app.nuts.core.util.fprint.FormattedPrintStream;

/**
 * @author vpc
 */
public class DefaultNutsTerminalFormat implements NutsTerminalFormat, NutsWorkspaceAware {
    private NutsWorkspace ws;

    @Override
    public void setWorkspace(NutsWorkspace workspace) {
        this.ws=workspace;
    }

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
     * @param style
     * @param locale
     * @param format
     * @param args
     * @return
     */
    @Override
    public String formatText(NutsTextFormatStyle style, Locale locale, String format, Object... args) {
        if (style == NutsTextFormatStyle.CSTYLE) {
            return FormattedPrintStreamUtils.formatCStyle(ws,locale, format, args);
        } else {
            return FormattedPrintStreamUtils.formatPositionalStyle(ws,locale, format, args);
        }
    }

    @Override
    public String formatText(NutsTextFormatStyle style, String format, Object... args) {
        if (style == NutsTextFormatStyle.CSTYLE) {
            return FormattedPrintStreamUtils.formatCStyle(ws,Locale.getDefault(), format, args);
        } else {
            return FormattedPrintStreamUtils.formatPositionalStyle(ws,Locale.getDefault(), format, args);
        }
    }

    @Override
    public PrintStream prepare(PrintStream out) {
        if (out instanceof ExtendedFormatAware) {
            return out;
        }
        ExtendedFormatAwarePrintStream s = new ExtendedFormatAwarePrintStream(out);
        s.setWorkspace(ws);
        return s;
    }

    @Override
    public PrintWriter prepare(PrintWriter out) {
        if (out instanceof ExtendedFormatAware) {
            return out;
        }
        ExtendedFormatAwarePrintWriter w = new ExtendedFormatAwarePrintWriter(out);
        w.setWorkspace(ws);
        return w;
    }

    @Override
    public boolean isFormatted(OutputStream out) {
        if (out instanceof ExtendedFormatAware) {
            return true;
        }
        if (out instanceof FormattedPrintStream) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isFormatted(Writer out) {
        if (out instanceof ExtendedFormatAware) {
            return true;
        }
        return false;
    }

}
