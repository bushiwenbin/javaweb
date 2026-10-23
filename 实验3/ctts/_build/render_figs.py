# -*- coding: utf-8 -*-
"""根据真实源码与真实运行输出，渲染实验报告所需的截图素材。
输出宽度统一 950px，便于在 A4 正文（15.5cm）内缩放显示而不溢出。
"""
import json
import os
import re

from PIL import Image, ImageDraw, ImageFont

OUT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "figs")
os.makedirs(OUT, exist_ok=True)

W = 950
PAD = 18
FS = 15
LH = 22

F = "C:/Windows/Fonts/"
MONO = ImageFont.truetype(F + "consola.ttf", FS)
MONO_B = ImageFont.truetype(F + "consolab.ttf", FS)
CJK = ImageFont.truetype(F + "msyh.ttc", FS - 1)
CJK_B = ImageFont.truetype(F + "msyhbd.ttc", FS - 1)

C_TEXT = "#D4D4D4"
C_KEY = "#569CD6"
C_ANN = "#DCDCAA"
C_STR = "#CE9178"
C_CMT = "#6A9955"
C_TYPE = "#4EC9B0"
C_NUM = "#B5CEA8"
C_PUNC = "#D4D4D4"
C_GUTTER = "#858585"
C_CMDTXT = "#F0F0F0"
C_OK = "#4EC9B0"
C_ERR = "#F48771"
C_HDRV = "#9CDCFE"
C_DIM = "#7F7F7F"


def is_cjk(ch):
    o = ord(ch)
    return (0x2E80 <= o <= 0x9FFF) or (0xF900 <= o <= 0xFAFF) or (0xFF00 <= o <= 0xFFEF) \
        or (0x3000 <= o <= 0x303F) or (0x2000 <= o <= 0x206F)


def fonts_for(text, bold=False):
    """按字符切分成 (子串, 字体)：中文用微软雅黑，其余用 Consolas。
    注意 cur 必须初始化为 MONO，否则首个中文字符之前的分段会被误判为 CJK 字体，
    导致 measure() 与真实渲染宽度不一致（曾造成截图右侧文字被裁切）。"""
    def font_of(kind):
        if kind is CJK:
            return CJK_B if bold else CJK
        return MONO_B if bold else MONO

    runs, buf, cur = [], "", MONO
    for ch in text:
        want = CJK if is_cjk(ch) else MONO
        if want is not cur:
            if buf:
                runs.append((buf, font_of(cur)))
            buf, cur = "", want
        buf += ch
    if buf:
        runs.append((buf, font_of(cur)))
    return runs


def measure(text, bold=False):
    return sum(f.getlength(s) for s, f in fonts_for(text, bold))


# ------------------------------------------------------------ Java 词法着色
KEYWORDS = {
    "package", "import", "public", "private", "protected", "class", "interface", "enum",
    "extends", "implements", "return", "new", "void", "static", "final", "if", "else", "for",
    "while", "throw", "throws", "try", "catch", "this", "super", "null", "true", "false",
    "abstract", "default", "synchronized", "instanceof", "break", "continue", "boolean",
    "int", "long", "double", "float", "char", "byte", "short", "record", "var",
}


def java_tokens(line):
    out, i, n = [], 0, len(line)
    while i < n:
        ch = line[i]
        if line.startswith("//", i):
            out.append((line[i:], C_CMT))
            break
        if ch == '"':
            j = i + 1
            while j < n:
                if line[j] == "\\":
                    j += 2
                    continue
                if line[j] == '"':
                    j += 1
                    break
                j += 1
            out.append((line[i:j], C_STR))
            i = j
            continue
        if ch == "@":
            m = re.match(r"@[\w.]+", line[i:])
            out.append((m.group(0), C_ANN))
            i += len(m.group(0))
            continue
        if ch.isalpha() or ch in "_$":
            m = re.match(r"[\w$]+", line[i:])
            w = m.group(0)
            rest = line[i + len(w):].lstrip()
            if w in KEYWORDS:
                col = C_KEY
            elif w[0].isupper():
                col = C_TYPE
            elif rest.startswith("("):
                col = C_ANN
            else:
                col = C_TEXT
            out.append((w, col))
            i += len(w)
            continue
        if ch.isdigit():
            m = re.match(r"[\d.]+[fFlLdD]?", line[i:])
            out.append((m.group(0), C_NUM))
            i += len(m.group(0))
            continue
        out.append((ch, C_PUNC))
        i += 1
    return out


