package com.company.text;

import java.awt.*;

public class Text {

    // FIELDS
    private double x;
    private double y;
    private long time;
    private String s;

    private long startTime;

    // CONSTRUCTOR
    public Text(double x, double y, long time, String s){
         this.x = x;
         this.y = y;
         this.time = time;
         this.s = s;
         startTime = System.nanoTime();
    }

    public boolean update() {
        long elapsed = (System.nanoTime() - startTime) / 1000000;
        if (elapsed > time) {
            return true;
        }
        return false;
    }

    public void draw(Graphics2D g) {
        g.setFont(new Font("Century Ghotic", Font.PLAIN, 12));

        long elapsed = (System.nanoTime() - startTime) / 1000000;
        int alpha = (int)(255 * Math.sin(3.14 * elapsed / startTime));
        if (alpha > 255) alpha = 255;
        g.setColor(new Color(255, 255, 255, alpha));
        int length = (int) g.getFontMetrics().getStringBounds(s, g).getWidth();
        g.drawString(s, (int) (x - (length / 2)), (int) y);
    }

}
