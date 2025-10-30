# Git Merge Flow (合流管理系统)

## 项目简介

git-merge-flow 合流管理系统是一个基于 Spring Boot + Thymeleaf + Vue.js 的单体工程，
主要功能是解决 GitLab、阿里云 CodeUp 等 Git 仓库的中间分支合并查询等相关问题。
基于 GitLab(v18.3)、CodeUp 提供的官方 API，使用 Java 实现对 Git 分支的管理。

## 效果图

主图：
![git-merge-flow效果图](doc/img/git_main.jpg)

最核心的功能：【重新合并】  
内部方法逻辑：
![【重新合并】内部方法逻辑](doc/img/git_remergeMixBranch.jpg)

主界面：
![主界面](doc/img/git_01.jpg)

填写操作人：
![填写操作人](doc/img/git_02.jpg)

Git组织管理：
![Git组织管理](doc/img/git_03.jpg)

Git工程管理：
![Git工程管理](doc/img/git_04.jpg)

添加Git工程：
![添加Git工程](doc/img/git_05.jpg)

Git分支管理：新建分支、拉取远程分支、清理分支
![Git分支管理](doc/img/git_06.jpg)

新建Git分支：
![新建Git分支](doc/img/git_07.jpg)

Git中间分支管理：
![Git中间分支管理](doc/img/git_08.jpg)

添加分支到中间分支：
![添加分支到中间分支](doc/img/git_09.jpg)

自动合并失败，需要手动处理冲突：
![自动合并失败，需要手动处理冲突](doc/img/git_10.jpg)

手动处理冲突shell脚本：
![手动处理冲突shell脚本](doc/img/git_11.jpg)

从中间分支退出分支：  
注意，这将重建中间分支，然后把当前集成的分支列表依次逐一合并到中间分支里，可能需要重新处理代码冲突！
![从中间分支退出分支](doc/img/git_12.jpg)


## 快速开始

### 环境要求

- JDK 1.8+
- MySQL 8.0+
- Maven 3.6+

### 数据库初始化

创建数据库，并初始化数据表  
sql脚本位置：
doc/sql/git_merge_flow.sql

### Git open API配置

codeup 个人访问令牌  
https://account-devops.aliyun.com/settings/personalAccessToken

gitlab 个人访问令牌  
http://192.168.111.221/-/user_settings/personal_access_tokens


在 `application.yml` 中配置open API相关参数：

```yaml
merge-flow:
  # git服务提供方：gitlab、codeup
  gitService: gitlab
  # gitService: codeup

# GitLab API 配置
gitlab:
  api:
    # gitlab api 基础url
    # baseUrl: http://192.168.111.221
    baseUrl: set-your-gitlab-base-url
    # gitlab api 访问令牌
    # http://192.168.111.221/-/user_settings/personal_access_tokens
    accessToken: set-your-gitlab-access-token

# CodeUp API 配置
codeup:
  api:
    # codeup api 基础url
    baseUrl: https://openapi-rdc.aliyuncs.com
    # codeup api 访问令牌
    # https://account-devops.aliyun.com/settings/personalAccessToken
    accessToken: set-your-codeup-access-token
```

### 配置文件

修改 `src/main/resources/application.yml` 中的数据库连接配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/git_merge_flow?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: your_username
    password: your_password
```

### 运行项目

```shell
# 编译打包
mvn clean package

# 运行项目
java -jar target/git-merge-flow-1.0-SNAPSHOT.jar

