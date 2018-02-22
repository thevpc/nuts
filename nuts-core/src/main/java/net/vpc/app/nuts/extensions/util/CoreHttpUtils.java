/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 *
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * Copyright (C) 2016-2017 Taha BEN SALAH
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.extensions.util;

import net.vpc.app.nuts.NutsHttpConnectionFacade;
import net.vpc.app.nuts.NutsTransportComponent;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.extensions.core.DefaultHttpTransportComponent;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import net.vpc.app.nuts.NutsIOException;

/**
 * Created by vpc on 5/16/17.
 */
public class CoreHttpUtils {

    /**
     * returns the url parameters in a map
     *
     * @param query
     * @return map
     */
    public static ListMap<String, String> queryToMap(String query) {
        ListMap<String, String> result = new ListMap<String, String>();
        if (query != null) {
            for (String param : query.split("&")) {
                String pair[] = param.split("=");
                if (pair.length > 1) {
                    result.add(urlDecodeString(pair[0]), urlDecodeString(pair[1]));
                } else {
                    result.add(urlDecodeString(pair[0]), "");
                }
            }
        }
        return result;
    }

    public static String urlEncodeString(String s) {
        if (s == null || s.trim().length() == 0) {
            return "";
        }
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new NutsIOException(e);
        }
    }

    //    public static InputStream httpDownloadStream(String url) throws IOException {
    //        return new URL(url).openStream();
    //    }
    //    public static void httpDownloadToFile(String url, File file, boolean mkdirs) throws IOException {
    //        if (mkdirs) {
    //            File parent = file.getParentFile();
    //            if (parent != null) {
    //                parent.mkdirs();
    //            }
    //        }
    //        InputStream stream = null;
    //        try {
    //            stream = new URL(url).openStream();
    //            IOUtils.copy(stream, file, true);
    //        } finally {
    //            if (stream != null) {
    //                stream.close();
    //            }
    //        }
    //    }
    //    public static InputStream httpUpload(String url, NutsTransportParamPart... parts) throws IOException {
    ////        String url = "http://example.com/upload";
    //        String charset = "UTF-8";
    //
    ////        File textFile = new File("/path/to/file.txt");
    ////        File binaryFile = new File("/path/to/file.bin");
    //
    //        String boundary = Long.toHexString(System.currentTimeMillis()); // Just generate some unique random value.
    //        String CRLF = "\r\n"; // Line separator required by multipart/form-data.
    //
    //        URLConnection connection = new URL(url).openConnection();
    //        connection.setDoOutput(true);
    //        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
    //
    //        OutputStream output = null;
    //        try {
    //            output = connection.getOutputStream();
    //            PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, charset), true);
    //            try {
    //                for (NutsTransportParamPart part : parts) {
    //                    if (part instanceof NutsTransportParamParamPart) {
    //                        // Send normal param.
    //                        writer.append("--" + boundary).append(CRLF);
    //                        writer.append("Content-Disposition: form-data; name=\"" + ((NutsTransportParamParamPart) part).getName() + "\"").append(CRLF);
    //                        writer.append("Content-Type: text/plain; charset=" + charset).append(CRLF);
    //                        writer.append(CRLF).append(((NutsTransportParamParamPart) part).getValue()).append(CRLF).flush();
    //                    } else if (part instanceof NutsTransportParamTextFilePart) {
    //                        // Send text file.
    //                        writer.append("--" + boundary).append(CRLF);
    //                        writer.append("Content-Disposition: form-data; name=\"" + ((NutsTransportParamTextFilePart) part).getName() + "\"; filename=\"" + ((NutsTransportParamTextFilePart) part).getFileName() + "\"").append(CRLF);
    //                        writer.append("Content-Type: text/plain; charset=" + charset).append(CRLF); // Text file itself must be saved in this charset!
    //                        writer.append(CRLF).flush();
    //                        IOUtils.copy(((NutsTransportParamTextFilePart) part).getValue(), output);
    //                        output.flush(); // Important before continuing with writer!
    //                        writer.append(CRLF).flush(); // CRLF is important! It indicates end of boundary.
    //                    } else if (part instanceof NutsTransportParamTextReaderPart) {
    //                        // Send text file.
    //                        writer.append("--" + boundary).append(CRLF);
    //                        writer.append("Content-Disposition: form-data; name=\"" + ((NutsTransportParamTextReaderPart) part).getName() + "\"; filename=\"" + ((NutsTransportParamTextReaderPart) part).getFileName() + "\"").append(CRLF);
    //                        writer.append("Content-Type: text/plain; charset=" + charset).append(CRLF); // Text file itself must be saved in this charset!
    //                        writer.append(CRLF).flush();
    //                        IOUtils.copy(((NutsTransportParamTextReaderPart) part).getValue(), output);
    //                        output.flush(); // Important before continuing with writer!
    //                        writer.append(CRLF).flush(); // CRLF is important! It indicates end of boundary.
    //                    } else if (part instanceof NutsTransportParamBinaryFilePart) {
    //                        // Send binary file.
    //                        writer.append("--" + boundary).append(CRLF);
    //                        writer.append("Content-Disposition: form-data; name=\"" + ((NutsTransportParamBinaryFilePart) part).getName() + "\"; filename=\"" + ((NutsTransportParamBinaryFilePart) part).getFileName() + "\"").append(CRLF);
    //                        writer.append("Content-Type: " + URLConnection.guessContentTypeFromName(((NutsTransportParamBinaryFilePart) part).getName())).append(CRLF);
    //                        writer.append("Content-Transfer-Encoding: binary").append(CRLF);
    //                        writer.append(CRLF).flush();
    //                        IOUtils.copy(((NutsTransportParamBinaryFilePart) part).getValue(), output);
    //                        output.flush(); // Important before continuing with writer!
    //                        writer.append(CRLF).flush(); // CRLF is important! It indicates end of boundary.
    //                    } else if (part instanceof NutsTransportParamBinaryStreamPart) {
    //                        // Send binary file.
    //                        writer.append("--" + boundary).append(CRLF);
    //                        writer.append("Content-Disposition: form-data; name=\"" + ((NutsTransportParamBinaryStreamPart) part).getName() + "\"; filename=\"" + ((NutsTransportParamBinaryStreamPart) part).getFileName() + "\"").append(CRLF);
    //                        writer.append("Content-Type: " + URLConnection.guessContentTypeFromName(((NutsTransportParamBinaryStreamPart) part).getName())).append(CRLF);
    //                        writer.append("Content-Transfer-Encoding: binary").append(CRLF);
    //                        writer.append(CRLF).flush();
    //                        IOUtils.copy(((NutsTransportParamBinaryStreamPart) part).getValue(), output);
    //                        output.flush(); // Important before continuing with writer!
    //                        writer.append(CRLF).flush(); // CRLF is important! It indicates end of boundary.
    //                    } else {
    //                        throw new IOException("Unsuported");
    //                    }
    //                }
    //                // End of multipart/form-data.
    //                writer.append("--" + boundary + "--").append(CRLF).flush();
    //            } finally {
    //                writer.close();
    //            }
    //        } finally {
    //            if (output != null) {
    //                output.close();
    //            }
    //        }
    //
    //// Request is lazily fired whenever you need to obtain information about response.
    //        int responseCode = ((HttpURLConnection) connection).getResponseCode();
    //        if (responseCode != 200) { // Should be 200
    //            throw new IOException("Invalid response " + responseCode + " : " + ((HttpURLConnection) connection).getResponseMessage());
    //        }
    //        return connection.getInputStream();
    //    }
    //    public static String httpUpload(OutputStream output, NutsTransportParamPart... parts) throws IOException {
    ////        String url = "http://example.com/upload";
    //        String charset = "UTF-8";
    //
    ////        File textFile = new File("/path/to/file.txt");
    ////        File binaryFile = new File("/path/to/file.bin");
    //
    //        String boundary = Long.toHexString(System.currentTimeMillis()); // Just generate some unique random value.
    //        String CRLF = "\r\n"; // Line separator required by multipart/form-data.
    //
    //
    //        try {
    //            PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, charset), true);
    //            try {
    //                for (NutsTransportParamPart part : parts) {
    //                    if (part instanceof NutsTransportParamParamPart) {
    //                        // Send normal param.
    //                        writer.append("--" + boundary).append(CRLF);
    //                        writer.append("Content-Disposition: form-data; name=\"" + ((NutsTransportParamParamPart) part).getName() + "\"").append(CRLF);
    //                        writer.append("Content-Type: text/plain; charset=" + charset).append(CRLF);
    //                        writer.append(CRLF).append(((NutsTransportParamParamPart) part).getValue()).append(CRLF).flush();
    //                    } else if (part instanceof NutsTransportParamTextFilePart) {
    //                        // Send text file.
    //                        writer.append("--" + boundary).append(CRLF);
    //                        writer.append("Content-Disposition: form-data; name=\"" + ((NutsTransportParamTextFilePart) part).getName() + "\"; filename=\"" + ((NutsTransportParamTextFilePart) part).getFileName() + "\"").append(CRLF);
    //                        writer.append("Content-Type: text/plain; charset=" + charset).append(CRLF); // Text file itself must be saved in this charset!
    //                        writer.append(CRLF).flush();
    //                        IOUtils.copy(((NutsTransportParamTextFilePart) part).getValue(), output);
    //                        output.flush(); // Important before continuing with writer!
    //                        writer.append(CRLF).flush(); // CRLF is important! It indicates end of boundary.
    //                    } else if (part instanceof NutsTransportParamTextReaderPart) {
    //                        // Send text file.
    //                        writer.append("--" + boundary).append(CRLF);
    //                        writer.append("Content-Disposition: form-data; name=\"" + ((NutsTransportParamTextReaderPart) part).getName() + "\"; filename=\"" + ((NutsTransportParamTextReaderPart) part).getFileName() + "\"").append(CRLF);
    //                        writer.append("Content-Type: text/plain; charset=" + charset).append(CRLF); // Text file itself must be saved in this charset!
    //                        writer.append(CRLF).flush();
    //                        IOUtils.copy(((NutsTransportParamTextReaderPart) part).getValue(), output);
    //                        output.flush(); // Important before continuing with writer!
    //                        writer.append(CRLF).flush(); // CRLF is important! It indicates end of boundary.
    //                    } else if (part instanceof NutsTransportParamBinaryFilePart) {
    //                        // Send binary file.
    //                        writer.append("--" + boundary).append(CRLF);
    //                        writer.append("Content-Disposition: form-data; name=\"" + ((NutsTransportParamBinaryFilePart) part).getName() + "\"; filename=\"" + ((NutsTransportParamBinaryFilePart) part).getFileName() + "\"").append(CRLF);
    //                        writer.append("Content-Type: " + URLConnection.guessContentTypeFromName(((NutsTransportParamBinaryFilePart) part).getName())).append(CRLF);
    //                        writer.append("Content-Transfer-Encoding: binary").append(CRLF);
    //                        writer.append(CRLF).flush();
    //                        IOUtils.copy(((NutsTransportParamBinaryFilePart) part).getValue(), output);
    //                        output.flush(); // Important before continuing with writer!
    //                        writer.append(CRLF).flush(); // CRLF is important! It indicates end of boundary.
    //                    } else if (part instanceof NutsTransportParamBinaryStreamPart) {
    //                        // Send binary file.
    //                        writer.append("--" + boundary).append(CRLF);
    //                        writer.append("Content-Disposition: form-data; name=\"" + ((NutsTransportParamBinaryStreamPart) part).getName() + "\"; filename=\"" + ((NutsTransportParamBinaryStreamPart) part).getFileName() + "\"").append(CRLF);
    //                        writer.append("Content-Type: " + URLConnection.guessContentTypeFromName(((NutsTransportParamBinaryStreamPart) part).getName())).append(CRLF);
    //                        writer.append("Content-Transfer-Encoding: binary").append(CRLF);
    //                        writer.append(CRLF).flush();
    //                        IOUtils.copy(((NutsTransportParamBinaryStreamPart) part).getValue(), output);
    //                        output.flush(); // Important before continuing with writer!
    //                        writer.append(CRLF).flush(); // CRLF is important! It indicates end of boundary.
    //                    } else {
    //                        throw new IOException("Unsuported");
    //                    }
    //                }
    //                // End of multipart/form-data.
    //                writer.append("--" + boundary + "--").append(CRLF).flush();
    //            } finally {
    //                writer.close();
    //            }
    //        } finally {
    //            if (output != null) {
    //                output.close();
    //            }
    //        }
    //        return boundary;
    //    }
    public static String urlDecodeString(String s) {
        if (s == null || s.trim().length() == 0) {
            return s;
        }
        try {
            return URLDecoder.decode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new NutsIOException(e);
        }
    }

    public static NutsHttpConnectionFacade getHttpClientFacade(NutsWorkspace ws, String url) throws IOException {
//        System.out.println("getHttpClientFacade "+url);
        NutsTransportComponent best = ws.getExtensionManager().getFactory().createSupported(NutsTransportComponent.class, url);
        if (best == null) {
            best = DefaultHttpTransportComponent.INSTANCE;
        }
        return best.open(url);
    }
}
