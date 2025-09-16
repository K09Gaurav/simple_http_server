# My goals
- Start Socket Server in Java
- Accept Connections
- Parse clients HTTP Request
- Send back correct http response


# MCQ

1. Why a ServerSocket and not just Socket?

 A client needs a Socket. A server needs a ServerSocket to listen. Why? Because you might accept many clients, not just one.

2. Why threads?

 What happens if you don’t spawn a new Connection thread? Try it — connect with two browsers at once and watch what happens.

3. How is an HTTP request formatted?

 Print out the raw request lines the browser sends. Is it what you expected? Did you know that requests end with a blank line?

4. What’s the minimum valid HTTP response?

 Spoiler: HTTP/1.1 200 sOK\r\n\r\nHello. Try it. Browsers are surprisingly forgiving.

5. What’s dangerous about reading the whole file into memory (byte[] bytes = new byte[(int) file.length()])?

 Imagine serving a 1 GB file. What happens?

6. Why close the socket after every request?

 Browsers often use persistent connections (keep-alive). Your toy server doesn’t.

 ---
# EXPLANATION

## 1. Why Two Files?
- **WebServer.java**
  - Handles **listening** for new clients.
  - Creates a `ServerSocket` → accepts incoming connections.
  - Delegates each client to a separate thread.

- **Connection.java**
  - Handles **one client’s request** from start to finish.
  - Reads request (parses HTTP headers).
  - Decides the correct response (301 redirect, 404 not found, 200 with file).
  - Sends the response back.
  - Runs inside its own thread so multiple clients can connect at once.

👉 Separation of concerns:
- `WebServer` = “traffic cop” (accepts cars and assigns lanes).
- `Connection` = “lane worker” (actually serves the request).

---

## 2. WebServer.java (Responsibility)
- Creates the **ServerSocket** (`new ServerSocket(6789)`).
- Infinite loop:
  1. `accept()` waits for a client to connect.
  2. Wraps the socket in a new `Connection` object.
  3. Starts a new thread → `new Thread(new Connection(socket)).start()`.

Why?
- One client = one thread.
- Without threads → only **one user at a time**.
- Using threads makes the server **concurrent**.

---

## 3. Connection.java (Responsibility)
- Represents a **single conversation** between client & server.
- Implements `Runnable` → allows it to run inside its own thread.

### Step 1: Parse Request
- Read the first line: `GET /index.html HTTP/1.1`.
- Split into:
  - **Method** = GET
  - **Resource** = `/index.html`
  - **Protocol** = HTTP/1.1
- Read headers into a HashMap (e.g., `Host: localhost`, `User-Agent: ...`).

### Step 2: Decide Response
- Check if the requested path is in `redirect` → return 301 redirect.
- If file doesn’t exist → return 404 HTML page.
- If file exists → return 200 with file content.

### Step 3: Send Response
- Write status line + headers.
- Write body (HTML or file bytes).
- Close streams + socket.

---

## 4. Why Runnable Instead of Extending Thread?
- `Connection implements Runnable` instead of `extends Thread`.
- This makes it more **flexible**:
  - You can run it in a thread pool later.
  - Keeps logic (request handling) separate from threading model.

---

## 5. Why HashMaps?
- **Request HashMap**
  - Stores parsed headers for easy access later.
  - Example: `request.get("Host")` → `"localhost:6789"`.

- **Redirect HashMap**
  - Maps old paths to new ones.
  - Example: `"/index"` → `"/index.html"`.
  - Makes redirection logic clean and extensible.

---

## 6. Flow of Execution
1. `WebServer.main()` starts.
2. Waits for client.
3. Client connects → new `Socket`.
4. `new Connection(socket)` object created.
5. Runs inside a new thread.
6. `parseRequest()` reads HTTP request.
7. `sendResponse()` sends correct reply.
8. Close connection.
9. Go back to step 2 for the next client.



 ---
 ---
 ---
