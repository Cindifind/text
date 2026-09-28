package org.example.text.util;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.*;

public class IpUtil {

    // 匹配 IPv6 地址（含 :: 压缩、可选 %scope）
    private static final Pattern IPV6 = Pattern.compile(
            "([0-9a-fA-F]{1,4}(?::[0-9a-fA-F]{0,4}){2,7}(?:%[0-9a-zA-Z]+)?)");

    /** 地址信息模型 */
    public static class IPv6Info {
        final String address;        // 纯地址（不含 %scope）
        final String iface;          // 网卡名
        final boolean temporary;     // 是否临时
        final boolean deprecated;    // 是否弃用
        final long preferredSec;     // 首选寿命（秒），-1=未知，Long.MAX_VALUE=infinite
        final long validSec;         // 有效寿命（秒）

        IPv6Info(String address, String iface, boolean temporary,
                 boolean deprecated, long preferredSec, long validSec) {
            this.address = address;
            this.iface = iface;
            this.temporary = temporary;
            this.deprecated = deprecated;
            this.preferredSec = preferredSec;
            this.validSec = validSec;
        }

        /** 是否可用的稳定地址：非临时、未弃用、首选寿命 > 0 */
        boolean isUsableStable() {
            return !temporary && !deprecated
                    && preferredSec != 0
                    && !address.toLowerCase().startsWith("fe80")
                    && !address.equals("::1");
        }

        /** 获取纯地址（不含 %scope） */
        public String getAddress() {
            return address;
        }

        @Override
        public String toString() {
            return String.format("%-46s iface=%-10s temp=%-5s deprecated=%-5s pref=%ss valid=%ss",
                    address, iface, temporary, deprecated,
                    preferredSec == Long.MAX_VALUE ? "infinite" : preferredSec,
                    validSec == Long.MAX_VALUE ? "infinite" : validSec);
        }
    }

    // ==================== 入口 ====================

    public static void main(String[] args) throws Exception {
        List<IPv6Info> all = listAll();

        System.out.println("=== 全部全局 IPv6（含临时/弃用） ===");
        all.forEach(System.out::println);

        System.out.println("\n=== 可用的稳定地址（按寿命排序） ===");
        List<IPv6Info> stable = getUsableStable(all);
        stable.forEach(System.out::println);

        System.out.println("\n=== 寿命最长的稳定地址（推荐使用） ===");
        IPv6Info best = getLongestLivedStable(all);
        System.out.println(best == null ? "(无)" : best.address);
    }

    // ==================== 对外方法 ====================

    /** 列出所有非链路本地、非回环 IPv6 */
    public static List<IPv6Info> listAll() throws Exception {
        String os = System.getProperty("os.name", "").toLowerCase();
        if (os.contains("win")) {
            return listWindows();
        } else if (os.contains("linux") || os.contains("mac") || os.contains("nix")) {
            return listLinux();
        }
        throw new UnsupportedOperationException("Unsupported OS: " + os);
    }

    /** 所有可用的稳定地址（按首选寿命降序） */
    public static List<IPv6Info> getUsableStable(List<IPv6Info> all) {
        List<IPv6Info> list = new ArrayList<>();
        for (IPv6Info info : all) {
            if (info.isUsableStable()) list.add(info);
        }
        list.sort((a, b) -> Long.compare(b.preferredSec, a.preferredSec));
        return list;
    }

    /**
     * 取寿命最长的稳定地址（同网卡内取首选寿命最大者）。
     * 这是你要的“稳定地址”。
     */
    public static IPv6Info getLongestLivedStable(List<IPv6Info> all) {
        // 先按网卡分组
        Map<String, List<IPv6Info>> byIface = new LinkedHashMap<>();
        for (IPv6Info info : all) {
            if (!info.isUsableStable()) continue;
            byIface.computeIfAbsent(info.iface, k -> new ArrayList<>()).add(info);
        }
        // 每个网卡内取寿命最长
        IPv6Info best = null;
        for (List<IPv6Info> group : byIface.values()) {
            group.sort((a, b) -> Long.compare(b.preferredSec, a.preferredSec));
            IPv6Info candidate = group.get(0);
            if (best == null || candidate.preferredSec > best.preferredSec) {
                best = candidate;
            }
        }
        return best;
    }

    // ==================== Windows 实现 ====================

