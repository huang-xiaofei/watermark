package com.learn.watermark.util;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.math3.complex.Complex;

import javax.imageio.ImageIO;
import javax.sql.DataSource;
import java.awt.image.BufferedImage;
import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WatermarkEmbedder {

    private static final Logger LOGGER = Logger.getLogger(WatermarkEmbedder.class.getName());
    private static final String DB_URL = System.getenv("DB_URL");
    private static final String DB_USER = System.getenv("DB_USER");
    private static final String DB_PASSWORD = System.getenv("DB_PASSWORD");
    private static final Complex COMPLEX_0_1 = new Complex(0, 1);
    private static final String IMAGE_PATH = "path/to/image.png"; // 修改为静态
    private static final String WATERMARK_TEXT = "Watermark";
    private static final String DATABASE_IMAGE_NAME = "watermarked_image";
    private static final String OUTPUT_IMAGE_PATH = "path/to/output.png";


    // 定义 getDataSource() 方法
    private static DataSource getDataSource() {
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setUrl(DB_URL);
        dataSource.setUsername(DB_USER);
        dataSource.setPassword(DB_PASSWORD);
        return dataSource;
    }

    public static byte[] embedWatermark(BufferedImage image, String watermarkText) {
        // 将图像转换为灰度图像
        BufferedImage grayImage = toGrayScale(image);

        // 将灰度图像转换为二维数组
        double[][] imageData = convertToImageData(grayImage);

        // 对图像数据进行傅立叶变换
        Complex[][] transformedData = fourierTransform(imageData);

        // 嵌入水印
        Complex[][] watermarkedData = embedWatermark(transformedData, textToBinary(watermarkText));

        // 对嵌入水印后的数据进行逆傅立叶变换
        double[][] watermarkedImageData = inverseFourierTransform(watermarkedData);

        // 将嵌入水印后的图像数据转换回BufferedImage
        BufferedImage watermarkedImage = convertToImage(watermarkedImageData);

        // 将嵌入水印后的图像保存为字节数组
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(watermarkedImage, "png", baos);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "无法将带水印的图像写入字节数组", e);
            throw new RuntimeException(e);
        }
        return baos.toByteArray();
    }

    // 新增的重载方法
    public static byte[] embedWatermark(BufferedImage image, String watermarkText, int seed) {
        // 使用 seed 进行水印嵌入
        // 示例逻辑：
        // byte[] watermarkedData = ...;
        // return watermarkedData;

        // 生成随机种子的哈希值
        String seedHash = DigestUtils.sha256Hex(Integer.toString(seed));

        // 将图像转换为灰度图像
        BufferedImage grayImage = toGrayScale(image);

        // 将灰度图像转换为二维数组
        double[][] imageData = convertToImageData(grayImage);

        // 对图像数据进行傅立叶变换
        Complex[][] transformedData = fourierTransform(imageData);

        // 嵌入水印
        Complex[][] watermarkedData = embedWatermark(transformedData, textToBinary(watermarkText + seedHash));

        // 对嵌入水印后的数据进行逆傅立叶变换
        double[][] watermarkedImageData = inverseFourierTransform(watermarkedData);

        // 将嵌入水印后的图像数据转换回BufferedImage
        BufferedImage watermarkedImage = convertToImage(watermarkedImageData);

        // 将嵌入水印后的图像保存为字节数组
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(watermarkedImage, "png", baos);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "无法将带水印的图像写入字节数组", e);
            throw new RuntimeException(e);
        }
        return baos.toByteArray();
    }

    private static String textToBinary(String text) {
        StringBuilder binary = new StringBuilder();
        for (char c : text.toCharArray()) {
            binary.append(String.format("%8s", Integer.toBinaryString(c)).replace(' ', '0'));
        }
        return binary.toString();
    }

    private static BufferedImage toGrayScale(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage grayImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                int gray = (int) (0.299 * r + 0.587 * g + 0.114 * b);
                grayImage.setRGB(x, y, gray << 16 | gray << 8 | gray);
            }
        }
        return grayImage;
    }

    private static double[][] convertToImageData(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        double[][] imageData = new double[height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                imageData[y][x] = (image.getRGB(x, y) & 0xFF);
            }
        }
        return imageData;
    }

    private static Complex[][] embedWatermark(Complex[][] transformedData, String binaryWatermark) {
        int height = transformedData.length;
        int width = transformedData[0].length;
        int index = 0;
        for (int y = 0; y < height && index < binaryWatermark.length(); y++) {
            for (int x = 0; x < width && index < binaryWatermark.length(); x++) {
                if (binaryWatermark.charAt(index) == '1') {
                    transformedData[y][x] = transformedData[y][x].add(COMPLEX_0_1);
                } else {
                    transformedData[y][x] = transformedData[y][x].subtract(COMPLEX_0_1);
                }
                index++;
            }
        }
        return transformedData;
    }

