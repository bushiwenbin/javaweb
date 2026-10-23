package com.gzu.ctts.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 字段自动填充处理器 —— 与实体类上的
 * {@code @TableField(fill = FieldFill.INSERT / INSERT_UPDATE)} 配套使用。
 *
 * <p>解决的问题：几乎每张表都有「创建时间、更新时间」这类字段，
 * 若在每个 Service 方法里手工 {@code entity.setCreateTime(LocalDateTime.now())}，
 * 代码会大量重复且容易漏写。把它们交给统一处理器后，
 * 插入/更新时由 MyBatis-Plus 自动回调本类填充。
 *
 * <p>实现要点：
 * <ul>
 *   <li>实现 {@link MetaObjectHandler} 接口并注册为 Spring 组件（{@code @Component}）；</li>
 *   <li>{@code insertFill} 在 INSERT 前回调，{@code updateFill} 在 UPDATE 前回调；</li>
 *   <li>{@code strictInsertFill} / {@code strictUpdateFill} 会先检查实体上该字段是否
 *       声明了对应的 {@code fill} 策略，只有声明过才填充，
 *       避免误伤其它实体上没有该语义的字段。</li>
 * </ul>
 *
 * <p>因此实验三的实体里，{@code createTime} 与 {@code updateTime} 无需在业务代码中赋值。
 */
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    private static final Logger log = LoggerFactory.getLogger(MyMetaObjectHandler.class);

    /** 插入时填充创建时间与更新时间 */
    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, now);
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, now);
        log.debug("自动填充[插入] createTime/updateTime = {}", now);
    }

    /** 更新时只刷新更新时间，创建时间保持不变 */
    @Override
    public void updateFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, now);
        log.debug("自动填充[更新] updateTime = {}", now);
    }
}