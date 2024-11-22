package com.learn.watermark.service.impl;

import com.gitee.fastmybatis.core.query.Query;
import com.learn.watermark.entity.WaterMark;
import com.learn.watermark.mapper.WaterMarkMapper;
import com.learn.watermark.service.WaterMarkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @program: watermark
 * @description:
 * @author: Martin Fowler
 * @create: 2024-10-21 17:47
 */
@Service
public class WaterMarkServiceImpl implements WaterMarkService {


    @Autowired
    private WaterMarkMapper waterMarkMapper;


    /**
     * 查询所有记录
     *
     * @return 返回集合，没有返回空List
     */
    @Override
    public List<WaterMark> list(Query query) {

        return waterMarkMapper.list(query);
    }


    /**
     * 根据主键查询
     *
     * @param id 主键
     * @return 返回记录，没有返回null
     */
    @Override
    public WaterMark getById(Integer id) {
        return waterMarkMapper.getById(id);
    }

    /**
     * 新增，插入所有字段
     *
     * @param waterMark 新增的记录
     * @return 返回影响行数
     */
    @Override
    public int insert(WaterMark waterMark) {
        return waterMarkMapper.saveIgnoreNull(waterMark);
    }

    /**
     * 新增，忽略null字段
     *
     * @param waterMark 新增的记录
     * @return 返回影响行数
     */
    @Override
    public int insertIgnoreNull(WaterMark waterMark) {
        return waterMarkMapper.saveIgnoreNull(waterMark);
    }

    /**
     * 修改，修改所有字段
     *
     * @param waterMark 修改的记录
     * @return 返回影响行数
     */
    @Override
    public int update(WaterMark waterMark) {
        return waterMarkMapper.update(waterMark);
    }

    /**
     * 修改，忽略null字段
     *
     * @param waterMark 修改的记录
     * @return 返回影响行数
     */
    @Override
    public int updateIgnoreNull(WaterMark waterMark) {
        return waterMarkMapper.updateIgnoreNull(waterMark);
    }

    /**
     * 删除记录
     *
     * @param waterMark 待删除的记录
     * @return 返回影响行数
     */
    @Override
    public int delete(WaterMark waterMark) {
        return waterMarkMapper.delete(waterMark);
    }

}