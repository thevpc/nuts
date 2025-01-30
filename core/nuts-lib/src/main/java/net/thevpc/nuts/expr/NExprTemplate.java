package net.thevpc.nuts.expr;

import net.thevpc.nuts.io.NInputSource;

import java.io.*;

public interface NExprTemplate {

    NExprTemplate withJspStyle();

    NExprTemplate withMoustacheStyle();

    NExprTemplate withBashStyle();

    NExprTemplate withBoundaries(String start, String stop);

    NExprTemplate process(InputStream inputStream, OutputStream outputStream);

    NExprTemplate process(Reader inputStream, Writer outputStream);

    NExprTemplate process(Reader inputStream, PrintStream outputStream);

    String processString(String string);

    NExprCompiledTemplate compile(InputStream inputStream);

    NExprCompiledTemplate compile(Reader inputStream);

    NExprCompiledTemplate compile(String string);

    NExprCompiledTemplate compile(NInputSource source);
}
