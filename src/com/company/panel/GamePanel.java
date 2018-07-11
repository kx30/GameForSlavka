package com.company.panel;

import com.company.bullet.Bullet;
import com.company.enemy.Enemy;
import com.company.explosion.Explosion;
import com.company.player.Player;
import com.company.powerUp.PowerUp;
import com.company.text.Text;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class GamePanel extends JPanel implements Runnable, KeyListener {

    // Fields
    public static int WIDTH = 400;
    public static int HEIGHT = 400;

    private Thread thread;
    private boolean running;

    private BufferedImage image;
    private Graphics2D g;

    private int FPS = 90;
    private double averageFPS;

    public static Player player;
    public static ArrayList<Bullet> bullets;
    public static ArrayList<Enemy> enemies;
    public static ArrayList<PowerUp> powerups;
    public static ArrayList<Explosion> explosions;
    public static ArrayList<Text> texts;

    private long waveStartTimer;
    private long waveStartTimerDiff;
    private int waveNumber;
    private int waveDelay = 2000;
    private boolean waveStart;

    private long slowDownTimer;
    private long slowDownTimerDiff;
    private int slowDownLength = 6000;

    // Constructor
    public GamePanel() {
        super();
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setFocusable(true);
        requestFocus();
    }

    // Functions

    public void addNotify() {
        super.addNotify();
        if (thread == null) {
            thread = new Thread(this);
            thread.start();
        }
        addKeyListener(this);
    }

    @Override
    public void run() {

        running = true;

        image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        g = (Graphics2D) image.getGraphics();
        g.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );
        g.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON
        );

        player = new Player();
        bullets = new ArrayList<Bullet>();
        enemies = new ArrayList<Enemy>();
        powerups = new ArrayList<PowerUp>();
        explosions = new ArrayList<Explosion>();
        texts = new ArrayList<Text>();

        waveStartTimer = 0;
        waveStartTimerDiff = 0;
        waveStart = true;
        waveNumber = 0;

        long startTime;
        long URDTimeMillis;
        long waitTime;
        long totalTime = 0;

        int frameCount = 0;
        int maxFrameCount = 30;

        long targetTime = 1000 / FPS;

        // GAME LOOP
        while (running) {

            startTime = System.nanoTime();

            gameUpdate();
            gameRender();
            gameDraw();

            URDTimeMillis = (System.nanoTime() - startTime) / 1000000;
            waitTime = targetTime - URDTimeMillis;

            try {
                Thread.sleep(waitTime);
            } catch (Exception e) {

            }

            totalTime += System.nanoTime() - startTime;
            frameCount++;
            if (frameCount == maxFrameCount) {
                averageFPS = ((int) 1000.0 / ((totalTime / frameCount) / 1000000));
                frameCount = 0;
                totalTime = 0;
            }
        }

        g.setColor(new Color(0, 100, 255));
        g.fillRect(0, 0, WIDTH, HEIGHT);
        g.setColor(Color.WHITE);

        g.setFont(new Font("Century Ghotic", Font.PLAIN, 16));
        String s = "G A M E   O V E R";
        int length = (int) g.getFontMetrics().getStringBounds(s, g).getWidth();
        g.drawString(s, (WIDTH - length) / 2, HEIGHT / 2);
        s = "Final score: " + player.getScore();
        length = (int) g.getFontMetrics().getStringBounds(s, g).getWidth();
        g.drawString(s, (WIDTH - length) / 2, HEIGHT / 2 + 30 );
        gameDraw();

    }

    private void gameUpdate() {

        // New wave
        if (waveStartTimer == 0 && enemies.size() == 0) {
            waveNumber++;
            waveStart = false;
            waveStartTimer = System.nanoTime();
        } else {
            waveStartTimerDiff = (System.nanoTime() - waveStartTimer) / 1000000;
            if (waveStartTimerDiff > waveDelay) {
                waveStart = true;
                waveStartTimer = 0;
                waveStartTimerDiff = 0;
            }
        }

        // Create enemies
        if (waveStart && enemies.size() == 0) {
            createNewEnemies();
        }

        // Player update
        player.update();

        // Bullet update
        for (int i = 0; i < bullets.size(); i++) {
            boolean remove = bullets.get(i).update();
            if (remove) {
                bullets.remove(i);
                i--;
            }
        }

        // Enemy update
        for (int i = 0; i < enemies.size(); i++) {
            enemies.get(i).update();
        }

        // Power up update
        for (int i = 0; i < powerups.size(); i++) {
            boolean remove = powerups.get(i).update();
            if (remove) {
                powerups.remove(i);
                i--;
            }
        }

        // Explosion update
        for (int i = 0; i < explosions.size(); i++) {
            boolean remove = explosions.get(i).update();
            if (remove) {
                explosions.remove(i);
                i--;
            }
        }

        // Text update
        for (int i = 0; i < texts.size(); i++) {
            boolean remove = texts.get(i).update();
            if (remove) {
                texts.remove(i);
                i--;
            }
        }


        // Bullets-enemies collision
        for (int i = 0; i < bullets.size(); i++) {

            Bullet b = bullets.get(i);
            double bx = b.getx();
            double by = b.gety();
            double br = b.getr();

            for (int j = 0; j < enemies.size(); j++) {

                Enemy e = enemies.get(j);
                double ex = e.getx();
                double ey = e.gety();
                double er = e.getr();

                double dx = bx - ex;
                double dy = by - ey;
                double dist = Math.sqrt(dx * dx + dy * dy);

                if (dist < br + er) {
                    e.hit();
                    bullets.remove(i);
                    i--;
                    break;
                }
            }
        }

        // Check dead enemies
        for (int i = 0; i < enemies.size(); i++) {

            if (enemies.get(i).isDead()) {

                Enemy e = enemies.get(i);

                // chance for power up
                double rand = Math.random();
                if (rand < 0.001) powerups.add(new PowerUp(1, e.getx(), e.gety()));
                else if (rand < 0.020) powerups.add(new PowerUp(3, e.getx(), e.gety()));
                else if (rand < 0.120) powerups.add(new PowerUp(2, e.getx(), e.gety()));
                else if (rand < 0.130) powerups.add(new PowerUp(4, e.getx(), e.gety()));

                player.addScore(e.getType() + e.getRank());
                enemies.remove(i);
                i--;

                e.explode();
                explosions.add(new Explosion(e.getx(), e.gety(), e.getr(), e.getr() + 30));
            }
        }

        if (player.isDead()) {
            running = false;
        }

        // Player-enemy collision
        if (!player.isRecovering()) {
            int px = player.getx();
            int py = player.gety();
            int pr = player.getr();
            for (int i = 0; i < enemies.size(); i++){

                Enemy e = enemies.get(i);
                double ex = e.getx();
                double ey = e.gety();
                double er = e.getr();

                double dx = px - ex;
                double dy = py - ey;
                double dist = Math.sqrt(dx * dx + dy * dy);

                if (dist < pr + er){
                    player.loseLife();
                }
            }
        }

        // player-powerup collision
        int px = player.getx();
        int py = player.gety();
        int pr = player.getr();
        for (int i = 0; i < powerups.size(); i++) {
            PowerUp p = powerups.get(i);
            double x = p.getx();
            double y = p.gety();
            double r = p.getr();
            double dx = px - x;
            double dy = py - y;
            double dist = Math.sqrt(dx * dx + dy * dy);

            // collected powerup
            if (dist < pr + r) {

                int type = p.getType();

                if (type == 1) {
                    player.gainLife();
                    texts.add(new Text(player.getx(), player.gety(), 2000, "Extra life"));
                }
                if (type == 2) {
                    player.increasePower(1);
                    texts.add(new Text(player.getx(), player.gety(), 2000, "Power"));
                }
                if (type == 3) {
                    player.increasePower(2);
                    texts.add(new Text(player.getx(), player.gety(), 2000, "Double power"));
                }
                if (type == 4) {
                    slowDownTimer = System.nanoTime();
                    for (int j = 0; j < enemies.size(); j++){
                        enemies.get(j).setSlow(true);
                    }
                    texts.add(new Text(player.getx(), player.gety(), 2000, "Slowdown"));
                }

                powerups.remove(i);
                i--;

            }
        }

        // slowdown update
        if (slowDownTimer != 0) {
            slowDownTimerDiff = (System.nanoTime() - slowDownTimer) / 1000000;
            if (slowDownTimerDiff > slowDownLength) {
                slowDownTimer = 0;
                for (int j = 0; j < enemies.size(); j++){
                    enemies.get(j).setSlow(false);
                }
            }
        }

    }

    private void gameRender() {

        // Draw background
        g.setColor(new Color(0, 100, 255));
        g.fillRect(0, 0, WIDTH, HEIGHT);

        // Draw slowdown screen
        if(slowDownTimer != 0) {
            g.setColor(new Color(0, 50, 128, 64));
            g.fillRect(0, 0, WIDTH, HEIGHT);
        }

        // Draw player
        player.draw(g);

        // Draw bullets
        for (int i = 0; i < bullets.size(); i++) {
            bullets.get(i).draw(g);
        }

        // Draw enemies
        for (int i = 0; i < enemies.size(); i++) {
            enemies.get(i).draw(g);
        }

        // Draw powerups
        for (int i = 0; i < powerups.size(); i++) {
            powerups.get(i).draw(g);
        }

        // Draw explosions
        for (int i = 0; i < explosions.size(); i++) {
            explosions.get(i).draw(g);
        }

        // Draw text
        for (int i = 0; i < texts.size(); i++) {
            texts.get(i).draw(g);
        }

        // Draw wave number
        if (waveStartTimer != 0) {
            g.setFont(new Font("Century Gothic", Font.PLAIN, 18 ));
            String s = "-:- W A V E   " + waveNumber + "   -:-";
            int length = (int) g.getFontMetrics().getStringBounds(s, g).getWidth();
            int alpha = (int)(255 * Math.sin(3.14 * waveStartTimerDiff / waveDelay));
            if (alpha > 255) alpha = 255;
            g.setColor(new Color(255, 255, 255, alpha));
            g.drawString(s, WIDTH / 2 - length / 2, HEIGHT / 2);
        }

        // Draw player lives
        for (int i = 0; i < player.getLives(); i++) {
            g.setColor(Color.WHITE);
            g.fillOval(20 + (20 * i), 20, player.getr() * 2, player.getr() * 2);
            g.setStroke(new BasicStroke(3));
            g.setColor(Color.WHITE.darker());
            g.drawOval(20 + (20 * i), 20, player.getr() * 2, player.getr() * 2);
            g.setStroke(new BasicStroke(1));
        }

        // Draw player power
        g.setColor(Color.YELLOW);
        g.fillRect(20, 40, player.getPower() * 8, 8);
        g.setColor(Color.YELLOW.darker());
        g.setStroke(new BasicStroke(2));
        for(int i = 0; i < player.getRequiredPower(); i++) {
            g.drawRect(20 + 8 * i, 40, 8, 8);
        }
        g.setStroke(new BasicStroke(1));

        // Draw player score
        g.setColor(Color.WHITE);
        g.setFont(new Font("Century Gothic", Font.PLAIN, 14));
        g.drawString("Score: " + player.getScore(), WIDTH - 80, 30);

        // draw slowdown meter
        if (slowDownTimer != 0) {
            g.setColor(Color.WHITE);
            g.drawRect(20, 60, 100, 8);
            g.fillRect(20, 60, (int)(100 - 100.0 * slowDownTimerDiff / slowDownLength), 8);
        }

    }

    private void createNewEnemies() {

        enemies.clear();
        Enemy e;

        if(waveNumber == 1) {
            for (int i = 0; i < 4; i++) {
                enemies.add(new Enemy(1, 1));
            }
        }
        if(waveNumber == 2) {
            for (int i = 0; i < 8; i++) {
                enemies.add(new Enemy(1, 1));
            }
        }
        if(waveNumber == 3) {
            for (int i = 0; i < 8; i++) {
                enemies.add(new Enemy(1, 1));
            }
            enemies.add(new Enemy(1, 2));
            enemies.add(new Enemy(1, 2));
            enemies.add(new Enemy(1, 3));
        }
        if(waveNumber == 4) {
            for (int i = 0; i < 4; i++) {
                enemies.add(new Enemy(1, 2));
            }
            enemies.add(new Enemy(1, 2));
            enemies.add(new Enemy(1, 2));
            enemies.add(new Enemy(1, 3));
            enemies.add(new Enemy(1, 4));
        }
        if(waveNumber == 5) {
            for (int i = 0; i < 3; i++) {
                enemies.add(new Enemy(1, 2));
            }
            enemies.add(new Enemy(1, 3));
            enemies.add(new Enemy(1, 4));
            enemies.add(new Enemy(1, 4));
            enemies.add(new Enemy(2, 2));
            enemies.add(new Enemy(2, 3));
        }


        if(waveNumber == 6) {
            for (int i = 0; i < 10; i++) {
                enemies.add(new Enemy(2, 1));
            }
            for (int i = 0; i < 6; i++){
                enemies.add(new Enemy(3, 1));
            }
            enemies.add(new Enemy(2, 4));
            enemies.add(new Enemy(3, 2));
        }
        if(waveNumber == 7) {
            for (int i = 0; i < 4; i++) {
                enemies.add(new Enemy(2, 2));
                enemies.add(new Enemy(3, 2));
            }
            enemies.add(new Enemy(2, 4));
            enemies.add(new Enemy(2, 4));
            enemies.add(new Enemy(3, 4));
            enemies.add(new Enemy(3, 4));
        }
        if(waveNumber == 8) {
            for (int i = 0; i < 3; i++) {
                enemies.add(new Enemy(2, 3));
                enemies.add(new Enemy(3, 3));
            }
            enemies.add(new Enemy(2, 4));
            enemies.add(new Enemy(2, 4));
            enemies.add(new Enemy(3, 4));
        }
        if(waveNumber == 9) {
            for (int i = 0; i < 5; i++) {
                enemies.add(new Enemy(3, 4));
                enemies.add(new Enemy(2, 4));
            }
        }
        if(waveNumber == 10) {
            enemies.add(new Enemy(4, 4));
        }
        if(waveNumber == 11) {
            running = false;
        }

    }

    private void gameDraw() {
        Graphics graphics = this.getGraphics();
        graphics.drawImage(image, 0, 0, null);
        graphics.dispose();
    }

    public void keyTyped(KeyEvent key) {

    }

    public void keyPressed(KeyEvent key) {

        int keyCode = key.getKeyCode();
        if (keyCode == KeyEvent.VK_A) {
            player.setLeft(true);
        }
        if (keyCode == KeyEvent.VK_D) {
            player.setRight(true);
        }
        if (keyCode == KeyEvent.VK_W) {
            player.setUp(true);
        }
        if (keyCode == KeyEvent.VK_S) {
            player.setDown(true);
        }
        if (keyCode == KeyEvent.VK_F) {
            player.setFiring(true);
        }
    }

    public void keyReleased(KeyEvent key) {
        int keyCode = key.getKeyCode();
        if (keyCode == KeyEvent.VK_A) {
            player.setLeft(false);
        }
        if (keyCode == KeyEvent.VK_D) {
            player.setRight(false);
        }
        if (keyCode == KeyEvent.VK_W) {
            player.setUp(false);
        }
        if (keyCode == KeyEvent.VK_S) {
            player.setDown(false);
        }
        if (keyCode == KeyEvent.VK_F) {
            player.setFiring(false);
        }
    }
}
