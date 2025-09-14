import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class server {
    public static void main(String[] args) {

        try {
            System.out.println("Waiting for Clients");

            ServerSocket serverSocket = new ServerSocket(8787);
            System.out.println("Server started at port: " + serverSocket.getLocalPort());

            Socket socket = serverSocket.accept();
            System.out.println("Client Accepted on socket: " + socket.getInetAddress());

            while (true) {
                BufferedReader clientData = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                String inputString = clientData.readLine();
                System.out.println("Input Stream from Buffered reader: " + inputString);

                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                out.println("Server says "+ inputString);

                BufferedReader UserInput = new BufferedReader(new InputStreamReader(System.in));

                System.out.println("Reply to Client: ");
                String str = UserInput.readLine();
                out.println(str);

            }



        } catch (Exception e) {
            // TODO: handle exception
            System.out.println("Unable to Start Socket. Error: "+ e.getMessage());
        }

    }
}
