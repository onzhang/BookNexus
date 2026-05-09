package com.zjw.booknexus.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zjw.booknexus.entity.Book;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 图书数据访问层 Mapper 接口。
 * <p>
 * 对应数据库表 {@code book}，提供图书信息的 CRUD 操作。
 * 继承自 MyBatis-Plus 的 BaseMapper，拥有内置的增删改查、分页查询等基础能力。
 * 额外提供原子化的库存扣减和恢复方法，避免并发场景下的超借/超还问题。
 * </p>
 *
 * @author 张俊文
 * @since 2026-04-30
 */
@Mapper
public interface BookMapper extends BaseMapper<Book> {

    /**
     * 原子化扣减可用库存。
     * <p>仅在 available_stock > 0 时才执行扣减，避免超借。
     * 扣减后若可用库存降为 0，需配合应用层将图书状态更新为 BORROWED。</p>
     *
     * @param bookId 图书 ID
     * @return 受影响行数，1 表示扣减成功，0 表示库存不足
     */
    @Update("UPDATE book SET available_stock = available_stock - 1 WHERE id = #{bookId} AND available_stock > 0")
    int decrementAvailableStock(@Param("bookId") Long bookId);

    /**
     * 原子化恢复可用库存。
     * <p>归还图书时恢复可用库存，同时配合应用层将图书状态更新为 AVAILABLE。</p>
     *
     * @param bookId 图书 ID
     * @return 受影响行数
     */
    @Update("UPDATE book SET available_stock = available_stock + 1 WHERE id = #{bookId}")
    int incrementAvailableStock(@Param("bookId") Long bookId);
}
