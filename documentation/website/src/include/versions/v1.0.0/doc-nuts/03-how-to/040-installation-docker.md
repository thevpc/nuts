---
title: Using Docker
---

## Running nuts in a containerized environment

If you want to run **nuts** in a containerized Docker environment without creating a dedicated Dockerfile, you can bootstrap it directly within a standard OpenJDK container.

On your bash terminal, run:
```bash
docker run -it --rm openjdk:8 bash -c "$(curl -sSL https://thevpc.net/nuts/bootstrap-container-latest.sh)"
```

Once inside the container, you can use **nuts** normally, such as installing and running applications with the auto-confirm flag (`-y`):

```bash
nuts -y <your-app>...
```

For example, you can run `net.thevpc.nuts.toolbox:noapi` to process a configuration file named `myrest-apis.json`. `noapi` is an OpenAPI documentation tool that generates a PDF file based on an API definition in JSON, YAML, or TSON formats.

```bash
nuts -Zy net.thevpc.nuts.toolbox:noapi myrest-apis.json
```

## Creating your app Dockerfile

If you are deploying your application in an isolated Docker environment, you can build a custom image. Here is a basic example `Dockerfile` that bootstraps **nuts** and installs your app:

```Dockerfile
FROM eclipse-temurin:21-jre
RUN curl -sSL https://thevpc.net/nuts/bootstrap-container-latest.sh | bash
RUN nuts -Zy install <your application>
CMD nuts -y <your application>
```

Alternatively, here is a more explicit approach that downloads the **nuts** JAR directly. This example packages the `noapi` OpenAPI tool:

```Dockerfile
FROM eclipse-temurin:21-jre
RUN wget {{latestJarLocation}} -qO nuts.jar
RUN java -jar nuts.jar -Zy install net.thevpc.nuts.toolbox:noapi
CMD java -jar nuts.jar -y net.thevpc.nuts.toolbox:noapi
```

### Multi-stage Docker Build

For production deployments, it is best practice to use a multi-stage Docker build. This approach allows you to use a full JDK for dependency resolution and packaging, while creating a minimal JRE-based image for the final runtime, resulting in significantly smaller container sizes:

```Dockerfile
# Stage 1: Build and Resolve
FROM eclipse-temurin:21-jdk AS builder
RUN wget {{latestJarLocation}} -qO nuts.jar
# Install and resolve all dependencies in the builder stage
RUN java -jar nuts.jar -Zy install com.mycompany:my-server

# Stage 2: Production Runtime
FROM eclipse-temurin:21-jre
COPY --from=builder /root/.nuts /root/.nuts
COPY --from=builder /nuts.jar /nuts.jar
CMD java -jar nuts.jar -y com.mycompany:my-server
```

### Using Docker Compose

You can easily manage **nuts**-powered applications as services using Docker Compose. Here is an example `docker-compose.yml` for running a web service:

```yaml
version: '3.8'
services:
  my-nuts-service:
    build: .
    ports:
      - "8080:8080"
    volumes:
      - nuts-workspace:/root/.nuts
    environment:
      - NUTS_PROFILE=production

volumes:
  nuts-workspace:
```

### Volume Mounts and Persistence

By default, **nuts** stores its workspace (including downloaded artifacts, caches, and configuration) in the user's home directory (e.g., `/root/.nuts`). When running in Docker, this data is lost when the container stops unless you use volume mounts.

To persist the workspace and avoid re-downloading dependencies on every container restart, mount a volume to the workspace location:

```bash
docker run -it --rm -v nuts-data:/root/.nuts my-nuts-image
```

### Best Practices

* **Layer Caching**: Place your `RUN nuts -Zy install ...` command high up in your Dockerfile so Docker can cache the downloaded dependencies layer, speeding up subsequent builds.
* **Workspace Pre-warming**: Always run the install command during the Docker build phase. This pre-warms the workspace, ensuring the container starts instantly without needing to fetch artifacts at runtime.
* **Base Images**: Prefer official JRE images (like `eclipse-temurin:21-jre`) over JDK images for the final runtime to minimize security surface area and image size.
