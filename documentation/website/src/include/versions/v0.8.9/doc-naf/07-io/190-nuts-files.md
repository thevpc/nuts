---
title: IO: Working with files
---


**nuts** Library allows multiple variants of string interpolation

## NCp

```java
    NCp.of()
        .from("http://my-server.com/file.pdf")
        .to("/home/my-file")
        .setProgressMonitor(true)
        .setValidator((in)->checkSHA1Hash(in))
        .run();

    NPs ps=NPs.of()
        if(ps.isSupportedKillProcess()){
            ps.killProcess("1234");
        }
```


## NCompress/NUncompress
```java
    NCompress aa = NCompress.of()
        .setTarget(options.outZip);
        for (NPath file : options.files) {
        aa.addSource(file);
        }
        aa.run();
        
```


```java
    NUncompress.of()
                    .from(is)
                    .visit(new NUncompressVisitor() {
    @Override
    public boolean visitFolder(String path) {
        return true;
    }

    @Override
    public boolean visitFile(String path, InputStream inputStream) {
        if ("META-INF/MANIFEST.MF".equals(path)) {
            ...
        } else) {
            ...
        }
        return true;
    }
}).run();
```


## NDigest
```java
   String digest=NDigest.of().setSource(x.getPath().getBytes()).computeString();
}).run();
```


