package com.gzu.ctts.service;

import com.gzu.ctts.dto.PageResult;
import com.gzu.ctts.dto.ProjectForm;
import com.gzu.ctts.entity.Project;

import java.util.List;
import java.util.Map;

/**
 * 实训项目业务接口。
 *
 * <p>实验二的实现是内存模拟数据；实验三改为经 {@code ProjectMapper}
 * 读写 MySQL 的 {@code training_project} 表。
 */
public interface ProjectService {

    /** 分页 + 条件查询（status / category / keyword 为空则不参与过滤） */
    PageResult<Project> page(int pageNum, int pageSize, Integer status, String category, String keyword);

    /** 按主键查询，不存在返回 null */
    Project getById(Long id);

    /** 新增项目 */
    Project create(ProjectForm form);

    /** 按主键修改项目 */
    Project update(Long id, ProjectForm form);

    /** 按主键删除项目 */
    boolean remove(Long id);

    /** 按类别查询（走 Mapper 注解 SQL） */
    List<Project> listByCategory(String category);

    /** 修改项目状态（走 Mapper 注解 SQL） */
    Project changeStatus(Long id, Integer status);

    /** 按类别分组统计项目数量（走 Mapper 聚合注解 SQL） */
    List<Map<String, Object>> categoryStatistics();
}