def render_code(path, start, end, out_name, title):
    src = open(path, encoding="utf-8").read().split("\n")
    lines = src[start - 1:end]
    hdr_h, body_h, top = 30, len(lines) * LH + PAD, 6
    img = Image.new("RGB", (W, hdr_h + body_h + top), "#1E1E1E")
    d = ImageDraw.Draw(img)
    d.rectangle([0, 0, W, hdr_h], fill="#2D2D30")
    d.text((PAD, 7), title, font=ImageFont.truetype(F + "msyh.ttc", 13), fill="#CCCCCC")
    d.line([0, hdr_h, W, hdr_h], fill="#3C3C3C", width=1)
    gut_w = 18 + int(measure(str(end)))

    y, in_block = hdr_h + top, False
    for i, ln in enumerate(lines, start=start):
        d.text((PAD, y), str(i), font=MONO, fill=C_GUTTER)
        x = PAD + gut_w
        if in_block:
            seg = ln
            if "*/" in ln:
                k = ln.index("*/") + 2
                seg, in_block = ln[:k], False
            out = [(seg, C_CMT)]
            if not in_block and len(seg) < len(ln):
                out += java_tokens(ln[len(seg):])
        elif ln.lstrip().startswith("/*"):
            if "*/" in ln:
                k = ln.index("*/") + 2
                out = [(ln[:k], C_CMT)] + java_tokens(ln[k:])
            else:
                in_block = True
                out = [(ln, C_CMT)]
        else:
            out = java_tokens(ln)
        for s, col in out:
            for seg, f in fonts_for(s):
                d.text((x, y), seg, font=f, fill=col)
                x += f.getlength(seg)
        y += LH
    img.save(os.path.join(OUT, out_name))
    print("saved", out_name, img.size)


# ------------------------------------------------------------ 通用行渲染
def render_lines(rows, out_name, title):
    """rows: 每行是一个 run 列表 [(text, color, bold), ...]"""
    hdr_h, top = 30, 14
    img = Image.new("RGB", (W, hdr_h + len(rows) * LH + top * 2), "#121314")
    d = ImageDraw.Draw(img)
    d.rectangle([0, 0, W, hdr_h], fill="#2D2D30")
    d.text((PAD, 7), title, font=ImageFont.truetype(F + "msyh.ttc", 13), fill="#CCCCCC")
    d.line([0, hdr_h, W, hdr_h], fill="#3C3C3C", width=1)
    y = hdr_h + top
    for row in rows:
        x = PAD
        for t, col, bold in row:
            for seg, f in fonts_for(t, bold):
                d.text((x, y), seg, font=f, fill=col)
                x += f.getlength(seg)
        y += LH
    img.save(os.path.join(OUT, out_name))
    print("saved", out_name, img.size)


def T(text, color=C_TEXT, bold=False):
    return [(text, color, bold)]


def T_wrap(text, color=C_TEXT, bold=False, cont="        "):
    """把过长的日志行折行，续行加缩进，避免右侧被裁切。"""
    parts = hard_split(text)
    out = []
    for i, p in enumerate(parts):
        out.append(T(p if i == 0 else cont + p.lstrip(), color, bold))
    return out


# ------------------------------------------------------------ JSON 处理
JTOK = re.compile(r'("(?:[^"\\]|\\.)*")|(-?\d+(?:\.\d+)?)|(true|false|null)|([{}\[\],:])')


def json_colorize(line):
    out, pos = [], 0
    for m in JTOK.finditer(line):
        if m.start() > pos:
            out.append((line[pos:m.start()], C_TEXT, False))
        tok = m.group(0)
        if m.group(1):
            rest = line[m.end():].lstrip()
            out.append((tok, C_HDRV if rest.startswith(":") else C_STR, False))
        elif m.group(2):
            out.append((tok, C_NUM, False))
        elif m.group(3):
            out.append((tok, C_KEY, False))
        else:
            out.append((tok, "#808080", False))
        pos = m.end()
    if pos < len(line):
        out.append((line[pos:], C_TEXT, False))
    return out


MAXW = W - 2 * PAD


def hard_split(line, maxw=MAXW):
    """兜底：单个原子过长时按字符硬切。"""
    out, cur = [], ""
    for ch in line:
        if measure(cur + ch) > maxw and cur:
            out.append(cur)
            cur = "  " + ch
        else:
            cur += ch
    if cur:
        out.append(cur)
    return out


def wrap_line(line, maxw=MAXW):
    """按真实像素宽度折行：在中文字符较多时字符数估算会失效，必须量像素。"""
    if measure(line) <= maxw:
        return [line]
    lead = len(line) - len(line.lstrip())
    ind = " " * (lead + 2)
    parts = line.split(", ")
    out, cur = [], ""
    for k, p in enumerate(parts):
        piece = p + ("," if k < len(parts) - 1 else "")
        if not cur:
            cur = piece
        elif measure(cur + " " + piece) <= maxw:
            cur += " " + piece
        else:
            out.append(cur)
            cur = ind + piece
    out.append(cur)
    res = []
    for l in out:
        res.extend(hard_split(l) if measure(l) > maxw else [l])
    return res


