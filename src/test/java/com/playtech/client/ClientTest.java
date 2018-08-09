package com.playtech.client;

import com.playtech.common.Card;
import com.playtech.common.PlayerAction;
import com.playtech.game.protocol.FinishRoundRequest;
import com.playtech.game.protocol.PlayerActionRequest;
import com.playtech.game.protocol.PlayerActionResponse;
import com.playtech.game.protocol.StartRoundRequest;
import com.playtech.server.api.SetBaseCardRequest;
import com.playtech.server.api.SetBaseCardResponse;
import org.junit.Test;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;


/**
 * Tests Game flow of client side with GameService.
 * Note: Before running these test run GameService first.
 */
public class ClientTest {
    Socket clientSocket;
    ObjectInputStream inputStream;
    ObjectOutputStream outputStream;


    private void setup()throws IOException {
        clientSocket = new Socket("127.0.0.1", 4444);
        inputStream = new ObjectInputStream(clientSocket.getInputStream());
        outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
    }

    private void tearDown() throws IOException {
        clientSocket.close();
        inputStream.close();
        outputStream.close();
    }

    @Test
    public void givenClient_checkServerRoundRequest() throws IOException, ClassNotFoundException {
        // open connection
        setup();

        Object obj = inputStream.readObject();
        assert (obj instanceof StartRoundRequest);
        StartRoundRequest startRoundRequest = (StartRoundRequest)obj;
        //Asserting that server didn't returned null baseCard
        assert(startRoundRequest.getBaseCard() instanceof Card && startRoundRequest.getBaseCard()!=null);

        // close connection
        tearDown();
    }

    @Test
    public void givenValidPlayerAction_ExpectPlayerActionResponseWithNoErrors_And_FinishRoundRequest() throws IOException, ClassNotFoundException {
        // open connection
        setup();

        inputStream.readObject();       // StartRoundRequest
        PlayerAction action =PlayerAction.HIGHER;
        PlayerActionRequest playerActionRequest = new PlayerActionRequest(action);
        outputStream.writeObject(playerActionRequest);

        Object obj = inputStream.readObject();
        assert (obj instanceof PlayerActionResponse);
        PlayerActionResponse playerActionResponse = (PlayerActionResponse)obj;
        //Asserting errors to be none because of the valid user move;
        assert (playerActionResponse.getErrorText().length()==0);


        obj = inputStream.readObject();
        assert (obj instanceof FinishRoundRequest);
        FinishRoundRequest finishRoundRequest = (FinishRoundRequest)obj;
        //Asserting win or lose
        assert (finishRoundRequest.isWin() || !finishRoundRequest.isWin());

        // close connection
        tearDown();
    }


    @Test
    public void givenInValidPlayerAction_ExpectPlayerActionResponseWithError_And_isWin_toBeFalse() throws IOException, ClassNotFoundException {
        // open connection
        setup();

        inputStream.readObject();       // StartRoundRequest
        PlayerAction action =null;
        PlayerActionRequest playerActionRequest = new PlayerActionRequest(action);
        outputStream.writeObject(playerActionRequest);

        Object obj = inputStream.readObject();
        assert (obj instanceof PlayerActionResponse);
        PlayerActionResponse playerActionResponse = (PlayerActionResponse)obj;
        //Expecting error text here..
        assert (playerActionResponse.getErrorText().length()>0);


        obj = inputStream.readObject();
        assert (obj instanceof FinishRoundRequest);
        FinishRoundRequest finishRoundRequest = (FinishRoundRequest)obj;

        //Asserting round lost!!
        assert (!finishRoundRequest.isWin());

        // close connection
        tearDown();
    }

    @Test
    public void givenMiddleOfRound_ExpectSetBaseCardResponse_withError() throws IOException, ClassNotFoundException {
        // open connection
        setup();

        inputStream.readObject();       // StartRoundRequest
        SetBaseCardRequest setBaseCardRequest = new SetBaseCardRequest(Card.C5);
        outputStream.writeObject(setBaseCardRequest);

        Object obj = inputStream.readObject();

        assert (obj instanceof SetBaseCardResponse);
        SetBaseCardResponse setBaseCardResponse = (SetBaseCardResponse)obj;
        //Expecting error here because round hasn't finished yet..
        assert (setBaseCardResponse.getErrorText().length()>0);

        // close connection
        tearDown();
    }
}
