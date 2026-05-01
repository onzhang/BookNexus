package com.zjw.booknexus.service;

import com.zjw.booknexus.dto.LoginReq;
import com.zjw.booknexus.dto.LoginResp;
import com.zjw.booknexus.dto.RefreshReq;
import com.zjw.booknexus.dto.RegisterReq;
import com.zjw.booknexus.vo.UserVO;

public interface AuthService {

    LoginResp register(RegisterReq req);

    LoginResp login(LoginReq req);

    LoginResp refresh(RefreshReq req);

    UserVO getCurrentUser(Long userId);
}
