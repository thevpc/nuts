/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . It's based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 * <p>
 * Copyright [2020] [thevpc]
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License");
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.format.props;

import net.thevpc.nuts.NUnsupportedOperationException;
import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.runtime.standalone.format.elem.NElementStreamFormat;
import net.thevpc.nuts.util.NHex;
import net.thevpc.nuts.util.NMsg;

import java.io.Reader;
import java.io.StringReader;

/**
 * @author thevpc
 */
public class DefaultPropsElementFormat implements NElementStreamFormat {

    public DefaultPropsElementFormat() {
    }

    public NElement parseElement(String string, NElementFactoryContext context) {
        if (string == null) {
            throw new NullPointerException("string is null");
        }
        return parseElement(new StringReader(string), context);
    }

    @Override
    public NElement normalize(NElement e) {
        return e;
    }

    public void write(NPrintStream out, NElement data, boolean compact) {
        write(out, data, compact ? null : "");
    }

    private void write(NPrintStream out, NElement data, String indent) {

        switch (data.type()) {
            case NULL: {
                out.print("null");
                break;
            }
            case BOOLEAN: {
                out.print(data.asBooleanValue().orElse(false));
                break;
            }
            case BYTE:
            case SHORT:
            case INTEGER:
            case LONG: {
                out.print(data.asNumberValue().orElse(0));
                break;
            }
            case FLOAT:
            case DOUBLE: {
                out.print(data.asNumberValue().orElse(0.0));
                break;
            }
            case INSTANT:
            case DOUBLE_QUOTED_STRING:
            case SINGLE_QUOTED_STRING:
            case ANTI_QUOTED_STRING:
            case TRIPLE_DOUBLE_QUOTED_STRING:
            case TRIPLE_SINGLE_QUOTED_STRING:
            case TRIPLE_ANTI_QUOTED_STRING:
            case LINE_STRING:
//            case NUTS_STRING:
            {
                StringBuilder sb = new StringBuilder("\"");
                final String str = data.asStringValue().orElse("");
                char[] chars = str.toCharArray();

                for (int i = 0; i < chars.length; i++) {
                    char c = chars[i];
                    if (c < 32) {
                        switch (c) {
                            case '\n': {
                                sb.append('\\').append('n');
                                break;
                            }
                            case '\f': {
                                sb.append('\\').append('f');
                                break;
                            }
                            case '\t': {
                                sb.append('\\').append('t');
                                break;
                            }
                            case '\r': {
                                sb.append('\\').append('r');
                                break;
                            }
                            case '\b': {
                                sb.append('\\').append('b');
                                break;
                            }
                            default: {
                                sb.append('\\');
                                sb.append('u');
                                sb.append(NHex.toHexChar((c >> 12) & 0xF));
                                sb.append(NHex.toHexChar((c >> 8) & 0xF));
                                sb.append(NHex.toHexChar((c >> 4) & 0xF));
                                sb.append(NHex.toHexChar(c & 0xF));
                            }
                        }
                    } else {
                        switch (c) {
                            case '\\': {
                                sb.append(c).append(c);
                                break;
                            }
                            case '"': {
                                sb.append('\\').append('"');
                                break;
                            }
                            default: {
                                if (c > 0x007e) {
                                    sb.append('\\');
                                    sb.append('u');
                                    sb.append(NHex.toHexChar((c >> 12) & 0xF));
                                    sb.append(NHex.toHexChar((c >> 8) & 0xF));
                                    sb.append(NHex.toHexChar((c >> 4) & 0xF));
                                    sb.append(NHex.toHexChar(c & 0xF));
                                } else {
                                    sb.append(c);
                                }
                            }
                        }
                    }
                }
                sb.append('\"');
                out.print(sb);
                break;
            }

            case ARRAY: {
                NArrayElement arr = data.asArray().get();
                if (arr.size() == 0) {
                    out.print("[]");
                } else {
                    out.print('[');
                    boolean first = true;
                    String indent2 = indent + "  ";
                    for (NElement e : arr.children()) {
                        if (first) {
                            first = false;
                        } else {
                            out.print(',');
                        }
                        if (indent != null) {
                            out.print('\n');
                            out.print(indent2);
                            write(out, e, indent2);
                        } else {
                            write(out, e, null);
                        }
                    }
                    if (indent != null) {
                        out.print('\n');
                        out.print(indent);
                    }
                    out.print(']');
                }
                break;
            }
            case OBJECT: {
                NObjectElement obj = data.asObject().get();
                if (obj.size() > 0) {
                    boolean first = true;
                    String indent2 = indent + "  ";
                    for (NElement e : obj.children()) {
                        if (first) {
                            first = false;
                        } else {
                            out.print("\n");
                        }
                        if (indent != null) {
                            out.print('\n');
                            out.print(indent2);
                            if (e instanceof NPairElement) {
                                NPairElement ee = (NPairElement) e;
                                write(out, ee.key(), indent2);
                                out.print(':');
                                out.print(' ');
                                write(out, ee.value(), indent2);
                            } else {
                                write(out, e, indent2);
                            }
                        } else {
                            if (e instanceof NPairElement) {
                                NPairElement ee = (NPairElement) e;
                                write(out, ee.key(), null);
                                out.print(':');
                                write(out, ee.value(), null);
                            } else {
                                write(out, e, null);
                            }
                        }
                    }
                    if (indent != null) {
                        out.print('\n');
                        out.print(indent);
                    }
                }
                break;
            }
            default: {
                throw new IllegalArgumentException("unsupported");
            }
        }
    }

    @Override
    public NElement parseElement(Reader reader, NElementFactoryContext context) {
        throw new NUnsupportedOperationException(NMsg.ofC("unable to parse Props for the moment"));
    }

    @Override
    public void printElement(NElement value, NPrintStream out, boolean compact, NElementFactoryContext context) {
        write(out, value, compact);
    }

}
