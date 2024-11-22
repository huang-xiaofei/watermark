package com.learn.watermark.util;

import org.apache.commons.math3.complex.Complex;
import java.awt.image.BufferedImage;

public class DCTUtil {

    public static Complex[][] forwardDCT(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        Complex[][] dctMatrix = new Complex[width][height];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                double sumReal = 0.0;
                double sumImag = 0.0;

                for (int u = 0; u < width; u++) {
                    for (int v = 0; v < height; v++) {
                        double alphaU = (u == 0) ? 1.0 / Math.sqrt(2.0) : 1.0;
                        double alphaV = (v == 0) ? 1.0 / Math.sqrt(2.0) : 1.0;
                        double cosU = Math.cos((2 * x + 1) * u * Math.PI / (2 * width));
                        double cosV = Math.cos((2 * y + 1) * v * Math.PI / (2 * height));
                        int pixelValue = image.getRGB(u, v);
                        sumReal += alphaU * alphaV * pixelValue * cosU * cosV;
                    }
                }

                sumReal *= 2.0 / Math.sqrt(width * height);
                sumImag = 0.0; // 假设图像数据是实数，所以虚部为0

                dctMatrix[x][y] = new Complex(sumReal, sumImag);
            }
        }

        return dctMatrix;
    }
}

