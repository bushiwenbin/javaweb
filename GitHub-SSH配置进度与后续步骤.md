# GitHub SSH 配置进度与后续步骤

> 参考文档：《GitHub 使用手册（SSH 方式）》
> 保存时间：2026-10-22　最后更新：2026-10-23
> 账号：bushiwenbin　邮箱：484360835@qq.com

本机连通性检测：`github.com:22` 可达、`ssh.github.com:443` 可达、`https://github.com` 返回 HTTP 200。
网络通道本身没问题；若浏览器仍打不开，属于浏览器代理/缓存层面，可换浏览器或清缓存后重试。

---

## 一、已完成的部分（本机，无需重做）

| 项目 | 结果 |
| --- | --- |
| Git 安装 | `git version 2.56.0.windows.2`，路径 `C:\Program Files\Git\cmd\git.exe`（系统 PATH 已包含该目录） |
| 全局用户名 | `user.name = bushiwenbin` |
| 全局邮箱 | `user.email = 484360835@qq.com` |
| 配置文件 | `C:\Users\asus\.gitconfig` |
| SSH 密钥 | 新建 RSA 4096：私钥 `C:\Users\asus\.ssh\id_rsa`，公钥 `C:\Users\asus\.ssh\id_rsa.pub` |
| 密钥指纹 | `SHA256:w28mC+xYyJk1kDwa1s23myaXmu6+6SJEGOD4fODd5rE` |
| 密钥密码 | **未设置（空密码）**，符合"不设置密钥密码"的要求 |
| known_hosts | 已写入 github.com 主机密钥，`ssh -T git@github.com` 不再提示确认主机 |
| 旧密钥 | 原有的 `id_ed25519` 保留未动，未删除 |

### 复现命令（万一需要重做）

```powershell
# 1. 生成密钥（空密码，一路回车）
ssh-keygen -t rsa -b 4096 -C "484360835@qq.com" -f "$env:USERPROFILE\.ssh\id_rsa" -N ""

# 2. 配置全局身份
& "C:\Program Files\Git\cmd\git.exe" config --global user.name "bushiwenbin"
& "C:\Program Files\Git\cmd\git.exe" config --global user.email "484360835@qq.com"

# 3. 查看配置是否生效
& "C:\Program Files\Git\cmd\git.exe" config --global --list
```

> 提示：如果在你自己的终端里输入 `git` 提示"无法识别"，重启一次终端即可（PATH 已配置好，只是当前窗口的环境变量是旧的）。

---

## 一·二、本地仓库准备（2026-10-23 完成）

项目目录 `C:\Users\asus\OneDrive\Desktop\javaweb`（Spring Boot + Vue 的 JavaWeb 项目）。

| 项目 | 结果 |
| --- | --- |
| 初始化仓库 | 已执行 `git init -b main`，默认分支为 `main` |
| 首次提交 | 提交信息 `first commit`，共 217 个文件（本文档也在其中） |
| 提交体积 | 约 16.19 MiB（已排除依赖与构建产物） |
| `.gitignore` 补充 | 新增 `node_modules/`、`frontend/node_modules/`、`frontend/dist/`、`.workbuddy/` |
| 已排除的大目录 | `frontend/node_modules`（144.3 MB / 7267 个文件）、`target/`、`.idea/` |
| 未纳入提交 | `.workbuddy/memory/`（本地 AI 助手记忆，含本机路径），如需纳入见下方命令 |
| 工作区状态 | 干净，`git status` 无未提交改动 |
| SSH 认证 | ✅ 2026-10-23 通过，`ssh -T git@github.com` 返回 `Hi bushiwenbin!` |
| 远程仓库 | **尚未添加**：`javaweb` 仓库还没在 GitHub 建出来（SSH `ls-remote` 返回 Repository not found，API 也 404） |
| 推送准备 | 已生成 `推送javaweb.bat`，仓库一建好双击即可推送 |

### 如果想让 .workbuddy/ 也进仓库

