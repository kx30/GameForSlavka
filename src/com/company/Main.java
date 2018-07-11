package com.company;

import com.company.panel.GamePanel;

import javax.swing.*;

public class Main {

    public static void main(String[] args) {

        JFrame window = new JFrame("My first game");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        window.setContentPane(new GamePanel());

        window.pack();
        window.setVisible(true);

        System.out.println(Math.random());

    }
}
