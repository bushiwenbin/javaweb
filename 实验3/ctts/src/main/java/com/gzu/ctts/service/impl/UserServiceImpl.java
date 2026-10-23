package com.gzu.ctts.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gzu.ctts.dto.PageResult;
import com.gzu.ctts.dto.Result;
import com.gzu.ctts.dto.UserForm;
import com.gzu.ctts.entity.User;
import com.gzu.ctts.exception.BizException;
import com.gzu.ctts.mapper.UserMapper;
import com.gzu.ctts.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 用户业务实现类 —— 实验三的持久层落地点。
 *
 * <p>与实验二最本质的区别：本类的数据来源由「内存 List」换成了
 * {@link UserMapper}，每一次方法调用最终都会产生一条真实的 SQL 语句。
 *
 * <p>本类用到的 MyBatis-Plus 能力：
 * <ol>
 *   <li><b>BaseMapper 的现成方法</b>：{@code selectById}、{@code insert}、
 *       {@code updateById}、{@code deleteById}、{@code selectCount}、{@code selectPage}，
 *       全部无需编写 SQL；</li>
 *   <li><b>条件构造器 {@link LambdaQueryWrapper}</b>：用 Lambda 方法引用
 *       （如 {@code User::getUsername}）代替硬编码列名。相比字符串写法
 *       {@code QueryWrapper.eq("username", ...)}，Lambda 写法在属性改名时
 *       编译期就会报错，不必等到运行时才发现「列名写错」；</li>
 *   <li><b>物理分页 {@link Page}</b>：配合 {@code MybatisPlusConfig} 中的分页插件，
 *       由数据库执行 {@code LIMIT}，而非把全表读进内存再截取。</li>
 * </ol>
 */
@Service
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    /** 新增用户时若未提供密码，使用的初始密码 */
    private static final String DEFAULT_PASSWORD = "123456";

    private final UserMapper userMapper;

    /** 构造器注入：依赖不可变、便于单元测试，是 Spring 官方推荐方式 */
    public UserServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public PageResult<User> page(int pageNum, int pageSize, String role, String keyword) {
        // ① 分页对象：页码与每页条数，由分页插件翻译成 LIMIT 语句
        Page<User> page = new Page<>(pageNum, pageSize);

        // ② 条件构造器：所有条件都用「条件重载」写法，
        //    第一个参数为 false 时该条件不会被拼进 SQL，因此无需在方法里写一堆 if
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>()
                .eq(StringUtils.hasText(role), User::getRole, role)
                // and(...) 把「账号模糊 or 姓名模糊」整体括起来，
                // 否则 or 会与前面的 role 条件平级，导致查询范围被意外放大
                .and(StringUtils.hasText(keyword), w -> w
                        .like(User::getUsername, keyword)
                        .or()
                        .like(User::getRealName, keyword))
                .orderByDesc(User::getId);

        userMapper.selectPage(page, wrapper);
        log.info("分页查询用户：pageNum={}, pageSize={}, role={}, keyword={}, 命中 {} 条 / 共 {} 条",
                pageNum, pageSize, role, keyword, page.getRecords().size(), page.getTotal());
        return new PageResult<>(page.getRecords(), page.getTotal(), pageNum, pageSize);
    }

    @Override
    public User getById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BizException(Result.CODE_NOT_FOUND, "用户不存在，id = " + id);
        }
        return user;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public User create(UserForm form) {
        // 唯一性校验：sys_user.username 上有唯一索引，先查一次可给出更友好的提示；
        // 即使并发下漏过这一步，数据库唯一索引也会兜底抛 DuplicateKeyException
        Long exists = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getUsername, form.getUsername()));
        if (exists != null && exists > 0) {
            throw new BizException(Result.CODE_BAD_REQUEST, "登录账号已存在：" + form.getUsername());
        }

        User user = new User();
        user.setUsername(form.getUsername());
        user.setPassword(encrypt(StringUtils.hasText(form.getPassword())
                ? form.getPassword() : DEFAULT_PASSWORD));
        user.setRealName(form.getRealName());
        user.setRole(form.getRole());
        user.setEmail(form.getEmail());
        user.setPhone(form.getPhone());
        user.setStatus(form.getStatus() == null ? 1 : form.getStatus());
        // 逻辑删除标记显式置 0：MyBatis-Plus 的 @TableLogic 只在 delete 时维护该列，
        // 插入时不会自动赋值，显式写出可避免响应里出现 deleted = null
        user.setDeleted(0);
        // createTime / updateTime 由 MyMetaObjectHandler 自动填充，这里无需赋值

        userMapper.insert(user);
        log.info("新增用户成功，id = {}, username = {}", user.getId(), user.getUsername());
        // insert 之后主键已回填到实体，但 password 仍带着摘要值，返回前清空以免外泄
        user.setPassword(null);
        return user;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public User update(Long id, UserForm form) {
        User exists = userMapper.selectById(id);
        if (exists == null) {
            throw new BizException(Result.CODE_NOT_FOUND, "用户不存在，id = " + id);
        }

        User user = new User();
        user.setId(id);
        user.setUsername(form.getUsername());
        user.setRealName(form.getRealName());
        user.setRole(form.getRole());
        user.setEmail(form.getEmail());
        user.setPhone(form.getPhone());
        user.setStatus(form.getStatus());
        // 密码留空表示不修改：MyBatis-Plus 默认更新策略为 NOT_NULL，
        // 值为 null 的字段不会出现在 SET 子句中，因此不会把密码覆盖成空
        if (StringUtils.hasText(form.getPassword())) {
            user.setPassword(encrypt(form.getPassword()));
        }

        userMapper.updateById(user);
        log.info("修改用户成功，id = {}", id);
        return userMapper.selectById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean remove(Long id) {
        if (userMapper.selectById(id) == null) {
            throw new BizException(Result.CODE_NOT_FOUND, "用户不存在，id = " + id);
        }
        // User.deleted 上有 @TableLogic，因此这里生成的其实是
        // UPDATE sys_user SET deleted = 1 WHERE id = ? AND deleted = 0
        int rows = userMapper.deleteById(id);
        log.info("逻辑删除用户，id = {}, 受影响行数 = {}", id, rows);
        return rows > 0;
    }

    @Override
    public List<User> listByRole(String role) {
        // 调用自定义注解 SQL：@Select 中已手写 deleted = 0
        List<User> users = userMapper.selectByRole(role);
        log.info("按角色查询用户，role = {}, 命中 {} 条", role, users.size());
        return users;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public User changeStatus(Long id, Integer status) {
        int rows = userMapper.updateStatus(id, status);
        if (rows == 0) {
            throw new BizException(Result.CODE_NOT_FOUND, "用户不存在或已删除，id = " + id);
        }
        log.info("修改用户状态，id = {}, status = {}", id, status);
        return userMapper.selectById(id);
    }

    /** 密码摘要：本实验用 MD5 演示，生产环境应改用 BCrypt 等带盐慢哈希算法 */
    private String encrypt(String raw) {
        return DigestUtils.md5DigestAsHex(raw.getBytes(StandardCharsets.UTF_8));
    }
}