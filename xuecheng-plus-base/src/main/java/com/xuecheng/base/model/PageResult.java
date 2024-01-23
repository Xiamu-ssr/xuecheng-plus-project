package com.xuecheng.base.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 页面结果
 *
 * @author mumu
 * @date 2024/01/21
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageResult<T> implements Serializable {

    /**
     * 数据列表
     */
    private List<T> items;


    /**
     * 总记录数
     */
    private long counts;


    /**
     * 当前页
     */
    private long page;


    /**
     * 每页数量
     */
    private long pageSize;
}
