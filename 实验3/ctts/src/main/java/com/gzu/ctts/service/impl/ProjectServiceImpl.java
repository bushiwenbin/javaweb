package com.gzu.ctts.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gzu.ctts.dto.PageResult;
import com.gzu.ctts.dto.ProjectForm;
import com.gzu.ctts.dto.Result;
import com.gzu.ctts.entity.Project;
import com.gzu.ctts.exception.BizException;
import com.gzu.ctts.mapper.ProjectMapper;
import com.gzu.ctts.service.ProjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * 实训项目业务实现类。
 *
 * <p>实验二的同类名为「内存模拟数据」版本，用 {@code CopyOnWriteArrayList} 存放数据；
 * 实验三改为注入 {@link ProjectMapper}，所有操作都落到 {@code training_project} 表，
 * 进程重启后数据不再丢失。
 *
 * <p>代码里再也看不到手写的 {@code INSERT}/{@code UPDATE}/{@code DELETE} ——
 * 单表 CRUD 全部由 {@code BaseMapper} 依据实体上的
 * {@code @TableName}/{@code @TableId}/{@code @TableField} 自动推导生成，
 * 这正是 MyBatis-Plus「只写接口、不写 SQL」的价值所在。
 */
@Service
public class ProjectServiceImpl implements ProjectService {

    private static final Logger log = LoggerFactory.getLogger(ProjectServiceImpl.class);

    private final ProjectMapper projectMapper;

    public ProjectServiceImpl(ProjectMapper projectMapper) {
        this.projectMapper = projectMapper;
    }

    @Override
    public PageResult<Project> page(int pageNum, int pageSize, Integer status,
                                    String category, String keyword) {
        Page<Project> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<Project> wrapper = new LambdaQueryWrapper<Project>()
                .eq(status != null, Project::getStatus, status)
                .eq(StringUtils.hasText(category), Project::getCategory, category)
                .and(StringUtils.hasText(keyword), w -> w
                        .like(Project::getName, keyword)
                        .or()
                        .like(Project::getTeacher, keyword))
                // 未开始/进行中的项目排在前面，便于按紧急程度查看
                .orderByAsc(Project::getStatus)
                .orderByAsc(Project::getDeadline);

        projectMapper.selectPage(page, wrapper);
        log.info("分页查询实训项目：pageNum={}, pageSize={}, status={}, category={}, keyword={}, "
                        + "命中 {} 条 / 共 {} 条",
                pageNum, pageSize, status, category, keyword,
                page.getRecords().size(), page.getTotal());
        return new PageResult<>(page.getRecords(), page.getTotal(), pageNum, pageSize);
    }

    @Override
    public Project getById(Long id) {
        Project project = projectMapper.selectById(id);
        if (project == null) {
            throw new BizException(Result.CODE_NOT_FOUND, "实训项目不存在，id = " + id);
        }
        return project;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Project create(ProjectForm form) {
        Project project = new Project();
        project.setName(form.getName());
        project.setCategory(form.getCategory());
        project.setTeacher(form.getTeacher());
        project.setDeadline(form.getDeadline());
        project.setStatus(form.getStatus() == null ? 0 : form.getStatus());
        project.setDescription(form.getDescription());

        projectMapper.insert(project);
        log.info("新增实训项目成功，id = {}, name = {}", project.getId(), project.getName());
        return project;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Project update(Long id, ProjectForm form) {
        if (projectMapper.selectById(id) == null) {
            throw new BizException(Result.CODE_NOT_FOUND, "实训项目不存在，id = " + id);
        }

        Project project = new Project();
        project.setId(id);
        project.setName(form.getName());
        project.setCategory(form.getCategory());
        project.setTeacher(form.getTeacher());
        project.setDeadline(form.getDeadline());
        project.setStatus(form.getStatus());
        project.setDescription(form.getDescription());

        projectMapper.updateById(project);
        log.info("修改实训项目成功，id = {}", id);
        return projectMapper.selectById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean remove(Long id) {
        if (projectMapper.selectById(id) == null) {
            throw new BizException(Result.CODE_NOT_FOUND, "实训项目不存在，id = " + id);
        }
        // Project 未标注 @TableLogic，因此这里是真正的物理删除
        int rows = projectMapper.deleteById(id);
        log.info("删除实训项目，id = {}, 受影响行数 = {}", id, rows);
        return rows > 0;
    }

    @Override
    public List<Project> listByCategory(String category) {
        return projectMapper.selectByCategory(category);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Project changeStatus(Long id, Integer status) {
        int rows = projectMapper.updateStatus(id, status);
        if (rows == 0) {
            throw new BizException(Result.CODE_NOT_FOUND, "实训项目不存在，id = " + id);
        }
        log.info("修改实训项目状态，id = {}, status = {}", id, status);
        return projectMapper.selectById(id);
    }

    @Override
    public List<Map<String, Object>> categoryStatistics() {
        return projectMapper.countGroupByCategory();
    }
}