```powershell
# 先手动删掉 .gitignore 里的 .workbuddy/ 那一行，然后：
git add .workbuddy
git commit -m "add workbuddy memory"
```

### 远程仓库建好后接上并推送

```powershell
cd C:\Users\asus\OneDrive\Desktop\javaweb
git remote add origin git@github.com:bushiwenbin/仓库名.git
git push -u origin main
```

---

## 二、SSH 认证（✅ 2026-10-23 已完成）

> 结果：`ssh -T git@github.com` 返回 `Hi bushiwenbin! You've successfully authenticated, but GitHub does not provide shell access.`
> 说明公钥已成功添加到 GitHub，SSH 认证全流程打通。下面保留原始步骤，供以后换机器时参考。

### 第 1 步　登录 GitHub
打开 <https://github.com/login>，用邮箱 `484360835@qq.com` 和你的账号密码登录（如有两步验证，验证码需自己输入）。

### 第 2 步　添加公钥
1. 打开 <https://github.com/settings/ssh/new>
   （或：右上角头像 → **Settings** → 左侧 **SSH and GPG keys** → **New SSH key**）
2. **Title** 随便填，例如 `laptop-4ie0cjl5`
3. **Key** 输入框里粘贴第二部分最后的公钥全文（本项目的 `github_id_rsa.pub.txt` 文件里也存了一份，打开后 Ctrl+A、Ctrl+C 即可）
4. 点 **Add SSH key**

### 第 3 步　测试连接

```powershell
ssh -T git@github.com
```

看到下面这句就说明全流程成功：

```plain
Hi bushiwenbin! You've successfully authenticated, but GitHub does not provide shell access.
```

如果仍显示 `Permission denied (publickey)`，说明公钥还没保存成功，回到第 2 步重做。

**本次实际结果（2026-10-23）：已通过 ✅**

```plain
Hi bushiwenbin! You've successfully authenticated, but GitHub does not provide shell access.
```

指纹核对：`ssh-keygen -lf "$env:USERPROFILE\.ssh\id_rsa.pub"` → `4096 SHA256:w28mC+xYyJk1kDwa1s23myaXmu6+6SJEGOD4fODd5rE 484360835@qq.com (RSA)`
（`ssh -T` 退出码是 1，属正常现象，GitHub 不提供 shell。）

### 第 4 步　（可选）ssh-agent
本机 `ssh-agent` 服务当前是 **Disabled** 且启动需要管理员权限，所以这一步没做。
因为密钥没有密码，**不做也不影响使用**。确实想要的话，用**管理员身份的 PowerShell** 执行一次：

```powershell
Set-Service ssh-agent -StartupType Automatic
Start-Service ssh-agent
ssh-add "$env:USERPROFILE\.ssh\id_rsa"
```

### 第 5 步　首次推送（⬅️ 唯一剩下的一步）
> 注意：`git init`、`git add .`、`git commit`、`git branch -M main` 本机已执行完毕（见"一·二"），下面只有 `git remote add` 和 `git push` 需要执行。

也可以直接双击本目录的 `推送javaweb.bat`（已配好 `git@github.com:bushiwenbin/javaweb.git`，失败时会提示是"仓库不存在"还是"远程已有提交"，并给出对应命令）。
先在 GitHub 网页上新建**空仓库**（不要勾选 README / .gitignore / License），然后复制它的 SSH 地址 `git@github.com:bushiwenbin/仓库名.git`：

```powershell
cd C:\Users\asus\OneDrive\Desktop\javaweb

git init
git add .
git commit -m "first commit"
git remote add origin git@github.com:bushiwenbin/仓库名.git
git branch -M main
git push -u origin main
```

克隆别人的仓库：

```powershell
git clone git@github.com:用户名/仓库名.git
```

---

## 三、公钥全文（添加时复制这一整行）

