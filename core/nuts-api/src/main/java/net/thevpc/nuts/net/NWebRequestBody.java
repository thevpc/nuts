package net.thevpc.nuts.net;

import net.thevpc.nuts.io.NInputSource;
import net.thevpc.nuts.util.NGetter;
import net.thevpc.nuts.util.NSetter;

public interface NWebRequestBody {
    @NGetter
    NInputSource body();

    @NGetter
    String contentType();

    @NGetter
    String encoding();

    @NGetter
    String name();

    @NGetter
    String fileName();

    @NGetter
    String stringValue();

    @NGetter
    String contentDisposition();

    @NSetter
    NWebRequestBody stringValue(String source);

    @NSetter
    NWebRequestBody body(NInputSource source);

    @NSetter
    NWebRequestBody contentType(String contentType);

    @NSetter
    NWebRequestBody encoding(String encoding);

    @NSetter
    NWebRequestBody name(String name);

    @NSetter
    NWebRequestBody fileName(String fileName);

    //return parent NWebRequest
    NWebRequest end();

}
