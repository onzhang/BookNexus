package com.zjw.booknexus.service;

import com.zjw.booknexus.common.PageResult;
import com.zjw.booknexus.dto.BookCreateReq;
import com.zjw.booknexus.dto.BookPageReq;
import com.zjw.booknexus.dto.BookUpdateReq;
import com.zjw.booknexus.vo.BookVO;

public interface BookService {

    PageResult<BookVO> page(BookPageReq req);

    BookVO getById(Long id);

    BookVO create(BookCreateReq req);

    BookVO update(Long id, BookUpdateReq req);

    void delete(Long id);
}
