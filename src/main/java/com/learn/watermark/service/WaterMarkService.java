package com.learn.watermark.service;

import com.gitee.fastmybatis.core.query.Query;
import com.learn.watermark.entity.WaterMark;

import java.util.List;

/**
 * @program: watermark
 * @description:
 * @author: Martin Fowler
 * @create: 2024-10-21 17:47
 */
public interface WaterMarkService {


    /**
     * 查询所有记录
     *
     * @return 返回集合，没有返回空List
     */
    public List<WaterMark>  list(Query query);


    /**
     * 根据主键查询
     *
     * @param id 主键
     * @return 返回记录，没有返回null
     */
    public WaterMark getById(Integer id);

    /**
     * 新增，插入所有字段
     *
     * @param waterMark 新增的记录
     * @return 返回影响行数
     */
    public int insert(WaterMark waterMark);

    /**
     * 新增，忽略null字段
     *
     * @param waterMark 新增的记录
     * @return 返回影响行数
     */
    public int insertIgnoreNull(WaterMark waterMark);

    /**
     * 修改，修改所有字段
     *
     * @param waterMark 修改的记录
     * @return 返回影响行数
     */
    public int update(WaterMark waterMark);

    /**
     * 修改，忽略null字段
     *
     * @param waterMark 修改的记录
     * @return 返回影响行数
     */
    public int updateIgnoreNull(WaterMark waterMark);

    /**
     * 删除记录
     *
     * @param waterMark 待删除的记录
     * @return 返回影响行数
     */
    public int delete(WaterMark waterMark);

}