package com.feeltens.git.oapi.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

import java.util.List;

/**
 * GetCompare - 查询代码比较内容 出参
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder(toBuilder = true)
@FieldNameConstants
public class GetCompareResp {

    /*
    {
        "commits": [],
        "diffs": [],
        "message": "没有比较内容",
        "messages": [
            "没有比较内容",
            "来源和目标比较没有差异"
        ]
    }
    */
    private List<Object> commits;

    /**
     * 文件变更列表（用于稀疏检出优化）
     */
    private List<DiffFile> diffs;

    /**
     * 统一的文件变更信息
     */
    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    @Builder
    public static class DiffFile {
        /**
         * 旧文件路径
         */
        private String oldPath;

        /**
         * 新文件路径
         */
        private String newPath;

        /**
         * diff内容（可选，用于判断冲突）
         */
        private String diff;

        /**
         * 是否新文件
         */
        private Boolean newFile;

        /**
         * 是否删除文件
         */
        private Boolean deletedFile;

        /**
         * 是否重命名文件
         */
        private Boolean renamedFile;

        /**
         * 是否二进制文件
         */
        private Boolean binary;
    }

}