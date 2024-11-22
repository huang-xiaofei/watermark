package com.learn.watermark.service.impl;

import com.gitee.fastmybatis.core.query.Query;
import com.learn.watermark.entity.WaterMark;
import com.learn.watermark.mapper.WaterMarkMapper;
import com.learn.watermark.util.MyUtil;
import com.learn.watermark.util.WatermarkEmbedder;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


@Component
public class WatermarkApp extends JFrame implements SmartLifecycle {
    private static final Logger LOGGER = Logger.getLogger(WatermarkApp.class.getName());

    private final JTextField watermarkTextField;
    private final JTextField seedField;
    private final JTextField publicKeyField;
    private final JTextField privateKeyPathField;
    private final JLabel resultLabel;
    private final JLabel verifyResultLabel;
    @Autowired
    private WaterMarkMapper waterMarkMapper;
    private JTextField imagePathField; // 声明为类的成员变量
    private JFileChooser fileChooser = new JFileChooser();
    private final WaterMark waterMark = new WaterMark();

    public WatermarkApp() {
        // 初始化各个文本框
        watermarkTextField = new JTextField(30);
        seedField = new JTextField(30);
        publicKeyField = new JTextField(30);
        publicKeyField.setEditable(false);
        privateKeyPathField = new JTextField(30);

        setTitle("图像水印应用");
        setSize(1600, 1200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // 文件选择部分
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(new JLabel("选择图像："), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JButton selectImageButton = new JButton("选择图像");
        panel.add(selectImageButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("水印文本："), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(watermarkTextField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("种子: (用于加密)"), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(seedField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("公钥: (用于传输)"), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(publicKeyField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("私钥文件: (用于解密)"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(privateKeyPathField, gbc);

        gbc.gridx = 3;
        gbc.gridy = 4;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        JButton selectPrivateKeyButton = new JButton("选择私钥文件");
        panel.add(selectPrivateKeyButton, gbc);

        // 操作按钮部分
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        JButton generateSeedButton = new JButton("生成种子");
        panel.add(generateSeedButton, gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        JButton savePrivateKeyButton = new JButton("保存私钥");
        panel.add(savePrivateKeyButton, gbc);

        gbc.gridx = 2;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        JButton decryptButton = new JButton("解密");
        panel.add(decryptButton, gbc);

        gbc.gridx = 3;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        JButton saveWatermarkButton = new JButton("保存水印");
        panel.add(saveWatermarkButton, gbc);

        gbc.gridx = 4;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        JButton saveWatermarkedImageButton = new JButton("保存水印图像");
        panel.add(saveWatermarkedImageButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        JButton verifyWatermarkButton = new JButton("验证水印完整性");
        panel.add(verifyWatermarkButton, gbc);

        // 新增生成 RSA 密钥对按钮
        gbc.gridx = 1;
        gbc.gridy = 6;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        JButton generateKeyPairButton = new JButton("生成 RSA 密钥对");
        panel.add(generateKeyPairButton, gbc);

        // 新增保存公钥按钮
        gbc.gridx = 2;
        gbc.gridy = 6;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        JButton savePublicKeyButton = new JButton("保存公钥");
        panel.add(savePublicKeyButton, gbc);

        // 结果显示部分
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 5;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        resultLabel = new JLabel();
        panel.add(resultLabel, gbc);

        gbc.gridy = 8;
        verifyResultLabel = new JLabel();
        panel.add(verifyResultLabel, gbc);

        // 添加事件监听器
        selectImageButton.addActionListener(e -> {
            int result = fileChooser.showOpenDialog(WatermarkApp.this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                imagePathField = new JTextField(selectedFile.getAbsolutePath());
                imagePathField.setText(selectedFile.getAbsolutePath()); // 设置 imagePathField 的值
                try {
                    try (InputStream in =   new FileInputStream(selectedFile.getAbsolutePath())){
                        byte[] content = new byte[in.available()];
                        in.read(content);
                        waterMark.setImage(content);//更新图像字段
                        waterMark.setMarkedImage(null);//图像变了，水印字段也清理掉
                        waterMark.setImageName(selectedFile.getAbsolutePath());
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });


        generateSeedButton.addActionListener(e ->
                new SwingWorker<Void, Void>() {
                    @Override
                    protected Void doInBackground() throws Exception {
                        try {
                            String seed = MyUtil.generateSeed();
                            seedField.setText(seed);
                            Map<String, String> keyPair = MyUtil.generateKeyPair(2048);
                            publicKeyField.setText(keyPair.get("publicKey"));
                            String privateKey = keyPair.get("privateKey");
                            int result = fileChooser.showSaveDialog(WatermarkApp.this);
                            if (result == JFileChooser.APPROVE_OPTION) {
                                File selectedFile = fileChooser.getSelectedFile();
                                MyUtil.savePrivateKey(privateKey, selectedFile.getAbsolutePath());
                            }
                            waterMark.setSeed(seed);
                            waterMark.setPrivateKey(privateKey);
                            waterMark.setPublicKey(publicKeyField.getText());
                        } catch (Exception ex) {
                            handleException(ex, "生成种子错误", resultLabel);
                        }
                        return null;
                    }

                    @Override
                    protected void done() {
                        JOptionPane.showMessageDialog(WatermarkApp.this, "私钥保存成功", "Success", JOptionPane.INFORMATION_MESSAGE);
                    }
                }.execute()
        );


        savePrivateKeyButton.addActionListener(e ->
                new SwingWorker<Void, Void>() {
                    @Override
                    protected Void doInBackground() throws IOException {
                        String seed = seedField.getText();
                        int seedLength = 128; // 假设种子长度为128
                        String key = MyUtil.generateKeyFromSeed(seed, seedLength);
                        JFileChooser fileChooser = new JFileChooser();
                        int result = fileChooser.showSaveDialog(WatermarkApp.this);
                        if (result == JFileChooser.APPROVE_OPTION) {
                            File selectedFile = fileChooser.getSelectedFile();
                            MyUtil.savePrivateKey(key, selectedFile.getAbsolutePath());
                        }
                        waterMark.setSeed(seed);
                        waterMark.setPrivateKey(key);
                        return null;
                    }

                    @Override
                    protected void done() {
                        JOptionPane.showMessageDialog(WatermarkApp.this, "私钥保存成功", "Success", JOptionPane.INFORMATION_MESSAGE);
                    }
                }.execute()
        );

        selectPrivateKeyButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            int result = fileChooser.showOpenDialog(WatermarkApp.this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                privateKeyPathField.setText(selectedFile.getAbsolutePath());
                waterMark.setPrivateKey(privateKeyPathField.getText());
            }
        });

        decryptButton.addActionListener(e ->
                new SwingWorker<Void, Void>() {
                    @Override
                    protected Void doInBackground() throws Exception {
                        if (imagePathField == null || imagePathField.getText() == null || imagePathField.getText().isEmpty()) {
                            resultLabel.setText("请选择图片路径");
                            return null;
                        }

                        if (privateKeyPathField == null || privateKeyPathField.getText() == null || privateKeyPathField.getText().isEmpty()) {
                            resultLabel.setText("请选择私钥文件路径");
                            return null;
                        }

                        String imagePath = imagePathField.getText();
                        String privateKeyPath = privateKeyPathField.getText();

                        File imageFile = new File(imagePath);
                        if (!imageFile.exists() || !imageFile.canRead()) {
                            resultLabel.setText("无效的图片路径或文件不可读");
                            return null;
                        }

                        File privateKeyFile = new File(privateKeyPath);
                        if (!privateKeyFile.exists() || !privateKeyFile.canRead()) {
                            resultLabel.setText("无效的私钥文件路径或文件不可读");
                            return null;
                        }

                        String privateKey = MyUtil.readPrivateKey(privateKeyPath);
                        byte[] encryptedWatermark = MyUtil.readEncryptedWatermark(imageFile);
                        String decryptedWatermark = MyUtil.decryptWatermark(encryptedWatermark, privateKey, "RSA/ECB/PKCS1Padding");
                        resultLabel.setText("解密成功: " + decryptedWatermark);
                        return null;
                    }

                    @Override
                    protected void done() {
                        // 处理异常
                        try {
                            get();
                        } catch (Exception ex) {
                            handleException(ex, "解密水印错误", resultLabel);
                        }
                    }
                }.execute()
        );

        saveWatermarkButton.addActionListener(e -> {
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    if (imagePathField == null || imagePathField.getText() == null || imagePathField.getText().isEmpty()) {
                        resultLabel.setText("请选择图片路径");
                        return null;
                    }

                    File imageFile = new File(imagePathField.getText());
                    if (!imageFile.exists() || !imageFile.canRead()) {
                        resultLabel.setText("无效的图片路径或文件不可读");
                        return null;
                    }

                    BufferedImage image = ImageIO.read(imageFile);
                    String watermarkText = watermarkTextField.getText();
                    int seed = Integer.parseInt(seedField.getText());

                    byte[] imageData = WatermarkEmbedder.embedWatermark(image, watermarkText); // 调用 WatermarkEmbedder 的方法
                    byte[] encryptedWatermark = MyUtil.encryptWatermark(watermarkText, publicKeyField.getText(), "RSA/ECB/PKCS1Padding"); // 使用 RSA 加密

                    MyUtil.saveWatermarkInfo(
                            new File(imagePathField.getText()).getName(),
                            imageData,
                            watermarkText,
                            encryptedWatermark,
                            seed,
                            "E:\\watermark_info.txt" // 假设这是第六个参数
                    );
                    resultLabel.setText("水印嵌入成功");
                    try (InputStream in =   new FileInputStream(imageFile)){
                        byte[] content = new byte[in.available()];
                        in.read(content);
                        waterMark.setImage(content);
                        waterMark.setMarkedImage(imageData);
                        waterMark.setPublicKey(publicKeyField.getText());
                        waterMark.setPrivateKey(privateKeyPathField.getText());
                        waterMark.setCreateTime(new Date());
                        waterMark.setImageName(imagePathField.getText());
                        waterMark.setSeed(seedField.getText());
                        waterMark.setWaterText(watermarkText);
                        waterMark.setEncryptedWaterText(new String(encryptedWatermark));
                        waterMarkMapper.saveIgnoreNull(waterMark);
                    }
                    return null;
                }

                @Override
                protected void done() {
                    // 处理异常
                    try {
                        get();
                    } catch (Exception ex) {
                        handleException(ex, "嵌入水印错误", resultLabel);
                    }
                }
            }.execute();
        });

        saveWatermarkedImageButton.addActionListener(e -> {
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    if (imagePathField == null || imagePathField.getText() == null || imagePathField.getText().isEmpty()) {
                        resultLabel.setText("请选择图片路径");
                        return null;
                    }

                    File imageFile = new File(imagePathField.getText());
                    if (!imageFile.exists() || !imageFile.canRead()) {
                        resultLabel.setText("无效的图片路径或文件不可读");
                        return null;
                    }
                    if(waterMark.getPrivateKey() ==null){
                        resultLabel.setText("请先设置私钥");
                        return null;
                    }

                    // 弹出文件选择对话框
                    int result = fileChooser.showSaveDialog(WatermarkApp.this);
                    if (result == JFileChooser.APPROVE_OPTION) {
                        File selectedFile = fileChooser.getSelectedFile();

                        byte[] watermarkedImageData = getWatermarkedImageData();
                        MyUtil.saveWatermarkedImage(selectedFile, watermarkedImageData);

//                        WaterMark waterMark =new WaterMark();
//                        waterMark.setImage();
//                        WaterMark waterMark = new WaterMark();
                        Query query = new Query();

                        query.eq("image", watermarkedImageData).
                                eq("water_text", watermarkTextField.getText()).
                                eq("seed", seedField.getText());
                        waterMark.setMarkedImage(watermarkedImageData);
                        waterMark.setPublicKey(publicKeyField.getText());
//                        waterMark.setPrivateKey(privateKeyPathField.getText());
                        waterMark.setCreateTime(new Date());
                        waterMark.setImageName(imagePathField.getText());
                        waterMark.setSeed(seedField.getText());
                        waterMark.setWaterText(watermarkTextField.getText());
                        if (waterMarkMapper.getCount(query) > 0) {
                            waterMarkMapper.updateIgnoreNull(waterMark);
                        } else {
                            waterMarkMapper.save(waterMark);
                        }

                    }
                    return null;
                }

                @Override
                protected void done() {
                    JOptionPane.showMessageDialog(WatermarkApp.this, "水印保存成功", "Success", JOptionPane.INFORMATION_MESSAGE);
                }
            }.execute();
        });

        verifyWatermarkButton.addActionListener(e -> {
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    if (imagePathField == null || imagePathField.getText() == null || imagePathField.getText().isEmpty()) {
                        verifyResultLabel.setText("请选择图片路径");
                        return null;
                    }

                    WaterMark result  = waterMarkMapper.select(waterMark.getImage(),waterMark.getWaterText(),waterMark.getSeed());
                    if(result != null){
                        verifyResultLabel.setText("水印验证成功");
                    }else{
                        verifyResultLabel.setText("水印验证失败");
                    }
                    return null;
                }

                @Override
                protected void done() {
                    // 处理异常
                    try {
                        get();
                    } catch (Exception ex) {
                        handleException(ex, "验证水印错误", verifyResultLabel);
                    }
                }
            }.execute();
        });


        // 新增生成 RSA 密钥对的监听器
        generateKeyPairButton.addActionListener(e -> {
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    Map<String, String> keyPair = MyUtil.generateKeyPair(2048);
                    publicKeyField.setText(keyPair.get("publicKey"));
                    String privateKey = keyPair.get("privateKey");
                    JFileChooser fileChooser = new JFileChooser();
                    int result = fileChooser.showSaveDialog(WatermarkApp.this);
                    if (result == JFileChooser.APPROVE_OPTION) {
                        File selectedFile = fileChooser.getSelectedFile();
                        MyUtil.savePrivateKey(privateKey, selectedFile.getAbsolutePath());
                        waterMark.setPrivateKey(privateKey);
                    }
                    return null;
                }

                @Override
                protected void done() {
                    JOptionPane.showMessageDialog(WatermarkApp.this, "私钥保存成功", "Success", JOptionPane.INFORMATION_MESSAGE);
                }
            }.execute();
        });

        // 新增保存公钥的监听器
        savePublicKeyButton.addActionListener(e -> {
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    String publicKey = publicKeyField.getText();
                    if (publicKey.isEmpty()) {
                        JOptionPane.showMessageDialog(WatermarkApp.this, "公钥为空", "Error", JOptionPane.ERROR_MESSAGE);
                        return null;
                    }

                    JFileChooser fileChooser = new JFileChooser();
                    int result = fileChooser.showSaveDialog(WatermarkApp.this);
                    if (result == JFileChooser.APPROVE_OPTION) {
                        File selectedFile = fileChooser.getSelectedFile();
                        MyUtil.savePublicKey(publicKey, selectedFile.getAbsolutePath());
                        waterMark.setPublicKey(publicKey);
                    }
                    return null;
                }

                @Override
                protected void done() {
                    JOptionPane.showMessageDialog(WatermarkApp.this, "公钥保存成功", "Success", JOptionPane.INFORMATION_MESSAGE);
                }
            }.execute();
        });
        this.setLayout(new BorderLayout());
        // 添加面板到窗口
        this.add(panel);
//        this.pack();
        this.setVisible(true);
    }

    private void handleException(Exception ex, String errorMessage, JLabel label) {
        LOGGER.log(Level.SEVERE, errorMessage, ex);
        if (label != null) {
            label.setText(errorMessage);
        } else {
            JOptionPane.showMessageDialog(this, errorMessage + ": " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private byte[] getWatermarkedImageData() throws IOException {
        if (imagePathField == null || imagePathField.getText() == null || imagePathField.getText().isEmpty()) {
            throw new IOException("请选择图片路径");
        }

        File imageFile = new File(imagePathField.getText());
        if (!imageFile.exists() || !imageFile.canRead()) {
            throw new IOException("无效的图片路径或文件不可读");
        }

        BufferedImage image = ImageIO.read(imageFile);
        String watermarkText = watermarkTextField.getText();
        int seed = Integer.parseInt(seedField.getText());

        byte[] imageData = WatermarkEmbedder.embedWatermark(image, watermarkText); // 调用 WatermarkEmbedder 的方法
        return imageData;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new WatermarkApp());
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public boolean isRunning() {
        return false;
    }
}
