package com.hbpu.smartpicture.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hbpu.smartpicture.exception.BusinessException;
import com.hbpu.smartpicture.exception.ErrorCode;
import com.hbpu.smartpicture.exception.ThrowUtils;
import com.hbpu.smartpicture.mapper.UserMapper;
import com.hbpu.smartpicture.model.dto.UserRegisterDTO;
import com.hbpu.smartpicture.model.enums.UserRoleEnum;
import com.hbpu.smartpicture.service.UserService;
import com.hbpu.smartpicture.model.pojo.User;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.nio.charset.Charset;

/**
 * @author 马可
 * @description 针对表【user(用户)】的数据库操作Service实现
 * @createDate 2025-11-10 17:42:36
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    /**
     * 用户注册方法具体实现
     *
     * @param userRegisterDTO 用户注册请求封装对象
     * @return 返回注册成功后的用户id
     */
    @Override
    public Long userRegister(UserRegisterDTO userRegisterDTO) {
        //先校验参数
        //判断参数中是否有空字符串，如果有直接抛出业务异常
        ThrowUtils.throwIf(
                StrUtil.hasBlank(
                        userRegisterDTO.getUserAccount(),
                        userRegisterDTO.getUserPassword(),
                        userRegisterDTO.getCheckPassword()
                ),
                new BusinessException(ErrorCode.PARAMS_ERROR)
        );
        //判断用户账号是否过短
        ThrowUtils.throwIf(
                userRegisterDTO.getUserAccount().length() < 4,
                new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短")
        );
        //判断用户的密码格式是否正确
        ThrowUtils.throwIf(
                userRegisterDTO.getUserPassword().length() < 8 || userRegisterDTO.getCheckPassword().length() < 8,
                new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度过短！")
        );
        //判断用户两次密码是否一致
        ThrowUtils.throwIf(
                !userRegisterDTO.getUserPassword().equals(userRegisterDTO.getCheckPassword()),
                new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致！")
        );
        //检查用户账号是否重复
        Long result = this.baseMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(
                        User::getUserAccount,
                        userRegisterDTO.getUserAccount()
                )
        );
        //从数据库查询到的数据大于1就说明有重复
        ThrowUtils.throwIf(result > 0, new BusinessException(ErrorCode.PARAMS_ERROR, "账户已经被注册！"));
        //将密码加密
        User user = new User();
        user.setUserAccount(userRegisterDTO.getUserAccount());
        user.setUserPassword(getEncryptedPassword(userRegisterDTO.getUserPassword()));
        user.setUserName("用户：" + userRegisterDTO.getUserAccount());
        user.setUserRole(UserRoleEnum.USER.getValue());
        //将数据插入数据库中
        ThrowUtils.throwIf(!this.save(user), new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败！数据库插入失败！"));
        //主键回填给user对象
        return user.getId();
    }

    /**
     * 获取加密后的密码
     *
     * @param password 要加密的密码
     * @return 返回加密后的密码
     */
    private String getEncryptedPassword(String password) {
        final String SALT = "Kefan";
        return DigestUtils.md5DigestAsHex((SALT + password).getBytes());
    }
}




