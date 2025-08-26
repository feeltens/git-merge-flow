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

}