    private static List<IPv6Info> listWindows() throws Exception {
        List<IPv6Info> result = new ArrayList<>();
        Process p = new ProcessBuilder(
                "netsh", "interface", "ipv6", "show", "addresses")
                .redirectErrorStream(true).start();

        Charset cs = System.getProperty("user.language", "")
                .toLowerCase().startsWith("zh")
                ? Charset.forName("GBK") : StandardCharsets.UTF_8;

        String currentIface = "unknown";
        try (BufferedReader r = new BufferedReader(
                new InputStreamReader(p.getInputStream(), cs))) {
            String line;
            while ((line = r.readLine()) != null) {
                String t = line.trim();
                if (t.isEmpty()) continue;

                if (t.startsWith("接口") || t.startsWith("Interface")) {
                    int idx = t.indexOf(':');
                    if (idx > 0) currentIface = t.substring(idx + 1).trim();
                    continue;
                }
                if (t.startsWith("---")) continue;
                if (t.startsWith("地址类型") || t.startsWith("Address Type")) continue;

                Matcher m = IPV6.matcher(t);
                String ip = null;
                while (m.find()) ip = m.group(1);
                if (ip == null) continue;

                String pure = stripScope(ip);
                if (pure.toLowerCase().startsWith("fe80") || pure.equals("::1")) continue;

                String lower = t.toLowerCase();
                boolean temp = t.contains("临时") || lower.contains("temporary");
                boolean deprecated = t.contains("反对") || lower.contains("deprecated");

                long[] lt = parseLifetimesWin(t); // [preferred, valid]
                long preferred = lt[0], valid = lt[1];
                if (preferred == 0) deprecated = true;

                result.add(new IPv6Info(pure, currentIface, temp,
                        deprecated, preferred, valid));
            }
        }
        p.waitFor();
        return result;
    }

    /** 解析行里的两个寿命，返回 [preferred, valid]（秒） */
    private static long[] parseLifetimesWin(String line) {
        List<String> found = new ArrayList<>();
        Matcher m = Pattern.compile(
                        "(infinite|\\d+d\\d+h\\d+m\\d+s|\\d+h\\d+m\\d+s|\\d+m\\d+s|\\d+s)")
                .matcher(line);
        while (m.find()) found.add(m.group(1));

        long valid = -1, preferred = -1;
        if (found.size() >= 2) {
            valid = parseDuration(found.get(found.size() - 2));
            preferred = parseDuration(found.get(found.size() - 1));
        } else if (found.size() == 1) {
            valid = parseDuration(found.get(0));
        }
        return new long[]{preferred, valid};
    }

    private static long parseDuration(String s) {
        if (s == null) return -1;
        if (s.equals("infinite")) return Long.MAX_VALUE;
        long total = 0;
        Matcher m = Pattern.compile("(\\d+)([dhms])").matcher(s);
        while (m.find()) {
            long v = Long.parseLong(m.group(1));
            switch (m.group(2)) {
                case "d": total += v * 86400; break;
                case "h": total += v * 3600;  break;
                case "m": total += v * 60;    break;
                case "s": total += v;         break;
            }
        }
        return total;
    }

    // ==================== Linux 实现 ====================

    private static List<IPv6Info> listLinux() throws Exception {
        List<IPv6Info> result = new ArrayList<>();
        Process p = new ProcessBuilder("ip", "-6", "addr", "show")
                .redirectErrorStream(true).start();

        String currentIface = "unknown";
        String pendingAddr = null;
        String pendingFlags = null;

        try (BufferedReader r = new BufferedReader(
                new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = r.readLine()) != null) {
                String t = line.trim();

                Matcher im = Pattern.compile("^\\d+:\\s+([^:]+):").matcher(t);
                if (im.find()) {
                    currentIface = im.group(1).trim();
                    continue;
                }

                Matcher am = Pattern.compile(
                        "^inet6\\s+([0-9a-fA-F:]+)(?:/\\d+)?\\s+(.*)$").matcher(t);
                if (am.find()) {
                    pendingAddr = am.group(1);
                    pendingFlags = am.group(2).toLowerCase();
                    continue;
                }

                if (t.startsWith("valid_lft") && pendingAddr != null) {
                    long[] lt = parseLftLinux(t); // [valid, preferred]
                    long valid = lt[0], preferred = lt[1];

                    boolean linkLocal = pendingFlags.contains("scope link");
                    boolean loopback = pendingAddr.equals("::1");
                    boolean temp = pendingFlags.contains("temporary");
                    boolean deprecated = pendingFlags.contains("deprecated") || preferred == 0;

                    if (!linkLocal && !loopback) {
                        result.add(new IPv6Info(pendingAddr, currentIface,
                                temp, deprecated, preferred, valid));
                    }
                    pendingAddr = null;
                    pendingFlags = null;
                }
            }
        }
        p.waitFor();
        return result;
    }

    /** 解析 "valid_lft 100sec preferred_lft 50sec"，返回 [valid, preferred] */
    private static long[] parseLftLinux(String line) {
        long valid = -1, preferred = -1;
        Matcher m = Pattern.compile(
                "valid_lft\\s+(\\S+)\\s+preferred_lft\\s+(\\S+)").matcher(line);
        if (m.find()) {
            valid = parseSec(m.group(1));
            preferred = parseSec(m.group(2));
        }
        return new long[]{valid, preferred};
    }

    private static long parseSec(String s) {
        if (s == null) return -1;
        if (s.equals("forever")) return Long.MAX_VALUE;
        s = s.replace("sec", "").trim();
        try {
            return Long.parseLong(s);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    // ==================== 工具 ====================

    private static String stripScope(String ip) {
        int i = ip.indexOf('%');
        return i > 0 ? ip.substring(0, i) : ip;
    }
}