# DEEP DIVE
## 1) Why two classes? (Separation of concerns)
- WebServer = acceptor / listener. Its single job: open a ServerSocket, wait for inbound TCP connections, and hand each one off.

    - Why separate? Accepting connections is orthogonal to handling them. Keeping it tiny makes shutdown, socket options, and scheduling easier to reason about.

- Connection = one connection handler. Its job: parse the HTTP request, produce a response, close resources.

    - Why a separate handler? Each connection needs its own lifecycle, streams, parsing state. Making it Runnable lets you choose concurrency strategy (new Thread now, thread-pool later, or async/NIO).

This is the single-responsibility principle: accept vs handle. It keeps the code simple to reason about, test, and replace (e.g., swap the thread-per-connection model for a thread pool or non-blocking IO).

## 2) The runtime lifecycle (sequence)
1. `WebServer` creates `ServerSocket` and loops `accept()`.

2. For each accepted `Socket` it creates `new Connection(socket)` and schedules it (here: `new Thread(...).start()`).

3. `Connection.run()` calls `parseRequest()` → reads request-line + headers (from socket input stream).

4. `Connection.run()` calls `sendResponse()` → inspects the parsed request and writes bytes to socket output stream.

5. `Connection` closes streams and the socket. The thread dies



## 3) Deep dive: parseRequest() — what it does and what to watch for

What it does:

- Uses `BufferedReader.readLine()` to get the request-line and subsequent header lines.

- Splits the first line into `Method Resource Protocol` and stores them in a map.

- Reads headers until an empty line and puts `HeaderName -> HeaderValue` into the request map.

Senior critiques / gotchas:

- `readLine()` returns `null` on EOF. Current code assumes non-null; you must check `if (requestLine == null) { handleBadRequest(); }` to avoid NPEs.

- The loop uses `while (!headerLine.isEmpty())` — but `headerLine` may be `null` on unexpected EOF. Use `while (headerLine != null && !headerLine.isEmpty())`.

- HTTP header names are case-insensitive. Storing them with original case is fragile. Normalize keys (`headerName.toLowerCase(Locale.ROOT)`) and `trim()` both name and value.

- The code does not handle a request body (POST/PUT). If you want to accept POST, you must read `Content-Length` (or handle `Transfer-Encoding: chunked`) and then read exactly that many bytes from the raw InputStream (not from `BufferedReader` which reads characters and may break binary data).

- The request resource may be URL-encoded (spaces as %20). You should `URLDecoder.decode(resource, "UTF-8")`.

- Malformed headers (`no ":"`) will cause `requestParams[1]` to be missing → NPE. Always check `if (requestParams.length == 2)`

- request map is per-connection so concurrency on it is fine; but lookups like `request.get("Resource").toString()` assume the key exists — defensive null checks are required.-


## 4) Deep dive: sendResponse() — correctness, robustness, improvements

What it does:

- Builds a `File` from `"." + resource` and checks existence.

- Redirects if the resource is in `redirect` map (sends 301).

- Sends a 404 page if file missing.

- For existing files, uses `Files.probeContentType()` to guess MIME type, reads the whole file into a `byte[]`, writes a `200` header and the bytes.

Senior critiques / critical bugs & improvements:

- HTTP line endings: HTTP requires `\r\n` (CRLF). The 301 branch uses `\n` only — browsers are forgiving, but this is not protocol-correct and can break some clients. Use `\r\n`.

- Missing required headers: For correctness you should send `Content-Length` (or `Transfer-Encoding: chunked`) and `Connection` headers. Browsers rely on `Content-Length` when connection is persistent.

- Redirect response incomplete: The 301 response should end with a blank line (`\r\n\r\n`) and ideally a small HTML body plus `Content-Length`. Current code writes no CRLFs and no content-length.

- Reading whole file into memory: `byte[] bytes = new byte[(int) file.length()];` `bufInputStream.read(bytes);` assumes `read()` returns full file in one shot (it might not), and loads the entire file into memory (OOM risk for large files). Instead, stream in chunks:
    ```java
    byte[] buf = new byte[8192];
    int n;
    while ((n = bufInputStream.read(buf)) != -1) {
        outStream.write(buf, 0, n);
    }
    ```
    This uses constant memory and is robust.

