---
title: NCompress / NUncompress — Flexible Compression & Extraction
subTitle:  |
  NAF provides unified APIs for compressing and uncompressing files,
  directories, or streams, supporting multiple formats (e.g., ZIP).
  You can monitor progress, skip root directories, or apply custom
  options for logging and safety. NCompress and NUncompress make it
  easy to handle resource packaging in a consistent, OS-aligned
  manner, while fully integrating with Nuts’ filesystem
  abstractions.
contentType: java
---

// --- Compress a file or directory into a ZIP ---
NCompress.of()
    .addSource(example)                          // source to compress
    .setTarget(example.resolveSibling(
        example.getNameParts(NPathExtensionType.SHORT).getBaseName() + ".zip")) // target zip path
    .setPackaging("zip")                          // compression format
    .run();                                       // execute compression

// --- Uncompress a ZIP file to a folder ---
NUncompress.of()
    .from(zipTo)                                 // source zip file
    .to(folderTo)                                // target directory
    .setSkipRoot(true)                           // optionally skip the root folder in zip
    .progressMonitor(new OpNInputStreamProgressMonitor(
        module.rt().addOperation("Unzipping " + i))) // progress monitoring
    .run();                                      // execute uncompression
