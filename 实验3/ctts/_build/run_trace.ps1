# ============================================================
# 实验三 接口回归脚本 —— 对运行中的 CTTS 服务发起真实 HTTP 请求，
# 把「请求命令 + 响应报文」原样留存为报告证据（_build/api_trace.txt）。
#
# 说明：请求体含中文，为避免 Windows 命令行引号转义问题，
#       一律写入 UTF-8 请求体文件后用 curl -d @file 发送。
# ============================================================
$ErrorActionPreference = "Continue"
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8

$BASE  = "http://localhost:8080"
$BUILD = Join-Path $PSScriptRoot ""
if (-not $BUILD) { $BUILD = "." }
$OUT   = Join-Path $BUILD "api_trace.txt"
$UTF8  = New-Object System.Text.UTF8Encoding $false

$lines = New-Object System.Collections.Generic.List[string]

function Emit([string]$text) {
    if ($null -eq $text) { $text = "" }
    foreach ($l in ($text -split "`r?`n")) { $lines.Add($l) }
}

function New-Payload([string]$name, [string]$json) {
    $p = Join-Path $BUILD $name
    [System.IO.File]::WriteAllText($p, $json, $UTF8)
    return $p
}

# 执行一次请求并记录到 trace
function Req {
    param(
        [string]$Label,       # 例：接口1
        [string]$Desc,        # 例：分页查询用户
        [string]$CmdText,     # 展示用的等价命令行
        [string[]]$CurlArgs,  # 真正传给 curl 的参数
        [string]$BodyText     # 可选：请求体原文（展示用）
    )
    Emit ("═" * 56)
    Emit "【$Label】   $Desc"
    Emit "`$ $CmdText"
    Emit ""
    if ($BodyText) {
        Emit "请求体（UTF-8 文件，curl -d @file 发送）："
        Emit $BodyText
        Emit ""
    }
    $resp = (& curl.exe @CurlArgs 2>&1 | Out-String)
    Emit $resp.TrimEnd()
    Emit ""
    Start-Sleep -Milliseconds 250
    return $resp
}

function JsonBody([string]$resp) {
    try {
        $line = ($resp.TrimEnd() -split "`r?`n" | Where-Object { $_.TrimStart().StartsWith("{") } | Select-Object -Last 1)
        if (-not $line) { return $null }
        return ($line | ConvertFrom-Json)
    } catch { return $null }
}

function JsonField([string]$resp, [string]$path) {
    $o = JsonBody $resp
    if ($null -eq $o) { return $null }
    foreach ($seg in $path.Split(".")) { $o = $o.$seg }
    return $o
}

$it = [System.Globalization.CultureInfo]::InvariantCulture

# ---------------------------------------------------------------- 请求体文件
$bodyUserCreate = '{
  "username": "2024008",
  "password": "123456",
  "realName": "赵敏",
  "role": "STUDENT",
  "email": "2024008@qq.com",
  "phone": "13900000008",
  "status": 1
}'
$bodyUserUpdate = '{
  "username": "2024008",
  "realName": "赵敏",
  "role": "TEACHER",
  "email": "zhaomin@ctts.edu.cn",
  "phone": "13900000009",
  "status": 1
}'
$bodyUserDup = '{
  "username": "admin",
  "password": "123456",
  "realName": "重复账号测试",
  "role": "ADMIN"
}'
$bodyUserBad = '{
  "username": "ab",
  "password": "123",
  "realName": "",
  "role": "BOSS",
  "email": "not-an-email"
}'
$bodyProjCreate = '{
  "name": "实训成果答辩系统",
  "category": "Web 后端",
  "teacher": "丁玺润",
  "deadline": "2027-01-08",
  "status": 1,
  "description": "支持答辩分组、评分与结果公示。"
}'
$bodyProjUpdate = '{
  "name": "实训成果答辩系统（V2）",
  "category": "Web 后端",
  "teacher": "李敏",
  "deadline": "2027-02-28",
  "status": 2,
  "description": "增加结果公示与成绩导出功能。"
}'
$bodyProjBadName = '{
  "name": "",
  "category": "Web 后端",
  "teacher": "丁玺润",
  "deadline": "2027-01-08"
}'
$bodyProjBadDate = '{
  "name": "日期格式测试项目",
  "category": "测试",
  "teacher": "丁玺润",
  "deadline": "2027/01/08"
}'

$fUserCreate = New-Payload "payload_user_create.json" $bodyUserCreate
$fUserUpdate = New-Payload "payload_user_update.json" $bodyUserUpdate
$fUserDup    = New-Payload "payload_user_dup.json"    $bodyUserDup
$fUserBad    = New-Payload "payload_user_bad.json"    $bodyUserBad
$fProjCreate = New-Payload "payload_project_create.json" $bodyProjCreate
$fProjUpdate = New-Payload "payload_project_update.json" $bodyProjUpdate
$fProjBadName = New-Payload "payload_project_badname.json" $bodyProjBadName
$fProjBadDate = New-Payload "payload_project_baddate.json" $bodyProjBadDate

