package it.polimi.ingsw.network.client;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import it.polimi.ingsw.view.ClientMain;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import static it.polimi.ingsw.controller.ServerMain.SLEEP_TIMEOUT;

/**
 * Implementation of Socket connection to server. It is one of the two alternatives for connecting
 * to the server.
 *
 * @author marcobaga
 */
public class TCPConnection implements Runnable {

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private ClientMain clientMain;
    private static final Logger LOGGER = Logger.getLogger("clientLogger");
    private ExecutorService executor = Executors.newCachedThreadPool();
    private static final int SOTIMEOUT = 100;
    private boolean shutdown;
    private long lastPingReceived;
    private boolean pingReceived;

    /**
     * Constructor establishing a standard TCP connection
     *
     * @param clientMain        reference to the main class
     * @param address           IP to connect to
     * @param port              port to connect to
     */
    public TCPConnection(ClientMain clientMain, String address, int port){
        this.clientMain = clientMain;
        this.shutdown = false;
        this.lastPingReceived = System.currentTimeMillis();
        this.pingReceived = false;
        LOGGER.log(Level.INFO, "Starting TCP connection");
        try {
            socket = new Socket(address, port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream());
            socket.setSoTimeout(SOTIMEOUT);
            LOGGER.log(Level.INFO, "Connected to TCP server");
        }catch (ConnectException ex){
            LOGGER.log(Level.SEVERE, "Cannot connect to server. Closing", ex);
            shutdown();
            clientMain.showDisconnection();
        }catch (IOException ex) {
            LOGGER.log(Level.INFO, "Cannot read or write to connection. Closing");
            shutdown();
            clientMain.showDisconnection();
        }
        executor.submit(()->{
           while(Thread.currentThread().isAlive()&&!shutdown){
               out.println("PING");
               out.flush();
               try {
                   TimeUnit.MILLISECONDS.sleep(1000);
               } catch (InterruptedException ex) {
                   LOGGER.log(Level.SEVERE, "Skipped waiting time");
                   Thread.currentThread().interrupt();
               }
           }
        });

    }

    /**
     * Differently from RMIConnection, TCPConnection requires a dedicated thread to listen to the socket.
     * In the main loop, the connection checks if a message is received (non blocking-call) and handles it,
     * or waits a little before reiterating.
     */
    public void run(){
        JsonParser jsonParser = new JsonParser();
        while(Thread.currentThread().isAlive()&&!shutdown){
            try {
                String message = receive();
                JsonObject jMessage = (JsonObject) jsonParser.parse(message);
                LOGGER.log(Level.INFO, "Received message: " + jMessage.get("head").getAsString());
                handleRequest(jMessage);
            }catch (SocketTimeoutException ex) {
                LOGGER.log(Level.FINEST, "No incoming message from TCPVirtualView", ex);
                if(pingReceived && System.currentTimeMillis() - lastPingReceived > 5000){
                    shutdown();
                    clientMain.showSuspension();
                }
            }catch(Exception ex){
                LOGGER.log(Level.SEVERE, "Received string cannot be parsed to Json", ex);
                JsonObject jMessage = new JsonObject();
                jMessage.addProperty("head", "MALFORMED");
                handleRequest(jMessage);
            }
            try {
                TimeUnit.MILLISECONDS.sleep(SLEEP_TIMEOUT);
            } catch (InterruptedException ex) {
                LOGGER.log(Level.SEVERE, "Skipped waiting time");
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Decodes incoming messages and calls related clientMain methods
     *
     * @param jMessage      incoming message
     */
    private void handleRequest(JsonObject jMessage){
        String head = jMessage.get("head").getAsString();
        switch(head){
            case "MSG" :    clientMain.display(jMessage.get("text").getAsString());
                            break;
            case "REQ" :    executor.submit(
                                ()-> {
                                    String msg = clientMain.getInput(jMessage.get("text").getAsString(),Integer.valueOf(jMessage.get("length").getAsString()));
                                    send(msg);
                                }
                            );
                            break;
            case "OPT" :    List<String> list = new ArrayList<>();
                            for(JsonElement j : jMessage.get("options").getAsJsonArray()){
                                list.add(j.getAsString());
                            }
                            executor.submit(
                                ()->{
                                    int choice = clientMain.choose(jMessage.get("type").getAsString(), jMessage.get("text").getAsString(), list);
                                    send(String.valueOf(choice));
                                }
                            );
                            break;
            case "UPD" :
                            clientMain.update(jMessage);
                            break;
            case "SUSP" :   shutdown();
                            clientMain.showSuspension();
                            break;
            case "END" :    shutdown();
                            clientMain.showEnd(jMessage.get("msg").getAsString());
                            break;
            default:        break;
        }
    }

    /**
     * Sends a message through the socket (NOT blocking)
     *
     * @param message       message to send
     */
    private void send(String message){
        if(message.equals("PING")){
            message = message.concat(" ");
        }
        out.println(message);
        out.flush();
        LOGGER.log(Level.FINE, "Message sent to TCP server: {0}", message);
    }

    /**
     * Checks the socket input stream for new messages (NOT blocking)
     *
     * @return          the string received (empty if no message arrived)
     * @throws SocketTimeoutException       if no message arrives before socket timeout
     */
    private String receive() throws SocketTimeoutException{
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("head", "PING");
        String message = jsonObject.toString();
        try {
            message = in.readLine();
            if (message == null) {
                LOGGER.log(Level.INFO, "TCPConnection: server disconnected, shutting down");
                shutdown();
                clientMain.showDisconnection();
            } else if (message.equals("PING")){
                lastPingReceived = System.currentTimeMillis();
                pingReceived = true;
                throw new SocketTimeoutException();
            }
        }catch(SocketTimeoutException ex) {
            throw new SocketTimeoutException();
        }catch(IOException ex) {
            LOGGER.log(Level.INFO, "TCPConnection: server disconnected, shutting down");
            shutdown();
            clientMain.showDisconnection();
        }
        return message;
    }

    /**
     * Closes the connection
     */
    private void shutdown(){
        shutdown=true;
        try {
            socket.close();
        }catch (Exception ex){
            LOGGER.log(Level.SEVERE, "Error while closing connection", ex);
        }
    }
}