def fmt_json_lines(obj, indent=0):
    """JSON 美化：外层对象逐字段展开；数组元素（记录）压成一行，再由像素折行器折行。"""
    pad = "  " * indent
    res = []
    if isinstance(obj, dict):
        if not obj:
            return [pad + "{}"]
        items = list(obj.items())
        res.append(pad + "{")
        for i, (k, v) in enumerate(items):
            comma = "," if i < len(items) - 1 else ""
            key = json.dumps(k, ensure_ascii=False)
            if isinstance(v, dict) and v:
                sub = fmt_json_lines(v, indent + 1)
                sub[0] = "%s  %s: %s" % (pad, key, sub[0].lstrip())
                sub[-1] += comma
                res.extend(sub)
            elif isinstance(v, list) and v:
                res.append("%s  %s: [" % (pad, key))
                for j, it in enumerate(v):
                    txt = json.dumps(it, ensure_ascii=False)
                    if j < len(v) - 1:
                        txt += ","
                    res.append("%s    %s" % (pad, txt))
                res.append("%s  ]%s" % (pad, comma))
            else:
                res.append("%s  %s: %s%s" % (pad, key,
                                             json.dumps(v, ensure_ascii=False), comma))
        res.append(pad + "}")
    elif isinstance(obj, list):
        if not obj:
            return [pad + "[]"]
        res.append(pad + "[")
        for i, it in enumerate(obj):
            txt = json.dumps(it, ensure_ascii=False)
            if i < len(obj) - 1:
                txt += ","
            res.append(pad + "  " + txt)
        res.append(pad + "]")
    else:
        res.append(pad + json.dumps(obj, ensure_ascii=False))
    return res


def load_blocks(trace_path):
    txt = open(trace_path, encoding="utf-8").read()
    blocks = []
    for chunk in txt.split("═" * 56):
        chunk = chunk.strip()
        if not chunk:
            continue
        m = re.search(r"【(.+?)】\s*(.*)", chunk)
        cmd = re.search(r"^\$ (.+)$", chunk, re.M).group(1)
        status = re.search(r"^(HTTP/\S+ .+)$", chunk, re.M).group(1).strip()
        body = chunk.split("\n\n")[-1].strip()
        blocks.append(dict(label=m.group(1), desc=m.group(2), cmd=cmd,
                           status=status, body=body))
    return blocks


def truncate_records(obj, keep=3):
    """对超长的记录数组做显式截断，返回 (新对象, 省略条数)。"""
    if isinstance(obj, dict):
        for k, v in obj.items():
            nv, omitted = truncate_records(v, keep)
            if omitted:
                obj = dict(obj)
                obj[k] = nv
                return obj, omitted
    elif isinstance(obj, list):
        if obj and isinstance(obj[0], dict) and len(obj) > keep:
            return obj[:keep], len(obj) - keep
        for i, it in enumerate(obj):
            nv, omitted = truncate_records(it, keep)
            if omitted:
                obj = list(obj)
                obj[i] = nv
                return obj, omitted
    return obj, 0


def block_rows(b, mark, color="#E8C07D"):
    rows = [T(mark + "  " + b["desc"], color, True), T("")]
    rows += T_wrap("$ " + b["cmd"], C_CMDTXT, True, cont="      ")
    rows.append(T(""))
    rows.append(T(b["status"].strip(), C_OK if " 200" in b["status"] else C_HDRV, True))
    rows.append(T("Content-Type: application/json", C_DIM))
    rows.append(T(""))
    try:
        obj = json.loads(b["body"])
        obj, omitted = truncate_records(obj)
        for raw in fmt_json_lines(obj):
            for w in wrap_line(raw):
                rows.append(json_colorize(w))
        if omitted:
            rows.append(T("      …… 其余 %d 条结构相同，此处省略 ……" % omitted, C_CMT))
    except Exception:
        rows.append(T(b["body"]))
    rows.append(T(""))
    return rows


BASE = r"C:\Users\asus\OneDrive\Desktop\javaweb\实验2"
ctts = BASE + r"\ctts"


