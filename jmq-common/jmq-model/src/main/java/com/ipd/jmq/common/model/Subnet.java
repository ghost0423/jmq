package com.ipd.jmq.common.model;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 子网
 */
public class Subnet extends BaseModel implements Serializable {
    private static Pattern P_RANGE1 = Pattern.compile("^([0-9]{1,3})\\.([0-9]{1,3})\\.([0-9]{1,3})$");
    private static Pattern P_RANGE2 = Pattern.compile("^([0-9]{1,3})\\.([0-9]{1,3})\\.([0-9]{1,3})\\-([0-9]{1,3})$");
    private static Pattern P_RANGE3 = Pattern.compile(
            "^([0-9]{1,3})\\.([0-9]{1,3})\\.([0-9]{1,3})\\.([0-9]{1,3})\\-([0-9]{1,3})\\.([0-9]{1,3})\\.([0-9]{1,3})"
                    + "\\.([0-9]{1,3})$");
    private static Pattern P_RANGE4 = Pattern.compile("^([0-9]{1,3})\\.([0-9]{1,3})\\.\\*$");

    // 代码
    private Identity dataCenter;
    // 地址
    private String cidr;
    // 描述
    private String description;

    public String getCidr() {
        return cidr;
    }

    public void setCidr(String cidr) {
        this.cidr = cidr;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Identity getDataCenter() {
        return dataCenter;
    }

    public void setDataCenter(Identity dataCenter) {
        this.dataCenter = dataCenter;
    }

    /**
     * IP匹配
     *
     * @param clientIp 客户端IP
     * @return 是否匹配
     */
    public boolean match(String clientIp) {
        if (clientIp == null) {
            return false;
        }
        if (cidr == null || cidr.isEmpty()) {
            return false;
        }
        // 去掉端口
        int pos = clientIp.indexOf(':');
        if (pos > 0) {
            clientIp = clientIp.substring(0, pos);
        }
        String[] parts = clientIp.split("[\\.]");
        String[] ips = this.cidr.split("[;,]");
        Matcher matcher;
        long beginIp;
        long endIp;
        for (String ip : ips) {
            ip = ip.trim();
            if (ip.isEmpty()) {
                continue;
            }
            matcher = P_RANGE3.matcher(ip);
            if (matcher.find()) {
                beginIp = toLong(matcher.group(1), matcher.group(2), matcher.group(3), matcher.group(4));
                endIp = toLong(matcher.group(5), matcher.group(6), matcher.group(7), matcher.group(8));
            } else {
                matcher = P_RANGE1.matcher(ip);
                if (matcher.find()) {
                    beginIp = toLong(matcher.group(1), matcher.group(2), matcher.group(3), "0");
                    endIp = toLong(matcher.group(1), matcher.group(2), matcher.group(3), "255");
                } else {
                    matcher = P_RANGE2.matcher(ip);
                    if (matcher.find()) {
                        beginIp = toLong(matcher.group(1), matcher.group(2), matcher.group(3), "0");
                        endIp = toLong(matcher.group(1), matcher.group(2), matcher.group(4), "255");
                    } else {
                        matcher = P_RANGE4.matcher(ip);
                        if (matcher.find()) {
                            beginIp = toLong(matcher.group(1), matcher.group(2), "0", "0");
                            endIp = toLong(matcher.group(1), matcher.group(2), "255", "255");
                        } else {
                            continue;
                        }
                    }
                }
            }
            long value = toLong(parts[0], parts[1], parts[2], parts[3]);
            if (value >= beginIp && value <= endIp) {
                return true;
            }
        }
        return false;
    }

    private static long toLong(final String p1, final String p2, final String p3, final String p4) {
        long[] ip = new long[4];
        ip[0] = Long.parseLong(p1);
        ip[1] = Long.parseLong(p2);
        ip[2] = Long.parseLong(p3);
        ip[3] = Long.parseLong(p4);
        return (ip[0] << 24) + (ip[1] << 16) + (ip[2] << 8) + ip[3];
    }
}
