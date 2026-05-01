package com.zjw.booknexus.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zjw.booknexus.entity.Favorite;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户收藏数据访问层 Mapper 接口。
 * <p>
 * 对应数据库表 {@code favorite}，提供用户收藏图书信息的 CRUD 操作。
 * 继承自 MyBatis-Plus 的 BaseMapper，拥有内置的增删改查、分页查询等基础能力。
 * </p>
 *
 * @author 张俊文
 * @since 2026-04-30
 */
@Mapper
public interface FavoriteMapper extends BaseMapper<Favorite> {
}
