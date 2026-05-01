package com.zjw.booknexus.dto;

import lombok.Data;

/**
 * 用户分页查询请求 DTO
 * <p>
 * 用于管理员查询用户列表时分页参数和筛选条件，
 * 支持按关键字对用户名进行模糊搜索。
 * </p>
 *
 * @author 张俊文
 * @since 2026-04-30
 */
@Data
public class UserPageReq {

    /** 当前页码，默认第 1 页 */
    private Integer page = 1;

    /** 每页记录数，默认 10 条 */
    private Integer size = 10;

    /** 关键字模糊搜索（用户名） */
    private String keyword;
}
