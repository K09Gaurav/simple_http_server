package src;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.sql.Connection;
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
        // using buffered reader because
        // http is line bassed protocol and we have to use readline for the ease of use
        // HTTP is line-based (\r\n separated)
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
        // We will check the request if the requested file is inside the map/directory
        // we will send the correct response

        /*
         * Create a DataOutputStream to send data
         * Not using OutputStream because its easier to use .writeBytes() using DataOutputStream
         * Since OutputStream is base class for writing raw bytes
         * We have to manually convert everything ourself
         * DataOutputStream wraps an OutputStream and adds convenience methods
         *
         * No need to create bufured reader or such because no need of it we need to send in bytes
         */
        DataOutputStream outputStream = new DataOutputStream(connectionSocket.getOutputStream());


        //Get file path requested by the client
        String resourcePath = request.get("Resource");

        /*
         * "." means current working directory of your Java process.
         * So "." + "/index.html" → "./index.html".
         *
         * If not adding "." just leaving "/" is problematic
         * as on some system "/" is treated as root directory
         */
        File file = new File("." + resourcePath);

        // now decide reponse code based on activity of resssource path
        if (redirect.get(resourcePath) != null){

            // if the requested path matches an entry in redirect map,
            // tell client to use the new location (301 Moved Permanently)
            // in crlf line end with \r\n
            outputStream.writeBytes("HTTP/1.1 301 Moved Permanently\r\n" +
                                "Location: " + redirect.get(resourcePath) + "\r\n\r\n");

            return;
        }

        //if file requested does not exists in sysstem
        // resspond 404 with a webpage
        else if (!file.exists()) {
            String httpResponse = """
                    HTTP/1.1 404 Not Found\r\n
                    Content-Type: text/html; charset=UTF-8\r\n
                    \r\n
                    <!DOCTYPE html>\n
                    <html>\n
                    \n
                    <head>\n
                    <title> Unknown File Requessted from server </title>\n
                    </head>\n
                    \n
                    <body>\n
                    <h1> WEB PAGE NOT FOUND </h1>\n
                    </body>\n
                    \n
                    </html>
                    """;
            // Cant ussse writeBytes like previouse one
            // writeBytes(String) expects a String. we are passing a byte[].
            outputStream.write(httpResponse.getBytes("UTF-8"));
        }

        // if the file iss not in redirect and exists
        // it means correct file requested
        // return 200
        else{
            //read data from file
            FileInputStream fileInputStream = new FileInputStream(file);

            /*
             * get mime file type of the file requested by the client
             * MIME = Multipurpose Internet Mail Extensions
             * In HTTP, it tells the browser what kind of file it is receiving.
             * eg - text\html -> html file
             * Without it, the browser may not know how to interpret the file
             * it might just download it as a raw binary instead of displaying.
             */
            String contentType = Files.probeContentType(file.toPath());

            /*
             * Now start sending the data
             * FileInputStream reads raw bytes from a file on disk.
             * If we use File Input stream only
             * Every read() call hits the disk directly, which is relatively slow. (1 byte → JVM → write → repeat)
             *
             * But with the help of Buffer we make a buffer in memory
             * 8KB by default, each time read() is called instead of hitting disk
             * its hitting RAM which has appx 8192bytes
             *
             * i.e
             * Without buffer: disk → 1 byte → JVM → write → repeat
             * With buffer: disk → 8192 bytes → JVM → write many from buffer
             *
             * Buffered streaming lets you send chunks gradually
             * instead of whole file at once or by 1-1 byte into the memory
             */
            BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);

            //Send Header
            outputStream.writeBytes("HTTP/1.1 200 OK\r\nContent-Type: " + contentType + "\r\n\r\n");

            //send body

            // for streaming the file to the client.
            // this acts as our working bucket to temporarily hold chunks of file data as we read them.
            byte[] buffer = new byte[4096];

            //store how many bytes were actually read in each iteration.
            int bytesRead;

            /*
             * bufferedInputStream.read(buffer) → Reads up to buffer.length bytes (here, 4096)
             * from the file into the array.
             * Returns the number of bytes actually read, or -1 if the end of the file (EOF) is reached.
             */
            while ((bytesRead = bufferedInputStream.read(buffer)) != -1) {
                /*
                 * Sends the chunk we just read to the socket’s output stream
                 *
                 * buffer → the data to send.
                 * 0 → start at position 0 in the buffer/ offset
                 * bytesRead → number of bytes to actually send.
                 */
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.flush();

            bufferedInputStream.close();
        }

        outputStream.close();


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
