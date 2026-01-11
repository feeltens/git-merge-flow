package com.feeltens.git.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.feeltens.git.common.exception.BizException;
import com.feeltens.git.entity.GitProjectDO;
import com.feeltens.git.entity.SysUserDO;
import com.feeltens.git.entity.SysUserProjectPermDO;
import com.feeltens.git.entity.SysUserRoleDO;
import com.feeltens.git.enums.RoleEnum;
import com.feeltens.git.mapper.GitProjectMapper;
import com.feeltens.git.mapper.SysUserMapper;
import com.feeltens.git.mapper.SysUserProjectPermMapper;
import com.feeltens.git.mapper.SysUserRoleMapper;
import com.feeltens.git.service.UserService;
import com.feeltens.git.vo.base.PageRequest;
import com.feeltens.git.vo.base.PageResponse;
import com.feeltens.git.vo.req.AddUserReqVO;
import com.feeltens.git.vo.req.LoginReqVO;
import com.feeltens.git.vo.req.PageUserReqVO;
import com.feeltens.git.vo.req.UpdatePasswordReqVO;
import com.feeltens.git.vo.req.UpdateUserProjectPermReqVO;
import com.feeltens.git.vo.req.UpdateUserReqVO;
import com.feeltens.git.vo.resp.LoginRespVO;
import com.feeltens.git.vo.resp.ProjectSimpleVO;
import com.feeltens.git.vo.resp.UserInfoRespVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户服务实现
 *
 * @author feeltens
 * @date 2026-01-11
 */
