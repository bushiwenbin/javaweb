package com.example.javaweb.jdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * 学生数据访问层（DAO）：<b>完整演示 JDBC 六步流程</b>。
 *
 * <p>整个类只用原生 JDBC 写法，没用 JdbcTemplate / MyBatis，
 * 就是为了让「哪一步做了什么事」一目了然。
 *
 * <pre>
 * ① 加载驱动   —— DbUtil 构造时执行（整个应用只做一次）
 * ② 获取连接   —— dbUtil.getConnection()
 * ③ 创建语句   —— conn.prepareStatement(sql)
 * ④ 执行 SQL   —— pstmt.executeQuery() / pstmt.executeUpdate()
 * ⑤ 处理结果集 —— while (rs.next()) { rs.getXxx(...) }
 * ⑥ 关闭资源   —— try-with-resources，离开 try 自动倒序 close
 * </pre>
 *
 * <p>注意：连接池暂时没引入，每次操作都新建一条连接。
 * 生产项目应换成 HikariCP 连接池（spring-boot-starter-jdbc 自带）。
 */
@Repository
public class StudentDao {

    private static final Logger log = LoggerFactory.getLogger(StudentDao.class);

    private static final String COLUMNS = "id, name, gender, score, city, create_time";

    private final DbUtil dbUtil;

    public StudentDao(DbUtil dbUtil) {
        this.dbUtil = dbUtil;
    }

    // ================================================================
    // 场景 A：查询 —— 走完 executeQuery() 分支，需要处理 ResultSet
    // ================================================================

    /**
     * 按条件查询学生。
     *
     * @param minScore 分数下限，传 null 表示不按分数过滤
     * @return 查到的学生列表，没查到返回空列表（不是 null）
     */
    public List<Student> findByScore(Double minScore) throws SQLException {
        // ③ 创建语句：SQL 里用 ? 占位，值后面单独绑 —— 这样能防 SQL 注入
        StringBuilder sql = new StringBuilder("SELECT " + COLUMNS + " FROM student WHERE 1 = 1");
        if (minScore != null) {
            sql.append(" AND score >= ?");
        }
        sql.append(" ORDER BY score DESC");

        try (Connection conn = dbUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            // ④ 绑定参数并执行：setXxx 的下标从 1 开始，不是 0
            if (minScore != null) {
                pstmt.setDouble(1, minScore);
            }

            try (ResultSet rs = pstmt.executeQuery()) {   // ④ 执行查询
                return readAll(rs);                       // ⑤ 处理结果集
            }
        }
    }

    /**
     * 按主键查询单个学生，找不到返回 null。
     */
    public Student findById(Integer id) throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM student WHERE id = ?";

