package com.learn.watermark.util;

import org.apache.commons.math3.complex.Complex;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * @program: watermark
 * @description:
 * @author: Martin Fowler
 * @create: 2024-11-17 14:10
 */
public class MyUtil {


    /**
     * 生成随机种子
     *
     * @return 随机种子的十六进制表示
     */
    public static String generateSeed() {
        SecureRandom random = new SecureRandom();
        return Integer.toHexString(random.nextInt());
    }

    public static void savePublicKey(String publicKey, String filePath) throws IOException {
        Files.write(Paths.get(filePath), publicKey.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 读取私钥文件
     *
     * @param privateKeyPath 私钥文件路径
     * @return 私钥字符串
     * @throws IOException 如果路径不是文件或读取失败
     */
    public static String readPrivateKey(String privateKeyPath) throws IOException {
        Path path = Paths.get(privateKeyPath);
        if (!Files.isRegularFile(path)) {
            throw new IOException("指定的路径不是一个文件: " + privateKeyPath);
        }
        //return Files.readString(path, StandardCharsets.UTF_8);
        return "";
    }

    public static byte[] readEncryptedWatermark(File watermarkedImageFile) throws IOException {
        BufferedImage image = ImageIO.read(watermarkedImageFile);
        int width = image.getWidth();
        int height = image.getHeight();
        int maxPixels = Math.min(128, width * height); // 确保不超过图像的总像素数

        byte[] encryptedWatermark = new byte[maxPixels];
        for (int i = 0; i < maxPixels; i++) {
            encryptedWatermark[i] = (byte) image.getRGB(i % width, i / width);
        }

        return encryptedWatermark;
    }

    public static String decryptWatermark(byte[] encryptedWatermark, String privateKey, String algorithm) throws Exception {
        Cipher cipher = Cipher.getInstance(algorithm);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKey));
        PrivateKey privateK = keyFactory.generatePrivate(keySpec);
        cipher.init(Cipher.DECRYPT_MODE, privateK);
        byte[] decryptedWatermark = cipher.doFinal(encryptedWatermark);
        return new String(decryptedWatermark, StandardCharsets.UTF_8);
    }

