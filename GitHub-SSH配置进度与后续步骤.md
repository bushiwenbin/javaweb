# GitHub SSH 配置进度与后续步骤

> 参考文档：《GitHub 使用手册（SSH 方式）》
> 保存时间：2026-10-22
> 账号：bushiwenbin　邮箱：484360835@qq.com

本机连通性检测（TCP 层）：`github.com:22` 可达、`ssh.github.com:22` 可达、`ssh.github.com:443` 可达。
如果浏览器打不开 github.com，属于 HTTPS/DNS 层问题，换网络或稍后重试即可，本机 SSH 通道本身没问题。

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

## 二、待完成的部分（等能访问 GitHub 时做）

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

### 第 4 步　（可选）ssh-agent
本机 `ssh-agent` 服务当前是 **Disabled** 且启动需要管理员权限，所以这一步没做。
因为密钥没有密码，**不做也不影响使用**。确实想要的话，用**管理员身份的 PowerShell** 执行一次：

```powershell
Set-Service ssh-agent -StartupType Automatic
Start-Service ssh-agent
ssh-add "$env:USERPROFILE\.ssh\id_rsa"
```

### 第 5 步　首次推送（把本地项目传到 GitHub）
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

## 六、备选方案：网络受限时

### 方案 A：SSH 走 443 端口（公司/校园网常封 22 端口）
新建或编辑 `C:\Users\asus\.ssh\config`，加入：

```plain
Host github.com
  HostName ssh.github.com
  Port 443
  User git
```

之后照常用 `ssh -T git@github.com` 测试。

### 方案 B：改用 HTTPS + 个人访问令牌（PAT）
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