# ================================================================ 用户模块
$r = Req "用户-接口1" "分页查询用户（pageNum=1, pageSize=3）" `
    "curl -s -i '$BASE/api/users/page?pageNum=1&pageSize=3'" `
    @("-s","-i","$BASE/api/users/page?pageNum=1&pageSize=3")
Emit "→ 解析：total = $(JsonField $r 'data.total')，本页 $(@(JsonField $r 'data.records').Count) 条"
Emit ""

$r = Req "用户-接口1" "条件 + 模糊查询（role=STUDENT，keyword=2024）" `
    "curl -s -i '$BASE/api/users/page?pageNum=1&pageSize=3&role=STUDENT&keyword=2024'" `
    @("-s","-i","$BASE/api/users/page?pageNum=1&pageSize=3&role=STUDENT&keyword=2024")
Emit "→ 解析：total = $(JsonField $r 'data.total')"
Emit ""

$r = Req "用户-接口1-异常" "pageSize 超出 @Max(50) 上限，被参数校验拦截" `
    "curl -s -i '$BASE/api/users/page?pageNum=1&pageSize=999'" `
    @("-s","-i","$BASE/api/users/page?pageNum=1&pageSize=999")
Emit ""

$r = Req "用户-接口2" "按 id 查询用户详情（id=5），password 列因 select=false 不返回" `
    "curl -s -i '$BASE/api/users/5'" `
    @("-s","-i","$BASE/api/users/5")
Emit ""

$r = Req "用户-接口2-异常" "id 不存在（999），Service 抛 BizException" `
    "curl -s -i '$BASE/api/users/999'" `
    @("-s","-i","$BASE/api/users/999")
Emit ""

$r = Req "用户-接口2-异常" "路径变量类型不匹配（id=abc）" `
    "curl -s -i '$BASE/api/users/abc'" `
    @("-s","-i","$BASE/api/users/abc")
Emit ""

$r = Req "用户-接口3" "新增用户（BaseMapper.insert，主键自增回填）" `
    "curl -s -i -X POST '$BASE/api/users' -H 'Content-Type: application/json' -d @payload_user_create.json" `
    @("-s","-i","-X","POST","$BASE/api/users","-H","Content-Type: application/json","-d","@$fUserCreate") `
    $bodyUserCreate
$newUserId = JsonField $r 'data.id'
$newUserCreate = JsonField $r 'data.createTime'
Emit "→ 解析：新增用户 id = $newUserId，createTime 由 MetaObjectHandler 自动填充为 $newUserCreate"
Emit ""

$r = Req "用户-接口3-异常" "账号重复（username=admin 已存在），Service 前置校验拦截" `
    "curl -s -i -X POST '$BASE/api/users' -H 'Content-Type: application/json' -d @payload_user_dup.json" `
    @("-s","-i","-X","POST","$BASE/api/users","-H","Content-Type: application/json","-d","@$fUserDup") `
    $bodyUserDup
Emit ""

$r = Req "用户-接口3-异常" "字段校验失败（账号过短、姓名空、角色非法、邮箱格式错）" `
    "curl -s -i -X POST '$BASE/api/users' -H 'Content-Type: application/json' -d @payload_user_bad.json" `
    @("-s","-i","-X","POST","$BASE/api/users","-H","Content-Type: application/json","-d","@$fUserBad") `
    $bodyUserBad
Emit ""

$r = Req "用户-接口4" "修改用户（BaseMapper.updateById，password 留空表示不改密码）" `
    "curl -s -i -X PUT '$BASE/api/users/$newUserId' -H 'Content-Type: application/json' -d @payload_user_update.json" `
    @("-s","-i","-X","PUT","$BASE/api/users/$newUserId","-H","Content-Type: application/json","-d","@$fUserUpdate") `
    $bodyUserUpdate
Emit ""

$r = Req "用户-接口7" "禁用用户（自定义 @Update 注解 SQL，返回受影响行数）" `
    "curl -s -i -X PATCH '$BASE/api/users/$newUserId/status?status=0'" `
    @("-s","-i","-X","PATCH","$BASE/api/users/$newUserId/status?status=0")
Emit ""

$r = Req "用户-接口6" "按角色查询（自定义 @Select 注解 SQL，role=TEACHER）" `
    "curl -s -i '$BASE/api/users/role/TEACHER'" `
    @("-s","-i","$BASE/api/users/role/TEACHER")
Emit "→ 解析：TEACHER 角色共 $(@(JsonField $r 'data').Count) 人"
Emit ""

$r = Req "用户-接口8" "按角色统计用户数（聚合注解 SQL COUNT(*)）" `
    "curl -s -i '$BASE/api/users/statistics/role'" `
    @("-s","-i","$BASE/api/users/statistics/role")
Emit ""

$r = Req "用户-接口5" "删除用户（@TableLogic 逻辑删除，实际执行 UPDATE deleted=1）" `
    "curl -s -i -X DELETE '$BASE/api/users/$newUserId'" `
    @("-s","-i","-X","DELETE","$BASE/api/users/$newUserId")
Emit ""

