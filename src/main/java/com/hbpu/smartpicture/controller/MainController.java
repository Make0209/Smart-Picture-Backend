package com.hbpu.smartpicture.controller;

import com.hbpu.smartpicture.common.BaseResponse;
import com.hbpu.smartpicture.common.ResultUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "MainController")
@RestController
@RequestMapping("/")
public class MainController {

    /**
     * 健康检查，判断当前服务是否正常
     * @return 当前服务运行状态
     */
    @Operation(summary = "健康检查，判断当前服务是否正常", description = "健康检查，判断当前服务是否正常")
    @GetMapping("/health")
    public BaseResponse<String> health() {
        return ResultUtils.success("ok！");
    }


}
