package com.hbpu.smartpicture.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hbpu.smartpicture.mapper.UserMapper;
import com.hbpu.smartpicture.service.UserService;
import com.hbpu.smartpicture.model.pojo.User;
import org.springframework.stereotype.Service;

/**
* @author 马可
* @description 针对表【user(用户)】的数据库操作Service实现
* @createDate 2025-11-10 17:42:36
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{

}




