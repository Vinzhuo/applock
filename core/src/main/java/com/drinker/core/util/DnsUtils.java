package com.drinker.core.util;

import android.text.TextUtils;

import java.util.HashSet;

/**
 * Created by zhuolin on 15-10-27.
 */
public class DnsUtils {


    private static HashSet<String> topDnsExcludeCountryCodeSet = new HashSet<>(40);

    static {
        topDnsExcludeCountryCodeSet.add("com");
        topDnsExcludeCountryCodeSet.add("top");
        topDnsExcludeCountryCodeSet.add("win");
        topDnsExcludeCountryCodeSet.add("net");
        topDnsExcludeCountryCodeSet.add("org");
        topDnsExcludeCountryCodeSet.add("wang");
        topDnsExcludeCountryCodeSet.add("gov");
        topDnsExcludeCountryCodeSet.add("edu");
        topDnsExcludeCountryCodeSet.add("mil");
        topDnsExcludeCountryCodeSet.add("biz");
        topDnsExcludeCountryCodeSet.add("name");
        topDnsExcludeCountryCodeSet.add("info");
        topDnsExcludeCountryCodeSet.add("mobi");
        topDnsExcludeCountryCodeSet.add("pro");
        topDnsExcludeCountryCodeSet.add("travel");
        topDnsExcludeCountryCodeSet.add("museum");
        topDnsExcludeCountryCodeSet.add("int");
        topDnsExcludeCountryCodeSet.add("aero");
        topDnsExcludeCountryCodeSet.add("post");
        topDnsExcludeCountryCodeSet.add("rec");
        topDnsExcludeCountryCodeSet.add("store");
        topDnsExcludeCountryCodeSet.add("web");
        topDnsExcludeCountryCodeSet.add("nom");
        topDnsExcludeCountryCodeSet.add("firm");
        topDnsExcludeCountryCodeSet.add("arts");
    }

    public static boolean isRegionDns(String dns) {
        if (TextUtils.isEmpty(dns)) {
            return false;
        }
        if (topDnsExcludeCountryCodeSet.contains(dns)) {
            return false;
        }
        return true;
    }

    public static String getDns(String url) {
        if (TextUtils.isEmpty(url)) {
            return "";
        }
        String dns = "";
        int index = url.indexOf("//");
        int start = 0;
        int end = url.length();
        if (index != -1) {
            start = index + 2;
        }
        index = url.indexOf("/", start);
        if (index != -1) {
            end = index;
        }
        dns = url.substring(start, end);
        end = dns.lastIndexOf(":");
        if (end != -1) {
            dns = dns.substring(0, end);
        }
        return dns;
    }
}
