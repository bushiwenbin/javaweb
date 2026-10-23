package com.gzu.ctts.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 分页查询结果封装。
 *
 * <p>分页接口除了返回当前页的数据列表（{@code records}）外，
 * 还必须返回总数、页码、每页条数等分页元信息，前端才能渲染出页码条。
 *
 * @param <T> 列表元素类型
 */
@Data
@Schema(description = "分页查询结果")
public class PageResult<T> implements Serializable {

    /** 当前页数据 */
    @Schema(description = "当前页数据列表")
    private List<T> records;

    /** 总记录数 */
    @Schema(description = "总记录数", example = "8")
    private long total;

    /** 当前页码，从 1 开始 */
    @Schema(description = "当前页码，从 1 开始", example = "1")
    private int pageNum;

    /** 每页条数 */
    @Schema(description = "每页条数", example = "3")
    private int pageSize;

    /** 总页数 */
    @Schema(description = "总页数（向上取整）", example = "3")
    private int pages;

    public PageResult(List<T> records, long total, int pageNum, int pageSize) {
        this.records = records;
        this.total = total;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        // 向上取整计算总页数，total 为 0 时返回 0 页
        this.pages = pageSize <= 0 ? 0 : (int) ((total + pageSize - 1) / pageSize);
    }
}
