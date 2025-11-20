package com.hbpu.smartpicture.service;

import com.hbpu.smartpicture.common.BaseResponse;
import com.hbpu.smartpicture.model.dto.UserLoginDTO;
import com.hbpu.smartpicture.model.dto.UserRegisterDTO;
import com.hbpu.smartpicture.model.pojo.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hbpu.smartpicture.model.vo.UserLoginVO;
import jakarta.servlet.http.HttpServletRequest;

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

    /**
     * 用户登录方法
     * @param userAccount 用户账号
     * @param userPassword 用户密码
     * @return 用户登录响应封装类
     */
    UserLoginVO userLogin(String userAccount, String userPassword);

    /**
     * 获取当前登录用户
     * @param request 用户请求
     * @return 当前登录用户封装类
     */
    User getCurrentUser(HttpServletRequest request);

    /**
     * 用户注销方法
     * @param request 用户请求
     * @return 注销成功返回true
     */
    Boolean loginOut(HttpServletRequest request);
}
