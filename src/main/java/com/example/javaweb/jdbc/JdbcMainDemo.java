package com.example.javaweb.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;

/**
 * JDBC 六步流程 —— <b>纯 main 方法版本</b>（不依赖 Spring，直接右键 Run 就能跑）。
 *
 * <p>和 {@link StudentDao} 的区别：这个类是「教学演示版」，
 * 六步一句一句摆在一起，方便在 IDEA 里打断点单步看；
 * StudentDao 是「工程版」，做了参数拼接、异常包装、分层封装。
 *
 * <p>运行方式：IDEA 里右键 → Run 'JdbcMainDemo.main()'
 */
public class JdbcMainDemo {

    // ===== 连接信息（和 application.properties 里保持一致）=====
    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final String URL = "jdbc:mysql://localhost:3306/jdbc_demo"
            + "?useSSL=false&serverTimezone=Asia/Shanghai"
            + "&characterEncoding=utf8&allowPublicKeyRetrieval=true";
    private static final String USER = "root";
    private static final String PASSWORD = "123456";

    public static void main(String[] args) {
        // ================================================================
        // JDBC 第 ① 步：加载驱动
        // 把驱动类加载进 JVM，驱动类里的 static 块会把自己注册进 DriverManager
        // ================================================================
        try {
            Class.forName(DRIVER);
            System.out.println("① 加载驱动成功：" + DRIVER);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("驱动类找不到，检查 mysql-connector-j 依赖", e);
        }

        // ================================================================
        // JDBC 第 ② 步：获取连接
        // DriverManager 按第 ① 步注册好的驱动去建一条物理 TCP 连接
        // 第 ③～⑥ 步全部依赖这个 conn 对象
        // ================================================================
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            System.out.println("② 获取连接成功：" + conn.getMetaData().getURL());

            // ============================================================
            // JDBC 第 ③ 步：创建 PreparedStatement
            // SQL 里用 ? 占位，参数值不写进 SQL 字符串 —— 这是防 SQL 注入的关键
            // ============================================================
            String sql = "SELECT id, name, gender, score, city, create_time "
                    + "FROM student WHERE score >= ? ORDER BY score DESC";

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

                // 第 ④ 步前半：绑定参数。注意 setXxx 的下标从 1 开始，不是 0
                pstmt.setDouble(1, 60.0);

                // 第 ④ 步后半：executeQuery 执行查询，返回 ResultSet
                try (ResultSet rs = pstmt.executeQuery()) {

                    // 第 ⑤ 步：处理结果集
                    // rs 的指针初始在第一行「之前」，next() = 下移一行，返回是否还有数据
                    System.out.println("③ 创建语句 / ④ 执行查询 / ⑤ 处理结果集：");
                    System.out.printf("%-5s %-10s %-6s %-7s %-8s %s%n",
                            "id", "姓名", "性别", "分数", "城市", "创建时间");
                    int count = 0;
                    while (rs.next()) {
                        int id = rs.getInt("id");
                        String name = rs.getString("name");
                        String gender = rs.getString("gender");
                        double score = rs.getDouble("score");
                        String city = rs.getString("city");
                        Timestamp createTime = rs.getTimestamp("create_time");

                        System.out.printf("%-5d %-10s %-6s %-7.1f %-8s %s%n",
                                id, name, gender, score, city, createTime);
                        count++;
                    }
                    System.out.println("共读到 " + count + " 行");
                }
            }

            // ------------------------------------------------------------
            // 附：增 / 改 / 删 走的是 executeUpdate()，没有 ResultSet
            // ------------------------------------------------------------

            // 新增
            String insertSql = "INSERT INTO student(name, gender, score, city) VALUES(?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                pstmt.setString(1, "JDBC新增");
                pstmt.setString(2, "男");
                pstmt.setDouble(3, 77.7);
                pstmt.setString(4, "昆明");
                int rows = pstmt.executeUpdate();
                System.out.println("④ executeUpdate 新增，影响行数：" + rows);
            }

            // 修改
            String updateSql = "UPDATE student SET score = ? WHERE name = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
                pstmt.setDouble(1, 66.6);
                pstmt.setString(2, "JDBC新增");
                int rows = pstmt.executeUpdate();
                System.out.println("④ executeUpdate 修改，影响行数：" + rows);
            }

            // 删除
            String deleteSql = "DELETE FROM student WHERE name = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(deleteSql)) {
                pstmt.setString(1, "JDBC新增");
                int rows = pstmt.executeUpdate();
                System.out.println("④ executeUpdate 删除，影响行数：" + rows);
            }

            // ============================================================
            // JDBC 第 ⑥ 步：关闭资源
            // 这里不写任何 close() 代码 —— try-with-resources 会在离开 try 块时
            // 按【声明的逆序】自动关闭：先关 rs，再关 pstmt，最后关 conn
            // ============================================================
            System.out.println("⑥ 离开 try 块，rs / pstmt / conn 已按逆序自动关闭");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}