# -*- coding: utf-8 -*-
"""本地 docx 编辑辅助：统一走 editor_sdk 的 edsdk.py，中文 payload 走 --json-file。"""
import json
import os
import subprocess
import sys

PY = r"C:\Users\asus\.workbuddy\binaries\python\envs\default\Scripts\python.exe"
SK = (r"C:\wkbuddy\WorkBuddy\resources\app.asar.unpacked\resources"
      r"\plugins\workbuddy-builtin\skills\tencent-local-office-edit")
EDSDK = os.path.join(SK, "edsdk.py")
TMP = os.path.join(os.path.dirname(os.path.abspath(__file__)), "_args.json")

DOC = r"C:\Users\asus\OneDrive\Desktop\javaweb\02.实验二 Controller 接口开发与参数接收.docx"
FID = "C://Users//asus//OneDrive//Desktop//javaweb//02.实验二 Controller 接口开发与参数接收.docx"


def call(tool, args=None, raw=False):
    args = dict(args or {})
    args.setdefault("file_id", FID)
    with open(TMP, "w", encoding="utf-8") as fp:
        json.dump(args, fp, ensure_ascii=False)
    r = subprocess.run([PY, EDSDK, "call", tool, "--json-file", TMP],
                       capture_output=True, cwd=SK)
    out = r.stdout.decode("utf-8", "replace").strip()
    if raw:
        return out
    try:
        return json.loads(out)
    except Exception:
        return {"_raw": out, "_stderr": r.stderr.decode("utf-8", "replace")[:400]}


def schema(tool):
    r = subprocess.run([PY, EDSDK, "schema", tool], capture_output=True, cwd=SK)
    return r.stdout.decode("utf-8", "replace")


if __name__ == "__main__":
    tool = sys.argv[1]
    payload = json.loads(sys.argv[2]) if len(sys.argv) > 2 else {}
    print(json.dumps(call(tool, payload), ensure_ascii=False, indent=1)[:4000])
