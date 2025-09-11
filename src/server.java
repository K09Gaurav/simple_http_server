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


        } catch (Exception e) {
            // TODO: handle exception
            System.out.println("Unable to Start Socket. Error: "+ e.getMessage());
        }

    }
}