        try (Connection conn = dbUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                // while 只会执行一次：有数据读第一行并 true，没有数据直接 false
                if (rs.next()) {
                    return mapRow(rs);
                }
                return null;
            }
        }
    }

    // ================================================================
    // 场景 B：新增 —— 走 executeUpdate() 分支，返回受影响行数，没有 ResultSet
    // ================================================================

    /**
     * 新增一个学生，成功后回填自增主键。
     *
     * @return 受影响行数（1 = 成功）
     */
    public int insert(Student s) throws SQLException {
        String sql = "INSERT INTO student(name, gender, score, city) VALUES(?, ?, ?, ?)";

        try (Connection conn = dbUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, s.name());
            pstmt.setString(2, s.gender());
            pstmt.setDouble(3, s.score());
            pstmt.setString(4, s.city());

            int rows = pstmt.executeUpdate();     // ④ 执行增删改，返回受影响行数

            // 取回数据库生成的自增主键
            try (ResultSet keys = pstmt.getGeneratedKeys()) {
                if (keys.next()) {
                    log.info("新增成功，自增主键 = {}", keys.getInt(1));
                }
            }
            return rows;
        }
    }

    // ================================================================
    // 场景 C：修改
    // ================================================================

    /**
     * 按主键修改学生分数。
     */
    public int updateScore(Integer id, Double score) throws SQLException {
        String sql = "UPDATE student SET score = ? WHERE id = ?";

        try (Connection conn = dbUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDouble(1, score);   // 第 1 个 ? → 分数
            pstmt.setInt(2, id);          // 第 2 个 ? → 主键

            return pstmt.executeUpdate();
        }
    }

    // ================================================================
    // 场景 D：删除
    // ================================================================

    /**
     * 按主键删除学生。
     */
    public int deleteById(Integer id) throws SQLException {
        String sql = "DELETE FROM student WHERE id = ?";

        try (Connection conn = dbUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            return pstmt.executeUpdate();
        }
    }

    // ================================================================
    // 场景 E：批处理 —— 一次连接执行多条 SQL，效率远高于循环单条
    // ================================================================

    /**
     * 批量插入。
     */
    public int insertBatch(List<Student> list) throws SQLException {
        String sql = "INSERT INTO student(name, gender, score, city) VALUES(?, ?, ?, ?)";

        try (Connection conn = dbUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            for (Student s : list) {
                pstmt.setString(1, s.name());
                pstmt.setString(2, s.gender());
                pstmt.setDouble(3, s.score());
                pstmt.setString(4, s.city());
                pstmt.addBatch();        // 把这一组参数攒起来
            }

            int[] results = pstmt.executeBatch();   // ④ 一次性全部提交
            int total = 0;
            for (int r : results) {
                total += Math.max(r, 0);            // 批量里可能出现 0 或 -2
            }
            return total;
        }
    }

    // ================================================================
    // 场景 F：事务 —— 要么全成功，要么全回滚
    // ================================================================

    /**
     * 演示事务：先插入再更新，中间出异常就整体回滚。
     */
    public void insertAndUpdateInOneTx(Student s, Double newScore) throws SQLException {
        Connection conn = dbUtil.getConnection();
        try {
            conn.setAutoCommit(false);                       // 关闭自动提交

            try (PreparedStatement ins = conn.prepareStatement(
                    "INSERT INTO student(name, gender, score, city) VALUES(?, ?, ?, ?)")) {
                ins.setString(1, s.name());
                ins.setString(2, s.gender());
                ins.setDouble(3, s.score());
                ins.setString(4, s.city());
                ins.executeUpdate();
            }

            if (newScore == null || newScore < 0) {
                throw new IllegalArgumentException("分数非法，触发回滚：newScore=" + newScore);
            }

            try (PreparedStatement upd = conn.prepareStatement(
                    "UPDATE student SET score = ? WHERE id = (SELECT MAX(id) FROM student)")) {
                upd.setDouble(1, newScore);
                upd.executeUpdate();
            }

            conn.commit();                                    // 都成功才提交
            log.info("事务提交成功");
        } catch (Exception e) {
            conn.rollback();                                  // 任一步失败就回滚
            log.warn("事务已回滚：{}", e.getMessage());
            throw e;
        } finally {
            conn.setAutoCommit(true);
            conn.close();                                     // ⑥ 关闭资源
        }
    }

    // ================================================================
    // 第 ⑤ 步的两个小工具：把 ResultSet 翻成对象
    // ================================================================

    /** ⑤ 逐行遍历结果集 */
    private List<Student> readAll(ResultSet rs) throws SQLException {
        List<Student> list = new ArrayList<>();
        // rs 初始位置在第一行「之前」，next() 表示下移一行并返回是否还有数据
        while (rs.next()) {
            list.add(mapRow(rs));
        }
        log.info("JDBC 第⑤步 读到 {} 行", list.size());
        return list;
    }

    /** 从当前行按列名取值，组装成 Student */
    private Student mapRow(ResultSet rs) throws SQLException {
        Timestamp ts = rs.getTimestamp("create_time");
        return new Student(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("gender"),
                rs.getDouble("score"),
                rs.getString("city"),
                ts == null ? null : ts.toLocalDateTime()
        );
    }
}