- `Files.probeContentType()` may return `null`; fallback to `application/octet-stream` or a guessed mapping.

- Mixing text and binary writes: Use explicit charset when writing headers (e.g., `getBytes(StandardCharsets.US_ASCII)`) instead of `writeBytes()` which is ambiguous. Always use byte arrays for the wire bytes to avoid charset surprises.

- No `try-with-resources`: Streams and sockets should be closed even on exceptions. Use try-with-resources to ensure cleanup.

- No support for HEAD method: If `Method` is `HEAD`, you must send headers but skip body.

- No keep-alive handling: HTTP/1.1 defaults to persistent connections; this server simply closes socket. That’s fine for a toy server, but be aware if a client expects keep-alive behavior.


## 5) Concurrency & scalability concerns

- Thread-per-connection (`new Thread(connection).start()`) is simple but not scalable: an attacker or flash crowd can exhaust threads or memory. Use an `ExecutorService` (fixed or bounded thread pool) and a bounded queue to throttle:
    ```java
    ExecutorService pool = Executors.newFixedThreadPool(50);
    pool.submit(new Connection(socket));
    ```

- For very high concurrency use NIO (selectors) or an async framework (Netty, Undertow).

- Add socket read timeouts: `socket.setSoTimeout(ms)` so a client that connects and then stalls doesn't hold a thread forever.

- Limit the maximum request header/body sizes to avoid resource exhaustion.

## 6. Security Issues to Keep in Mind
- **Input Validation**
  - Clients can send malformed or malicious requests.
  - Never assume the request string is safe.
  - Check method (only `GET`/`POST` if that’s all you support).

- **Directory Traversal**
  - If serving files, guard against paths like `../../etc/passwd`.
  - Normalize and validate paths before accessing the file system.

- **Denial of Service (DoS)**
  - A client could keep a connection open forever → use **timeouts**.
  - Limit max request size to avoid memory blowups.

- **Sensitive Info**
  - Never leak stack traces or system details in error responses.
  - Keep responses generic (e.g., `500 Internal Server Error`).

---

## 7. Helpful Small Code Patterns (Practical Fixes)
- **Try-with-resources**
  ```java
  try (Socket client = serverSocket.accept();
       BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
       PrintWriter out = new PrintWriter(client.getOutputStream(), true)) {
      // handle request
  }
  ```
  → Ensures sockets and streams close automatically.

- **Simple Router Pattern**
  ```java
  if (path.equals("/users")) {
      handleUsers(out);
  } else {
      sendNotFound(out);
  }
  ```
  → Prevents huge if-else chains later.

- **Constants for Status Codes**
  ```java
  private static final String HTTP_200 = "HTTP/1.1 200 OK\r\n";
  private static final String HTTP_404 = "HTTP/1.1 404 Not Found\r\n";
  ```
  → Avoids typo-ridden strings scattered everywhere.

## 8. Debugging Habits
- **Log Every Step**
  - Print out request lines, headers, and chosen responses while testing.
  Example:
  ```java
  System.out.println("Received: " + requestLine);
  ```
- **Predict Before Run**
  - Guess what response the browser should show before you test.
  - Builds intuition and faster debugging skills.

- **Browser vs. cURL**

  - Use a browser for simplicity.

  - Use curl -v http://localhost:8080/users to see raw request/response details.

## 9. Common Mistakes (and How to Avoid Them)

### 1. Forgetting CRLF
- HTTP requires `\r\n` (Carriage Return + Line Feed) line endings.
- If you miss them, the browser may hang or fail to parse the response.

### 2. Not Flushing Output
- Always flush your output streams.
- Example: `out.flush()` or use a `PrintWriter` with `autoFlush = true`.

### 3. Blocking Everything on One Thread
- Without threads, only **one client** can be served at a time.
- Fix: use concurrency →
  ```java
  new Thread(new Connection(client)).start();
