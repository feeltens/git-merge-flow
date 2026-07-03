<div align="center">
  <h1>🌊 git-merge-flow</h1>
  <p><strong>Git 中间分支合流管理系统</strong></p>
  <p>🚀 让多分支并行开发、共用测试环境变得简单高效</p>

  ![Java](https://img.shields.io/badge/Java-1.8-orange.svg)
  ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.2.1-brightgreen.svg)
  ![License](https://img.shields.io/badge/License-GPL--3.0-blue.svg)
  ![MySQL](https://img.shields.io/badge/MySQL-8.0-blue.svg)
  ![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg)

  <p>
    <a href="#-核心价值">核心价值</a> •
    <a href="#-功能特性">功能特性</a> •
    <a href="#️-效果预览">效果预览</a> •
    <a href="#-快速开始">快速开始</a> •
    <a href="#️-技术栈">技术栈</a> •
    <a href="#-常见问题">常见问题</a>
  </p>

  <img src="doc/img/git_main.jpg" alt="git-merge-flow效果图" width="800"/>
</div>

---

## 💎 核心价值

### 🎯 解决什么问题？

在实际开发中，团队经常面临这样的困境：

| 现状 | 痛点 |
|:---:|:---:|
| 📋 多个需求并行开发 | 每个需求一个分支 |
| 🧪 多个测试任务同时进行 | 需要频繁切换部署 |
| 💰 但测试环境只有一套 | 手动合并分支繁琐易错 |

**git-merge-flow** 通过「中间分支」概念，自动将多个开发分支合并到统一的环境分支（如 `dev_mix`、`test_mix`），实现：

- ✅ **一键合并** - 告别手动 merge 的繁琐操作
- ✅ **冲突可视化** - Web 界面直接解决代码冲突
- ✅ **灵活管理** - 随时添加/移除分支，自动重新合并

### 🌊 名称释义

| 名称 | 含义 |
|:---:|:---|
| **git-merge-flow** | git（管理对象）+ merge（合并）+ flow（持续流动的合并流程） |
| **合流** | 如同河流分支汇入主干，多个开发分支汇聚到中间分支 |

---

## ✨ 功能特性

| 功能 | 描述                                        |
|:---|:------------------------------------------|
| 🔀 **中间分支管理** | 自动管理 `dev_mix`、`test_mix`、`pre_mix` 等环境分支 |
| 🔄 **一键重新合并** | 核心功能！基于主分支重建中间分支，依次合并所有分支                 |
| 🌿 **分支操作** | 新建分支、拉取远程分支、批量清理无效分支                      |
| 🏢 **多工程支持** | 同时管理多个 Git 仓库                             |
| 👥 **权限管理** | 用户 + 工程级别的细粒度权限控制                         |
| 🔌 **多平台适配** | 支持 GitLab (v18.3+) 和阿里云 CodeUp            |
| 🖥️ **可视化界面** | 基于 Vue.js + Element UI 的友好 Web 界面         |
| 🛠️ **冲突处理** | Web 在线解决 或 自动生成脚本手动处理                     |
| 📝 **操作日志** | 完整记录所有分支操作历史                              |
| 🎨 **三路Diff查看** | 支持Base/Ours/Theirs三路Diff可视化查看             |

---

## 🖼️ 效果预览

| 工程管理 | 分支管理 |
|:---:|:---:|
| ![工程管理](doc/img/git_04.jpg) | ![分支管理](doc/img/git_06.jpg) |

| Web 代码冲突解决 | Web 代码提交推送 |
|:---:|:---:|
| ![web代码冲突解决](doc/img/git_15.jpg) | ![web代码提交并推送](doc/img/git_16.jpg) |

| 手动处理冲突脚本 | 一键重新合并成功 |
|:---:|:---:|
| ![手动处理冲突](doc/img/git_11.jpg) | ![自动合并成功](doc/img/git_13.jpg) |

| 用户列表 | 用户权限分配 |
|:---:|:---:|
| ![用户列表](doc/img/git_17.jpg) | ![用户权限管理](doc/img/git_18.jpg) |

📸 **[查看完整效果图 →](system_show.md)**

---

## 🔄 核心功能：重新合并

**「重新合并」** 是 git-merge-flow 最核心的功能：

1. 🗑️ 删除旧的中间分支
2. 🌱 基于主分支创建新的中间分支
3. 🔀 依次合并所有已添加的分支
4. ⚠️ 遇到冲突时暂停，提供解决方案

<div align="center">
  <img src="doc/img/git_remergeMixBranch.jpg" alt="重新合并流程" width="600"/>
  <p><em>重新合并内部逻辑流程图</em></p>
</div>

---

## 🚀 快速开始

### 📋 环境要求

| 软件 | 版本 |
|:---:|:---:|
| JDK | 1.8+ |
| MySQL | 8.0+ |
| Maven | 3.6+ |

### 1️⃣ 初始化数据库

```bash
# 创建数据库
mysql -u root -p -e "CREATE DATABASE git_merge_flow DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 导入表结构
mysql -u root -p git_merge_flow < doc/sql/git_merge_flow.sql
```

### 2️⃣ 获取 Git API 令牌

<details>
<summary>🦊 <b>GitLab 配置</b></summary>

访问：`http://your-gitlab-domain/-/user_settings/personal_access_tokens`

勾选权限：
- ✅ `api`
- ✅ `read_repository`
- ✅ `write_repository`

</details>

<details>
<summary>☁️ <b>阿里云 CodeUp 配置</b></summary>

访问：`https://account-devops.aliyun.com/settings/personalAccessToken`

</details>

### 3️⃣ 修改配置文件

编辑 `src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/git_merge_flow?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: your_username
    password: your_password

merge-flow:
  gitService: gitlab  # 或 codeup

gitlab:
  api:
    baseUrl: http://your-gitlab-domain
    accessToken: your-access-token
```

### 4️⃣ 启动项目

```bash
# 编译打包
mvn clean package -DskipTests

# 启动
java -jar target/git-merge-flow-1.0-SNAPSHOT.jar
```

### 5️⃣ 访问系统

🌐 浏览器打开：**http://localhost:18081**

📝 默认管理员账号：`admin` / `admin123`

---

## 🛠️ 技术栈

| 类型 | 技术 |
|:---:|:---|
| 🖥️ 后端框架 | Spring Boot 2.2.1 |
| 🎨 前端框架 | Vue.js 2.6 + Element UI 2.15 |
| 📄 模板引擎 | Thymeleaf |
| 🗄️ 数据库 | MySQL 8.0 |
| 🔗 ORM | MyBatis |
| 🔐 认证授权 | Sa-Token |
| 🔧 Git 操作 | JGit 5.13.5 |
| 📊 Diff计算 | java-diff-utils 4.12 (Myers算法) |
| 🎨 代码编辑器 | Monaco Editor |
| 🔌 Git API | GitLab API / CodeUp OpenAPI |

---

## 📂 项目结构

```
git-merge-flow/
├── src/main/java/com/feeltens/git/
│   ├── controller/          # 🎮 控制器层
│   ├── service/             # 💼 业务逻辑层
│   ├── mapper/              # 🗄️ 数据访问层
│   ├── entity/              # 📦 实体类
│   ├── oapi/                # 🔌 Git API 适配器
│   ├── config/              # ⚙️ 配置类
│   ├── util/                # 🔧 工具类
│   ├── dto/                 # 📦 数据传输对象
│   │   ├── conflict/        # 冲突相关DTO
│   │   └── diff/            # Diff相关DTO
│   ├── reader/              # 📖 Git索引读取器
│   ├── parser/              # 🔍 Diff3格式解析器
│   ├── detector/            # 🔎 冲突类型检测器
│   └── diff/                # 📊 Diff计算器
├── src/main/resources/
│   ├── templates/           # 🎨 页面模板
│   │   └── diff-viewer.html # 三路Diff查看器
│   ├── static/              # 📁 静态资源
│   └── application.yml      # ⚙️ 配置文件
└── doc/
    ├── img/                 # 🖼️ 效果图
    └── sql/                 # 🗄️ 数据库脚本
```

---

## 💾 数据库表说明

| 表名 | 说明 |
|:---|:---|
| `git_organization` | Git 组织信息 |
| `git_project` | Git 工程信息 |
| `git_branch` | Git 原始分支 |
| `git_mix_branch` | Git 中间分支 |
| `git_mix_branch_item` | 中间分支包含的分支列表 |
| `sys_user` | 系统用户 |
| `sys_user_role` | 用户角色关联 |
| `sys_user_project_perm` | 用户工程权限 |

---

## ❓ 常见问题

<details>
<summary>💡 <b>git-merge-flow 适合什么场景？</b></summary>

**核心场景**：多个开发分支需要部署到同一套测试环境

**典型用例**：
- 🏢 企业内部多需求并行开发
- 🧪 QA 需要同时测试多个功能
- 💰 测试环境资源有限，无法为每个分支单独部署

**设计理念**：小而美的工具，专注解决一个痛点

</details>

<details>
<summary>🔌 <b>支持 GitHub 吗？</b></summary>

**暂不支持**

原因：
- 🏢 GitLab：企业私有部署首选
- ☁️ CodeUp：云托管方案
- 🌐 GitHub：主要用于开源项目，较少有多分支合并场景

欢迎 PR 贡献！

</details>

<details>
<summary>⚙️ <b>支持哪些环境？可以自定义吗？</b></summary>

**默认环境**：`dev`、`test`、`pre`

**自定义方法**：修改 `com.feeltens.git.enums.EnvEnum` 枚举类

</details>

<details>
<summary>📝 <b>中间分支命名规则可以改吗？</b></summary>

**可以！** 默认：`dev_mix`、`test_mix`、`pre_mix`

**修改位置**：`GitFlowServiceImpl.getMixBranchName()` 方法

</details>

<details>
<summary>🔗 <b>可以集成 Jenkins 实现 CI/CD 吗？</b></summary>

**完全可以！**

在 `GitFlowService#remergeMixBranch` 方法后，通过 Spring 事件机制触发 Jenkins 部署。

参考：`dev_jenkins` 分支的 `JenkinsController#testJenkins` 示例

</details>

---

## � 参考资料

- 📖 [GitLab REST API (v18.3)](https://docs.gitlab.com/18.3/api/rest/)
- 📖 [阿里云 CodeUp OpenAPI](https://help.aliyun.com/zh/yunxiao/developer-reference/codeup/)
- 🎨 Favicon 来源于 Araxis Merge

---

## 📄 开源协议

本项目基于 **[GPL-3.0](LICENSE)** 协议开源。

---

<div align="center">
  <p>⭐ 如果这个项目对您有帮助，请给个 Star 支持一下！</p>
  <p>Made with ❤️ by feeltens</p>
  <p>Copyright © 2025 git-merge-flow</p>
</div>
