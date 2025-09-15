package src;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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

        // connct buffered reader to clients socket input stream
        // using buffered reader because http is line bassed protocol and we have to use readline for the ease of use
        BufferedReader connectionReader = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));

        //Read top request line from client
        //ex: GET /index.html HTTP/1.1
        String  requestLine = connectionReader.readLine();

        // If request line is not null
        if (requestLine != null){
            // since top line is formatted different we extract it first
            // First line = request line → special 3-part format.
            // rest have same format They are always Key: Value
            String[] requestLineParams = requestLine.split(" ");

            request.put("Method", requestLineParams[0]);
            request.put("Resource", requestLineParams[1]);
            request.put("Protocol", requestLineParams[2]);

            // read next line
            String headerLine = connectionReader.readLine();

            /*
            * as a header has many key value pairs storred inside
            * till now we have only extracted 1 pair
            * we store the lines/parameters in the map
            * and continue with next parameter
            */
            while (!headerLine.isEmpty()) {
                // Split the requsets filed into key value pair
                String[] requestParams = headerLine.split(":", 2);

                //store it in map
                //the second text will have a leading space bar
                request.put(requestParams[0], requestParams[1].replaceFirst(" ", ""));


                /*
                * In HTTP, headers are terminated by a blank line (\r\n).
                * every header line ends with \r\n.
                * But there’s a special extra \r\n after the last header.
                * Now it sees the empty line (\r\n with no characters in it) → readLine() returns ""
                */
                headerLine = connectionReader.readLine();

            }

        }

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
