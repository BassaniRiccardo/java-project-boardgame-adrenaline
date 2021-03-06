package it.polimi.ingsw.network.client;

import com.google.gson.JsonParser;
import it.polimi.ingsw.network.server.RemoteController;
import it.polimi.ingsw.network.server.RemoteServer;
import it.polimi.ingsw.view.ClientMain;

import java.rmi.NoSuchObjectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import static it.polimi.ingsw.controller.ServerMain.SLEEP_TIMEOUT;

/**
 * Class implementing remote functions to be called by the server when needed.
 * This is one of the two classes creating a connection to the server.
 *
 * @author marcobaga
 */
public class RMIConnection implements Runnable, RemoteView {

    private RemoteController playerStub;
    private ClientMain clientMain;
    private static final Logger LOGGER = Logger.getLogger("clientLogger");
    private JsonParser jsonParser;
    private ExecutorService executor;



    /**
     * Class constructor. Passes itself as a parameter to a remote method so that the server can alter call
     * its own remote methods. On a separate thread, the connection is periodically checked by pinging a
     * remote object created by the server.
     *
     * @param clientMain        reference to the main class
     * @param address           IP to connect to
     * @param port              port to connect to
     */
    public RMIConnection(ClientMain clientMain, String address, int port){
        this.clientMain = clientMain;
        this.jsonParser = new JsonParser();
        this.executor = Executors.newCachedThreadPool();
        try {
            Registry reg = LocateRegistry.getRegistry(address, port);
            RemoteServer serverStub = (RemoteServer) reg.lookup("RMIServer");
            String pcLookup = serverStub.getVirtualView((RemoteView) UnicastRemoteObject.exportObject(this, 0));
            playerStub = (RemoteController) reg.lookup(pcLookup);

            executor.submit(()->{
                while(Thread.currentThread().isAlive()){

                    try{
                        Future<Void> future = executor.submit(new Callable<Void>() {
                            public Void call() throws Exception {
                                playerStub.ping();
                                return null;
                            }
                        });
                        future.get(1, TimeUnit.SECONDS);
                    }catch (Exception ex){
                        LOGGER.log(Level.INFO, "Unable to ping RMI server", ex);
                        shutdown();
                        clientMain.showDisconnection();
                        break;
                    }
                    try {
                        TimeUnit.MILLISECONDS.sleep(SLEEP_TIMEOUT);
                    }catch(InterruptedException ex){
                        LOGGER.log(Level.INFO,"Skipped waiting time.");
                        Thread.currentThread().interrupt();
                    }
                }
            });
        }catch(NotBoundException ex){
            LOGGER.log(Level.SEVERE, "Could not find stubs in registry", ex);
        }catch(RemoteException ex){
            LOGGER.log(Level.SEVERE, "RMI server disconnected, shutting down", ex);
            shutdown();
            clientMain.showDisconnection();
        }

    }

    public void run(){
        //empty method as we do not need to periodically check for incoming messages
    }

    /**
     * Asks clientMain to carry out a decision
     *
     * @param type      the type of options
     * @param msg       message to display
     * @param options   options between which to choose
     * @return          int corresponding to the choice
     * @throws RemoteException according to RMI principles
     */
    public int choose(String type, String msg, List<String> options) throws RemoteException{
        return clientMain.choose(type, msg, options);
    }

    /**
     * Asks clientMain to display a message
     *
     * @param msg           message to display
     * @throws RemoteException according to RMI principles
     */
    public void display(String msg) throws RemoteException{
        clientMain.display(msg);
    }

    /**
     * Asks clientMain to provide a String
     *
     * @param msg       message to display
     * @param max       max length of the answer
     * @return          clientMain's response
     * @throws RemoteException according to RMI principles
     */
    public String getInput(String msg, int max) throws RemoteException{
        return clientMain.getInput(msg, max);
    }

    /**
     * Remote method to be called by the server to assert the state of the connection
     * @throws RemoteException according to RMI principles
     */
    public void ping() throws RemoteException{
        //empty method useful for checking whether the server can callback remote functions from the client
    }

    /**
     * Passes an update to clientMain
     *
     * @param jsonObject        encoded update
     * @throws RemoteException according to RMI principles
     */
    public void update(String jsonObject) throws RemoteException{
        try {
            clientMain.update(jsonParser.parse(jsonObject).getAsJsonObject());
        }catch(Exception ex){
            LOGGER.log(Level.SEVERE, "Exception in parsing jsonObject", ex);
        }
    }

    /**
     * Closes the connection
     */
    private void shutdown(){
        try {
            executor.shutdown();
            UnicastRemoteObject.unexportObject(this, false);
        }catch(NoSuchObjectException ex){
            LOGGER.log(Level.INFO, "issue while closing connection", ex);
        }
    }

    /**
     * Asks the ClientMain to show the suspension screen and eventually terminate.
     *
     * @throws RemoteException according to RMI principles
     */
    public void showSuspension() throws RemoteException{
        shutdown();
        clientMain.showSuspension();
    }

    /**
     * Asks the ClientMain to show the end screen and eventually terminate
     *
     * @param message           message to display at the end
     * @throws RemoteException according to RMI principles
     */
    public void showEnd(String message) throws  RemoteException{
        shutdown();
        clientMain.showEnd(message);
    }
}
