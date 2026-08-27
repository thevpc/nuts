---
title: HTTP Web Client (NWebCli)
---

For communicating with external HTTP web services, NAF provides `NWebCli`[cite: 7]. It offers a fluent, highly configurable, and context-aware builder API to construct synchronous or asynchronous HTTP requests, handle payloads (JSON, URL-encoded forms, multipart), manage headers/cookies, and process raw or structured JSON responses[cite: 7, 8, 9].

## Key features:
- **Fluent Request Building** – Construct requests naturally using method shortcuts like `GET()`, `POST()`, `PUT()`, `DELETE()`, etc., passing relative or absolute target paths[cite: 7, 8].
- **Flexible Body Serialization** – Send payloads easily with `jsonRequestBody(Object)`[cite: 8], raw text with `requestBody(String)`[cite: 8], or handle multipart file uploads via `addPart()`[cite: 8].
- **Integrated JSON Mapping** – Automatically bind response payloads directly into Java POJOs using `contentAsJson(Class<T>)`[cite: 9], or interact with them dynamically via `contentAsJsonMap()` or `contentAsJsonList()`[cite: 9].
- **Stateful Cookie & Header Management** – Attach global cookies and base URIs to the client instance (`NWebCli`)[cite: 7], or override them granularly on a per-request layer (`NWebRequest`)[cite: 8].
- **Robust Error Handling** – Inspect status codes quickly using semantic utilities like `isOk()`, `isClientError()`, and `isServerError()`[cite: 5, 9], or use `ifErrorThrow()` to chain defensive error state processing[cite: 9].
- **Timeout Adaptability** – Configure explicit connection and read deadlines via `connectTimeout(NDuration)` and `readTimeout(NDuration)` directly on the client[cite: 7] or specific requests[cite: 8].

```java
// Fast execution instance retrieving a payload
String payload = NWebCli.of()
        .GET("https://api.example.com/status")
        .run()
        .contentAsString();
```

## Example: JSON Request and Response Parsing

The client simplifies interactions with REST APIs by abstracting the explicit manual mapping boilerplate of JSON payloads back and forth:
```java
public void login(String login, String password) {
    NWebResponse response = NWebCli.of()
            .POST(resolveUrl("login"))
            .jsonRequestBody(
                    NMapBuilder.ofLinked()
                            .put("userName", login)
                            .put("password", password)
                            .build()
            )
            .run();

    if (response.isOk()) {
        LoginResult rr = response.contentAsJson(LoginResult.class);
        if (rr != null && !NBlankable.isBlank(rr.accessToken)) {
            this.loginResult = rr;
            return;
        }
    } else {
        throw new NIllegalArgumentException(
            NMsg.ofC("unable to login to %s", toSafeConnectionString(resolveConnectionString()))
        );
    }
    throw new NIllegalArgumentException(
        NMsg.ofC("unable to login to %s", toSafeConnectionString(resolveConnectionString()))
    );
}
```

## Example: Seamless Multipart Form Uploads

NWebCli provides highly optimized overloads for uploading files (File, Path, or NPath). It automatically infers the form parameters and filenames behind the scenes:
```java
// Upload a file where the form name matches the local filename
NWebCli.of().POST("/upload")
    .addPart(myFile) 
    .run();

// Or specify an explicit form parameter name
NWebCli.of().POST("/upload")
    .addPart("avatar", myFile)
    .run();
```


## Resilience & Asynchronous Execution

To keep your application non-blocking, you can submit tasks asynchronously. Additionally, rather than bloating the HTTP client with retry logic, NWebCli integrates cleanly with your workspace's concurrency engine (NConcurrent):

```java
// Run asynchronously using a custom Executor or your NConcurrent ExecutorService
CompletableFuture<NWebResponse> asyncResponse = NWebCli.of()
        .GET("/long-task")
        .runAsync(NConcurrent.of().executorService());

// Combine NWebCli and NConcurrent for robust network retry behaviors
NWebResponse resilientResponse = NConcurrent.of()
        .retryCall(() -> NWebCli.of().GET("/flaky-service").run())
        .run();
```
