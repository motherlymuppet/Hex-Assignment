/**
 * Created by Steven on 15/03/2016.
 */


@SuppressWarnings ("ALL") public class Main{
/*
    public static void main(String[] args){
        try{
            ArrayList<Double> list = new ArrayList<Double>();
            for(int k = 0; k < 1; k++){
                GameManager gameManager = new GameManager();
                int size = 12;
                gameManager.boardSize(1, 2);
                PlayerInterface playerRed;
                PlayerInterface playerBlue;
                playerRed = new HumanPlayer(1000, 1000);
                //playerBlue = new HumanPlayer();
                playerBlue = new ComputerPlayer(AIType.COMBO);
                //playerRed = new ComputerPlayer(AIType.COMBO);

                gameManager.specifyPlayer(playerRed, Piece.RED);
                gameManager.specifyPlayer(playerBlue, Piece.BLUE);
                gameManager.playGame();
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
    */

    public static void main(String[] args){
        MainMenu mainMenu = new MainMenu();
    }
}