# 或者直接运行主类
mvn spring-boot:run
```

启动jar时，指定参数：
```shell
java -jar app.jar \
--merge-flow.gitService=gitlab \
--gitlab.api.baseUrl=your-base-url \
--gitlab.api.accessToken=you-gitlab-access-token \
--codeup.api.accessToken=you-codeup-access-token
```

在idea里启动时，指定参数：
```
--merge-flow.gitService=gitlab
--gitlab.api.baseUrl=your-base-url
--gitlab.api.accessToken=you-gitlab-access-token
--codeup.api.accessToken=you-codeup-access-token
```

### 访问地址

项目启动后，访问：  
http://localhost:18081


## Q&A

1. git-merge-flow 的定位是什么？有哪些应用场景？  
定位：管理多个Git工程，多个环境，多个分支管理的中间分支，用于多个分支部署在同一环境的问题。
小而美的工具，而非各种功能都要集成的富工具。当然，基于开源，也可以支持做二次开发和集成。比如集成jenkins、钉钉邮件通知等，进一步接入CI、CD等DevOps。  
解决的痛点问题：多个分支共用同一套环境。  
应用场景：企业或团队内部，在降本增效的趋势下，往往有多个需求并行、多个代码分支、多个测试任务并行，但基于只有一套环境下，每次手动合并多个分支到中间分支，非常麻烦。


2. 除了支持 GitLab、阿里云CodeUp，是否考虑支持其他git服务平台？  
暂不考虑。  
个人看法：  
自建gitlab，是大部分公司选择的方案。  
阿里云的CodeUp，是公司不自建git服务的第二选择。  
GitHub，经常用于开源组织、个人开源项目，目前个人使用GitHub很少有需要多个分支合并到一起的场景。


3. 支持的环境有 dev、test、pre，支持其他环境吗？  
环境枚举在 EnvEnum，可以根据具体使用场景进行增删改。


4. 中间分支名是 dev_mix、test_mix、pre_mix，可以自定义吗？  
完全可以，按团队实际需要，自行修改。  
方法在 com.feeltens.git.service.impl.GitFlowServiceImpl.getMixBranchName


5. 为什么会有【git组织管理】模块？  
阿里云的CodeUp，允许同一用户归属不同的git组织。  
所以为了统一多个git服务平台的模型，而添加了【git组织】这个模型和菜单管理模块。  
在git-merge-flow的设计里，gitlab默认的组织名是default。


6. 为什么只有填写操作人这么简单的配置，没有常见的注册、登录功能？  
git-merge-flow 的定位是专注于解决中间分支合并问题的小而美的工具。（虽然它已经经历过个人、团队、公司内测，但仍然有变美的空间。）  
所以只是填写操作人，为了记录操作日志。当然，也可以自行添加登录功能。  
再者，目前没有实现操作记录审计功能，也可以自行添加审计。

## 技术栈

- **后端框架**: Spring Boot 2.2.1.RELEASE
- **模板引擎**: Thymeleaf
- **前端框架**: Vue.js 2.6 + Element UI 2.15
- **数据库**: MySQL 8.0
- **ORM框架**: MyBatis
- **构建工具**: Maven
- **JDK版本**: 1.8

## 核心功能

### 1. 多工程多环境支持
- 支持多个Git工程（如order、pay等）
- 支持多环境（dev、test、pre）的代码分支管理

### 2. Git工程配置
- 分页列表展示，支持模糊查询
- 新建、编辑、删除Git工程
- 自动验证仓库连接
- 自动同步远程分支信息

### 3. Git分支管理
- 分页展示Git工程的所有分支
- 基于GitLab、CodeUp OpenAPI拉取最新分支信息
- 支持创建新分支
- 自动同步分支提交信息

### 4. 中间分支管理
- 支持dev/test/pre三个环境的中间分支管理
- 分支合并状态跟踪
- 支持手动合并冲突解决
- 批量操作支持

## 系统界面

### 主页面
- 左侧固定菜单：工作台、Git工程配置、Git分支管理、Dev中间分支、Test中间分支、Pre中间分支

### Git工程配置
- 支持分页查询和模糊搜索
- 新建工程时自动验证仓库连接
- 编辑时仓库地址不可修改
- 删除时级联删除相关数据

### Git分支管理
- 左右分栏布局
- 左侧显示工程列表，右侧显示分支详情
- 支持创建新分支
- 自动同步远程分支信息

### 中间分支管理
- 三个环境独立管理
- 支持分支合并操作
- 冲突处理指导
- 批量操作支持

## API文档

## 配置说明

### 数据库表结构

项目包含以下核心表：

1. `git_organization` - git组织表
2. `git_project` - git工程表
3. `git_branch` - git原始分支表
4. `git_mix_branch` - git中间分支表
5. `git_mix_branch_item` - git中间分支条目表

详细表结构请参考 `src/main/resources/sql/git_merge_flow.sql`

## 开发指南

### 项目结构

```
src/
├── main/
│   ├── java/
│   │   └── com/feeltens/git/
│   │       ├── GitMergeFlowApplication.java  # 启动类
│   │       ├── common/                          # 通用类
│   │       ├── controller/                      # 控制器
│   │       ├── entity/                          # 实体类
│   │       ├── mapper/                          # Mapper接口
│   │       └── service/                         # 服务类
│   └── resources/
│       ├── mapper/                              # MyBatis映射文件
│       ├── sql/                                 # SQL脚本
│       ├── templates/                           # Thymeleaf模板
│       └── application.yml                      # 配置文件
```

### 扩展开发

1. 新增功能模块时，按照现有的Controller-Service-Mapper架构
2. 前端页面使用Thymeleaf+Vue.js+Element UI技术栈
3. 数据库操作统一使用MyBatis
4. API接口统一返回Result格式

## 注意事项

1. 首次使用需要配置GitLab、CodeUp的访问凭证
2. 确保数据库连接正常
3. 分支合并操作需要有相应的Git仓库权限
4. 建议在测试环境先验证功能

## 参考资料

favicon 来源于 Araxis Merge 软件。

## 开源许可证

基于 GPL 协议开源
