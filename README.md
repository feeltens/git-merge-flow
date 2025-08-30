# 合流管理系统 (Git Merge Flow)

## 项目简介

合流管理系统是一个基于Spring Boot + Thymeleaf + Vue.js的单体工程，主要功能是解决GitLab、阿里云CodeUp Git仓库的中间分支合并查询等相关问题。

基于GitLab(v18.3)、CodeUp提供的官方API，使用Java实现对Git分支的管理，核心功能是管理合并中间分支，对合并到中间分支的各个单独分支进行列表查看、新建分支、分支清理、批量加入、批量退出。

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
- 支持多个Git工程（如ofc、oms、rma等）
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

## 快速开始

### 环境要求

- JDK 1.8+
- MySQL 8.0+
- Maven 3.6+

### 数据库初始化

1. 创建数据库：
```sql
CREATE DATABASE git_merge_flow DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 执行初始化脚本：
```bash
mysql -u root -p git_merge_flow < src/main/resources/sql/init.sql
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

```bash
# 编译打包
mvn clean package

# 运行项目
java -jar target/git-merge-flow-1.0.0-SNAPSHOT.jar

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

项目启动后，访问：http://localhost:18081

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

### Git open API配置

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
    accessToken: set-your-gitlab-access-token

# CodeUp API 配置
codeup:
  api:
    # codeup api 基础url
    baseUrl: https://openapi-rdc.aliyuncs.com
    # codeup api 访问令牌
    accessToken: set-your-codeup-access-token

```

### 数据库表结构

项目包含以下核心表：

1. `git_organization` - git组织表
2. `git_project` - git工程表
3. `git_branch` - git原始分支表
4. `git_mix_branch` - git中间分支表
5. `git_mix_branch_item` - git中间分支条目表

详细表结构请参考 `src/main/resources/sql/init.sql`

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

## 访问

http://localhost:18081
