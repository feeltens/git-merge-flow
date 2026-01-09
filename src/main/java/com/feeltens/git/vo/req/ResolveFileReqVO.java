package com.feeltens.git.vo.req;

import lombok.Data;

/**
 * 解决文件请求
 *
 * @author feeltens
 */
@Data
public class ResolveFileReqVO {

    /**
     * 文件路径
     */
    private String filePath;

    /**
     * 解决后的完整内容
     */
    private String resolvedContent;

}