```plain
ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAACAQDJ8RGEH/fJ7CxgqVep+UP0k4fQwUr4hsBvmGRoefIRDw0JdyYo67Tiq2dFea3xYjNO/FHy7yZSWAIVHZZnSqBEbW2lyfg9FrasmwoX0WyEdTD9kfnj6k9xAGqDnZC3d3K/j1cLuBf7ic9a6KyvjC2xH6S/T+uzO+3xXP+ALAjmcEFxNxg7Usbys9rvnNIMo6PnKBa9gEcEm2cuSPRAIlnbpV9auyRrvdTGHzSHNo5ipRRh12wu9IjV2nV71EGp48m4MpbaDUMfiaAbqr5AUlIsdwyS1KM6Jm8P0TqRLu6TYxKEmTFJsDqPD8+4qTP+7OspiSznwR4D/LIhurLajvyqoBzHv3gWsRmo1DE+t6mgywQaWx08YQouJGBN79M45HKfTr2iJBW6b23wc6oKw2xnyapmi4eMjtSZYcnAL+WGkf0qSreG+DkmNiv6/8/GjRqElX9wxR2qORDgkIYy1M4+0wq2C81dkZtcWW+pIyoF/ZFIbydVaY4J5qsRZ6mIrNFVHjgHxI+Cy6nhQ03vn8TsG5TC5lfWY8BBcoIy4A8XACUJeAvpgxqV3HFoCje88F2m7l8wDH518OlvM+5IDYMmCk8Rwsr4IrihiUyf/j+sBv182bQbMKruEJHFSw3cODilYlG3+yigOE6ShQzOuqrkw+UQP2AG13J4FbTQ/A/Kow== 484360835@qq.com
```

也可以随时在终端里查看：

```powershell
Get-Content "$env:USERPROFILE\.ssh\id_rsa.pub"
```

---

## 四、日常开发流程（文档摘要）

```powershell
git pull origin main          # 拉取最新代码
git status                    # 查看状态
git add .                     # 添加所有修改
git commit -m "本次修改的描述"  # 提交到本地
git push origin main          # 推送到远程
```

推荐顺序：克隆/初始化 → 开发 → `git add` → `git commit` → `git pull` → `git push`
每次提交前先 `git pull`，可避免推送被拒。

### 分支管理

```powershell
git branch                      # 查看本地分支
git checkout -b feature-login   # 创建并切换新分支
git checkout main               # 切回主分支
git merge feature-login         # 合并分支
git push origin feature-login   # 推送新分支
git branch -d feature-login     # 删除本地分支
git push origin --delete feature-login  # 删除远程分支
```

### 小技巧

- `.gitignore` 排除 `node_modules`、`.env`、`target/` 等不需要跟踪的内容
- `git log --oneline`　简洁查看提交历史
- `git diff`　查看未暂存的改动
- `git reset HEAD 文件名`　取消暂存某个文件

---

## 五、常见问题排查

### ❌ Permission denied (publickey)
- 确认公钥已添加到 GitHub（<https://github.com/settings/keys> 能看到 `SHA256:w28mC+xYyJk1kDwa1s23myaXmu6+6SJEGOD4fODd5rE` 这一条）
- 确认远程地址是 SSH 形式 `git@github.com:用户名/仓库名.git`，不是 `https://...`
- 重新运行 `ssh -T git@github.com` 测试

### ❌ 推送被拒绝（non-fast-forward）
```powershell
git pull origin main --rebase
git push origin main
```

### ❌ 想从 HTTPS 换成 SSH
```powershell
git remote -v
git remote set-url origin git@github.com:用户名/仓库名.git
```

---

## 六、网络问题诊断与备选方案

### 诊断结论（2026-10-23 实测）