//    private static void saveWatermarkedImageToDatabase(byte[] imageData, String watermarkHash) throws SQLException {
//        DataSource dataSource = getDataSource(); // 获取数据源
//        try (Connection conn = dataSource.getConnection();
//             PreparedStatement pstmt = conn.prepareStatement("INSERT INTO images (name, data, watermark_hash) VALUES (?, ?, ?)")) {
//            pstmt.setString(1, WatermarkEmbedder.DATABASE_IMAGE_NAME);
//            pstmt.setBytes(2, imageData);
//            pstmt.setString(3, watermarkHash);
//            pstmt.executeUpdate();
//        }
//    }

    private static byte[] getWatermarkedImageFromDatabase() throws SQLException {
        DataSource dataSource = getDataSource(); // 获取数据源
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = dataSource.getConnection();
            pstmt = conn.prepareStatement("SELECT data, watermark_hash FROM images WHERE name = ?");
            pstmt.setString(1, WatermarkEmbedder.DATABASE_IMAGE_NAME);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getBytes("data");
            }
        } finally {
            // 确保资源被关闭
            if (rs != null) {
                rs.close();
            }
            if (pstmt != null) {
                pstmt.close();
            }
            if (conn != null) {
                conn.close();
            }
        }
        return null;
    }

    public static void main(String[] args) throws IOException {
        processImage(IMAGE_PATH, OUTPUT_IMAGE_PATH);
    }

    public static void processImage(String imagePath, String outputPath) throws IOException {
        try {
            // 读取图像
            File imageFile = new File(imagePath);
            if (!imageFile.exists()) {
                throw new FileNotFoundException("找不到图像文件: " + imageFile.getAbsolutePath());
            }
            BufferedImage image = ImageIO.read(imageFile);

            // 嵌入水印
            byte[] watermarkedImageData = embedWatermark(image, WATERMARK_TEXT);

            // 计算水印的哈希值
            String watermarkHash = DigestUtils.sha256Hex(watermarkedImageData);

            // 保存嵌入水印后的图像到数据库
//            saveWatermarkedImageToDatabase(watermarkedImageData, watermarkHash);

            // 从数据库中读取嵌入水印后的图像
            byte[] watermarkedImageDataFromDB = getWatermarkedImageFromDatabase();
            if (watermarkedImageDataFromDB != null) {
                try (ByteArrayInputStream bais = new ByteArrayInputStream(watermarkedImageDataFromDB)) {
                    BufferedImage watermarkedImage = ImageIO.read(bais);
                    if (watermarkedImage == null) {
                        throw new IOException("无法从ByteArrayInputStream读取带水印的图像");
                    }
                    saveImageToFile(watermarkedImage, outputPath); // 可能抛出 IOException
                }
            } else {
                System.out.println("在数据库中找不到带水印的图像");
            }
        } catch (FileNotFoundException e) {
            LOGGER.log(Level.SEVERE, "找不到文件： " + e.getMessage() + " 方法进程中带参数的图像：imagePath=" + imagePath + ", outputPath=" + outputPath, e);
            throw e;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "发生SQL异常: " + e.getMessage() + "方法中处理带参数的图像: imagePath=" + imagePath + ", outputPath=" + outputPath, e);
            throw new IOException(e);
        }
    }

    private static void saveImageToFile(BufferedImage image, String filePath) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            ImageIO.write(image, "png", fos);
        }
    }

    private static Complex[][] fourierTransform(double[][] imageData) {
        int height = imageData.length;
        int width = imageData[0].length;

        // 将二维数组转换为一维数组
        Complex[] data = new Complex[height * width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                data[y * width + x] = new Complex(imageData[y][x], 0);
            }
        }

        // 执行傅立叶变换
        fft(data);

        // 将一维数组转换回二维数组
        Complex[][] result = new Complex[height][width];
        for (int y = 0; y < height; y++) {
            System.arraycopy(data, y * width, result[y], 0, width);
        }

        return result;
    }

    private static double[][] inverseFourierTransform(Complex[][] transformedData) {
        int height = transformedData.length;
        int width = transformedData[0].length;

        // 将二维数组转换为一维数组
        Complex[] data = new Complex[height * width];
        for (int y = 0; y < height; y++) {
            System.arraycopy(transformedData[y], 0, data, y * width, width);
        }

        // 执行逆傅立叶变换
        ifft(data);

        // 将一维数组转换回二维数组
        double[][] result = new double[height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                result[y][x] = data[y * width + x].getReal();
            }
        }

        // 验证水印完整性
