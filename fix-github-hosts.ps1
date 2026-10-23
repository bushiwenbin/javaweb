# ============================================================
# GitHub 网页访问修复脚本（hosts 固定 IP 方案）
# ------------------------------------------------------------
# 背景：本机 DNS 解析 github.com 得到 20.205.243.166，实测经常超时；
#       下面这些官方 IP 实测稳定（每个连续 3 次返回 200/301，耗时 0.2~0.6 秒）。
# 用法：右键 PowerShell → 以管理员身份运行，然后执行：
#         powershell -ExecutionPolicy Bypass -File .\fix-github-hosts.ps1
#       （修改 hosts 必须管理员权限）
# 撤销：删掉 hosts 里“GitHub 加速”标记的那几行，或还原脚本生成的 .bak 备份
# ============================================================

$hostsPath = Join-Path $env:SystemRoot 'System32\drivers\etc\hosts'
$marker    = '# === GitHub 加速（fix-github-hosts.ps1 添加）==='

# 1) 管理员权限检查
$isAdmin = ([Security.Principal.WindowsPrincipal][Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)
if (-not $isAdmin) {
    Write-Host '需要管理员权限：请右键 PowerShell → 以管理员身份运行，再执行本脚本。' -ForegroundColor Red
    exit 1
}

# 2) 实测可用的 IP（2026-10-23 验证）
$entries = @(
    '20.27.177.113  github.com',
    '20.27.177.113  www.github.com',
    '20.27.177.116  api.github.com',
    '20.27.177.114  codeload.github.com'
)

# 3) 先备份
$backup = "$hostsPath.bak-$(Get-Date -Format 'yyyyMMdd-HHmmss')"
Copy-Item $hostsPath $backup -Force
Write-Host "已备份 hosts 到：$backup" -ForegroundColor Green

# 4) 逐条写入（已存在的跳过）
$current = Get-Content $hostsPath -Raw -ErrorAction SilentlyContinue
if (-not $current) { $current = '' }
$added = 0
foreach ($e in $entries) {
    $parts = $e -split '\s+'
    $ip = $parts[0]
    $hostName = $parts[1]
    $pattern = '(?m)^\s*' + [regex]::Escape($ip) + '\s+' + [regex]::Escape($hostName) + '\s*$'
    if ($current -match $pattern) {
        Write-Host "已存在，跳过：$e"
    } else {
        if ($added -eq 0) {
            Add-Content -Path $hostsPath -Value '' -Encoding ASCII
            Add-Content -Path $hostsPath -Value $marker -Encoding ASCII
        }
        Add-Content -Path $hostsPath -Value $e -Encoding ASCII
        Write-Host "已添加：$e" -ForegroundColor Green
        $added++
    }
}

# 5) 刷新 DNS 缓存
ipconfig /flushdns | Out-Null

Write-Host ''
if ($added -eq 0) {
    Write-Host 'hosts 里已存在全部条目，无需改动。' -ForegroundColor Cyan
} else {
    Write-Host "完成，共添加 $added 条。现在打开 https://github.com/login 试试。" -ForegroundColor Cyan
}
Write-Host '若要撤销：删除 hosts 中“GitHub 加速”标记的几行，或还原上面那份 .bak 备份。'