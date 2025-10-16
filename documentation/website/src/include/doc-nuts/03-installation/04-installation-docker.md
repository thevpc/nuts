---
title: Using Docker
---


## Running nuts in a containerized environment
If you want to run `nuts` in a containerized docker environment without creating a Dockerfile, you would run the following commands:

on your bash terminal, type :
```bash
docker run -it --rm openjdk:8 bash -c "$(curl -sSL https://thevpc.net/nuts/bootstrap-container-latest.sh)"
```

Now that you are in a container

```bash
nuts -y <your-app>...
```

As an example here where you can run `net.thevpc.nuts.noapi:noapi#{{latestJarLocation}}` on your config file `myrest-apis.json`.

`noapi` is actually an OpenAPI documentation tool that generates a pdf file based on a opn api definition in `JSON`, `YAML` or `TSON` formats.

```bash
nuts -P=%s -ZyS net.thevpc.nuts.toolbox:noapi#{{latestJarLocation}} myrest-apis.json
```


## Creating your app Dockerfile

If you are willing to deploy your application in a docker isolated environment you can use this example of `Dockerfile` 

```Dockerfile
FROM openjdk:8
RUN curl -sSL https://thevpc.net/nuts/bootstrap-container-latest.sh | bash
RUN nuts -Zy install <your application>
CMD nuts -y <your application>
```

This is a docker file to run `net.thevpc.nuts.toolbox:noapi` an OpenAPI documentation tool.

```Dockerfile
FROM openjdk:8
RUN wget {{latestJarLocation}} -qO nuts.jar
RUN java -jar nuts.jar -Zy install net.thevpc.nuts.toolbox:noapi
CMD java -jar nuts.jar -y net.thevpc.nuts.toolbox:noapi
```
