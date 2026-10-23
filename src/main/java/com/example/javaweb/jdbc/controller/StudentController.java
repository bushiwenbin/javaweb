package com.example.javaweb.jdbc.controller;

import com.example.javaweb.jdbc.Student;
import com.example.javaweb.jdbc.StudentDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 学生接口：把 JDBC 六步流程包装成 HTTP 接口，浏览器/Apifox 直接可测。
 *
 * <pre>
 *   GET    /api/students?minScore=60   查询（executeQuery + while(rs.next())）
 *   GET    /api/students/{id}          按主键查单个
 *   POST   /api/students               新增（executeUpdate）
 *   PUT    /api/students/{id}/score    改分数（executeUpdate）
 *   DELETE /api/students/{id}          删除（executeUpdate）
 *   POST   /api/students/batch         批量新增（executeBatch）
 * </pre>
 */
@Slf4j
@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentDao studentDao;

    public StudentController(StudentDao studentDao) {
        this.studentDao = studentDao;
    }

    /** 查询列表，可选按分数下限过滤 */
    @GetMapping
    public Map<String, Object> list(@RequestParam(required = false) Double minScore) throws SQLException {
        List<Student> list = studentDao.findByScore(minScore);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("total", list.size());
        data.put("list", list);
        return ok(data);
    }

    /** 按主键查单个 */
    @GetMapping("/{id}")
    public Map<String, Object> get(@PathVariable Integer id) throws SQLException {
        Student s = studentDao.findById(id);
        if (s == null) {
            return fail(404, "id=" + id + " 的学生不存在");
        }
        return ok(s);
    }

    /** 新增 */
    @PostMapping
    public Map<String, Object> create(@RequestBody Student s) throws SQLException {
        int rows = studentDao.insert(s);
        return ok("新增成功，影响行数 " + rows);
    }

    /** 改分数 */
    @PutMapping("/{id}/score")
    public Map<String, Object> updateScore(@PathVariable Integer id,
                                           @RequestParam Double score) throws SQLException {
        int rows = studentDao.updateScore(id, score);
        if (rows == 0) {
            return fail(404, "id=" + id + " 的学生不存在");
        }
        return ok("修改成功，影响行数 " + rows);
    }

    /** 删除 */
    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Integer id) throws SQLException {
        int rows = studentDao.deleteById(id);
        if (rows == 0) {
            return fail(404, "id=" + id + " 的学生不存在");
        }
        return ok("删除成功，影响行数 " + rows);
    }

    /** 批量新增，body 传学生数组 */
    @PostMapping("/batch")
    public Map<String, Object> createBatch(@RequestBody List<Student> list) throws SQLException {
        int rows = studentDao.insertBatch(list);
        return ok("批量新增 " + rows + " 条");
    }

    private Map<String, Object> ok(Object data) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("code", 200);
        result.put("data", data);
        return result;
    }

    private Map<String, Object> fail(int code, String msg) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("code", code);
        result.put("message", msg);
        return result;
    }
}