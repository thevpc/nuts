---
title: NDigest – Hashing Made Simple
subTitle:  |
  NDigest is a fluent I/O command to compute hash digests of one or
  multiple sources. It supports files, streams, URLs, byte arrays, or
  Nuts descriptors, and allows combining multiple sources into a
  single digest. Built on top of Java's MessageDigest, it simplifies
  hashing in Nuts workflows, integrates seamlessly with download
  validation, and supports MD5, SHA1, SHA256, or any algorithm
  provided by MessageDigest.
contentType: java
---

// Simple SHA256 digest of a file
String hash = NDigest.of()
        .sha256()
        .addSource(path)
        .computeString();

// Digest multiple sources
String combined = NDigest.of()
        .sha1()
        .addSource(file1)
        .addSource(file2)
        .addSource(url)
        .computeString();

// Digest and get raw bytes
byte[] bytes = NDigest.of()
        .md5()
        .addSource(stream)
        .computeBytes();
