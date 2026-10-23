package com.gzu.ctts.service;

import com.gzu.ctts.dto.PageResult;
import com.gzu.ctts.dto.ProjectForm;
import com.gzu.ctts.entity.Project;

import java.util.List;

/**
 * 实训项目业务接口。
 *
 * <p>本实验阶段由 {@link com.gzu.ctts.service.impl.ProjectServiceImpl} 用内存模拟数据实现，
 * 后续实验接入 MyBatis-Plus 后，只需替换实现类即可，Controller 层无需改动
 * —— 这正是「面向接口编程 + 分层解耦」的价值。
 */
public interface ProjectService {

    /** 查询全部实训项目 */
    List<Project> listAll();

    /** 按 id 查询单个实训项目，查不到返回 {@code null} */
    Project getById(Integer id);

    /** 分页查询，pageNum 从 1 开始 */
    PageResult<Project> page(int pageNum, int pageSize);

    /** 新增实训项目，返回新增后的对象（含自增 id） */
    Project create(ProjectForm form);
}
