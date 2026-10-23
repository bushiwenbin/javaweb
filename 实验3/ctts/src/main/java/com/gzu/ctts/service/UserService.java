package com.gzu.ctts.service;

import com.gzu.ctts.dto.PageResult;
import com.gzu.ctts.dto.UserForm;
import com.gzu.ctts.entity.User;

import java.util.List;

/**
 * 用户业务接口。
 *
 * <p>实验二的 Service 用内存 {@code CopyOnWriteArrayList} 模拟数据；
 * 实验三改为调用 {@code UserMapper}，数据真正落在 MySQL 的 {@code sys_user} 表中，
 * 进程重启后数据依然存在。
 */
public interface UserService {

    /** 分页 + 条件查询用户列表（role 与 keyword 可为空，为空则不参与过滤） */
    PageResult<User> page(int pageNum, int pageSize, String role, String keyword);

    /** 按主键查询用户，不存在返回 null */
    User getById(Long id);

    /** 新增用户，返回带自增主键的完整对象 */
    User create(UserForm form);

    /** 按主键修改用户 */
    User update(Long id, UserForm form);

    /** 逻辑删除用户 */
    boolean remove(Long id);

    /** 按角色查询用户列表（走 Mapper 的注解 SQL） */
    List<User> listByRole(String role);

    /** 修改用户启用状态（走 Mapper 的注解 SQL） */
    User changeStatus(Long id, Integer status);
}