---
id: installation-docker
title: Docker Env
---


## Running nuts in a containerized environment
If you want to run `nuts` in a containerized docker environment without creating a Dockerfile, you would run the following commands:

on your bash terminal, type :
```bash
docker pull openjdk:8
docker run -it -v $(pwd):/workspace openjdk:8 sh
```

Now that you are in a container

```bash
cd /workspace
wget https://thevpc.net/nuts/nuts-preview.jar -qO nuts.jar
java -jar nuts.jar -y <your-app>...
```

As an example here where you can run `net.thevpc.nuts.toolbox:noapi#0.8.5.0` on your config file `myrest-apis.json`.

`noapi` is actually an OpenAPI documentation tool that generates a pdf file based on a opn api definition in `JSON`, `YAML` or `TSON` formats.

```bash
cd /workspace
wget https://thevpc.net/nuts/nuts-preview.jar -qO nuts.jar
java -jar nuts.jar -P=%s -ZyS net.thevpc.nuts.toolbox:noapi#0.8.5.0 myrest-apis.json
```


## Creating your app Dockerfile

If you are willing to deploy your application in a docker isolated environment you can use this example pf `Dockerfile` 

```Dockerfile
FROM openjdk:8
RUN wget https://thevpc.net/nuts/nuts-preview.jar -qO nuts.jar
RUN java -jar nuts.jar -Zy install <your application>
CMD java -jar nuts.jar -y <your application>
docker run -it -v $(pwd):/workspace openjdk:8 sh
cd /workspace
wget https://thevpc.net/nuts/nuts-preview.jar -qO nuts.jar
java -jar nuts.jar -P=no -ZyS -r=+thevpc net.thevpc.nuts.toolbox:noapi#0.8.5.0 buat-insurance-connector.json
#############

```

This is a docker file to run `net.thevpc.nuts.toolbox:noapi` an OpenAPI documentatation tool.

```Dockerfile
FROM openjdk:8
RUN wget https://thevpc.net/nuts/nuts-preview.jar -qO nuts.jar
RUN java -jar nuts.jar -Zy install net.thevpc.nuts.toolbox:noapi
CMD java -jar nuts.jar -y net.thevpc.nuts.toolbox:noapi
```