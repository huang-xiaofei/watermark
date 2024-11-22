package com.learn.watermark.util;

import javax.swing.*;
import java.awt.*;

/**
 * @program: watermark
 * @description:
 * @author: Martin Fowler
 * @create: 2024-11-17 15:26
 */
public class Test {
    public static void main(String[] args) {
        JFrame frame = new JFrame("自动调整JPanel大小");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1600, 1200);

        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        c.weighty = 1.0;

        JLabel label = new JLabel("这是一个JLabel");
        panel.add(label, c);

        frame.add(panel);
        frame.setVisible(true);
    }
}