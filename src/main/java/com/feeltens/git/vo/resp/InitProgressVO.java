package com.feeltens.git.vo.resp;

import com.feeltens.git.enums.InitProgressStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 初始化进度信息
 *
 * @author feeltens
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InitProgressVO {

    /**
     * 会话ID
     */
    private String sessionId;

    /**
     * 进度状态
     */
    private InitProgressStatus status;

    /**
     * 进度值 (0-100)
     */
    private Integer progress;

    /**
     * 当前步骤描述
     */
    private String currentStep;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 冲突文件总数
     */
    private Integer totalFiles;

    /**
     * 已处理文件数
     */
    private Integer processedFiles;

    /**
     * 预估剩余时间(毫秒)
     */
    private Long estimatedTime;

}