//        verifyWatermarkIntegrity(transformedData, 1);
        return result;
    }

    public static boolean verifyWatermarkIntegrity(Complex[][] transformedData, int imageId) {
        try {
            DataSource dataSource = getDataSource();
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement("SELECT watermark_hash FROM images WHERE name = ?")) {
                pstmt.setString(1, WatermarkEmbedder.DATABASE_IMAGE_NAME);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    String storedWatermarkHash = rs.getString("watermark_hash");

                    // 计算当前图像的水印哈希值
                    byte[] currentImageData = embedWatermark(transformToBufferedImage(transformedData), WATERMARK_TEXT);
                    String currentWatermarkHash = DigestUtils.sha256Hex(currentImageData);

                    // 比较哈希值
                    if (storedWatermarkHash.equals(currentWatermarkHash)) {
                        System.out.println("水印完整");
                        return true;
                    } else {
                        System.out.println("水印已更改");
                        return false;
                    }
                } else {
                    System.out.println("未找到水印哈希值");
                    return false;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static BufferedImage transformToBufferedImage(Complex[][] transformedData) {
        int height = transformedData.length;
        int width = transformedData[0].length;
        double[][] imageData = new double[height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                imageData[y][x] = transformedData[y][x].getReal();
            }
        }
        return convertToImage(imageData);
    }

    private static void bitReversalPermutation(Complex[] data) {
        int n = data.length;
        for (int i = 0; i < n; i++) {
            int j = Integer.reverse(i) >>> (32 - log2(n));
            if (i < j) {
                Complex temp = data[i];
                data[i] = data[j];
                data[j] = temp;
            }
        }
    }

    private static void fft(Complex[] data) {
        int n = data.length;
        if (n == 1) {
            return;
        }

        bitReversalPermutation(data);

        // 蝶形运算
        for (int m = 1; m < n; m <<= 1) {
            double theta = -2 * Math.PI / (2 * m);
            Complex w = new Complex(Math.cos(theta), Math.sin(theta));
            for (int k = 0; k < m; k++) {
                Complex wm = w.pow(k);
                for (int r = k; r < n; r += 2 * m) {
                    if (r + m < n) { // 确保 r + m 不会越界
                        Complex u = data[r];
                        Complex t = data[r + m].multiply(wm);
                        data[r] = u.add(t);
                        data[r + m] = u.subtract(t);
                    } else {
                        // 处理越界情况，可以根据需要进行适当的处理
                        System.out.println("Index " + (r + m) + " 长度超出限制 " + n);
                    }
                }
            }
        }
    }

    private static void ifft(Complex[] data) {
        int n = data.length;
        if (n == 1) {
            return;
        }

        bitReversalPermutation(data);

        // 蝶形运算
        for (int m = 1; m < n; m <<= 1) {
            double theta = 2 * Math.PI / (2 * m);
            Complex w = new Complex(Math.cos(theta), Math.sin(theta));
            for (int k = 0; k < m; k++) {
                Complex wm = w.pow(k);
                for (int r = k; r + m < n; r += 2 * m) {
                    Complex u = data[r];
                    Complex t = data[r + m].multiply(wm);
                    data[r] = u.add(t);
                    data[r + m] = u.subtract(t);
                }
            }
        }

        // 归一化
        for (int i = 0; i < n; i++) {
            data[i] = data[i].divide(n);
        }
    }

    private static int log2(int n) {
        return (int) (Math.log(n) / Math.log(2));
    }

    private static BufferedImage convertToImage(double[][] imageData) {
        int width = imageData[0].length;
        int height = imageData.length;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int gray = (int) Math.round(imageData[y][x]);
                gray = Math.max(0, Math.min(255, gray)); // 确保值在0-255范围内
                image.setRGB(x, y, gray << 16 | gray << 8 | gray);
            }
        }
        return image;
    }
}