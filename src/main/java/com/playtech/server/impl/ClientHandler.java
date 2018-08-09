package com.playtech.server.impl;

import com.playtech.common.Debugger;
import com.playtech.game.protocol.FinishRoundRequest;
import com.playtech.game.protocol.PlayerActionRequest;
import com.playtech.game.protocol.PlayerActionResponse;
import com.playtech.game.protocol.StartRoundRequest;
import com.playtech.server.api.SetBaseCardRequest;
import com.playtech.server.api.SetBaseCardResponse;

import java.io.*;
import java.net.Socket;
import java.sql.Timestamp;

/**
 * ClientHandler handles each client that requests game service in a separate runnable thread.
 */
public class ClientHandler implements Runnable{

    private volatile boolean done = false; // is Client disconnected
    private int clientID;                  // auto incremented clientID by GameServiceImpl
    private Socket clientSocket;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;
    private GameServiceImpl gameService;
    private GameLogic gameLogic;           // GameLogic for this client

    public ClientHandler(Socket clientSocket, GameServiceImpl gameService,int clientID) {
        this.clientID=clientID;
        this.clientSocket = clientSocket;
        this.gameService = gameService;
        this.gameLogic = new GameLogic();

        outputStream=null;
        try {
            outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        inputStream=null;
        try {
            inputStream = new ObjectInputStream(clientSocket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Shuts down the runnable thread of this client
     */
    private void shutdown() {
        done = true;
    }


    /**
     *  Runs a continuous game service for this client
     */
    public void run() {

        Debugger.logConnect(String.format("Received Connection from %s",this.clientSocket.getLocalSocketAddress()));

        try {
            do {

                Timestamp timestamp = Timestamp.valueOf(Debugger.embedTimeStamp());
                long lsTimeStamp = timestamp.getTime();
                StartRoundRequest startRoundRequest = new StartRoundRequest(10,lsTimeStamp,gameLogic.getRoundID(),gameLogic.getNextCard());
                Debugger.logMessage("This is current BaseCard: "+startRoundRequest.getBaseCard()+" of client["+clientID+"]");
                outputStream.writeObject(startRoundRequest);
                gameLogic.setRoundFinished(false);
                //-----------------------------------

                Object req = inputStream.readObject();

                if(req instanceof PlayerActionRequest)
                {
                    // Check player action
                    PlayerActionRequest playerActionRequest =(PlayerActionRequest)req;
                    PlayerActionResponse playerActionResponse = gameService.playerAction(playerActionRequest,gameLogic);
                    Debugger.logMessage("This is client["+clientID+"] choice: "+playerActionRequest.getPlayerAction());
                    outputStream.writeObject(playerActionResponse);

                    // Send finish round request
                    FinishRoundRequest finishRoundRequest = new FinishRoundRequest(gameLogic.getRoundID(),gameLogic.checkWin(playerActionRequest.getPlayerAction()));
                    Debugger.logMessage("This is servers final decision for client["+clientID+"]. Win: "+finishRoundRequest.isWin());
                    outputStream.writeObject(finishRoundRequest);
                    gameLogic.setRoundFinished(true);
                }
                else if(req instanceof SetBaseCardRequest)
                {

                    SetBaseCardRequest setBaseCardRequest = (SetBaseCardRequest)req;
                    Debugger.logMessage("client["+clientID+"] set this as Basecard: "+setBaseCardRequest.getBaseCard());
                    SetBaseCardResponse setBaseCardResponse = gameService.setBaseCard(setBaseCardRequest,gameLogic);
                    if(setBaseCardResponse.getErrorText().length()==0){
                        gameLogic.setCurrentBaseCard(setBaseCardRequest.getBaseCard());
                    }

                    Debugger.logMessage("Sending SetBaseCardResponse");
                    //Set BaseCard Response no errors.
                    outputStream.writeObject(setBaseCardResponse);
                }
                else{
                    // Unknown Request
                    Debugger.logError("Unknown Request..!");
                }

            }while(true);
        } catch (IOException e) {
            Debugger.logDisconnect("Disconnect received from client["+clientID+"]");
            stopComm();
        }
        catch (ClassNotFoundException e) {
            Debugger.logDisconnect("In accurate input from client "+clientSocket.getLocalSocketAddress());
            stopComm();
        }
    }


    /**
     * Stops the serving for this client and closes sockets and connections
     */
    public void stopComm() {
        try {
            inputStream.close();
            outputStream.close();
            clientSocket.close();
            gameService.getClientHandlers().remove(this);
            gameService.getClientSockets().remove(this.clientSocket);
            this.shutdown();

        }
        catch (IOException e)
        {
            Debugger.logError(e.getMessage());
        }
    }
}
