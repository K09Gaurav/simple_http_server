package echo_oneToOne_Connection;

import java.io.*;
import java.net.Socket;

public class client {
    public static void main(String[] args) {
        System.out.println("client started");
        try {
            try (// Create Socket object with IP Adress of sserver and port number
            Socket socket = new Socket("localhost", 8787)) {
                System.out.println("Connected to Server on : "+ socket.getInetAddress());

                while (true) {

                    /*
                        *Input stream reader - takes byte strea and gives character string
                        *Buffered reader readss entire string at a time.
                        */
                    BufferedReader UserInput = new BufferedReader(new InputStreamReader(System.in));

                    System.out.println("Say Hello to the Server: ");
                    String str = UserInput.readLine();

                    /*
                        * socket.getOutputStream() gives raw bytes.
                        * You could write directly with OutputStream.write(),
                        * but then you’d have to handle manual conversion from String → byte[].
                        *
                        * Why true (autoFlush)?
                        * Normally, PrintWriter buffers output for efficiency.
                        * Without flushing, data may sit in memory instead of being sent immediately.
                        */
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                    out.println(str);

                    BufferedReader ServerInput = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                    String Servermessage = ServerInput.readLine();
                    System.out.println("Message from " + Servermessage);

                }
            }

        } catch (Exception e) {
            // TODO: handle exception
            System.out.println("Unable to connect to server : "+ e.getMessage());
        }
    }
}
