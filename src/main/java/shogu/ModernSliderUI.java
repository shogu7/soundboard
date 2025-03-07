package shogu;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JSlider;
import javax.swing.plaf.basic.BasicSliderUI;

public class ModernSliderUI extends BasicSliderUI {
    private static final int TRACK_HEIGHT = 8;
    private static final int THUMB_WIDTH = 20;
    private static final int THUMB_HEIGHT = 20;
    
    public ModernSliderUI(JSlider slider) {
        super(slider);
    }
    
    @Override
    public void paintTrack(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int width = trackRect.width;
        int height = TRACK_HEIGHT;
        int x = trackRect.x;
        int y = trackRect.y + (trackRect.height - height) / 2;
    
        g2d.setColor(new Color(100, 100, 100, 150));
        g2d.fillRoundRect(x, y, width, height, height, height);
        
        int thumbPos = thumbRect.x + thumbRect.width / 2 - x;
        g2d.setColor(new Color(65, 105, 225));
        g2d.fillRoundRect(x, y, thumbPos, height, height, height);
    }
    
    @Override
    public void paintThumb(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int x = thumbRect.x;
        int y = thumbRect.y;
        
        g2d.setColor(new Color(220, 220, 220));
        g2d.fillOval(x, y, THUMB_WIDTH, THUMB_HEIGHT);
        
        g2d.setColor(new Color(180, 180, 180));
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawOval(x, y, THUMB_WIDTH, THUMB_HEIGHT);
    }
    
    @Override
    protected Dimension getThumbSize() {
        return new Dimension(THUMB_WIDTH, THUMB_HEIGHT);
    }
}