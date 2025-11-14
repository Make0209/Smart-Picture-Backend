package com.hbpu.smartpicture.service;

import com.hbpu.smartpicture.common.BaseResponse;
import com.hbpu.smartpicture.model.dto.UserRegisterDTO;
import com.hbpu.smartpicture.model.pojo.User;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author 马可
* @description 针对表【user(用户)】的数据库操作Service
* @createDate 2025-11-10 17:42:36
*/
public interface UserService extends IService<User> {

    /**
     * 用户注册方法
     * @param userRegisterDTO 用户注册请求封装对象
     * @return 用户注册成功后的id
     */
    Long userRegister(UserRegisterDTO userRegisterDTO);
}
