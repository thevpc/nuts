package net.thevpc.nuts.web;

import net.thevpc.nuts.io.NInputSource;

public interface NWebRequestBody {
    NInputSource getBody();

    String getContentType();

    String getEncoding();

    String getName();

    String getFileName();

    String getStringValue();

    String getContentDisposition();

    NWebRequestBody setStringValue(String source);

    NWebRequestBody setBody(NInputSource source);

    NWebRequestBody setContentType(String contentType);

    NWebRequestBody setEncoding(String encoding);

    NWebRequestBody setName(String name);

    NWebRequestBody setFileName(String fileName);

    //return parent NWebRequest
    NWebRequest end();

}
