package com.company.explosion;

import java.awt.*;

public class Explosion {

    private double xCoordinateOfTheDeadEnemy;
    private double yCoordinateOfTheDeadEnemy;
    private int radius;
    private int maxRadius;

    public Explosion(double xCoordinateOfTheDeadEnemy, double yCoordinateOfTheDeadEnemy, int radius, int max) {
        this.xCoordinateOfTheDeadEnemy = xCoordinateOfTheDeadEnemy;
        this.yCoordinateOfTheDeadEnemy = yCoordinateOfTheDeadEnemy;
        this.radius = radius;
        maxRadius = max;
    }

    public boolean update() {
        radius++;
        if (radius >= maxRadius){
            return true;
        }
        return false;
    }

    public void draw(Graphics2D g) {
        g.setColor(new Color(255, 255, 255 ,128));
        g.setStroke(new BasicStroke(3));
        g.drawOval((int)(xCoordinateOfTheDeadEnemy - radius), (int)(yCoordinateOfTheDeadEnemy - radius), 2 * radius, 2 * radius );
        g.setStroke(new BasicStroke(1));
    }

}