$r = Req "用户-接口5-验证" "删除后再查该 id，已被逻辑删除过滤，返回 404" `
    "curl -s -i '$BASE/api/users/$newUserId'" `
    @("-s","-i","$BASE/api/users/$newUserId")
Emit ""

# ================================================================ 项目模块
$r = Req "项目-接口1" "分页查询实训项目（pageNum=1, pageSize=3）" `
    "curl -s -i '$BASE/api/projects/page?pageNum=1&pageSize=3'" `
    @("-s","-i","$BASE/api/projects/page?pageNum=1&pageSize=3")
Emit "→ 解析：total = $(JsonField $r 'data.total')，pages = $(JsonField $r 'data.pages')"
Emit ""

$cat = [uri]::EscapeDataString("Web 后端")
$r = Req "项目-接口1" "条件查询（status=1 且 category=Web 后端）" `
    "curl -s -i '$BASE/api/projects/page?pageNum=1&pageSize=5&status=1&category=Web%20%E5%90%8E%E7%AB%AF'" `
    @("-s","-i","$BASE/api/projects/page?pageNum=1&pageSize=5&status=1&category=$cat")
Emit "→ 解析：命中 total = $(JsonField $r 'data.total')"
Emit ""

$r = Req "项目-接口2" "按 id 查询项目详情（BaseMapper.selectById，id=3）" `
    "curl -s -i '$BASE/api/projects/3'" `
    @("-s","-i","$BASE/api/projects/3")
Emit ""

$r = Req "项目-接口2-异常" "id 不存在（999）" `
    "curl -s -i '$BASE/api/projects/999'" `
    @("-s","-i","$BASE/api/projects/999")
Emit ""

$r = Req "项目-接口3" "新增实训项目（含中文与日期，验证列名映射 project_name）" `
    "curl -s -i -X POST '$BASE/api/projects' -H 'Content-Type: application/json' -d @payload_project_create.json" `
    @("-s","-i","-X","POST","$BASE/api/projects","-H","Content-Type: application/json","-d","@$fProjCreate") `
    $bodyProjCreate
$newProjId = JsonField $r 'data.id'
Emit "→ 解析：新增项目 id = $newProjId"
Emit ""

$r = Req "项目-接口3-异常" "项目名称为空，触发 @NotBlank 校验" `
    "curl -s -i -X POST '$BASE/api/projects' -H 'Content-Type: application/json' -d @payload_project_badname.json" `
    @("-s","-i","-X","POST","$BASE/api/projects","-H","Content-Type: application/json","-d","@$fProjBadName") `
    $bodyProjBadName
Emit ""

$r = Req "项目-接口3-异常" "截止日期格式错误（2027/01/08），Jackson 无法按 yyyy-MM-dd 解析" `
    "curl -s -i -X POST '$BASE/api/projects' -H 'Content-Type: application/json' -d @payload_project_baddate.json" `
    @("-s","-i","-X","POST","$BASE/api/projects","-H","Content-Type: application/json","-d","@$fProjBadDate") `
    $bodyProjBadDate
Emit ""

$r = Req "项目-接口4" "修改实训项目（BaseMapper.updateById）" `
    "curl -s -i -X PUT '$BASE/api/projects/$newProjId' -H 'Content-Type: application/json' -d @payload_project_update.json" `
    @("-s","-i","-X","PUT","$BASE/api/projects/$newProjId","-H","Content-Type: application/json","-d","@$fProjUpdate") `
    $bodyProjUpdate
Emit ""

$r = Req "项目-接口7" "修改项目状态为 2-已完成（自定义 @Update 注解 SQL）" `
    "curl -s -i -X PATCH '$BASE/api/projects/3/status?status=2'" `
    @("-s","-i","-X","PATCH","$BASE/api/projects/3/status?status=2")
Emit ""

$r = Req "项目-接口6" "按类别查询（自定义 @Select 注解 SQL，category=Web 后端）" `
    "curl -s -i '$BASE/api/projects/category/Web%20%E5%90%8E%E7%AB%AF'" `
    @("-s","-i","$BASE/api/projects/category/$cat")
Emit ""

$r = Req "项目-接口8" "按类别聚合统计（GROUP BY category + COUNT(*)）" `
    "curl -s -i '$BASE/api/projects/statistics/category'" `
    @("-s","-i","$BASE/api/projects/statistics/category")
Emit ""

$r = Req "项目-接口5" "删除实训项目（Project 未用 @TableLogic，属物理删除）" `
    "curl -s -i -X DELETE '$BASE/api/projects/$newProjId'" `
    @("-s","-i","-X","DELETE","$BASE/api/projects/$newProjId")
Emit ""

$r = Req "项目-接口5-验证" "删除后再查该 id，已不存在，返回 404" `
    "curl -s -i '$BASE/api/projects/$newProjId'" `
    @("-s","-i","$BASE/api/projects/$newProjId")
Emit ""

[System.IO.File]::WriteAllLines($OUT, $lines, $UTF8)
Write-Host "trace 已写出：$OUT  共 $($lines.Count) 行"
Write-Host "新增用户 id = $newUserId ; 新增项目 id = $newProjId"