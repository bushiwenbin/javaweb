# -*- coding: utf-8 -*-
"""从模板备份重建实验二报告（可重复执行）。
坑：markdown 里的 ``` 代码块会导致后续内容被挤进下一行、覆盖「五、实验总结」标题，
    因此正文一律不使用围栏代码块。
"""
import json
import os
import shutil
import sys
import time

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import docx
from PIL import Image

HERE = os.path.dirname(os.path.abspath(__file__))
FIGS = os.path.join(HERE, "figs")
BACKUP = r"C:\dev\_backup\02.实验二_模板原件备份.docx"
W = 530
MARK = "MARKERSTEP2A7F"

FIG_PLAN = [
    ("@@FIG_RUNTIME@@", "fig_runtime.png"),
    ("@@FIG_API4@@", "fig_api4.png"),
    ("@@FIG_API3B@@", "fig_api3b.png"),
    ("@@FIG_API3@@", "fig_api3.png"),
    ("@@FIG_API2@@", "fig_api2.png"),
    ("@@FIG_API1@@", "fig_api1.png"),
    ("@@FIG_STARTUP@@", "fig_startup.png"),
    ("@@FIG_CTRL3@@", "fig_ctrl_3.png"),
    ("@@FIG_CTRL2@@", "fig_ctrl_2.png"),
    ("@@FIG_CTRL1@@", "fig_ctrl_1.png"),
    ("@@FIG_FORM@@", "fig_form_1.png"),
    ("@@FIG_RESULT@@", "fig_result_1.png"),
    ("@@FIG_TREE@@", "fig_tree.png"),
]

CAPTIONS = [
    "CTTS 工程目录结构", "统一响应封装 Result.java", "新增请求参数对象 ProjectForm.java",
    "第 1–54 行：类声明、构造器注入与接口 1", "第 55–107 行：接口 2 与接口 3",
    "第 108–160 行：接口 4 与请求头参数", "Spring Boot 启动日志",
    "接口1 查询实训项目列表", "接口2 按 id 查详情", "接口3 分页查询",
    "接口3 缺省参数与参数越界校验", "接口4 新增项目", "服务端运行日志",
    "实验二新增的包及职责", "ProjectService 提供的方法", "实训项目接口清单",
    "三种参数接收方式对比",
]


def log(*a):
    print(*a, flush=True)


def find(text, want=None):
    r = docx.call("doc_find", {"text": text, "max_results": 10})
    locs = r.get("locations") or []
    if want:
        for l in locs:
            if want in (l.get("related_text") or ""):
                return l
    return locs[0] if locs else None


def row_digest():
    """输出正文表格每行的段落数/图片数/首段文本，用于校验插入是否越界。"""
    import zipfile
    from lxml import etree
    Wn = "{http://schemas.openxmlformats.org/wordprocessingml/2006/main}"
    x = etree.fromstring(zipfile.ZipFile(docx.DOC).read("word/document.xml"))
    main = x.find(Wn + "body").findall(Wn + "tbl")[1]
    out = []
    for ri, tr in enumerate(main.findall(Wn + "tr"), 1):
        nps = len(tr.findall(".//" + Wn + "p"))
        nd = len(tr.findall(".//" + Wn + "drawing"))
        t = "".join(tr.itertext()).replace("\n", "")
        out.append((ri, nps, nd, t[:46]))
    return out


def list_tables():
    tl = docx.call("doc_list_tables")
    return tl.get("tables", tl)


def table_id_by(rows, cols):
    for t in list_tables():
        if t.get("row_count") == rows and t.get("col_count") == cols:
            return t["table_id"]
    return None


def left_align_nested_tables(main_id):
    """把 markdown 生成的嵌套表格全部改为左对齐，避免分散对齐把中文拉散。"""
    for t in list_tables():
        if t["table_id"] == main_id:
            continue
        if t.get("col_count") == 2 and t.get("row_count") == 7:
            continue  # 封面表
        cells = []
        for r in range(1, t["row_count"] + 1):
            for c in range(1, t["col_count"] + 1):
                cells.append({"row": r, "col": c, "inherit": False,
                              "horizontal_align": "left"})
        docx.call("doc_set_table_cells", {"table_id": t["table_id"], "cells": cells})
        log("      左对齐:", t["table_id"], t["row_count"], "x", t["col_count"])


