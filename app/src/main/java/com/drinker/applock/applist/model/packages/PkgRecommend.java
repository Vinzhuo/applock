package com.drinker.applock.applist.model.packages;

/**
 * Created by zhuolin on 16/2/3.
 */
public class PkgRecommend {
    final private String pkgName;
    final private int pkgType;
    final private int indiaRatio;
    final private int arRatio;
    final private int inRatio;
    final private int ruRatio;

    public PkgRecommend(String name, int type, int india, int ar, int in, int ru) {
        this.pkgName = name;
        this.pkgType = type;
        this.indiaRatio = india;
        this.arRatio = ar;
        this.inRatio = in;
        this.ruRatio = ru;
    }

    public int getPkgType() {
        return pkgType;
    }

    public int getIndiaRatio() {
        return indiaRatio;
    }

    public int getArRatio() {
        return arRatio;
    }

    public int getInRatio() {
        return inRatio;
    }

    public int getRuRatio() {
        return ruRatio;
    }

    public int getRatioWithLang(String locale) {
        if ("ru".equals(locale)) {
            return ruRatio;
        }
        if ("in".equals(locale)) {
            return inRatio;
        }
        if ("ar".equals(locale)) {
            return arRatio;
        }
        return indiaRatio;
    }

}
