package net.vpc.app.nuts.extensions.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

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
            throw new RuntimeException(e);
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
    //    public static InputStream httpUpload(String url, TransportParamPart... parts) throws IOException {
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
    //                for (TransportParamPart part : parts) {
    //                    if (part instanceof TransportParamParamPart) {
    //                        // Send normal param.
    //                        writer.append("--" + boundary).append(CRLF);
    //                        writer.append("Content-Disposition: form-data; name=\"" + ((TransportParamParamPart) part).getName() + "\"").append(CRLF);
    //                        writer.append("Content-Type: text/plain; charset=" + charset).append(CRLF);
    //                        writer.append(CRLF).append(((TransportParamParamPart) part).getValue()).append(CRLF).flush();
    //                    } else if (part instanceof TransportParamTextFilePart) {
    //                        // Send text file.
    //                        writer.append("--" + boundary).append(CRLF);
    //                        writer.append("Content-Disposition: form-data; name=\"" + ((TransportParamTextFilePart) part).getName() + "\"; filename=\"" + ((TransportParamTextFilePart) part).getFileName() + "\"").append(CRLF);
    //                        writer.append("Content-Type: text/plain; charset=" + charset).append(CRLF); // Text file itself must be saved in this charset!
    //                        writer.append(CRLF).flush();
    //                        IOUtils.copy(((TransportParamTextFilePart) part).getValue(), output);
    //                        output.flush(); // Important before continuing with writer!
    //                        writer.append(CRLF).flush(); // CRLF is important! It indicates end of boundary.
    //                    } else if (part instanceof TransportParamTextReaderPart) {
    //                        // Send text file.
    //                        writer.append("--" + boundary).append(CRLF);
    //                        writer.append("Content-Disposition: form-data; name=\"" + ((TransportParamTextReaderPart) part).getName() + "\"; filename=\"" + ((TransportParamTextReaderPart) part).getFileName() + "\"").append(CRLF);
    //                        writer.append("Content-Type: text/plain; charset=" + charset).append(CRLF); // Text file itself must be saved in this charset!
    //                        writer.append(CRLF).flush();
    //                        IOUtils.copy(((TransportParamTextReaderPart) part).getValue(), output);
    //                        output.flush(); // Important before continuing with writer!
    //                        writer.append(CRLF).flush(); // CRLF is important! It indicates end of boundary.
    //                    } else if (part instanceof TransportParamBinaryFilePart) {
    //                        // Send binary file.
    //                        writer.append("--" + boundary).append(CRLF);
    //                        writer.append("Content-Disposition: form-data; name=\"" + ((TransportParamBinaryFilePart) part).getName() + "\"; filename=\"" + ((TransportParamBinaryFilePart) part).getFileName() + "\"").append(CRLF);
    //                        writer.append("Content-Type: " + URLConnection.guessContentTypeFromName(((TransportParamBinaryFilePart) part).getName())).append(CRLF);
    //                        writer.append("Content-Transfer-Encoding: binary").append(CRLF);
    //                        writer.append(CRLF).flush();
    //                        IOUtils.copy(((TransportParamBinaryFilePart) part).getValue(), output);
    //                        output.flush(); // Important before continuing with writer!
    //                        writer.append(CRLF).flush(); // CRLF is important! It indicates end of boundary.
    //                    } else if (part instanceof TransportParamBinaryStreamPart) {
    //                        // Send binary file.
    //                        writer.append("--" + boundary).append(CRLF);
    //                        writer.append("Content-Disposition: form-data; name=\"" + ((TransportParamBinaryStreamPart) part).getName() + "\"; filename=\"" + ((TransportParamBinaryStreamPart) part).getFileName() + "\"").append(CRLF);
    //                        writer.append("Content-Type: " + URLConnection.guessContentTypeFromName(((TransportParamBinaryStreamPart) part).getName())).append(CRLF);
    //                        writer.append("Content-Transfer-Encoding: binary").append(CRLF);
    //                        writer.append(CRLF).flush();
    //                        IOUtils.copy(((TransportParamBinaryStreamPart) part).getValue(), output);
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
    //    public static String httpUpload(OutputStream output, TransportParamPart... parts) throws IOException {
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
    //                for (TransportParamPart part : parts) {
    //                    if (part instanceof TransportParamParamPart) {
    //                        // Send normal param.
    //                        writer.append("--" + boundary).append(CRLF);
    //                        writer.append("Content-Disposition: form-data; name=\"" + ((TransportParamParamPart) part).getName() + "\"").append(CRLF);
    //                        writer.append("Content-Type: text/plain; charset=" + charset).append(CRLF);
    //                        writer.append(CRLF).append(((TransportParamParamPart) part).getValue()).append(CRLF).flush();
    //                    } else if (part instanceof TransportParamTextFilePart) {
    //                        // Send text file.
    //                        writer.append("--" + boundary).append(CRLF);
    //                        writer.append("Content-Disposition: form-data; name=\"" + ((TransportParamTextFilePart) part).getName() + "\"; filename=\"" + ((TransportParamTextFilePart) part).getFileName() + "\"").append(CRLF);
    //                        writer.append("Content-Type: text/plain; charset=" + charset).append(CRLF); // Text file itself must be saved in this charset!
    //                        writer.append(CRLF).flush();
    //                        IOUtils.copy(((TransportParamTextFilePart) part).getValue(), output);
    //                        output.flush(); // Important before continuing with writer!
    //                        writer.append(CRLF).flush(); // CRLF is important! It indicates end of boundary.
    //                    } else if (part instanceof TransportParamTextReaderPart) {
    //                        // Send text file.
    //                        writer.append("--" + boundary).append(CRLF);
    //                        writer.append("Content-Disposition: form-data; name=\"" + ((TransportParamTextReaderPart) part).getName() + "\"; filename=\"" + ((TransportParamTextReaderPart) part).getFileName() + "\"").append(CRLF);
    //                        writer.append("Content-Type: text/plain; charset=" + charset).append(CRLF); // Text file itself must be saved in this charset!
    //                        writer.append(CRLF).flush();
    //                        IOUtils.copy(((TransportParamTextReaderPart) part).getValue(), output);
    //                        output.flush(); // Important before continuing with writer!
    //                        writer.append(CRLF).flush(); // CRLF is important! It indicates end of boundary.
    //                    } else if (part instanceof TransportParamBinaryFilePart) {
    //                        // Send binary file.
    //                        writer.append("--" + boundary).append(CRLF);
    //                        writer.append("Content-Disposition: form-data; name=\"" + ((TransportParamBinaryFilePart) part).getName() + "\"; filename=\"" + ((TransportParamBinaryFilePart) part).getFileName() + "\"").append(CRLF);
    //                        writer.append("Content-Type: " + URLConnection.guessContentTypeFromName(((TransportParamBinaryFilePart) part).getName())).append(CRLF);
    //                        writer.append("Content-Transfer-Encoding: binary").append(CRLF);
    //                        writer.append(CRLF).flush();
    //                        IOUtils.copy(((TransportParamBinaryFilePart) part).getValue(), output);
    //                        output.flush(); // Important before continuing with writer!
    //                        writer.append(CRLF).flush(); // CRLF is important! It indicates end of boundary.
    //                    } else if (part instanceof TransportParamBinaryStreamPart) {
    //                        // Send binary file.
    //                        writer.append("--" + boundary).append(CRLF);
    //                        writer.append("Content-Disposition: form-data; name=\"" + ((TransportParamBinaryStreamPart) part).getName() + "\"; filename=\"" + ((TransportParamBinaryStreamPart) part).getFileName() + "\"").append(CRLF);
    //                        writer.append("Content-Type: " + URLConnection.guessContentTypeFromName(((TransportParamBinaryStreamPart) part).getName())).append(CRLF);
    //                        writer.append("Content-Transfer-Encoding: binary").append(CRLF);
    //                        writer.append(CRLF).flush();
    //                        IOUtils.copy(((TransportParamBinaryStreamPart) part).getValue(), output);
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
                throw new RuntimeException(e);
            }
        }
}
