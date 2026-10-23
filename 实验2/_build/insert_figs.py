# -*- coding: utf-8 -*-
"""把步骤正文里的 @@FIG_xxx@@ 占位符替换为真实截图（从后往前处理，避免坐标漂移）。"""
import json
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import docx

HERE = os.path.dirname(os.path.abspath(__file__))
FIGS = os.path.join(HERE, "figs")

W = 530

# 占位符 -> (图片文件名, 原始高度)
PLAN = [
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


def find(text):
    r = docx.call("doc_find", {"text": text, "max_results": 3})
    locs = r.get("locations") or []
    if not locs:
        return None
    return locs[0]


def main():
    for ph, fname in PLAN:
        loc = find(ph)
        if not loc:
            print("!! 未找到 %s" % ph)
            continue
        b, e = loc["begin"], loc["end"]
        path = os.path.join(FIGS, fname)
        from PIL import Image
        sw, sh = Image.open(path).size
        h = round(W * sh / sw)

        r1 = docx.call("doc_modify_paragraph", {"ranges": [{"begin": b, "end": e}], "jc": "center"})
        r2 = docx.call("doc_insert_image", {"idx": b, "image_path": path, "w": W, "h": h})
        r3 = docx.call("doc_find_and_replace", {"old_text": ph, "new_text": ""})
        ok = "insert_image" in json.dumps(r2, ensure_ascii=False) or "ok" in json.dumps(r2, ensure_ascii=False)
        print("%-18s -> %s  w=%d h=%d  %s" % (ph, fname, W, h, "OK" if ok else json.dumps(r2, ensure_ascii=False)[:160]))

    # 删除临时标记段落
    loc = find("MARKERSTEP2A7F")
    if loc:
        r = docx.call("doc_delete_paragraph", {"idx": loc["begin"]})
        print("marker deleted:", json.dumps(r, ensure_ascii=False)[:160])


if __name__ == "__main__":
    main()
