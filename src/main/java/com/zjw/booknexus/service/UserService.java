package com.zjw.booknexus.service;

import com.zjw.booknexus.common.PageResult;
import com.zjw.booknexus.dto.UserPageReq;
import com.zjw.booknexus.dto.UserUpdateReq;
import com.zjw.booknexus.vo.UserVO;

public interface UserService {

    PageResult<UserVO> page(UserPageReq req);

    UserVO getById(Long id);

    void update(Long id, UserUpdateReq req);

    void updateStatus(Long id, String status);
}
