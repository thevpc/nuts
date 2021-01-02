package net.thevpc.nuts.runtime.core.format;

import net.thevpc.nuts.NutsCommandLine;
import net.thevpc.nuts.NutsStringFormat;
import net.thevpc.nuts.NutsTextFormatStyle;
import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.runtime.core.format.text.util.FormattedPrintStreamUtils;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class DefaultNutsStringFormat extends DefaultFormatBase<NutsStringFormat> implements NutsStringFormat {
    private StringBuilder sb = new StringBuilder();
    private List<Object> parameters = new ArrayList<>();
    private NutsTextFormatStyle style = NutsTextFormatStyle.CSTYLE;

    public DefaultNutsStringFormat(NutsWorkspace ws) {
        super(ws, "string");
    }

    @Override
    public NutsTextFormatStyle getStyle() {
        return style;
    }

    @Override
    public NutsStringFormat style(NutsTextFormatStyle style) {
        return setStyle(style);
    }

    @Override
    public NutsStringFormat setStyle(NutsTextFormatStyle style) {
        this.style = style == null ? NutsTextFormatStyle.CSTYLE : style;
        return this;
    }

    @Override
    public Object[] getParameters() {
        return parameters.toArray(new Object[0]);
    }

    @Override
    public NutsStringFormat addParameters(Object... parameters) {
        if (parameters != null && parameters.length > 0) {
            this.parameters.addAll(Arrays.asList(parameters));
        }
        return this;
    }

    @Override
    public NutsStringFormat setParameters(Object... parameters) {
        this.parameters = new ArrayList<>();
        if (parameters != null) {
            this.parameters.addAll(Arrays.asList(parameters));
        }
        return this;
    }

    @Override
    public NutsStringFormat setParameters(List<Object> parameters) {
        this.parameters = new ArrayList<>();
        if (parameters != null) {
            this.parameters.addAll(parameters);
        }
        return this;
    }

    @Override
    public String getString() {
        return sb.toString();
    }

    @Override
    public NutsStringFormat of(String value, Object... parameters) {
        setString(value);
        setParameters(parameters);
        return this;
    }

    @Override
    public NutsStringFormat append(String value, Object... parameters) {
        sb.append(value);
        addParameters(parameters);
        return this;
    }

    @Override
    public NutsStringFormat set(String value) {
        return setString(value);
    }

    @Override
    public NutsStringFormat setString(Object value) {
        sb.delete(0, sb.length());
        if (value != null) {
            sb.append(value);
        }
        return this;
    }

    private String format0() {
        if (style == NutsTextFormatStyle.CSTYLE) {
            return FormattedPrintStreamUtils.formatCStyle(getSession(), Locale.getDefault(), sb.toString(), getParameters());
        } else {
            return FormattedPrintStreamUtils.formatPositionalStyle(getSession(), Locale.getDefault(), sb.toString(), getParameters());
        }
    }

    @Override
    public String format() {
        return format0();//super.format();
    }

    @Override
    public void print(PrintStream out) {
        out.print(format0());
    }

    @Override
    public boolean configureFirst(NutsCommandLine commandLine) {
        return false;
    }
}
