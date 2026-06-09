/**
 * This class 'MainServer' allows to create and lunch the server.
 */

import Serveur.Server;
import PrincipalApplication.ClockingManager;

public class MainServer
{
    // - - - MAIN - - -
    /**
     * The main who launches the server application.
     */
    public static void main(String[] args) throws Exception {
        //We create an object from the 'ClockingManager' class to be able to launch the server
        ClockingManager clockingManager = new ClockingManager();

        //We instantiate the server
        Server myServer = new Server(clockingManager);

        //we start the server
        myServer.start();

        System.out.println("The server is lunched");

        //allows you to not stop the program and to run it continuously
        Thread.currentThread().join();
    }
}