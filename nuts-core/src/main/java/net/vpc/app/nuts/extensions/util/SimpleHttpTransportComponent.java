///**
// * ====================================================================
// *            Nuts : Network Updatable Things Service
// *                  (universal package manager)
// *
// * is a new Open Source Package Manager to help install packages
// * and libraries for runtime execution. Nuts is the ultimate companion for
// * maven (and other build managers) as it helps installing all package
// * dependencies at runtime. Nuts is not tied to java and is a good choice
// * to share shell scripts and other 'things' . Its based on an extensible
// * architecture to help supporting a large range of sub managers / repositories.
// *
// * Copyright (C) 2016-2017 Taha BEN SALAH
// *
// * This program is free software; you can redistribute it and/or modify
// * it under the terms of the GNU General Public License as published by
// * the Free Software Foundation; either version 3 of the License, or
// * (at your option) any later version.
// *
// * This program is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// * GNU General Public License for more details.
// *
// * You should have received a copy of the GNU General Public License along
// * with this program; if not, write to the Free Software Foundation, Inc.,
// * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
// * ====================================================================
// */
//package net.vpc.app.nuts.extensions.util;
//
//import net.vpc.app.nuts.*;
//import net.vpc.app.nuts.util.IOUtils;
//
//import java.io.*;
//import java.net.HttpURLConnection;
//import java.net.URL;
//import java.net.URLConnection;
//import java.util.NoSuchElementException;
//
///**
// * Created by vpc on 1/21/17.
// */
//public class SimpleHttpTransportComponent implements NutsTransportComponent {
//
//    @Override
//    public int getSupportLevel(String url) {
//        return CORE_SUPPORT;
//    }
//
//    @Override
//    public HttpConnectionFacade open(String url) throws IOException {
//        return new DefaultHttpConnectionFacade(new URL(url));
//    }
//
//    private static class DefaultHttpConnectionFacade implements HttpConnectionFacade {
//
//        private URL url;
//
//        public DefaultHttpConnectionFacade(URL url) {
//            this.url = url;
//        }
//
//        @Override
//        public InputStream open() throws IOException {
//            InputStream stream = url.openStream();
//            return stream;
//        }
//
//        public InputStream upload(TransportParamPart... parts) throws IOException {
////        String url = "http://example.com/upload";
//            String charset = "UTF-8";
//
////        File textFile = new File("/path/to/file.txt");
////        File binaryFile = new File("/path/to/file.bin");
//            String boundary = Long.toHexString(System.currentTimeMillis()); // Just generate some unique random value.
//            String CRLF = "\r\n"; // Line separator required by multipart/form-data.
//
//            URLConnection connection = url.openConnection();
//            connection.setDoOutput(true);
//            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
//
//            OutputStream output = null;
//            try {
//                output = connection.getOutputStream();
//                PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, charset), true);
//                try {
//                    for (TransportParamPart part : parts) {
//                        if (part instanceof TransportParamParamPart) {
//                            // Send normal param.
//                            writer.append("--" + boundary).append(CRLF);
//                            writer.append("Content-Disposition: form-data; name=\"" + ((TransportParamParamPart) part).getName() + "\"").append(CRLF);
//                            writer.append("Content-Type: text/plain; charset=" + charset).append(CRLF);
//                            writer.append(CRLF).append(((TransportParamParamPart) part).getValue()).append(CRLF).flush();
//                        } else if (part instanceof TransportParamTextFilePart) {
//                            // Send text file.
//                            writer.append("--" + boundary).append(CRLF);
//                            writer.append("Content-Disposition: form-data; name=\"" + ((TransportParamTextFilePart) part).getName() + "\"; filename=\"" + ((TransportParamTextFilePart) part).getFileName() + "\"").append(CRLF);
//                            writer.append("Content-Type: text/plain; charset=" + charset).append(CRLF); // Text file itself must be saved in this charset!
//                            writer.append(CRLF).flush();
//                            CoreIOUtils.copy(((TransportParamTextFilePart) part).getValue(), output, false);
//                            output.flush(); // Important before continuing with writer!
//                            writer.append(CRLF).flush(); // CRLF is important! It indicates end of boundary.
//                        } else if (part instanceof TransportParamTextReaderPart) {
//                            // Send text file.
//                            writer.append("--" + boundary).append(CRLF);
//                            writer.append("Content-Disposition: form-data; name=\"" + ((TransportParamTextReaderPart) part).getName() + "\"; filename=\"" + ((TransportParamTextReaderPart) part).getFileName() + "\"").append(CRLF);
//                            writer.append("Content-Type: text/plain; charset=" + charset).append(CRLF); // Text file itself must be saved in this charset!
//                            writer.append(CRLF).flush();
//                            CoreIOUtils.copy(((TransportParamTextReaderPart) part).getValue(), output, true, false);
//                            output.flush(); // Important before continuing with writer!
//                            writer.append(CRLF).flush(); // CRLF is important! It indicates end of boundary.
//                        } else if (part instanceof TransportParamBinaryFilePart) {
//                            // Send binary file.
//                            writer.append("--" + boundary).append(CRLF);
//                            writer.append("Content-Disposition: form-data; name=\"" + ((TransportParamBinaryFilePart) part).getName() + "\"; filename=\"" + ((TransportParamBinaryFilePart) part).getFileName() + "\"").append(CRLF);
//                            writer.append("Content-Type: " + URLConnection.guessContentTypeFromName(((TransportParamBinaryFilePart) part).getName())).append(CRLF);
//                            writer.append("Content-Transfer-Encoding: binary").append(CRLF);
//                            writer.append(CRLF).flush();
//                            CoreIOUtils.copy(((TransportParamBinaryFilePart) part).getValue(), output, false);
//                            output.flush(); // Important before continuing with writer!
//                            writer.append(CRLF).flush(); // CRLF is important! It indicates end of boundary.
//                        } else if (part instanceof TransportParamBinaryStreamPart) {
//                            // Send binary file.
//                            writer.append("--" + boundary).append(CRLF);
//                            writer.append("Content-Disposition: form-data; name=\"" + ((TransportParamBinaryStreamPart) part).getName() + "\"; filename=\"" + ((TransportParamBinaryStreamPart) part).getFileName() + "\"").append(CRLF);
//                            writer.append("Content-Type: " + URLConnection.guessContentTypeFromName(((TransportParamBinaryStreamPart) part).getName())).append(CRLF);
//                            writer.append("Content-Transfer-Encoding: binary").append(CRLF);
//                            writer.append(CRLF).flush();
//                            IOUtils.copy(((TransportParamBinaryStreamPart) part).getValue(), output, true, false);
//                            output.flush(); // Important before continuing with writer!
//                            writer.append(CRLF).flush(); // CRLF is important! It indicates end of boundary.
//                        } else {
//                            throw new IOException("Unsupported");
//                        }
//                    }
//                    // End of multipart/form-data.
//                    writer.append("--" + boundary + "--").append(CRLF).flush();
//                } finally {
//                    writer.close();
//                }
//            } finally {
//                if (output != null) {
//                    output.close();
//                }
//            }
//
//// Request is lazily fired whenever you need to obtain information about response.
//            int responseCode = ((HttpURLConnection) connection).getResponseCode();
//            if (responseCode / 100 >= 4) { // Should be 200
//                if (responseCode == 404) {
//                    throw new NoSuchElementException(((HttpURLConnection) connection).getResponseMessage());
//                }
//                if (responseCode == 401 || responseCode == 402 || responseCode == 403 || responseCode == 405) {
//                    throw new SecurityException(((HttpURLConnection) connection).getResponseMessage());
//                }
//                throw new IOException(((HttpURLConnection) connection).getResponseMessage());
//            }
//            if (responseCode != 200) { // Should be 200
//                throw new IOException("Invalid response " + responseCode + " : " + ((HttpURLConnection) connection).getResponseMessage());
//            }
//            return connection.getInputStream();
//        }
//    }
//}
