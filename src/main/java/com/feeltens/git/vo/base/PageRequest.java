package com.feeltens.git.vo.base;

import lombok.Data;

import java.io.Serializable;

/**
 * 列表分页查询
 *
 * @author ：feeltens
 * @date ：2025-08-20
 */
@Data
public class PageRequest<T> implements Serializable {

    private static final long serialVersionUID = -5790241843522462958L;

    /**
     * 当前页数
     */
    private Integer currentPage = 1;

    /**
     * 每页的大小
     */
    private Integer pageSize = 10;

    private T item;

}