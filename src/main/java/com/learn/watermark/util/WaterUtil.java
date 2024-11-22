package com.learn.watermark.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * @program: watermark
 * @description:
 * @author: Martin Fowler
 * @create: 2024-11-19 16:43
 */
public class WaterUtil {
    public static void addWatermark(String watermarkText, File sourceImageFile, File destImageFile) {
        try {
            // 读取原始图片
            BufferedImage sourceImage = ImageIO.read(sourceImageFile);

            // 创建图形上下文
            Graphics2D g2d = (Graphics2D) sourceImage.createGraphics();

            // 设置水印文字颜色和透明度
            g2d.setColor(new Color(0, 0, 0, 100)); // 前三个数字是RGB颜色，最后一个是透明度
            g2d.setFont(new Font("Arial", Font.BOLD, 30)); // 设置字体大小和样式

            // 使用高斯模糊填充文字
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_VRGB);

            // 计算水印位置
            FontMetrics fontMetrics = g2d.getFontMetrics();
            Rectangle2D rect = fontMetrics.getStringBounds(watermarkText, g2d);
            double x = (sourceImage.getWidth() - rect.getWidth()) / 2;
            double y = (sourceImage.getHeight() - rect.getHeight()) / 2;


            // 添加水印
            g2d.drawString(watermarkText, (int) x, (int) y);

            // 释放图形上下文资源
            g2d.dispose();

            // 写入新的图片文件
            ImageIO.write(sourceImage, "jpg", destImageFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        File sourceImage = new File("E:\\1.jpg"); // 原始图片路径
        File destImage = new File("E:\\watermarked.jpg"); // 加水印后的图片路径
        String watermarkText = "Confidential"; // 水印文字

        addWatermark(watermarkText, sourceImage, destImage);
    }
}