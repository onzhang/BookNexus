package com.zjw.booknexus.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 审计字段自动填充处理器
 * <p>自动为实体类的 {@code createdAt} 和 {@code updatedAt} 字段填充当前时间戳。
 * 继承 {@code BaseEntity} 的实体类在 INSERT 和 UPDATE 操作时无需手动设置审计字段，
 * 由该处理器自动完成填充，确保审计时间戳的一致性与准确性。</p>
 *
 * <p><b>填充策略：</b></p>
 * <ul>
 *   <li>INSERT 操作：自动填充 {@code createdAt} 和 {@code updatedAt} 为当前时间</li>
 *   <li>UPDATE 操作：自动更新 {@code updatedAt} 为当前时间</li>
 * </ul>
 *
 * @author 张俊文
 * @since 2026-04-30
 */
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    /**
     * INSERT 操作时的自动填充逻辑
     * <p>使用 strictInsertFill 严格模式填充创建时间和更新时间，
     * 仅在字段类型与值类型一致且字段值非空时执行填充。</p>
     *
     * @param metaObject MyBatis-Plus 元数据对象，包含实体类的字段信息
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "createdAt", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
    }

    /**
     * UPDATE 操作时的自动填充逻辑
     * <p>使用 strictUpdateFill 严格模式自动更新修改时间字段，
     * 确保每次修改都记录最新的更新时间戳。</p>
     *
     * @param metaObject MyBatis-Plus 元数据对象，包含实体类的字段信息
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
    }
}
