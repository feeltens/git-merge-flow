package com.feeltens.git.dto.conflict;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Git 凭证
 *
 * @author feeltens
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GitCredentials {

    /**
     * 用户名（通常为 oauth2 或实际用户名）
     */
    private String username;

    /**
     * 密码或访问令牌
     */
    private String password;

}