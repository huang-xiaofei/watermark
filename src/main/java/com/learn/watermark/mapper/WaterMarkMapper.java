package com.learn.watermark.mapper;

import com.gitee.fastmybatis.core.mapper.CrudMapper;
import com.learn.watermark.entity.WaterMark;
import org.apache.ibatis.annotations.Select;


/**
 * @author tanghc
 */
public interface WaterMarkMapper extends CrudMapper<WaterMark, Integer> {
    @Select("select * from water_mark where (image=#{image} or marked_image=#{image}) and  water_text=#{waterText} and seed=#{seed}")
    public WaterMark select(byte[] image,String waterText,String seed);
}
