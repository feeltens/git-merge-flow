# git-merge-flow (合流管理系统)

## 项目简介

git-merge-flow (合流管理系统)是一个基于 Spring Boot + Thymeleaf + Vue.js 的单体工程，
主要功能是解决 GitLab、阿里云 CodeUp 等 Git 仓库的中间分支合并查询等相关问题。
基于 GitLab(v18.3)、CodeUp 提供的官方 API，使用 Java 实现对 Git 分支的管理。


英文名git-merge-flow释义：  
git是操作对象；  
merge有合并之义；  
flow即每次【重新合并】，都会把当前集成分支（包括主分支）重新合并到中间分支一遍，就像一段可持续的流程。

中文名【合流】释义：  
就像河流的各个分支汇聚到河流主干，git-merge-flow的核心功能就是合并git分支到主干河流(中间分支)。  


## 效果图

主图：
![git-merge-flow效果图](doc/img/git_main.jpg)

[更多效果图](system_show.md)

## 最核心的功能：【重新合并】  

内部方法逻辑：
![【重新合并】内部方法逻辑](doc/img/git_remergeMixBranch.jpg)


## 快速开始

### 环境要求

- JDK 1.8
- MySQL 8.0
- Maven 3.6+

### 数据库初始化

创建数据库，并初始化数据表  
sql脚本位置：
doc/sql/git_merge_flow.sql

### Git open API配置

gitlab 个人访问令牌  (以 gitlab 私服地址为 http://192.168.111.221/ 举例)
http://192.168.111.221/-/user_settings/personal_access_tokens

codeup 个人访问令牌  
https://account-devops.aliyun.com/settings/personalAccessToken


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
小而美的工具，而非各种功能都要集成的富工具。  
当然，基于开源，也可以支持做二次开发和集成。比如集成jenkins、钉钉邮件通知等，进一步接入CI、CD等DevOps。  
解决的痛点问题：多个分支共用同一套环境。  
应用场景：企业或团队内部，在降本增效的趋势下，往往有多个需求并行、多个代码分支、多个测试任务并行，但基于只有一套环境下，每次手动合并多个分支到中间分支，非常麻烦。


3. 除了支持 GitLab、阿里云CodeUp，是否考虑支持其他git服务平台？  
暂不考虑。  
个人看法：  
自建gitlab，是大部分公司选择的方案。  
阿里云的CodeUp，是公司不自建git服务的第二选择。  
GitHub，经常用于开源组织、个人开源项目，目前个人使用GitHub很少有需要多个分支合并到一起的场景。


4. 支持的环境有 dev、test、pre，支持其他环境吗？  
环境枚举在 EnvEnum，可以根据具体使用场景进行增删改。


5. 中间分支名是 dev_mix、test_mix、pre_mix，可以自定义吗？  
完全可以，按团队实际需要，自行修改。  
方法在 com.feeltens.git.service.impl.GitFlowServiceImpl.getMixBranchName


6. 为什么会有【git组织管理】模块？  
阿里云的CodeUp，允许同一用户归属不同的git组织。  
所以为了统一多个git服务平台的模型，而添加了【git组织】这个模型和菜单管理模块。  
在git-merge-flow的设计里，gitlab默认的组织名是default。


7. 为什么只有填写操作人这么简单的配置，没有常见的注册、登录功能？  
git-merge-flow 的定位是专注于解决中间分支合并问题的小而美的工具。（虽然它已经经历过个人、团队、公司的使用内测，但仍然有变美的空间。）  
所以为了记录操作日志，只是简单的填写操作人。当然，也可以自行添加登录功能。  
再者，目前没有实现操作记录审计功能，也可以自行添加审计。

## 技术栈

- **后端框架**: Spring Boot 2.2.1.RELEASE
- **模板引擎**: Thymeleaf
- **前端框架**: Vue.js 2.6 + Element UI 2.15
- **数据库**: MySQL 8.0
- **ORM框架**: MyBatis
- **构建工具**: Maven
- **JDK版本**: 1.8

## 配置说明

### 数据库表结构

项目包含以下核心表：

1. `git_organization` - git组织表
2. `git_project` - git工程表
3. `git_branch` - git原始分支表
4. `git_mix_branch` - git中间分支表
5. `git_mix_branch_item` - git中间分支条目表

详细表结构请参考 `doc/sql/git_merge_flow.sql`

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
│       ├── templates/                           # Thymeleaf模板
│       └── application.yml                      # 配置文件
```

## 参考资料

GitLab OpenAPI  
https://docs.gitlab.com/18.3/api/rest/

阿里云云效CodeUp OpenAPI  
https://help.aliyun.com/zh/yunxiao/developer-reference/codeup/

favicon 来源于 Araxis Merge 软件。

## 开源许可证

基于 GPL 协议开源
