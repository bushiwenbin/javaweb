#!/usr/bin/env bash
# 重跑实验二全部接口并留存真实请求/响应证据
set -u
cd "C:/Users/asus/OneDrive/Desktop/javaweb/实验2"
PY="C:/Users/asus/.workbuddy/binaries/python/envs/default/Scripts/python.exe"

for i in $(seq 1 30); do
  if netstat -ano | grep "LISTENING" | grep -q ":8080"; then break; fi
  sleep 2
done
netstat -ano | grep "LISTENING" | grep ":8080" >/dev/null || { echo "服务未启动"; exit 1; }
echo "服务已就绪"

OUT=_build/api_trace.txt
: > "$OUT"

req() {
  local label="$1"; local desc="$2"; shift 2
  echo "════════════════════════════════════════════════════════" >> "$OUT"
  echo "【$label】   $desc" >> "$OUT"
  echo "\$ $*" >> "$OUT"
  echo "" >> "$OUT"
  eval "$@" >> "$OUT" 2>&1
  echo "" >> "$OUT"
  sleep 0.3
}

req "接口1" "查询实训项目列表（模拟数据）" \
  "curl -s -i 'http://localhost:8080/api/projects'"

req "接口2" "按 id 查询项目详情（@PathVariable，id=3）" \
  "curl -s -i 'http://localhost:8080/api/projects/3'"

req "接口2-异常" "项目不存在（id=999）" \
  "curl -s -i 'http://localhost:8080/api/projects/999'"

req "接口2-异常" "路径变量类型不匹配（id=abc）" \
  "curl -s -i 'http://localhost:8080/api/projects/abc'"

req "接口3" "分页查询 第1页 每页3条（@RequestParam）" \
  "curl -s -i 'http://localhost:8080/api/projects/page?pageNum=1&pageSize=3'"

req "接口3" "分页查询 第2页 每页3条" \
  "curl -s -i 'http://localhost:8080/api/projects/page?pageNum=2&pageSize=3'"

req "接口3" "不传参数，走 defaultValue 默认值" \
  "curl -s -i 'http://localhost:8080/api/projects/page'"

req "接口3-异常" "pageSize 超出 @Max 上限" \
  "curl -s -i 'http://localhost:8080/api/projects/page?pageNum=1&pageSize=999'"

req "接口4" "新增项目（@RequestBody + @RequestHeader）" \
  "curl -s -i -X POST 'http://localhost:8080/api/projects' -H 'Content-Type: application/json' -H 'Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.demo' -d '{\"name\":\"实训成果答辩系统\",\"category\":\"Web 后端\",\"teacher\":\"丁玺润\",\"deadline\":\"2027-01-08\",\"status\":1,\"description\":\"支持答辩分组、评分与结果公示。\"}'"

req "接口4-异常" "请求体 @NotBlank 校验失败" \
  "curl -s -i -X POST 'http://localhost:8080/api/projects' -H 'Content-Type: application/json' -d '{\"name\":\"\",\"category\":\"Web 后端\"}'"

echo "trace 采集完成"
"$PY" - <<'EOF'
import json, sys
sys.path.insert(0, '_build')
import render_figs as R
bl = R.load_blocks('_build/api_trace.txt')
print('接口1 条数 =', len(json.loads(bl[0]['body'])['data']))
print('接口3 total =', json.loads(bl[4]['body'])['data']['total'])
print('接口4 新增 id =', json.loads(bl[8]['body'])['data']['id'])
EOF
