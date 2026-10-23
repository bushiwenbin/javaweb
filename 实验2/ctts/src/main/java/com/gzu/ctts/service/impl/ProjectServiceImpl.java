package com.gzu.ctts.service.impl;

import com.gzu.ctts.dto.PageResult;
import com.gzu.ctts.dto.ProjectForm;
import com.gzu.ctts.entity.Project;
import com.gzu.ctts.service.ProjectService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 实训项目业务实现类 —— 本实验阶段使用「内存模拟数据」。
 *
 * <p>之所以先用模拟数据，是为了把注意力集中在 Controller 层的
 * 注解使用与参数接收机制上；数据访问层（MyBatis-Plus）留到后续实验。
 *
 * <p>用 {@link CopyOnWriteArrayList} 而非普通 ArrayList 存放数据，
 * 是为了在并发请求下仍能安全地读写，模拟出一个"看起来像数据库"的内存存储。
 */
@Service
public class ProjectServiceImpl implements ProjectService {

    /** 内存"数据表"，进程重启即还原 */
    private final List<Project> store = new CopyOnWriteArrayList<>();

    /** 模拟数据库自增主键 */
    private final AtomicInteger idGenerator = new AtomicInteger(0);

    /** 构造时灌入 8 条模拟数据，便于验证分页效果 */
    public ProjectServiceImpl() {
        add("校园实训任务管理系统（CTTS）后端", "Web 后端", "丁玺润", "2026-12-31", 1,
                "基于 Spring Boot 3 构建实训任务发布、提交与评分接口。");
        add("学生信息管理模块", "Web 后端", "丁玺润", "2026-11-15", 1,
                "实现学生信息的增删改查接口，并完成参数校验。");
        add("实训成果统计报表", "数据分析", "李敏", "2026-11-30", 0,
                "按班级、项目维度统计实训提交率与平均得分。");
        add("前后端分离接口联调", "Web 后端", "丁玺润", "2026-12-10", 1,
                "使用 Apifox 对全部 REST 接口进行调试与文档生成。");
        add("任务提交记录导出", "工具开发", "王强", "2026-12-20", 0,
                "支持将学生提交记录导出为 Excel 文件。");
        add("登录鉴权与 Token 校验", "Web 后端", "李敏", "2026-12-05", 2,
                "基于拦截器实现 JWT 登录鉴权与接口权限控制。");
        add("接口自动化测试用例", "测试", "王强", "2026-12-25", 0,
                "为 Controller 层接口编写 JUnit 5 + MockMvc 自动化测试。");
        add("大数据量分页性能优化", "性能优化", "丁玺润", "2027-01-05", 0,
                "对比物理分页与内存分页在大数据量下的性能差异。");
    }

    private void add(String name, String category, String teacher,
                     String deadline, Integer status, String description) {
        store.add(new Project(idGenerator.incrementAndGet(), name, category,
                teacher, deadline, status, description));
    }

    @Override
    public List<Project> listAll() {
        return new ArrayList<>(store);
    }

    @Override
    public Project getById(Integer id) {
        return store.stream()
                .filter(p -> p.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    @Override
    public PageResult<Project> page(int pageNum, int pageSize) {
        long total = store.size();
        // 内存分页：先算起始下标，再用 stream 截取当前页
        int from = (pageNum - 1) * pageSize;
        List<Project> records;
        if (from >= store.size()) {
            records = Collections.emptyList();
        } else {
            records = store.stream().skip(from).limit(pageSize).collect(Collectors.toList());
        }
        return new PageResult<>(records, total, pageNum, pageSize);
    }

    @Override
    public Project create(ProjectForm form) {
        Project project = new Project(
                idGenerator.incrementAndGet(),
                form.getName(),
                form.getCategory(),
                form.getTeacher(),
                form.getDeadline(),
                form.getStatus() == null ? 0 : form.getStatus(),
                form.getDescription());
        store.add(project);
        return project;
    }
}
