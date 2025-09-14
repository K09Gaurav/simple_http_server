package src;

import java.io.IOException;
import java.net.Socket;
import java.util.*;


public class connection implements Runnable{
    
    // socket used to connect to client
    private Socket connectionSocket;

    //HashMap to store Client Request
    private HashMap <String, String> request;

    // A HashMap that contains keys that need to be redirected to the given value
    private HashMap<String, String> redirect;


    /*
     * Create Connection Object
     *
     * @param connectionSocket -> Socket recieved from server that has accepted the client.
     */
    public connection(connectionSocket){
        this.connectionSocket = connectionSocket;
        this.request = new HashMap<>();
        this.redirect = new HashMap<>();

        redirect.put("/", "/index.html");
        // redirect.put("", "/index.html"); -> redundant because http by default never send "" always / is default
        redirect.put("index.html", "/index.html");
        redirect.put("index", "/index.html");
        redirect.put("home.html", "/index.html");
        redirect.put("homepage.html", "/index.html");
    }

    /*
     * Parse Client Request
     * inserts all the request fields into request hashmap
     *
     * Key is the Request Field and value is the value recieved
     *
     * @throws IOException if connectionSocket is not Present
     */
    private void parseRequest() throws IOException{

    }

    /*
     * Send Appropriate Response based on client Request
     *
     * If URL requested is:
     *      inside rediirect hashmap    : 301 (sending client to new url)
     *      not inside redirect hashmap : 404 (not found)
     *      if normal url (url exists)  : 200 (OK)
     *
     * @throws IOException if any kind of file streaming is closed / do not exist while in use
     */
    private void sendResponse() throws IOException{

    }

    @Override
    public void Run(){
        try {
            //Parse the client Request and store the request in request hashmap
            parseRequest();

            //send ressponse
            sendResponse();

            //close connection after sending the response
            this.connectionSocket.close();

        } catch (Exception e) {
            // TODO: handle exception
            System.out.println(e.printStackTrace());
        }

    }
}
