<div align="center">
  <h1>🖼️ 系统效果图</h1>
  <p><strong>git-merge-flow 功能演示</strong></p>
</div>

---

## 📌 目录

- [主界面](#-主界面)
- [Git 组织管理](#-git-组织管理)
- [Git 工程管理](#-git-工程管理)
- [Git 分支管理](#-git-分支管理)
- [Git 中间分支管理](#-git-中间分支管理)
- [冲突处理](#-冲突处理)

---

## 🏠 主界面

<div align="center">
  <img src="doc/img/git_01.jpg" alt="主界面" width="800"/>
  <p><em>主界面 - 中间分支列表展示</em></p>
</div>

### 填写操作人

<div align="center">
  <img src="doc/img/git_02.jpg" alt="填写操作人" width="600"/>
  <p><em>简单填写操作人姓名即可开始使用</em></p>
</div>

---

## 🏢 Git 组织管理

<div align="center">
  <img src="doc/img/git_03.jpg" alt="Git组织管理" width="800"/>
  <p><em>管理 Git 组织（支持 GitLab 和 CodeUp）</em></p>
</div>

---

## 📦 Git 工程管理

<div align="center">
  <img src="doc/img/git_04.jpg" alt="Git工程管理" width="800"/>
  <p><em>工程列表 - 展示所有已配置的 Git 项目</em></p>
</div>

### 添加 Git 工程

<div align="center">
  <img src="doc/img/git_05.jpg" alt="添加Git工程" width="700"/>
  <p><em>快速添加新的 Git 项目到系统</em></p>
</div>

---

## 🌿 Git 分支管理

<div align="center">
  <img src="doc/img/git_06.jpg" alt="Git分支管理" width="800"/>
  <p><em>分支管理 - 新建分支、拉取远程分支、清理无效分支</em></p>
</div>

### 新建 Git 分支

<div align="center">
  <img src="doc/img/git_07.jpg" alt="新建Git分支" width="700"/>
  <p><em>从指定分支创建新的开发分支</em></p>
</div>

---

## 🔀 Git 中间分支管理

<div align="center">
  <img src="doc/img/git_08.jpg" alt="Git中间分支管理" width="800"/>
  <p><em>中间分支详情 - 查看当前集成分支列表、待集成分支列表</em></p>
</div>

### 添加分支到中间分支

<div align="center">
  <img src="doc/img/git_09.jpg" alt="添加分支到中间分支" width="700"/>
  <p><em>选择需要合并到中间分支的分支，然后批量加入</em></p>
</div>

---

## 🛠️ 冲突处理

### 自动合并失败提示

<div align="center">
  <img src="doc/img/git_10.jpg" alt="冲突提示" width="800"/>
  <p><em>当自动合并失败时，系统会提供冲突处理指引</em></p>
</div>

### 手动处理冲突脚本

<div align="center">
  <img src="doc/img/git_11.jpg" alt="冲突处理脚本" width="800"/>
  <p><em>自动生成 Shell 脚本，简化手动冲突解决流程</em></p>
</div>

### 从中间分支退出分支

<div align="center">
  <img src="doc/img/git_12.jpg" alt="退出分支" width="800"/>
  <p><em>从中间分支移除指定分支</em></p>
</div>

> ⚠️ **重要提示**：退出分支操作会重建中间分支，然后将剩余的分支列表依次重新合并。这可能需要重新处理代码冲突！

---

## 📸 主要功能一览

| 功能模块 | 说明 |
|:---|:---|
| 🏢 组织管理 | 管理 GitLab 和 CodeUp 组织 |
| 📦 工程管理 | 添加、编辑、删除 Git 项目 |
| 🌿 分支管理 | 新建、拉取、清理分支 |
| 🔀 中间分支 | 合并多个分支到统一环境 |
| 🔄 重新合并 | 一键重建并重新合并所有分支 |
| 🛠️ 冲突处理 | 自动生成冲突解决脚本 |
| 📝 操作记录 | 记录所有操作日志 |

---

<div align="center">
  <p><strong>返回 <a href="README.md">主文档</a></strong></p>
  <p>Made with ❤️ by feeltens</p>
</div>