@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Resource
    private SysUserMapper sysUserMapper;

    @Resource
    private SysUserRoleMapper sysUserRoleMapper;

    @Resource
    private SysUserProjectPermMapper sysUserProjectPermMapper;

    @Resource
    private GitProjectMapper gitProjectMapper;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public LoginRespVO login(LoginReqVO req) {
        if (!StringUtils.hasText(req.getUsername()) || !StringUtils.hasText(req.getPassword())) {
            throw new BizException("用户名和密码不能为空");
        }

        SysUserDO user = sysUserMapper.selectByUsername(req.getUsername());
        if (user == null) {
            throw new BizException("用户名或密码错误");
        }

        if (user.getStatus() != 1) {
            throw new BizException("用户已被禁用");
        }

        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new BizException("用户名或密码错误");
        }

        // 登录成功，生成token
        StpUtil.login(user.getUserId());

        List<String> roles = sysUserRoleMapper.selectRoleCodesByUserId(user.getUserId());
        boolean isAdmin = roles.contains(RoleEnum.ADMIN.getCode());

        return LoginRespVO.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .tokenValue(StpUtil.getTokenValue())
                .tokenName(StpUtil.getTokenName())
                .roles(roles)
                .isAdmin(isAdmin)
                .build();
    }

    @Override
    public void logout() {
        StpUtil.logout();
    }

    @Override
    public UserInfoRespVO getCurrentUserInfo() {
        Long userId = StpUtil.getLoginIdAsLong();
        SysUserDO user = sysUserMapper.selectByUserId(userId);
        if (user == null) {
            throw new BizException("用户不存在");
        }
        return convertToUserInfoRespVO(user);
    }

    @Override
    public PageResponse<UserInfoRespVO> pageUser(PageRequest<PageUserReqVO> req) {
        int offset = (req.getCurrentPage() - 1) * req.getPageSize();
        List<SysUserDO> users = sysUserMapper.selectPageList(req.getParam(), offset, req.getPageSize());
        Long total = sysUserMapper.selectCount(req.getParam());

        List<UserInfoRespVO> records = users.stream()
                .map(this::convertToUserInfoRespVO)
                .collect(Collectors.toList());

        return PageResponse.build(records, req.getCurrentPage(), req.getPageSize(), total);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addUser(AddUserReqVO req) {
        if (!StringUtils.hasText(req.getUsername())) {
            throw new BizException("用户名不能为空");
        }
        if (!StringUtils.hasText(req.getPassword())) {
            throw new BizException("密码不能为空");
        }
        if (!StringUtils.hasText(req.getNickname())) {
            throw new BizException("用户昵称不能为空");
        }

        // 检查用户名是否已存在
        SysUserDO existUser = sysUserMapper.selectByUsername(req.getUsername());
        if (existUser != null) {
            throw new BizException("用户名已存在");
        }
        // 检查昵称是否已存在
        SysUserDO existNicknameUser = sysUserMapper.selectByNickname(req.getNickname());
        if (existNicknameUser != null) {
            throw new BizException("昵称已存在");
        }

        // 创建用户
        SysUserDO user = SysUserDO.builder()
                .username(req.getUsername())
                .password(passwordEncoder.encode(req.getPassword()))
                .nickname(req.getNickname())
                .email(req.getEmail())
                .status(1)
                .createBy(req.getOperator())
                .updateBy(req.getOperator())
                .build();
        sysUserMapper.insert(user);

        // 设置角色
        List<String> roleCodes = req.getRoleCodes();
        if (CollectionUtils.isEmpty(roleCodes)) {
            roleCodes = new ArrayList<>();
            roleCodes.add(RoleEnum.USER.getCode());
        }
        saveUserRoles(user.getUserId(), roleCodes);

        // 保存项目权限（非管理员）
        if (!roleCodes.contains(RoleEnum.ADMIN.getCode())) {
            saveUserProjectPerms(user.getUserId(), req.getProjectIds(), req.getOperator());
        }

        log.info("新增用户成功, userId:{}, username:{}, operator:{}", user.getUserId(), user.getUsername(), req.getOperator());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(UpdateUserReqVO req) {
        if (req.getUserId() == null) {
            throw new BizException("用户ID不能为空");
        }

        SysUserDO user = sysUserMapper.selectByUserId(req.getUserId());
        if (user == null) {
            throw new BizException("用户不存在");
        }

        // 更新用户基本信息
        SysUserDO updateUser = SysUserDO.builder()
                .userId(req.getUserId())
                .nickname(req.getNickname())
                .email(req.getEmail())
                .status(req.getStatus())
                .updateBy(req.getOperator())
                .build();
        sysUserMapper.updateById(updateUser);

        // 更新角色
        if (!CollectionUtils.isEmpty(req.getRoleCodes())) {
            sysUserRoleMapper.deleteByUserId(req.getUserId());
            saveUserRoles(req.getUserId(), req.getRoleCodes());
        }

        // 更新项目权限
        sysUserProjectPermMapper.deleteByUserId(req.getUserId());
        if (!CollectionUtils.isEmpty(req.getRoleCodes()) && !req.getRoleCodes().contains(RoleEnum.ADMIN.getCode())) {
            saveUserProjectPerms(req.getUserId(), req.getProjectIds(), req.getOperator());
        }

        log.info("更新用户成功, userId:{}, operator:{}", req.getUserId(), req.getOperator());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long userId, String operator) {
        if (userId == null) {
            throw new BizException("用户ID不能为空");
        }

        SysUserDO user = sysUserMapper.selectByUserId(userId);
        if (user == null) {
            throw new BizException("用户不存在");
        }

        // 不能删除admin用户
        if ("admin".equals(user.getUsername())) {
            throw new BizException("不能删除管理员账号");
        }

        sysUserMapper.deleteById(userId, operator);
        sysUserRoleMapper.deleteByUserId(userId);
        sysUserProjectPermMapper.deleteByUserId(userId);

        // 强制下线
        StpUtil.logout(userId);

        log.info("删除用户成功, userId:{}, operator:{}", userId, operator);
    }

    @Override
    public void updateMyPassword(UpdatePasswordReqVO req) {
        Long userId = StpUtil.getLoginIdAsLong();
        SysUserDO user = sysUserMapper.selectByUserId(userId);
        if (user == null) {
            throw new BizException("用户不存在");
        }

        if (!StringUtils.hasText(req.getOldPassword())) {
            throw new BizException("旧密码不能为空");
        }
        if (!StringUtils.hasText(req.getNewPassword())) {
            throw new BizException("新密码不能为空");
        }

        // 验证旧密码
        if (!passwordEncoder.matches(req.getOldPassword(), user.getPassword())) {
            throw new BizException("旧密码错误");
        }

        // 更新密码
        sysUserMapper.updatePassword(userId, passwordEncoder.encode(req.getNewPassword()), user.getUsername());

        log.info("用户修改密码成功, userId:{}", userId);
    }

    @Override
    public void resetPassword(UpdatePasswordReqVO req) {
        if (req.getUserId() == null) {
            throw new BizException("用户ID不能为空");
        }
        if (!StringUtils.hasText(req.getNewPassword())) {
            throw new BizException("新密码不能为空");
        }

        SysUserDO user = sysUserMapper.selectByUserId(req.getUserId());
        if (user == null) {
            throw new BizException("用户不存在");
        }

        sysUserMapper.updatePassword(req.getUserId(), passwordEncoder.encode(req.getNewPassword()), req.getOperator());

        log.info("管理员重置密码成功, userId:{}, operator:{}", req.getUserId(), req.getOperator());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserProjectPerm(UpdateUserProjectPermReqVO req) {
        if (req.getUserId() == null) {
            throw new BizException("用户ID不能为空");
        }

        // 先删除原有权限
        sysUserProjectPermMapper.deleteByUserId(req.getUserId());

        // 添加新权限
        if (!CollectionUtils.isEmpty(req.getProjectIds())) {
            List<SysUserProjectPermDO> permList = req.getProjectIds().stream()
                    .map(projectId -> SysUserProjectPermDO.builder()
                            .userId(req.getUserId())
                            .projectId(projectId)
                            .permType(req.getPermType() != null ? req.getPermType() : "READ_WRITE")
                            .createBy(req.getOperator())
                            .build())
                    .collect(Collectors.toList());
            sysUserProjectPermMapper.batchInsert(permList);
        }

        log.info("更新用户工程权限成功, userId:{}, projectIds:{}, operator:{}",
                req.getUserId(), req.getProjectIds(), req.getOperator());
    }

    @Override
    public boolean isAdmin(Long userId) {
        List<String> roles = sysUserRoleMapper.selectRoleCodesByUserId(userId);
        return roles.contains(RoleEnum.ADMIN.getCode());
    }

    private void saveUserRoles(Long userId, List<String> roleCodes) {
        if (CollectionUtils.isEmpty(roleCodes)) {
            return;
        }
        List<SysUserRoleDO> roleList = roleCodes.stream()
                .map(roleCode -> SysUserRoleDO.builder()
                        .userId(userId)
                        .roleCode(roleCode)
                        .build())
                .collect(Collectors.toList());
        sysUserRoleMapper.batchInsert(roleList);
    }

    private void saveUserProjectPerms(Long userId, List<Long> projectIds, String operator) {
        if (CollectionUtils.isEmpty(projectIds)) {
            return;
        }
        Date now = new Date();
        for (Long projectId : projectIds) {
            SysUserProjectPermDO perm = new SysUserProjectPermDO();
            perm.setUserId(userId);
            perm.setProjectId(projectId);
            perm.setPermType("ALL");
            perm.setCreateTime(now);
            perm.setCreateBy(operator);
            sysUserProjectPermMapper.insert(perm);
        }
    }

    @Override
    public List<ProjectSimpleVO> getAllProjects() {
        List<GitProjectDO> list = gitProjectMapper.selectAll();
        return list.stream()
                .map(p -> new ProjectSimpleVO(p.getProjectId(), p.getProjectName()))
                .collect(Collectors.toList());
    }

    private UserInfoRespVO convertToUserInfoRespVO(SysUserDO user) {
        List<String> roleCodes = sysUserRoleMapper.selectRoleCodesByUserId(user.getUserId());
        List<String> roleDescs = roleCodes.stream()
                .map(code -> {
                    if (RoleEnum.ADMIN.getCode().equals(code)) {
                        return RoleEnum.ADMIN.getDesc();
                    } else if (RoleEnum.USER.getCode().equals(code)) {
                        return RoleEnum.USER.getDesc();
                    }
                    return code;
                })
                .collect(Collectors.toList());

        // 查询项目权限
        List<Long> projectIds = sysUserProjectPermMapper.selectProjectIdsByUserId(user.getUserId());

        return UserInfoRespVO.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .status(user.getStatus())
                .statusDesc(user.getStatus() == 1 ? "启用" : "禁用")
                .roleCodes(roleCodes)
                .roleDescs(roleDescs)
                .isAdmin(roleCodes.contains(RoleEnum.ADMIN.getCode()))
                .createTime(user.getCreateTime())
                .updateTime(user.getUpdateTime())
                .projectIds(projectIds)
                .build();
    }

}