package org.example.text.util;

import java.lang.management.ManagementFactory;
import com.sun.management.OperatingSystemMXBean;
import java.text.DecimalFormat;

public class SystemMonitor {
    private static final OperatingSystemMXBean osBean =
            (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
    private static final DecimalFormat df = new DecimalFormat("#.##");

    public static void printSystemInfo() {
        System.out.println("=== 系统资源监控信息 ===");
        // CPU信息
        System.out.println("CPU核心数: " + osBean.getAvailableProcessors());
        System.out.println("系统CPU负载: " + formatPercentage(osBean.getCpuLoad()));
        System.out.println("进程CPU负载: " + formatPercentage(osBean.getProcessCpuLoad()));
        System.out.println("系统负载平均值: " + df.format(osBean.getSystemLoadAverage()));

        // 内存信息
        long totalMemory = osBean.getTotalMemorySize();
        long freeMemory = osBean.getFreeMemorySize();
        long usedMemory = totalMemory - freeMemory;

        System.out.println("总物理内存: " + formatBytes(totalMemory));
        System.out.println("已用物理内存: " + formatBytes(usedMemory));
        System.out.println("可用物理内存: " + formatBytes(freeMemory));
        System.out.println("内存使用率: " + formatPercentage((double) usedMemory / totalMemory));
    }

    private static String formatPercentage(double value) {
        if (value < 0) return "N/A";
        return df.format(value * 100) + "%";
    }

    private static String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return df.format(bytes / 1024.0) + " KB";
        if (bytes < 1024 * 1024 * 1024) return df.format(bytes / (1024.0 * 1024)) + " MB";
        return df.format(bytes / (1024.0 * 1024 * 1024)) + " GB";
    }
    public static String getCPU() {
        return formatPercentage(osBean.getProcessCpuLoad());
    }
    public static String getMemory() {
        long totalMemory = osBean.getTotalMemorySize();
        long freeMemory = osBean.getFreeMemorySize();
        long usedMemory = totalMemory - freeMemory;
        return formatPercentage((double) usedMemory / totalMemory);
    }
}
