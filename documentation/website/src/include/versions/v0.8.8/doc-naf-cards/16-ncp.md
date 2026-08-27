---
title: NCp — Copy with Options & Validation
subTitle:  |
  NCp provides a unified, flexible API to copy resources of various
  types — files, streams, URLs, or paths — while supporting advanced
  features like logging, progress monitoring, and validation. You can
  start with a simple copy, then gradually enable tracing, progress
  feedback, or integrity checks, all with the same intuitive API.
contentType: java
---

// --- Basic copy: simple source to target ---
NCp.of()
    .from(source)                     // set source (File, Path, InputStream, etc.)
    .to(download_path.resolve(source.getName())) // set target path
    .addOptions(NPathOption.LOG, NPathOption.TRACE) // enable logging and tracing
    .run();                            // execute copy

// --- Copy with progress monitoring ---
NCp.of()
    .from(from)
    .to(to)
    .addOptions(NPathOption.LOG, NPathOption.TRACE)
    // --- add validation validation (e.g., SHA-1 integrity check) ---
    .setValidator(new NCpValidator() {
        @Override
        public void validate(InputStream in) throws IOException {
            checkSHA1Hash(id.builder().setFace(NConstants.QueryFaces.CONTENT_HASH).build(),
                          in, "artifact binaries");
        }
    })
    .setProgressMonitor(event -> {
        NOut.println(event.getProgress()); // display progress
        return true;                            // continue copy
    })
    .run();
