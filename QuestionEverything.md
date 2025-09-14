# 1) iteration

```java
    ServerSocket serverSocket = new ServerSocket(8787);
    Socket socket = serverSocket.accept();
    System.out.println("Client Accepted on socket: " + socket.getInetAddress());
```

when running the server and connecting to it via
- browser : Client Accepted on socket: /127.0.0.1
- curl : Client Accepted on socket: /0:0:0:0:0:0:0:1

why? becuase
- `/127.0.0.1` → IPv4 loopback address.
- Browser is connecting over **IPv4**.

- `/0:0:0:0:0:0:0:1` → IPv6 loopback address (`::1` shorthand).
- `curl` defaults to **IPv6** if available.

- Both addresses point to the **same machine**.

**Why it happens**s
1. `ServerSocket` listens on **all interfaces** (IPv4 + IPv6).
2. The client decides which protocol to use:
 - Browser → IPv4
 - curl → IPv6

# 2) Iteration

Connecting client Manually

```java
// Create Socket object with IP Adress of sserver and port number
Socket socket = new Socket("localhost", 8787);
System.out.println("Connected to Server on : "+ socket.getInetAddress());
```
- Client connects to this socket in perticular
- address of server is -> IP adresss (local host) and port number (8787)
- `socket.getInetAddress()` -> Returns the IP address of the server the client is connected to.


# 3) Echo Application

`BufferedReader UserInput = new BufferedReader(new InputStreamReader(System.in));`

Used Buffered reader because:
- its faster than scanner for larger data
- scanner parses input while buffer provide exact chunk of data
- `BufferedReader` works directly with `InputStreams`, which is what sockets provide.

Used Input Stream reader:
- Converts a byte stream (System.in or a socket InputStream) into a character stream (Reader).
- Handles encoding (e.g., UTF-8) so you can read text properly.

Why Not Scanner?

- Scanner also reads from streams, but it parses input into tokens (nextInt(), nextDouble(), etc.), which adds overhead.
- For servers and raw text input, you usually want the exact line as typed or sent, not tokenized values.

# 4) Send Data to Server

```java
    System.out.println("Say Hello to the Server: ");
    String str = userInput.readLine();

    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
    out.println(str);
```

Why PrintWriter?

- Socket Output is Bytes
- socket.getOutputStream() gives raw bytes.
- You could write directly with OutputStream.write(), but then you’d have to handle manual conversion from String → byte[].

PrintWriter = Text-Friendly

- Converts characters/strings into bytes automatically.
- Provides convenient methods like print(), println(), printf() (just like System.out).
- Makes writing text-based protocols (like HTTP, chat messages) much easier.

Why true (autoFlush)?

- Normally, PrintWriter buffers output for efficiency.
- Without flushing, data may sit in memory instead of being sent immediately.
- true in the constructor turns on autoFlush: