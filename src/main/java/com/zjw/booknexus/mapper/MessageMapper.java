package com.zjw.booknexus.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zjw.booknexus.entity.Message;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MessageMapper extends BaseMapper<Message> {
}