    /**
     * 生成 RSA 密钥对
     *
     * @param keySize 密钥长度（例如 2048 位）
     * @return 包含公钥和私钥的 Map
     */
    public static Map<String, String> generateKeyPair(int keySize) {
        if (keySize < 512 || keySize > 4096) {
            throw new IllegalArgumentException("Invalid key size: " + keySize + ". Must be between 512 and 4096 bits.");
        }
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(keySize);
            KeyPair keyPair = keyGen.generateKeyPair();
            PublicKey publicKey = keyPair.getPublic();
            PrivateKey privateKey = keyPair.getPrivate();

            String publicKeyBase64 = Base64.getEncoder().encodeToString(publicKey.getEncoded());
            String privateKeyBase64 = Base64.getEncoder().encodeToString(privateKey.getEncoded());

            Map<String, String> keyPairMap = new HashMap<>();
            keyPairMap.put("publicKey", publicKeyBase64);
            keyPairMap.put("privateKey", privateKeyBase64);

            return keyPairMap;
        } catch (Exception e) {
            throw new RuntimeException("生成密钥对时发生错误", e);
        }
    }

    /**
     * 从种子生成密钥
     *
     * @param seed    种子字符串
     * @param keySize 密钥长度（128, 192, 或 256 位）
     * @return Base64 编码的密钥字符串
     */
    public static String generateKeyFromSeed(String seed, int keySize) {
        if (keySize != 128 && keySize != 192 && keySize != 256) {
            throw new IllegalArgumentException("Invalid key size: " + keySize + ". Must be 128, 192, or 256 bits.");
        }
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            SecureRandom secureRandom = new SecureRandom(seed.getBytes());
            keyGen.init(keySize, secureRandom); // 可以是 128, 192, 或 256
            SecretKey secretKey = keyGen.generateKey();
            byte[] encodedKey = secretKey.getEncoded();
            return Base64.getEncoder().encodeToString(encodedKey);
        } catch (Exception e) {
            throw new RuntimeException("从种子生成密钥时发生错误", e);
        }
    }

    /**
     * 保存水印信息到文件
     *
     * @param imageName          图片名称
     * @param imageData          图片数据
     * @param watermarkText      水印文本
     * @param encryptedWatermark 加密后的水印数据
     * @param seed               种子
     * @param infoFilePath       保存水印信息的文件路径
     * @throws IOException 文件操作失败
     */
    public static void saveWatermarkInfo(String imageName, byte[] imageData, String watermarkText, byte[] encryptedWatermark, int seed, String infoFilePath) throws SQLException, IOException {
//        try (Connection conn = getConnection()) {
//            try (PreparedStatement pstmt = conn.prepareStatement(
//                    "INSERT INTO watermark_info (image_name, image_binary, watermark_text, encrypted_watermark, seed) VALUES (?, ?, ?, ?, ?)")) {
//                pstmt.setString(1, imageName);
//                pstmt.setBytes(2, imageData);
//                pstmt.setString(3, watermarkText);
//                pstmt.setBytes(4, encryptedWatermark);
//                pstmt.setInt(5, seed);
//                pstmt.executeUpdate();
//            }
//        }

        // 保存水印信息到文件
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(infoFilePath))) {
            writer.write("Image Name: " + imageName + "\n");
            writer.write("Watermark Text: " + watermarkText + "\n");
            writer.write("Seed: " + seed + "\n");
            writer.write("Encrypted Watermark: " + Base64.getEncoder().encodeToString(encryptedWatermark) + "\n");
        }
    }

    /**
     * 保存私钥到文件
     *
     * @param key  私钥字符串
     * @param path 文件路径
     * @throws IOException 写入文件失败
     */
    public static void savePrivateKey(String key, String path) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path))) {
            writer.write(key);
        }
    }

    /**
     * 使用私钥解密种子
     *
     * @param encryptedSeed 加密后的种子字节数组
     * @param privateKey    私钥字符串
     * @param algorithm     解密算法
     * @return 解密后的种子字符串
     */
    public static String decryptSeed(byte[] encryptedSeed, String privateKey, String algorithm) {
        try {
            // 去除多余的标记和空格
            privateKey = privateKey.replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s+", "");

            // 解码 Base64 字符串
            byte[] decodedPrivateKey = Base64.getDecoder().decode(privateKey);

            // 生成 PKCS8EncodedKeySpec
            PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(decodedPrivateKey);

            // 使用 KeyFactory 生成私钥
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PrivateKey privKey = keyFactory.generatePrivate(pkcs8KeySpec);

            // 初始化 Cipher
            Cipher cipher = Cipher.getInstance(algorithm);
            cipher.init(Cipher.DECRYPT_MODE, privKey);

            // 解密种子
            byte[] decryptedSeed = cipher.doFinal(encryptedSeed);
            return new String(decryptedSeed, StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace(); // 打印堆栈跟踪信息，便于调试
            throw new RuntimeException("解密种子时发生错误", e);
        }
    }

    /**
     * 使用公钥加密水印
     *
     * @param watermarkText 水印文本
     * @param publicKey     公钥字符串
     * @param algorithm     加密算法
     * @return 加密后的水印字节数组
     */
    public static byte[] encryptWatermark(String watermarkText, String publicKey, String algorithm) {
        try {
            // 去除多余的标记和空格
            publicKey = publicKey.replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s+", "");

            // 解码 Base64 字符串
            byte[] decodedPublicKey = Base64.getDecoder().decode(publicKey);

            // 生成 X509EncodedKeySpec
            X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(decodedPublicKey);

            // 使用 KeyFactory 生成公钥
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey pubKey = keyFactory.generatePublic(x509KeySpec);

            // 初始化 Cipher
            Cipher cipher = Cipher.getInstance(algorithm);
            cipher.init(Cipher.ENCRYPT_MODE, pubKey);

            // 加密水印
            return cipher.doFinal(watermarkText.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            e.printStackTrace(); // 打印堆栈跟踪信息，便于调试
            throw new RuntimeException("加密水印时发生错误", e);
        }
    }

    /**
     * 保存带水印的图片到文件
     *
     * @param file                 目标文件
     * @param watermarkedImageData 带水印的图片数据
     * @throws IOException 写入文件失败
     */
    public static void saveWatermarkedImage(File file, byte[] watermarkedImageData) throws IOException {
        BufferedImage watermarkedImage = bytesToImage(watermarkedImageData);
        ImageIO.write(watermarkedImage, "png", file);
    }

    /**
     * 将 BufferedImage 转换为字节数组
     *
     * @param image 图片
     * @return 字节数组
     */
    private static byte[] imageToBytes(BufferedImage image) {
        try {
            File tempFile = File.createTempFile("temp", ".png");
            ImageIO.write(image, "png", tempFile);
            return Files.readAllBytes(tempFile.toPath());
        } catch (IOException e) {
            throw new RuntimeException("将图片转换为字节数组时发生错误", e);
        }
    }

    /**
     * 将字节数组转换为 BufferedImage
     *
     * @param imageData 图片数据
     * @return BufferedImage
     */
    private static BufferedImage bytesToImage(byte[] imageData) {
        try {
            return ImageIO.read(new ByteArrayInputStream(imageData));
        } catch (IOException e) {
            throw new RuntimeException("将字节数组转换为图片时发生错误", e);
        }
    }

    /**
     * 验证水印
     *
     * @param imagePath            原始图片路径
     * @param watermarkedImagePath 带水印的图片路径
     * @param seed                 种子
     * @return 验证结果
     */
    public static boolean verifyWatermark(String imagePath, String watermarkedImagePath, int seed) {
        try {
            BufferedImage originalImage = ImageIO.read(new File(imagePath));
            BufferedImage watermarkedImage = ImageIO.read(new File(watermarkedImagePath));

            // 使用 DCTUtil 进行离散余弦变换
            Complex[][] originalDCT = DCTUtil.forwardDCT(originalImage);
            Complex[][] watermarkedDCT = DCTUtil.forwardDCT(watermarkedImage);

            // 提取水印
            byte[] extractedWatermark = extractWatermark(watermarkedDCT, seed);

            // 生成原始水印
            String originalWatermarkText = "Sample Watermark"; // 替换为实际的水印文本
            byte[] originalWatermark = originalWatermarkText.getBytes(StandardCharsets.UTF_8);

            // 比较提取的水印和原始水印
            return Arrays.equals(extractedWatermark, originalWatermark);
        } catch (IOException e) {
            throw new RuntimeException("验证水印时发生错误", e);
        }
    }

    /**
     * 提取水印
     *
     * @param dctMatrix DCT 矩阵
     * @param seed      种子
     * @return 提取的水印字节数组
     */
    private static byte[] extractWatermark(Complex[][] dctMatrix, int seed) {
        // 使用种子生成随机数生成器
        SecureRandom random = new SecureRandom();
        random.setSeed(seed);

        int width = dctMatrix.length;
        int height = dctMatrix[0].length;
        int maxPixels = Math.min(128, width * height); // 确保不超过图像的总像素数

        byte[] extractedWatermark = new byte[maxPixels];
        for (int i = 0; i < maxPixels; i++) {
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            extractedWatermark[i] = (byte) (dctMatrix[x][y].getReal() > 0 ? 1 : 0);
        }

        return extractedWatermark;
    }
}