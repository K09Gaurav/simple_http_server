package src;

import java.net.ServerSocket;
import java.net.Socket;

public class server_for_multiple_Clients {
    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(8787);
            System.out.println("Started Server on port : "+ serverSocket.getLocalPort() + "\r\n");
            
            /*
             * Listening for new Clients
             */
            while (true) {

                //Accepting new Client connection
                Socket connectionSocket = serverSocket.accept();

                //create new thread to accept client request
                Thread connectionThread = new Thread(new connection(connectionSocket));

                //start connection Thread
                connectionThread.start();
                System.out.println("New Client connected on port : " + connectionSocket.getInetAddress() + "\r\n");

            }


        } catch (Exception e) {
            // TODO: handle exception
            System.out.println(e.printStackTrace(););
        }


    }
}
