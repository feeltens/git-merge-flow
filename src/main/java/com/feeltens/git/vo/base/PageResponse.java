package com.feeltens.git.vo.base;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 列表分页查询 出参
 *
 * @author ：feeltens
 * @date ：2025-08-20
 */
@Data
public class PageResponse<T> implements Serializable {

    private static final long serialVersionUID = -5790241843522462958L;

    /**
     * 总条数
     */
    private Long total;

    /**
     * 总页数
     */
    private Integer totalPage;

    /**
     * 当前数据
     */
    private List<T> items;

    /**
     * 当前页数
     */
    private Integer currentPage;

    /**
     * 每页的大小
     */
    private Integer pageSize;

    public PageResponse() {
    }

    public PageResponse(Long total, List<T> items) {
        this.total = total;
        this.items = items;
    }

    public PageResponse(Long total, Integer totalPage, List<T> items) {
        this.total = total;
        this.totalPage = totalPage;
        this.items = items;
    }

    /**
     * 有参总数，当前页，每页数量的构造方法
     * 计算总页数
     *
     * @param total       总数
     * @param currentPage 当前页
     * @param pageSize    每页数量
     */
    public PageResponse(Long total, Integer currentPage, Integer pageSize) {
        this.total = total;
        this.totalPage = (total.intValue() - 1) / pageSize + 1;
        this.currentPage = currentPage;
        this.pageSize = pageSize;
    }

    public static <T> PageResponse build(List<T> items, Integer currentPage, Integer pageSize, Long total) {
        PageResponse response = new PageResponse();
        response.setCurrentPage(currentPage);
        response.setPageSize(pageSize);
        response.setTotal(total);
        response.setItems(items);
        return response;
    }

}