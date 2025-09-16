# Java Networking Projects

This repository contains two educational Java projects that explore networking, sockets, and multi-threaded server-client communication. Both projects were developed to understand the fundamentals of backend systems and low-level network programming. Extensive inline comments are included throughout the code to explain the logic, design decisions, and core concepts.

---

## 1. MultiClientServer – Minimal HTTP Server

**Location:** `MultiClientServer/`  
**Technologies:** Java, Sockets, Multi-threading, File I/O  

### Overview
This project implements a multi-threaded HTTP server from scratch using raw Java sockets. The server can handle multiple client connections concurrently and serves static files while demonstrating fundamental HTTP request/response handling.

### Features
- **Multi-threaded server:** Each client connection is handled in a separate thread for concurrent requests.
- **HTTP request parsing:** Supports parsing HTTP methods, headers, and resource paths.
- **Response handling:** Returns correct HTTP responses including:
  - `200 OK` for valid resources
  - `301 Moved Permanently` for redirects
  - `404 Not Found` for missing files
- **File serving:** Sends static files with proper MIME types using buffered I/O for performance.
- **URL routing and redirection:** Supports custom redirect rules and default routing logic.
- **Inline comments:** Every class and method contains detailed comments explaining the code’s function and purpose.

### Learning Outcomes
- Understanding low-level network programming concepts.
- Gained experience with concurrency and thread management.
- Learned HTTP protocol internals and response formatting.
- Practiced clean code documentation through detailed commenting.

---

## 2. Echo_OneToOne_Connection – Simple Client-Server Chat

**Location:** `echo_oneToOne_Connection/`  
**Technologies:** Java, Sockets, Input/Output Streams  

### Overview
This project implements a basic client-server chat system where a single client communicates with a server over TCP sockets. It serves as an educational introduction to socket programming and blocking I/O.

### Features
- **Single client-server communication:** Establishes a TCP connection between one client and one server.
- **Two-way messaging:** Client and server can send and receive messages in real time.
- **Input/output handling:** Uses `BufferedReader` and `PrintWriter` for stream management.
- **Inline comments:** Step-by-step explanations provided throughout the code to clarify socket creation, message sending, and stream handling.

### Learning Outcomes
- Gained hands-on experience with socket-based communication.
- Learned the basics of blocking I/O and simple client-server design.
- Developed practice in documenting code for clarity and understanding.

---

## How to Run

### MultiClientServer
1. Navigate to the `MultiClientServer/` directory.
2. Compile the server and connection classes:
   ```bash
   javac Server.java Connection.java
   ```
3. Start the server:
  ```bash
  java Server
  ```
4. Open a web browser and navigate to http://localhost:8787/index.html you can play with end point while observing network tab to see what response code is being recieved from the server.
5. You can also access it via command line using:
   ```bash
   curl -v localhost:8787/index.html
   ```
   That -v will help you observe what is happening under the hood between the server and the client.
