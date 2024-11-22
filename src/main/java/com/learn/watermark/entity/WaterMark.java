package com.learn.watermark.entity;

import lombok.Data;


import javax.persistence.*;
import java.util.Date;


/**
 * 表名：water_mark
 *
 * @author tanghc
 */
@Table(name = "water_mark")
@Data
public class WaterMark {
    /**
     * 数据库字段：id
     */
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 源图片, 数据库字段：image
     */
    private byte[] image;

    /**
     * 加水印后的图片, 数据库字段：marked_image
     */
    private byte[] markedImage;

    /**
     * 图片名称, 数据库字段：image_name
     */
    private String imageName;

    /**
     * 数据库字段：create_time
     */
    private Date createTime;

    /**
     * 种子, 数据库字段：seed
     */
    private String seed;

    /**
     * 私钥, 数据库字段：private_key
     */
    private String privateKey;

    /**
     * 公钥, 数据库字段：public_key
     */
    private String publicKey;

    /**
     * 水印文本, 数据库字段：water_text
     */
    private String waterText;
    private String encryptedWaterText;
}
