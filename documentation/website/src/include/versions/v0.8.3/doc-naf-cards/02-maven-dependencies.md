---
title: Maven Integration
subTitle:  |
  NAF can be added to your project with ##one## single JAR
  dependency, fully compatible with Maven repositories. Once included,
  it brings runtime dependency resolution, artifact management, and
  dynamic execution to your application without additional setup. Just
  add the dependency, and NAF takes care of the rest — from fetching
  artifacts to managing versions, all seamlessly integrated with your
  existing Maven workflow.
contentType: xml
---

<dependencies>
    <dependency><groupId>net.thevpc.nuts</groupId>
    <artifactId>nuts</artifactId>
    <version>{{apiVersion}}</version>
</dependency>
</dependencies>
<repositories>
    <repository><id>thevpc</id><url>https://maven.thevpc.net</url></repository>
</repositories>