def main():
    # 0) 还原备份
    docx.call("close_file", {})
    time.sleep(1.5)
    shutil.copy2(BACKUP, docx.DOC)
    log("[0] 已从备份还原模板")
    r = docx.call("open_file", {"file_path": docx.DOC, "open_with_existing": True})
    log("[0] open:", json.dumps(r, ensure_ascii=False)[:120])
    time.sleep(1.0)

    # 1) 封面
    cover_id = table_id_by(7, 2)
    log("[1] 封面表:", cover_id)
    docx.call("doc_set_table_cells", {"table_id": cover_id, "cells": [
        {"row": 4, "col": 2, "text": "软件工程2403"},
        {"row": 5, "col": 2, "text": "2400770225"},
        {"row": 6, "col": 2, "text": "皮宸西"},
        {"row": 7, "col": 2, "text": "皮宸西"}]})
    loc = find("2026年X月X日")
    if loc:
        docx.call("doc_replace_text", {"text": "2026年9月17日",
                                      "ranges": [{"begin": loc["begin"], "end": loc["end"]}]})
    log("[1] 封面完成")

    # 2) 删掉正文单元格里的示例表格
    for t in list_tables():
        if t.get("row_count") == 7 and t.get("col_count") == 4:
            docx.call("doc_delete_table", {"table_id": t["table_id"]})
            log("[2] 已删除示例表格", t["table_id"])
    main_id = table_id_by(14, 5)
    log("[2] 正文字表:", main_id)

    # 3) 步骤单元格写入标记
    docx.call("doc_set_table_cells", {"table_id": main_id,
                                     "cells": [{"row": 9, "col": 1, "text": MARK}]})
    loc = find(MARK)
    log("[3] 标记位置:", loc["begin"], loc["end"])

    # 4) 插入正文（全部块级内容一次写入）
    md = open(os.path.join(HERE, "step_body.md"), encoding="utf-8").read()
    r = docx.call("doc_insert_markdown", {"idx": loc["end"] + 1, "markdown": md})
    log("[4] 插入正文:", json.dumps(r, ensure_ascii=False)[:140])

    # 5) 校验：五、实验总结 是否还在
    chk = docx.call("doc_find", {"text": "五、实验总结"})
    log("[5] 五、实验总结 命中:", (chk.get("locations") or [{}])[0].get("begin"),
        " 条数:", chk.get("total"))
    for row in row_digest():
        log("     r%-3d 段落%3d 图%2d | %s" % row)

    # 6) 插图（从后往前）
    for ph, fname in FIG_PLAN:
        l = find(ph)
        if not l:
            log("!! 未找到", ph)
            continue
        path = os.path.join(FIGS, fname)
        sw, sh = Image.open(path).size
        h = round(W * sh / sw)
        docx.call("doc_modify_paragraph", {"ranges": [{"begin": l["begin"], "end": l["end"]}],
                                           "jc": "center"})
        docx.call("doc_insert_image", {"idx": l["begin"], "image_path": path, "w": W, "h": h})
        docx.call("doc_find_and_replace", {"old_text": ph, "new_text": ""})
    log("[6] 插图完成")

    l = find(MARK)
    if l:
        docx.call("doc_delete_paragraph", {"idx": l["begin"]})
    log("[6] 标记已删")

    # 7) 居中题注 + 嵌套表格左对齐
    ranges = []
    for cap in CAPTIONS:
        l = find(cap)
        if not l:
            log("!! 题注未定位:", cap)
            continue
        ranges.append({"begin": l["begin"], "end": l["end"]})
    docx.call("doc_modify_paragraph", {"ranges": ranges, "jc": "center"})
    log("[7] 居中题注", len(ranges))

    # 7b) 把 markdown 生成的嵌套表格改为左对齐（避免分散对齐拉散中文）
    left_align_nested_tables(main_id)

    # 8) 实验总结
    summ = open(os.path.join(HERE, "summary.txt"), encoding="utf-8").read()
    docx.call("doc_set_table_cells", {"table_id": main_id,
                                     "cells": [{"row": 11, "col": 1, "text": summ}]})
    log("[8] 实验总结已填")

    # 9) 保存
    r = docx.call("save_file", {"file_path": docx.DOC})
    log("[9] 保存:", json.dumps(r, ensure_ascii=False)[:160])
    for row in row_digest():
        log("     r%-3d 段落%3d 图%2d | %s" % row)


if __name__ == "__main__":
    main()
