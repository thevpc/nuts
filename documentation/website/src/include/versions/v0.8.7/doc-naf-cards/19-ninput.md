---
title: NInputSource & NInputSourceBuilder – Flexible Input Abstraction
subTitle:  |
  NInputSource represents a flexible, metadata-aware source of content
  in NAF. It can wrap files, paths, URLs, byte arrays, streams,
  readers, or other providers, offering a unified API for reading,
  digesting, or streaming content. NInputSourceBuilder allows
  fine-grained configuration of sources, such as expected length,
  progress monitoring, interruptibility, multi-read capability, or
  non-blocking streams. Together, they decouple I/O handling from
  concrete types and simplify building robust I/O pipelines.
contentType: java
---

NInputSource src = NInputSource.of(file)
        .readBytes(); // get content

NInputSource multi = NInputSource.ofMultiRead(src); // reusable input

NInputSourceBuilder b = NInputSourceBuilder.of(stream)
        .setMetadata(metadata)
        .setInterruptible(true)
        .setCloseBase(true);

InputStream in = b.createInputStream();
NInputSource source = b.createInputSource();
