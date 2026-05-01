package com.zjw.booknexus.service;

import com.zjw.booknexus.common.PageResult;
import com.zjw.booknexus.dto.AdminBorrowPageReq;
import com.zjw.booknexus.dto.BorrowPageReq;
import com.zjw.booknexus.dto.BorrowReq;
import com.zjw.booknexus.vo.BorrowRecordVO;

public interface BorrowService {

    BorrowRecordVO borrow(Long userId, BorrowReq req);

    BorrowRecordVO returnBook(Long userId, Long recordId);

    BorrowRecordVO renew(Long userId, Long recordId);

    PageResult<BorrowRecordVO> myBorrows(Long userId, BorrowPageReq req);

    PageResult<BorrowRecordVO> adminPage(AdminBorrowPageReq req);

    BorrowRecordVO adminReturnRecord(Long recordId);
}
