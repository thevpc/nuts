package net.thevpc.nuts.web;

import net.thevpc.nuts.io.NInputSource;
import net.thevpc.nuts.io.NPath;

import java.util.List;
import java.util.Map;

public interface NWebRequest {
    boolean isOneWay();

    NWebRequest setOneWay(boolean oneWay);

    String getUrl();

    NWebRequest setUrl(String url, Object... vars);

    NWebRequest setUrl(String url);

    NHttpMethod getMethod();

    NWebRequest setMethod(NHttpMethod method);

    NWebRequest get();

    NWebRequest post();

    NWebRequest patch();

    NWebRequest options();
    NWebRequest head();
    NWebRequest connect();

    NWebRequest put();

    NWebRequest delete();

    String getHeader(String name);

    List<String> getHeaders(String name);

    Map<String, List<String>> getHeaders();

    NWebRequest setHeaders(Map<String, List<String>> headers);

    NWebRequest addHeaders(Map<String, List<String>> headers);

    NWebRequest addParameters(Map<String, List<String>> parameters);

    NWebRequest setPropsFileHeaders(NPath path);

    NWebRequest addPropsFileHeaders(NPath path);

    NWebRequest addJsonFileHeaders(NPath path);

    NWebRequest setJsonFileHeaders(NPath path);

    NWebRequest setPropsFileParameters(NPath path);

    NWebRequest addPropsFileParameters(NPath path);

    NWebRequest addJsonFileParameters(NPath path);

    NWebRequest setJsonFileParameters(NPath path);

    NWebRequest addHeader(String name, String value);

    NWebRequest setHeader(String name, String value);

    Map<String, List<String>> getParameters();

    NWebRequest setParameters(Map<String, List<String>> parameters);

    NWebRequest addParameter(String name, String value);

    NWebRequest setParameter(String name, String value);

    NInputSource getBody();

    NWebRequest setJsonBody(Object body);

    NWebRequest setBody(byte[] body);

    NWebRequest setBody(NInputSource body);

    NWebRequest setContentLanguage(String contentLanguage);

    NWebRequest setAuthorizationBearer(String authorizationBearer);

    NWebRequest setAuthorization(String authorization);

    String getAuthorization();

    String getAuthorizationBearer();

    String getContentLanguage();

    String getContentType();

    NWebRequest setContentTypeForm();

    NWebRequest setContentType(String contentType);

    Integer getReadTimeout();

    NWebRequest setReadTimeout(Integer readTimeout);

    Integer getConnectTimeout();

    NWebRequest setConnectTimeout(Integer connectTimeout);

    List<NWebRequestBody> getParts();

    NWebRequest addPart(NWebRequestBody body);

    NWebRequestBody addPart(String name);

    NWebRequestBody addPart();

    NWebRequest addPart(String name, String value);

    NWebRequest addPart(String name, String fileName,String contentType, NInputSource body);

    NWebResponse run();
}
