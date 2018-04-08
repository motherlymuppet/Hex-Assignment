import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by Steven on 23/04/2016.
 */
public class GUIPanel extends QualityPanel{
    /**
     * The board, storing all the hexes
     */
    private GUIBoard board;

    /**
     * The x coordinates of the text to be drawn, stored as a percentage of the whole width
     */
    private ArrayList<Double> xPoints;

    /**
     * The y coordinates of the text to be drawn, stored as a percentage of the whole height
     */
    private ArrayList<Double> yPoints;

    /**
     * The strings to draw on the panel when there is a winner
     */
    private ArrayList<String> texts;

    /**
     * The font to draw the text in
     */
    private Font font;

    /**
     * Create a new panel
     */
    public GUIPanel(){
        super();
        board = new GUIBoard();
        xPoints = new ArrayList<Double>();
        yPoints = new ArrayList<Double>();
        texts = new ArrayList<String>();
    }

    /**
     * @return The board which stores the hexes
     */
    public GUIBoard getBoard(){
        return board;
    }

    /**
     * Update the board, giving it a new board view to represent
     *
     * @param boardView The new board to represent
     */
    public void updateBoard(Piece[][] boardView){
        board.updateBoard(boardView, getSize());
    }

    /**
     * Update the drawing to have a new size, but keep the board represented the same
     */
    public void updateBoard(){
        board.updateBoard(getSize());
        repaint();
    }

    @Override public void paintComponent(Graphics g){
        Graphics2D g2 = (Graphics2D) g;
        //Turn on antialiasing and improve quality
        RenderingHints rh = new RenderingHints(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        rh.add(new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY));
        rh.add(new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON));
        rh.add(new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON));
        g2.setRenderingHints(rh);

        super.paintComponent(g2);

        //Iterate through the list of hexes
        LinkedList<Hex> hexes = new LinkedList<Hex>(board.getHexes());
        for(Hex hex : hexes){
            Color color = null;
            try{
                color = hex.getColor();
            }
            catch(InvalidColourException e){
                e.printStackTrace();
            }
            if(color != null){
                //Draw the hex
                g2.setColor(color);
                g2.fillPolygon(hex.getHexPoly());
                g2.setColor(Color.BLACK);
                g2.drawPolygon(hex.getHexPoly());
            }
        }
        //If someone has won, draw the strings stored at the location specified
        font = new Font("Impact", Font.PLAIN, getWidth() / 50);
        g2.setFont(font);
        FontMetrics fontMetrics = g2.getFontMetrics();
        for(int i = 0; i < texts.size(); i++){
            Point point = new Point((int) (xPoints.get(i) * getWidth()), (int) (yPoints.get(i) * getHeight()));
            String text = texts.get(i);
            //Draw the string, centered
            g2.drawString(text, point.x - (fontMetrics.stringWidth(text) / 2), point.y);
        }
    }

    /**
     * Remove the winner text
     */
    public void clearWinnerText(){
        texts = new ArrayList<String>();
    }

    /**
     * Create winner text at a random location, with 1/8 chance of changing the text to a doge meme
     *
     * @param text The text to use 7/8 of the time
     */
    public void informWinner(String text){
        ThreadLocalRandom rand = ThreadLocalRandom.current();
        //With 1/8 chance, change the text to one of the following:
        if(rand.nextInt(8) == 0){
            switch(rand.nextInt(6)){
                case 0:
                    text = "wow.";
                    break;
                case 1:
                    text = "such win";
                    break;
                case 2:
                    text = "many win";
                    break;
                case 3:
                    text = "very strategy";
                    break;
                case 4:
                    text = "how to lose?";
                    break;
                case 5:
                    text = "much plays";
                    break;
            }
        }
        //Save the text at a random location
        texts.add(text);
        xPoints.add(rand.nextDouble());
        yPoints.add(rand.nextDouble());
    }

    @Override public Dimension getPreferredSize(){
        if(board.getSize() == null){
            return super.getPreferredSize();
        }
        else{
            return board.getSize();
        }
    }
}
