package com.playtech.client.impl;

import com.playtech.common.Debugger;
import com.playtech.common.PlayerAction;

/**
 *  Singleton Class: Validates and manages client side user inputs
 */
public class GameManager {

    private static GameManager gameManager = new GameManager();

    //private constructor to avoid client applications to use constructor
    private GameManager(){}

    public static GameManager getInstance(){
        return gameManager;
    }


    /**
     * Validates users console input.
     * @param input string entered by user
     * @return true if input is valid, false if not.
     */
    public boolean validateInput(String input)
    {
        if(input==null || input.equals(""))
            return false;

        if(input.toLowerCase().trim().equals("1") ||
                input.toLowerCase().trim().equals("2") ||
                input.toLowerCase().trim().equals("3") ||
                input.toLowerCase().trim().equals("4"))
        {
            return true;
        }
        return false;
    }

    /**
     * Gets PlayerAction based on user input from console
     * @param input integer entered by user 1-Higher,2-Lower,3-Equals
     * @return corresponding PlayerAction.
     */
    public PlayerAction getActionByInput(int input)
    {
        return PlayerAction.values()[input-1];
    }

    /**
     * Displays instructions on console.
     */
    public void showInstructions()
    {
        System.out.println(Debugger.ANSI_GREEN+"\n[Instructions]=> During Countdown\nPress 1 [Higher]\nPress 2 [Lower]\nPress 3 [Equal]\nPress 4 Quit-Game\n"+Debugger.ANSI_GREEN);
    }

}
