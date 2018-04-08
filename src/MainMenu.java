import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

/**
 * Create the main menu, and initialise games based on its settings
 *
 * @author Steven Lowes
 */
public class MainMenu extends JFrame{

    private Dimension size;
    private QualityPanel panel;
    private JLabel titleText;
    private JLabel boardTitle;
    private QualityPanel boardSettingsPanel;
    private JLabel boardWidth;
    private JTextField widthField;
    private JLabel boardHeight;
    private JTextField heightField;
    private JLabel playersTitle;
    private QualityPanel redPanel;
    private JLabel redPlayerText;
    private JRadioButton redHuman;
    private JRadioButton redMCTS;
    private JRadioButton redDjikstra;
    private JRadioButton redCombo;
    private QualityPanel bluePanel;
    private JLabel bluePlayerText;
    private JRadioButton blueHuman;
    private JRadioButton blueMCTS;
    private JRadioButton blueDjikstra;
    private JRadioButton blueCombo;
    private MyMouseListener mouseListener;
    private JButton startButton;

    public MainMenu(){
        size = new Dimension(900, 900);
        initialiseFrame(size);
    }

    public MainMenu(Dimension size){
        this.size = size;
        initialiseFrame(size);
    }

    private void initialiseFrame(Dimension size){
        setSize(size);
        setTitle("Main Menu");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        panel = new QualityPanel();
        panel.setBackground(Color.WHITE);
        Box box = Box.createVerticalBox();

        titleText = new JLabel("Main Menu");
        titleText.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleText.setForeground(Color.BLACK);
        titleText.setFont(new Font("Arial", Font.BOLD, 36));
        box.add(titleText);

        boardTitle = new JLabel("Board");
        boardTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        boardTitle.setForeground(Color.BLACK);
        boardTitle.setFont(new Font("Arial", Font.BOLD, 24));
        box.add(boardTitle);

        boardSettingsPanel = new QualityPanel();
        boardWidth = new JLabel("Width");
        boardWidth.setAlignmentX(Component.CENTER_ALIGNMENT);
        boardWidth.setForeground(Color.BLACK);
        boardWidth.setFont(new Font("Arial", Font.PLAIN, 12));
        boardSettingsPanel.add(boardWidth);
        widthField = new JTextField("11", 5);
        boardSettingsPanel.add(widthField);
        boardHeight = new JLabel("Height");
        boardHeight.setAlignmentX(Component.CENTER_ALIGNMENT);
        boardHeight.setForeground(Color.BLACK);
        boardHeight.setFont(new Font("Arial", Font.PLAIN, 12));
        boardSettingsPanel.add(boardHeight);
        heightField = new JTextField("11", 5);
        boardSettingsPanel.add(heightField);
        box.add(boardSettingsPanel);

        playersTitle = new JLabel("Players");
        playersTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        playersTitle.setForeground(Color.BLACK);
        playersTitle.setFont(new Font("Arial", Font.BOLD, 24));
        box.add(playersTitle);

        redPanel = new QualityPanel();
        redPanel.setBackground(Color.RED);
        redPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        redPlayerText = new JLabel("Red Player:");
        redPlayerText.setAlignmentX(Component.CENTER_ALIGNMENT);
        redPlayerText.setForeground(Color.BLACK);
        redPlayerText.setFont(new Font("Arial", Font.BOLD, 12));
        redPanel.add(redPlayerText);
        redHuman = new JRadioButton("Human");
        redMCTS = new JRadioButton("MCTS");
        redDjikstra = new JRadioButton("Djikstra");
        redCombo = new JRadioButton("Combo");
        redHuman.setBackground(Color.RED);
        redMCTS.setBackground(Color.RED);
        redDjikstra.setBackground(Color.RED);
        redCombo.setBackground(Color.RED);
        ButtonGroup redTypes = new ButtonGroup();
        redTypes.add(redHuman);
        redTypes.add(redMCTS);
        redTypes.add(redDjikstra);
        redTypes.add(redCombo);
        redPanel.add(redHuman);
        redPanel.add(redMCTS);
        redPanel.add(redDjikstra);
        redPanel.add(redCombo);
        box.add(redPanel);

        bluePanel = new QualityPanel();
        bluePanel.setBackground(Color.BLUE);
        bluePanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        bluePlayerText = new JLabel("Blue Player:");
        bluePlayerText.setAlignmentX(Component.CENTER_ALIGNMENT);
        bluePlayerText.setForeground(Color.WHITE);
        bluePlayerText.setFont(new Font("Arial", Font.BOLD, 12));
        bluePanel.add(bluePlayerText);
        blueHuman = new JRadioButton("Human");
        blueMCTS = new JRadioButton("MCTS");
        blueDjikstra = new JRadioButton("Djikstra");
        blueCombo = new JRadioButton("Combo");
        blueHuman.setBackground(Color.BLUE);
        blueMCTS.setBackground(Color.BLUE);
        blueDjikstra.setBackground(Color.BLUE);
        blueCombo.setBackground(Color.BLUE);
        blueHuman.setForeground(Color.WHITE);
        blueMCTS.setForeground(Color.WHITE);
        blueDjikstra.setForeground(Color.WHITE);
        blueCombo.setForeground(Color.WHITE);
        blueMCTS.setBackground(Color.BLUE);
        blueDjikstra.setBackground(Color.BLUE);
        blueCombo.setBackground(Color.BLUE);
        ButtonGroup blueTypes = new ButtonGroup();
        blueTypes.add(blueHuman);
        blueTypes.add(blueMCTS);
        blueTypes.add(blueDjikstra);
        blueTypes.add(blueCombo);
        bluePanel.add(blueHuman);
        bluePanel.add(blueMCTS);
        bluePanel.add(blueDjikstra);
        bluePanel.add(blueCombo);
        redHuman.setSelected(true);
        blueHuman.setSelected(true);
        box.add(bluePanel);

        startButton = new JButton("Start Game");
        startButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        mouseListener = new MyMouseListener();
        startButton.addMouseListener(mouseListener);
        box.add(startButton);

        panel.add(box);

        add(panel);
        setContentPane(panel);
        setVisible(true);
        pack();
    }