def chunk_code(path, title_prefix, out_prefix, max_lines=52):
    """把源码均衡切图；仅在切点附近 ±6 行的空行处断开，避免碎片化。"""
    lines = open(path, encoding="utf-8").read().split("\n")
    while lines and lines[-1].strip() == "":
        lines.pop()
    total = len(lines)
    n = max(1, -(-total // max_lines))

    def snap(t):
        """在 t 附近 ±6 行找空行作为切点，找不到就按 t 切。"""
        for k in range(min(t + 6, total), max(t - 6, 1) - 1, -1):
            if lines[k - 1].strip() == "":
                return k
        return t

    bounds = [0] + ([snap(round(total * k / n)) for k in range(1, n)]) + [total]
    bounds = sorted(set(bounds))
    for idx in range(1, len(bounds)):
        render_code(path, bounds[idx - 1] + 1, bounds[idx],
                    "%s_%d.png" % (out_prefix, idx),
                    "%s（第 %d–%d 行 / 共 %d 行）" % (title_prefix, bounds[idx - 1] + 1,
                                                     bounds[idx], total))


# ================================================================ 出图
if __name__ == "__main__":
    # 图2.1 项目目录结构
    rows = [T(r"C:\Users\asus\OneDrive\Desktop\javaweb\实验2\ctts> dir /s /b src", C_CMDTXT, True), T("")]
    for root, dirs, files in os.walk(ctts + r"\src"):
        dirs.sort()
        rel = os.path.relpath(root, ctts).replace("\\", "/")
        depth = 0 if rel == "src" else rel.count("/")
        rows.append(T("  " * depth + ("src" if rel == "src" else os.path.basename(root)), C_TYPE))
        for f in sorted(files):
            rows.append(T("  " * (depth + 1) + f))
    rows += [T(""), T("说明：实验二在实验一骨架上新增 controller / service / service.impl /", C_CMT),
             T("entity / dto / exception 六个包，共 10 个 Java 源文件。", C_CMT)]
    render_lines(rows, "fig_tree.png", "图 2.1  项目目录结构（实验二新增 Controller 层）")

    chunk_code(ctts + r"\src\main\java\com\gzu\ctts\controller\ProjectController.java",
               "ProjectController.java", "fig_ctrl", max_lines=56)
    chunk_code(ctts + r"\src\main\java\com\gzu\ctts\dto\ProjectForm.java",
               "ProjectForm.java", "fig_form", max_lines=40)
    chunk_code(ctts + r"\src\main\java\com\gzu\ctts\dto\Result.java",
               "Result.java", "fig_result", max_lines=70)

    al = open(BASE + r"\_build\app.log", encoding="utf-8").read().split("\n")
    srows = []
    for t in al[:25]:
        if "Tomcat started on port" in t or "Started CttsApplication" in t:
            srows += T_wrap(t, C_OK, True)
        elif ":: Spring Boot ::" in t:
            srows += T_wrap(t, C_TYPE, True)
        elif any(k in t for k in (".   ____", "/\\ ", "( ( )", "\\\\/ ", "'  |____", "=====")):
            srows += T_wrap(t, C_KEY)
        else:
            srows += T_wrap(t)
    render_lines(srows, "fig_startup.png", "图 2.4  项目启动日志（内嵌 Tomcat 监听 8080）")

    bl = load_blocks(BASE + r"\_build\api_trace.txt")
    print("blocks:", [b["label"] for b in bl])

    render_lines(block_rows(bl[0], "①"), "fig_api1.png",
                 "图 2.6  接口1 查询实训项目列表 —— 请求与响应")

    r2 = block_rows(bl[1], "②") + block_rows(bl[2], "② 异常") + block_rows(bl[3], "② 异常")
    render_lines(r2, "fig_api2.png", "图 2.8  接口2 按 id 查详情（含异常分支）")

    r3 = block_rows(bl[4], "③") + block_rows(bl[5], "③")
    render_lines(r3, "fig_api3.png", "图 2.10  接口3 分页查询（@RequestParam 接收页码与每页条数）")

    r3b = block_rows(bl[6], "③ 默认值") + block_rows(bl[7], "③ 异常")
    render_lines(r3b, "fig_api3b.png", "图 2.11  接口3 缺省参数与参数越界校验")

    r4 = block_rows(bl[8], "④") + block_rows(bl[9], "④ 异常")
    render_lines(r4, "fig_api4.png", "图 2.12  接口4 新增项目（@RequestBody）")

    rrows = []
    for t in al[26:]:
        if not t.strip():
            continue
        if "ProjectController - " in t:
            rrows += T_wrap(t, C_HDRV)
        elif "GlobalExceptionHandler" in t:
            rrows += T_wrap(t, C_ERR)
        elif "DispatcherServlet" in t:
            rrows += T_wrap(t, C_DIM)
        else:
            rrows += T_wrap(t)
    render_lines(rrows, "fig_runtime.png", "图 2.13  服务端运行日志 —— 参数接收与异常处理")
    print("ALL DONE")
