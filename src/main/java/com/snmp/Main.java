package com.snmp;

public class Main {
    public static void main(String[] args) {
        String targetIP = "140.143.122.67";
        String community = "public";

        SNMPManager manager = new SNMPManager(targetIP, community);

        System.out.println("========== SNMP管理软件演示 ==========\n");

        // 1. GET操作：获取系统描述
        System.out.println("【1. GET操作】获取系统描述:");
        String sysDescr = manager.snmpGet("1.3.6.1.2.1.1.1.0");
        System.out.println("系统描述: " + sysDescr);

        // 2. GET操作：获取系统运行时间
        System.out.println("\n【2. GET操作】获取系统运行时间:");
        String uptime = manager.snmpGet("1.3.6.1.2.1.1.3.0");
        System.out.println("运行时间: " + uptime);

        // 3. WALK操作：遍历系统信息
        System.out.println("\n【3. WALK操作】遍历系统信息组:");
        manager.snmpWalk("1.3.6.1.2.1.1");

        // 4. SET操作（可选，需要写权限）
        System.out.println("\n【4. SET操作】设置系统联系人:");
        boolean setResult = manager.snmpSet("1.3.6.1.2.1.1.4.0", "admin@example.com");
        System.out.println("SET结果: " + (setResult ? "成功" : "失败"));

        manager.close();
        System.out.println("\n========== 程序结束 ==========");
    }
}