    private void attemptStart(){
        boolean errorsOccured = false;
        StringBuilder errors = new StringBuilder("Errors Occurred:" + System.lineSeparator());
        int width = 0;
        int height = 0;
        try{
            width = Integer.parseInt(widthField.getText());
        }
        catch(NumberFormatException e){
            errorsOccured = true;
            errors.append("Width value must be an integer" + System.lineSeparator());
        }
        try{
            height = Integer.parseInt(heightField.getText());
        }
        catch(NumberFormatException e){
            errorsOccured = true;
            errors.append("Width value must be an integer" + System.lineSeparator());
        }
        if(height < 1){
            errorsOccured = true;
            errors.append("Height must be at least 1");
        }
        if(width < 1){
            errorsOccured = true;
            errors.append("Width must be at least 1");
        }
        boolean redHuman = false;
        AIType redAI = null;
        boolean blueHuman = false;
        AIType blueAI = null;

        if(this.redHuman.isSelected()){
            redHuman = true;
        }
        else if(redMCTS.isSelected()){
            redAI = AIType.MCTS;
        }
        else if(redDjikstra.isSelected()){
            redAI = AIType.DJIKSTRA;
        }
        else if(redCombo.isSelected()){
            redAI = AIType.COMBO;
        }
        else{
            errorsOccured = true;
            errors.append("Please choose an option for the red player" + System.lineSeparator());
        }

        if(this.blueHuman.isSelected()){
            blueHuman = true;
        }
        else if(blueMCTS.isSelected()){
            blueAI = AIType.MCTS;
        }
        else if(blueDjikstra.isSelected()){
            blueAI = AIType.DJIKSTRA;
        }
        else if(blueCombo.isSelected()){
            blueAI = AIType.COMBO;
        }
        else{
            errorsOccured = true;
            errors.append("Please choose an option for the blue player" + System.lineSeparator());
        }

        if(errorsOccured){
            JOptionPane.showMessageDialog(this, errors.toString(), "Errors", JOptionPane.ERROR_MESSAGE);
        }
        else{
            GameManager gameManager = new GameManager();
            try{
                gameManager.boardSize(width, height);
            }
            catch(InvalidBoardSizeException e){
                e.printStackTrace();
            }
            catch(BoardAlreadySizedException e){
                e.printStackTrace();
            }

            PlayerInterface playerRed;
            PlayerInterface playerBlue;

            if(redHuman){
                playerRed = new HumanPlayer((int) size.getWidth(), (int) size.getHeight());
            }
            else if(redAI == AIType.COMBO){
                playerRed = new ComputerPlayer_xvhn44(AIType.COMBO);
            }
            else if(redAI == AIType.MCTS){
                playerRed = new ComputerPlayer_xvhn44(AIType.MCTS);
            }
            else if(redAI == AIType.DJIKSTRA){
                playerRed = new ComputerPlayer_xvhn44(AIType.DJIKSTRA);
            }
            else{
                playerRed = new ComputerPlayer_xvhn44();
            }

            if(blueHuman){
                playerBlue = new HumanPlayer((int) size.getWidth(), (int) size.getHeight());
            }
            else if(blueAI == AIType.COMBO){
                playerBlue = new ComputerPlayer_xvhn44(AIType.COMBO);
            }
            else if(blueAI == AIType.MCTS){
                playerBlue = new ComputerPlayer_xvhn44(AIType.MCTS);
            }
            else if(blueAI == AIType.DJIKSTRA){
                playerBlue = new ComputerPlayer_xvhn44(AIType.DJIKSTRA);
            }
            else{
                playerBlue = new ComputerPlayer_xvhn44();
            }

            try{
                gameManager.specifyPlayer(playerRed, Piece.RED);
                gameManager.specifyPlayer(playerBlue, Piece.BLUE);
            }
            catch(ColourAlreadySetException e){
                e.printStackTrace();
            }
            catch(InvalidColourException e){
                e.printStackTrace();
            }
            Thread thread = new Thread(new GameThread(gameManager));
            thread.start();
            setVisible(false);
        }
    }

    private class MyMouseListener implements MouseListener{

        @Override public void mouseClicked(MouseEvent e){

        }

        @Override public void mousePressed(MouseEvent e){
            if(e.getSource() == startButton){
                attemptStart();
            }
        }

        @Override public void mouseReleased(MouseEvent e){

        }

        @Override public void mouseEntered(MouseEvent e){

        }

        @Override public void mouseExited(MouseEvent e){

        }
    }

    class GameThread implements Runnable {
        private GameManager gameManager;

        public GameThread(GameManager gameManager) {
            this.gameManager = gameManager;
        }

        public void run() {
            this.gameManager.playGame();
        }
    }
}
