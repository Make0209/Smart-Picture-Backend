package com.hbpu.smartpicture.api.aliyunai.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 查询任务响应类
 */
@Schema(description = "查询任务响应类")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetOutPaintingTaskResponse {

    /**
     * 请求唯一标识
     */
    @Schema(description = "请求唯一标识")
    private String requestId;

    /**
     * 输出信息
     */
    @Schema(description = "输出信息")
    private Output output;

    /**
     * 表示任务的输出信息
     */
    @Schema(description = "表示任务的输出信息")
    @Data
    public static class Output {

        /**
         * 任务 ID
         */
        @Schema(description = "任务 ID")
        private String taskId;

        /**
         * 任务状态
         * <ul>
         *     <li>PENDING：排队中</li>
         *     <li>RUNNING：处理中</li>
         *     <li>SUSPENDED：挂起</li>
         *     <li>SUCCEEDED：执行成功</li>
         *     <li>FAILED：执行失败</li>
         *     <li>UNKNOWN：任务不存在或状态未知</li>
         * </ul>
         */
        @Schema(description = "任务状态 <ul>     <li>PENDING：排队中</li>     <li>RUNNING：处理中</li>     <li>SUSPENDED：挂起</li>     <li>SUCCEEDED：执行成功</li>     <li>FAILED：执行失败</li>     <li>UNKNOWN：任务不存在或状态未知</li> </ul>")
        private String taskStatus;

        /**
         * 提交时间
         * 格式：YYYY-MM-DD HH:mm:ss.SSS
         */
        @Schema(description = "提交时间 格式：YYYY-MM-DD HH:mm:ss.SSS")
        private String submitTime;

        /**
         * 调度时间
         * 格式：YYYY-MM-DD HH:mm:ss.SSS
         */
        @Schema(description = "调度时间 格式：YYYY-MM-DD HH:mm:ss.SSS")
        private String scheduledTime;

        /**
         * 结束时间
         * 格式：YYYY-MM-DD HH:mm:ss.SSS
         */
        @Schema(description = "结束时间 格式：YYYY-MM-DD HH:mm:ss.SSS")
        private String endTime;

        /**
         * 输出图像的 URL
         */
        @Schema(description = "输出图像的 URL")
        private String outputImageUrl;

        /**
         * 接口错误码
         * <p>接口成功请求不会返回该参数</p>
         */
        @Schema(description = "接口错误码 <p>接口成功请求不会返回该参数</p>")
        private String code;

        /**
         * 接口错误信息
         * <p>接口成功请求不会返回该参数</p>
         */
        @Schema(description = "接口错误信息 <p>接口成功请求不会返回该参数</p>")
        private String message;

        /**
         * 任务指标信息
         */
        @Schema(description = "任务指标信息")
        private TaskMetrics taskMetrics;
    }

    /**
     * 表示任务的统计信息
     */
    @Schema(description = "表示任务的统计信息")
    @Data
    public static class TaskMetrics {

        /**
         * 总任务数
         */
        @Schema(description = "总任务数")
        private Integer total;

        /**
         * 成功任务数
         */
        @Schema(description = "成功任务数")
        private Integer succeeded;

        /**
         * 失败任务数
         */
        @Schema(description = "失败任务数")
        private Integer failed;
    }
}

