package com.drinker.applock.applist.model.packages;

import java.util.List;


public interface PackageListener {

    /**
     * a data list changed
     * @param packages
     */
    void onPackagesChanged(List<PackageData> packages);


    /**
     * a single data updated
     * @param data
     */
    void onPackageUpdated(PackageData data, int position);

    /**
     * a single data removed
     * @param data
     */
    void onPackageRemoved(PackageData data, int position);

    /**
     * a single data added
     * @param data
     */
    void onPackageInserted(PackageData data, int position);

}
