import java.net.Socket;

public class client {
    public static void main(String[] args) {
        System.out.println("client started");
        try {
            // Create Socket object with IP Adress of sserver and port number
            Socket socket = new Socket("localhost", 8787);
            System.out.println("Connected to Server on : "+ socket.getInetAddress());
        } catch (Exception e) {
            // TODO: handle exception
            System.out.println("Unable to connect to server : "+ e.getMessage());
        }
    }
}
