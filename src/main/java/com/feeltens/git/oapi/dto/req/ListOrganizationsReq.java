package com.feeltens.git.oapi.dto.req;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

/**
 * ListOrganizations - 查询组织列表 入参
 */
@NoArgsConstructor
@Data
@Builder(toBuilder = true)
@FieldNameConstants
public class ListOrganizationsReq extends OapiBaseReq {

}