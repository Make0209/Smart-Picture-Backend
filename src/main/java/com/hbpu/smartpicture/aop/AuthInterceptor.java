package com.hbpu.smartpicture.aop;

import com.hbpu.smartpicture.annotation.AuthCheck;
import com.hbpu.smartpicture.exception.BusinessException;
import com.hbpu.smartpicture.exception.ErrorCode;
import com.hbpu.smartpicture.exception.ThrowUtils;
import com.hbpu.smartpicture.model.enums.UserRoleEnum;
import com.hbpu.smartpicture.model.pojo.User;
import com.hbpu.smartpicture.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
public class AuthInterceptor {
    private final UserService userService;

    public AuthInterceptor(UserService userService) {
        this.userService = userService;
    }

    @Around("@annotation(authCheck)")
    public Object doIntercept(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        //根据注解类返回的值确定当前方法所需要的权限值
        String role = authCheck.mustRole();
        //根据值去用户权限枚举类找对应的权限枚举类对象
        UserRoleEnum value = UserRoleEnum.findByValue(role);
        //如果没有则表明当前方法不需要权限即可访问，直接放行
        if (value == null) {
            return joinPoint.proceed();
        }
        //从全局的request上下文获取请求属性
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        //将请求属性类型转换成Servlet并获取request用户请求
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        //根据请求获取当前用户
        User currentUser = userService.getCurrentUser(request);
        //利用当前用户信息取得当前用户的权限值
        String userRole = currentUser.getUserRole();
        //根据用户权限值获得对应的权限枚举类对象
        UserRoleEnum userRoleEnum = UserRoleEnum.findByValue(userRole);
        //根据获取的用户权限值，直接去枚举类找是否有相应权限，没有则表明无权限
        ThrowUtils.throwIf(
                userRoleEnum == null,
                new BusinessException(ErrorCode.NO_AUTH_ERROR)
        );
        //如果方法需要管理员权限，则进行判断
        ThrowUtils.throwIf(
                UserRoleEnum.ADMIN.equals(value) && !UserRoleEnum.ADMIN.equals(userRoleEnum),
                new BusinessException(ErrorCode.NO_AUTH_ERROR)
        );
        //放行继续执行方法
        return joinPoint.proceed();
    }
}