| 检测项 | 结果 |
| --- | --- |
| DNS 解析 github.com | `20.205.243.166`（该 IP 经常连接超时） |
| `https://github.com/login` 连续 6 次 | **0 / 6 成功**（全部 8 秒超时） |
| 换个 IP 再测（`--resolve` 指定） | `20.27.177.113`、`140.82.112/113/114/116.3` 均返回 200 |
| 稳定性复测 3 次 | `20.27.177.113` 3/3 成功，0.35~0.53 秒 |
| `api.github.com` / `avatars.githubusercontent.com` / `codeload.github.com` | 正常（200 / 200 / 301） |
| `raw.githubusercontent.com`、`gist.github.com` | 失败（属 SNI 层阻断，改 hosts 无效） |
| SSH 22 端口 `ssh -T git@github.com` | **3 / 3 成功握手**（返回 Permission denied 属正常，说明通道通） |
| SSH 443 端口 `ssh -T -p 443 git@ssh.github.com` | 成功握手 |
| 系统代理 | 未配置（Direct access） |
| hosts 文件 | 未被修改过 |

结论：**网页 HTTPS 是间歇性被阻断（时通时不通），SSH 通道一直稳定。**
所以推送代码没问题，只有"登录网页加公钥"这一步需要网络配合。

### 方案 A（首选）：用 hosts 固定到可用 IP

本目录下的 [fix-github-hosts.ps1](./fix-github-hosts.ps1) 就是干这个的。
**右键 PowerShell → 以管理员身份运行**，然后：

```powershell
cd C:\Users\asus\OneDrive\Desktop\javaweb
powershell -ExecutionPolicy Bypass -File .\fix-github-hosts.ps1
```

脚本会自动备份 hosts、写入 4 条固定解析（`20.27.177.113 github.com` 等），并刷新 DNS 缓存。

**更省事的做法**：直接双击本目录下的 `修复GitHub网页访问.bat`，它会自动弹出 UAC 提权请求（点"是"），然后执行同一个脚本。

实测对比（2026-10-23 11:50 同一时刻）：

| 目标 | 结果 |
| --- | --- |
| 默认 DNS `20.205.243.166` 访问 `https://github.com/new` | ❌ 000，8 秒超时 |
| 固定 `20.27.177.113` | ✅ 302，0.19~0.38 秒 |
| 固定 `140.82.116.3` | ✅ 302，0.77 秒 |
| 固定 `20.27.177.116`（api.github.com） | ✅ 200，0.19~0.21 秒 |

注意：`asus` 已在管理员组内（`Get-LocalGroupMember Administrators` 可查），所以 UAC 提权可行。

**12:10 复测补充**：进入更彻底的阻断时段后，默认 DNS 与固定 IP `20.27.177.113` **同时超时**（都为 000）。
这说明 hosts 方案只能解决"部分 IP 被墙"的情况；遇到全面阻断时，只能换网络（手机热点最有效）或开代理。
SSH 22 端口在同一时段依然可用（推送不受影响），受阻的只有网页。
想撤销就删掉 hosts 里带"GitHub 加速"标记的那几行，或还原同名 `.bak` 文件。

### 方案 B：SSH 走 443 端口（公司/校园网常封 22 端口）
新建或编辑 `C:\Users\asus\.ssh\config`，加入：

```plain
Host github.com
  HostName ssh.github.com
  Port 443
  User git
```

之后照常用 `ssh -T git@github.com` 测试。

### 方案 C：改用 HTTPS + 个人访问令牌（PAT）
GitHub 已不支持用账号密码做 HTTPS 推送，需要用 PAT：
GitHub → Settings → Developer settings → Personal access tokens，生成后：

```powershell
git remote set-url origin https://github.com/bushiwenbin/仓库名.git
# 推送时用户名填 GitHub 用户名，密码处粘贴 PAT
```

---

## 七、安全提醒

1. **账号密码不参与 SSH 配置**：SSH 用密钥认证，不需要 GitHub 密码，本次也没有把密码写入任何文件或配置。
2. 密码曾经以明文形式发出来过，建议到 <https://github.com/settings/security> 修改一次密码。
3. 私钥 `C:\Users\asus\.ssh\id_rsa` 绝对不要发给别人或上传到仓库；只有 `id_rsa.pub`（公钥）可以公开。