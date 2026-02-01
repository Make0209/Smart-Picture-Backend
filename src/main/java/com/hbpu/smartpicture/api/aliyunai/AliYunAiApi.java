package com.hbpu.smartpicture.api.aliyunai;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.ContentType;
import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.hbpu.smartpicture.api.aliyunai.model.CreateOutPaintingTaskRequest;
import com.hbpu.smartpicture.api.aliyunai.model.CreateOutPaintingTaskResponse;
import com.hbpu.smartpicture.api.aliyunai.model.GetOutPaintingTaskResponse;
import com.hbpu.smartpicture.exception.BusinessException;
import com.hbpu.smartpicture.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AliYunAiApi {

    // 阿里云AI apiKey
    @Value("${aliYunAi.apiKey}")
    private String apiKey;

    // 创建任务地址
    public static final String CREATE_OUT_PAINTING_TASK_URL = "https://dashscope.aliyuncs.com/api/v1/services/aigc/image2image/out-painting";

    // 查询任务状态
    public static final String GET_OUT_PAINTING_TASK_URL = "https://dashscope.aliyuncs.com/api/v1/tasks/%s";

    /**
     * 创建任务
     *
     * @param createOutPaintingTaskRequest 创建任务请求参数
     * @return 创建任务响应参数
     */
    public CreateOutPaintingTaskResponse createOutPaintingTask(CreateOutPaintingTaskRequest createOutPaintingTaskRequest) {
        if (createOutPaintingTaskRequest == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "扩图参数为空");
        }
        // 发送请求
        HttpRequest httpRequest = HttpRequest.post(CREATE_OUT_PAINTING_TASK_URL) // 创建 POST 请求
                                             .header(Header.AUTHORIZATION, "Bearer " + apiKey) // 设置授权头
                                             // 必须开启异步处理，设置为enable。
                                             .header("X-DashScope-Async", "enable") // 设置异步处理
                                             .header(Header.CONTENT_TYPE, ContentType.JSON.getValue()) // 设置内容类型为 JSON
                                             .body(JSONUtil.toJsonStr(createOutPaintingTaskRequest)); // 设置请求体
        // 获取响应结果
        try (HttpResponse httpResponse = httpRequest.execute()) {
            // 判断响应状态码是否为 2xx，即请求是否成功，不是则记录错误日志
            if (!httpResponse.isOk()) {
                log.error("请求异常：{}", httpResponse.body());
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "AI 扩图失败");
            }
            // 解析响应结果
            CreateOutPaintingTaskResponse response = JSONUtil.toBean(
                    httpResponse.body(), CreateOutPaintingTaskResponse.class);
            // 判断接口是否成功调用
            String errorCode = response.getCode();
            // 如果接口调用失败，则记录错误日志
            if (StrUtil.isNotBlank(errorCode)) {
                String errorMessage = response.getMessage();
                log.error("AI 扩图失败，errorCode:{}, errorMessage:{}", errorCode, errorMessage);
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "AI 扩图接口响应异常");
            }
            return response;
        }
    }

    /**
     * 查询创建的任务
     *
     * @param taskId 任务 id
     * @return 查询任务响应参数
     */
    public GetOutPaintingTaskResponse getOutPaintingTask(String taskId) {
        // 任务 id 不能为空
        if (StrUtil.isBlank(taskId)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "任务 id 不能为空");
        }
        // 发送请求
        try (HttpResponse httpResponse = HttpRequest.get(String.format(GET_OUT_PAINTING_TASK_URL, taskId))
                                                    .header(Header.AUTHORIZATION, "Bearer " + apiKey)
                                                    .execute()) {
            // 判断响应状态码是否为 2xx，即请求是否成功
            if (!httpResponse.isOk()) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取任务失败");
            }
            // 解析响应结果
            return JSONUtil.toBean(httpResponse.body(), GetOutPaintingTaskResponse.class);
        }
    }
}

