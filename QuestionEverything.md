# 1st iteration

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

# 2nd Iteration

Connecting client Manually

```java
// Create Socket object with IP Adress of sserver and port number
Socket socket = new Socket("localhost", 8787);
System.out.println("Connected to Server on : "+ socket.getInetAddress());
```
- Client connects to this socket in perticular
- address of server is -> IP adresss (local host) and port number (8787)
- `socket.getInetAddress()` -> Returns the IP address of the server the client is connected to.