package com.wkswind.plugin.component.api.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import java.util.HashSet;
import java.util.Set;

import static com.wkswind.plugin.component.api.utils.Consts.COMPONENT_GEN_PACKAGENAME;
import static com.wkswind.plugin.component.api.utils.Consts.COMPONENT_SP;
import static com.wkswind.plugin.component.api.utils.Consts.GENS_CACHE;
import static com.wkswind.plugin.component.api.utils.Consts.LAST_VERSION_CODE;
import static com.wkswind.plugin.component.api.utils.Consts.LAST_VERSION_NAME;

public final class PackageUtils {


  private PackageUtils(){}

  private static PackageInfo getPackageInfo(Context context) {
    try {
      return context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_CONFIGURATIONS);
    } catch (PackageManager.NameNotFoundException e) {
      e.printStackTrace();
    }
    return null;
  }

  public static boolean isNewVersion(Context context) {
    PackageInfo info = getPackageInfo(context);
    if(info != null) {
      String versionName = info.versionName;
      int versionCode = info.versionCode;
      SharedPreferences sp = sp(context);
      return !TextUtils.equals(versionName, sp.getString(LAST_VERSION_NAME, null)) || versionCode != sp.getInt(LAST_VERSION_CODE, -1);
    }
    return true;
  }

  public static void updateVersion(Context context) {
    PackageInfo info = getPackageInfo(context);
    if(info != null) {
      SharedPreferences sp = sp(context);
      sp.edit().putString(LAST_VERSION_NAME, info.packageName).putInt(LAST_VERSION_CODE, info.versionCode).apply();
    }
  }

  private static SharedPreferences sp(Context context) {
    return context.getSharedPreferences(COMPONENT_SP, Context.MODE_PRIVATE);
  }

  public static Set<String> gensFromSp(Context context) {
    return sp(context).getStringSet(GENS_CACHE, new HashSet<String>());
  }

  public static void cacheGens(Context context, Set<String> gens) {
    sp(context).edit().putStringSet(GENS_CACHE, gens).apply();
  }
}
