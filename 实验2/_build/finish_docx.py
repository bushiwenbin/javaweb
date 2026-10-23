# -*- coding: utf-8 -*-
"""居中所有图注/表注段落，并填写「五、实验总结」。"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import docx

CAPTIONS = [
    "CTTS 工程目录结构",
    "统一响应封装 Result.java",
    "新增请求参数对象 ProjectForm.java",
    "第 1–54 行：类声明、构造器注入与接口 1",
    "第 55–107 行：接口 2 与接口 3",
    "第 108–160 行：接口 4 与请求头参数",
    "Spring Boot 启动日志",
    "接口1 查询实训项目列表",
    "接口2 按 id 查详情",
    "接口3 分页查询",
    "接口3 缺省参数与参数越界校验",
    "接口4 新增项目",
    "服务端运行日志",
    "实验二新增的包及职责",
    "ProjectService 提供的方法",
    "实训项目接口清单",
    "三种参数接收方式对比",
]

SUMMARY = (
    "本次实验在实验一的 CTTS 工程骨架上完成了 Controller 层的编写与联调，"
    "实现了实训项目列表、按 id 查详情、分页查询、新增项目四个接口，"
    "并通过真实 HTTP 请求逐一验证了 @RequestParam、@PathVariable、@RequestBody "
    "三类参数接收方式。\n"
    "\n"
    "主要收获有三点。其一，理解了请求映射的匹配机制。@RequestMapping 在类上提供统一前缀，"
    "方法级注解只需写相对路径；@GetMapping(\"/{id}\") 属于宽匹配，会把任何单段路径都吸进来，"
    "因此与路径变量同级的固定名词必须显式声明（如本实验的 /page），否则会被误匹配到详情接口上。\n"
    "\n"
    "其二，厘清了三种参数注解的分工。@RequestParam 对应 URL 查询串，适合可选的筛选与分页条件，"
    "配合 required=false 与 defaultValue 可以避免参数缺失报错；@PathVariable 对应 URL 路径占位符，"
    "适合表达资源标识，并会自动完成字符串到目标类型的转换；@RequestBody 对应请求体 JSON，"
    "适合结构较复杂的提交数据，必须与 Content-Type: application/json 搭配使用。\n"
    "\n"
    "其三，体会到统一响应与全局异常处理的价值。引入 Result<T> 之后，成功与失败返回同一个三段式结构，"
    "前端只需判断 code，不必再解析 HTTP 状态码；把 BizException、MethodArgumentNotValidException、"
    "ConstraintViolationException、MethodArgumentTypeMismatchException 等异常集中到 "
    "@RestControllerAdvice 中处理，Controller 里就只剩下成功路径，代码明显清爽。\n"
    "\n"
    "需要改进的地方：目前接口返回的是内存中的模拟数据，应用一重启数据就丢失，"
    "按班级规模统计等真实业务还需要接入数据库；此外本实验只做了手工调试，"
    "缺少对接口的自动化回归测试，后续可以补充 MockMvc 用例，"
    "并把参数校验规则与接口文档（OpenAPI）同步维护，减少前后端联调时的沟通成本。"
)


def main():
    # 1) 居中图注 / 表注
    ranges = []
    for cap in CAPTIONS:
        r = docx.call("doc_find", {"text": cap, "max_results": 8})
        locs = r.get("locations") or []
        # 题注段落一定以「图2.」或「表2.」开头，取 related_text 里不带空格前缀的那条
        picked = None
        for l in locs:
            rel = l.get("related_text", "")
            for pfx in ("图2.", "表2."):
                if pfx in rel:
                    picked = l
                    break
            if picked:
                break
        if not picked:
            print("!! 题注未定位:", cap)
            continue
        ranges.append({"begin": picked["begin"], "end": picked["end"]})
    if ranges:
        res = docx.call("doc_modify_paragraph", {"ranges": ranges, "jc": "center"})
        print("居中题注 %d 处: %s" % (len(ranges), str(res)[:200]))

    # 2) 填写实验总结
    res2 = docx.call("doc_set_table_cells",
                     {"table_id": "klzwd3ui",
                      "cells": [{"row": 11, "col": 1, "text": SUMMARY}]})
    print("实验总结:", str(res2)[:200])


if __name__ == "__main__":
    main()
