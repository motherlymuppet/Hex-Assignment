import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.RenderingHints;
import javax.swing.JPanel;

/**
 * Just sets antialiasing to be used on the panel and keeps everything else to be the same - so it doesn't have to be set each and every time.
 *
 * @author Steven Lowes
 */
public class QualityPanel extends JPanel{
    public QualityPanel(){
        super();
    }

    public QualityPanel(LayoutManager layout, boolean isDoubleBuffered){
        super(layout, isDoubleBuffered);
    }

    public QualityPanel(LayoutManager layout){
        super(layout);
    }

    public QualityPanel(boolean isDoubleBuffered){
        super(isDoubleBuffered);
    }

    @Override protected void paintComponent(Graphics g){
        Graphics2D g2 = (Graphics2D) g;
        //Turn on antialiasing and improve quality
        RenderingHints rh = new RenderingHints(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        rh.add(new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY));
        rh.add(new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON));
        rh.add(new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON));
        g2.setRenderingHints(rh);
        super.paintComponent(g2);
    }
}
