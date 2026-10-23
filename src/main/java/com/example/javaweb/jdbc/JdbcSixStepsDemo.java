package com.example.javaweb.jdbc;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

/**
 * JDBC 六步流程「逐行打印版」演示器。
 *
 * <p>Spring Boot 启动完成后会自动执行这段代码，
 * 控制台会按 ①~⑥ 的顺序把每一步打印出来，方便对照流程图核对。
 *
 * <p>想让它跑：直接启动应用即可；不想看：把下面 @Component 删掉或注释掉。
 */
@Component
public class JdbcSixStepsDemo implements CommandLineRunner {

    private final StudentDao studentDao;

    public JdbcSixStepsDemo(StudentDao studentDao) {
        this.studentDao = studentDao;
    }

    @Override
    public void run(String... args) {
        System.out.println();
        System.out.println("==================================================================");
        System.out.println("                JDBC 六步流程 · 完整走一遍");
        System.out.println("==================================================================");

        try {
            step1And2();
            step3To5_Query();
            step3To4_Insert();
            step3To4_Update();
            step3To4_Delete();
            step6_Batch();
            step6_Transaction();
        } catch (Exception e) {
            System.out.println("演示过程中出现异常：" + e.getMessage());
        }

        System.out.println("==================================================================");
        System.out.println("                  JDBC 六步流程演示结束");
        System.out.println("==================================================================");
        System.out.println();
    }

    /** ① 加载驱动 + ② 获取连接 */
    private void step1And2() throws Exception {
        System.out.println();
        System.out.println("--- ① 加载驱动 + ② 获取连接 ---");
        System.out.println("  ① 驱动已在 DbUtil 构造时加载：com.mysql.cj.jdbc.Driver");

        // ② 单独要一条连接，验证连接可用后立刻关掉
        DbUtil dbUtil = new DbUtil(new JdbcConfig(
                "com.mysql.cj.jdbc.Driver",
                "jdbc:mysql://localhost:3306/jdbc_demo?useSSL=false&serverTimezone=Asia/Shanghai"
                        + "&characterEncoding=utf8&allowPublicKeyRetrieval=true",
                "root", "123456"));

        try (Connection conn = dbUtil.getConnection()) {
            System.out.println("  ✓ 连接成功，数据库版本：" + conn.getMetaData().getDatabaseProductVersion()
                    + "，协议：" + conn.getMetaData().getURL());
            System.out.println("  ⑥ 连接已在 try 块结束时自动关闭");
        }
    }

    /** ③ 创建语句 + ④ executeQuery + ⑤ 处理结果集 */
    private void step3To5_Query() throws Exception {
        System.out.println();
        System.out.println("--- ③ 创建语句 + ④ 执行查询 + ⑤ 处理结果集 ---");
        List<Student> all = studentDao.findByScore(null);
        System.out.println("  ⑤ 遍历结果集，共读到 " + all.size() + " 行：");
        System.out.printf("     %-5s %-10s %-6s %-7s %-8s %s%n", "id", "姓名", "性别", "分数", "城市", "创建时间");
        for (Student s : all) {
            System.out.printf("     %-5d %-10s %-6s %-7.1f %-8s %s%n",
                    s.id(), s.name(), s.gender(), s.score(), s.city(),
                    s.createTime() == null ? "-" : s.createTime().toLocalDate());
        }
    }

    /** ③ 创建语句 + ④ executeUpdate（新增） */
    private void step3To4_Insert() throws Exception {
        System.out.println();
        System.out.println("--- ③ 创建语句 + ④ executeUpdate（新增一条） ---");
        int rows = studentDao.insert(new Student(null, "赵六", "男", 85.5, "杭州", null));
        System.out.println("  ✓ INSERT 影响行数：" + rows);
    }

    /** ③ 创建语句 + ④ executeUpdate（修改） */
    private void step3To4_Update() throws Exception {
        System.out.println();
        System.out.println("--- ③ 创建语句 + ④ executeUpdate（改分数） ---");
        List<Student> top = studentDao.findByScore(90.0);
        if (top.isEmpty()) {
            System.out.println("  没有分数 >= 90 的学生，跳过");
            return;
        }
        Integer id = top.get(0).id();
        int rows = studentDao.updateScore(id, 99.0);
        System.out.println("  ✓ UPDATE 影响行数：" + rows + "（把 id=" + id + " 的分数改成 99.0）");
    }

    /** ③ 创建语句 + ④ executeUpdate（删除） */
    private void step3To4_Delete() throws Exception {
        System.out.println();
        System.out.println("--- ③ 创建语句 + ④ executeUpdate（删掉分数最低的一条） ---");
        List<Student> all = studentDao.findByScore(0.0);
        if (all.isEmpty()) {
            System.out.println("  表里没数据，跳过");
            return;
        }
        Student last = all.get(all.size() - 1);   // 已经按分数倒序排了，末尾就是最低分
        int rows = studentDao.deleteById(last.id());
        System.out.println("  ✓ DELETE 影响行数：" + rows + "（删掉 " + last.name() + "）");
    }

    /** ④ executeBatch 批处理 */
    private void step6_Batch() throws Exception {
        System.out.println();
        System.out.println("--- ④ executeBatch（一次插 3 条） ---");
        int rows = studentDao.insertBatch(List.of(
                new Student(null, "批一", "男", 70.0, "西安", null),
                new Student(null, "批二", "女", 71.0, "武汉", null),
                new Student(null, "批三", "男", 72.0, "南京", null)
        ));
        System.out.println("  ✓ 批量插入成功，共 " + rows + " 行");
    }

    /** ⑥ 事务：要么全成功要么全回滚 */
    private void step6_Transaction() {
        System.out.println();
        System.out.println("--- ⑥ 事务：故意传入非法分数，观察回滚 ---");
        try {
            studentDao.insertAndUpdateInOneTx(
                    new Student(null, "事务测试", "女", 66.0, "长沙", null), -1.0);
        } catch (Exception e) {
            System.out.println("  ✓ 已按预期抛出异常并回滚，表中不会留下「事务测试」这条数据");
        }
    }
}