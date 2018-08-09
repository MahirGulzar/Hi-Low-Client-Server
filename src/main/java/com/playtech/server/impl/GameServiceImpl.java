package com.playtech.server.impl;

import com.playtech.common.Debugger;
import com.playtech.common.ProtocolConfiguration;
import com.playtech.game.protocol.PlayerActionRequest;
import com.playtech.game.protocol.PlayerActionResponse;
import com.playtech.server.api.GameService;
import com.playtech.server.api.SetBaseCardRequest;
import com.playtech.server.api.SetBaseCardResponse;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


/**
 * GameServiceImpl handles new connections from clients and maintains clients
 * list of Sockets and ClientHandlers.
 * Assigns a ClientHandler thread to a new client.
 */
public class GameServiceImpl implements GameService {

    private ServerSocket serverSocket;                              // ServerSocket
    private List<Socket> clientSockets = new ArrayList<Socket>();   // socket of all currently connected clients
    private List<ClientHandler> clientHandlers = new ArrayList<ClientHandler>(); //ClientHandlers with each client
    private int clientCounter;          // counts the number of clients


    public List<Socket> getClientSockets() {
        return clientSockets;
    }

    public List<ClientHandler> getClientHandlers() {
        return clientHandlers;
    }

    @Override
    public PlayerActionResponse playerAction(PlayerActionRequest playerActionRequest, GameLogic gameLogic) {

        if(playerActionRequest.getPlayerAction()==null)
        {
            return new PlayerActionResponse("Invalid Action Type");
        }
        String error="";
        switch (playerActionRequest.getPlayerAction())
        {
            case HIGHER:
                break;
            case LOWER:
                break;
            case EQUALS:
                break;
            default:
                error="Invalid Action Type";
                break;
        }
        return new PlayerActionResponse(error);
    }

    @Override
    public SetBaseCardResponse setBaseCard(SetBaseCardRequest setBaseCardRequest, GameLogic gameLogic) {
        if(gameLogic.isRoundFinished())
        {
            return new SetBaseCardResponse("");
        }
        else
        {
            return new SetBaseCardResponse("Cannot set base card until round is finished");
        }
    }

    /**
     * Starts the server on the given port number
     * @param port The port number on which the server will start
     * @throws IOException Possible exception thrown with every IO operation
     */
    public void start(int port)throws IOException{
        serverSocket = new ServerSocket(port);
        Debugger.logNotifcation(String.format("Server started at PORT:%d",port));

        // loop for multiple client reception
        while(true)
        {
            Socket clientSocket = serverSocket.accept();
            ClientHandler newRunnableClient= new ClientHandler(clientSocket,this,clientCounter);

            clientSockets.add(clientSocket);
            clientHandlers.add(newRunnableClient);

            Thread thread=new Thread(newRunnableClient);
            thread.start();
            clientCounter++;
        }
    }

    /**
     * Stops Server and all corresponding clients are disconnected
     */
    public void stopService() throws IOException {
        serverSocket.close();
        for(ClientHandler client:clientHandlers)
        {
            client.stopComm();
        }
    }

    /**
     * Server Application entry point
     */
    public static void main(String[] args){
        GameServiceImpl server=new GameServiceImpl();

        try {
            server.start(ProtocolConfiguration.PORT);

        }
        catch (IOException e)
        {

            try {
                server.stopService();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
//            Debugger.logError(e.getMessage());

        }
    }
}
