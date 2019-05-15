package com.wkswind.plugin.component.api.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dalvik.system.DexFile;

import static com.wkswind.plugin.component.api.utils.Consts.EXTRACTED_SUFFIX;
import static com.wkswind.plugin.component.api.utils.Consts.KEY_DEX_NUMBER;
import static com.wkswind.plugin.component.api.utils.Consts.PREFS_FILE;
import static com.wkswind.plugin.component.api.utils.Consts.SECONDARY_FOLDER_NAME;
import static com.wkswind.plugin.component.api.utils.Consts.VM_WITH_MULTIDEX_VERSION_MAJOR;
import static com.wkswind.plugin.component.api.utils.Consts.VM_WITH_MULTIDEX_VERSION_MINOR;

public final class ClassUtils {


  private ClassUtils(){}
  public static Set<String> getFileNameByPackageName(Context context, final String packageName) throws PackageManager.NameNotFoundException, IOException, InterruptedException {
    final Set<String> classNames = new HashSet<>();
    List<String> paths = getSourcePaths(context);
    final CountDownLatch latch = new CountDownLatch(paths.size());
    for (final String path : paths) {
      Executors.newFixedThreadPool(3).execute(new Runnable() {
        @Override
        public void run() {
          DexFile dexfile = null;
          try {
            if (path.endsWith(EXTRACTED_SUFFIX)) {
              //NOT use new DexFile(path), because it will throw "permission error in /data/dalvik-cache"
              dexfile = DexFile.loadDex(path, path + ".tmp", 0);
            } else {
              dexfile = new DexFile(path);
            }

            Enumeration<String> dexEntries = dexfile.entries();
            while (dexEntries.hasMoreElements()) {
              String className = dexEntries.nextElement();
              if (className.startsWith(packageName)) {
                classNames.add(className);
              }
            }
          } catch (Throwable ignore) {
          } finally {
            if (null != dexfile) {
              try {
                dexfile.close();
              } catch (Throwable ignore) {
              }
            }

            latch.countDown();
          }
        }
      });
    }
    latch.await();
    return classNames;
  }

  private static List<String> getSourcePaths(Context context) throws PackageManager.NameNotFoundException, IOException {
    ApplicationInfo info = context.getPackageManager().getApplicationInfo(context.getPackageName(),0);
    File sourceApk = new File(info.sourceDir);
    List<String> sourcePaths = new ArrayList<>();
    sourcePaths.add(info.sourceDir);
    String extractedFilePrefix = sourceApk.getName() + ".classes";
    if(multiDexAvaliable()) {
      int totalDexNumber = getMultiDexPreferences(context).getInt(KEY_DEX_NUMBER, 1);
      File dexDir = new File(info.dataDir, SECONDARY_FOLDER_NAME);

      for (int secondaryNumber = 2; secondaryNumber <= totalDexNumber; secondaryNumber++) {
        //for each dex file, ie: test.classes2.zip, test.classes3.zip...
        String fileName = extractedFilePrefix + secondaryNumber + EXTRACTED_SUFFIX;
        File extractedFile = new File(dexDir, fileName);
        if (extractedFile.isFile()) {
          sourcePaths.add(extractedFile.getAbsolutePath());
          //we ignore the verify zip part
        } else {
          throw new IOException("Missing extracted secondary dex file '" + extractedFile.getPath() + "'");
        }
      }
      sourcePaths.addAll(tryLoadInstantRunDexFile(info));
    }
    return sourcePaths;
  }

  private static List<String> tryLoadInstantRunDexFile(ApplicationInfo applicationInfo) {
    List<String> instantRunSourcePaths = new ArrayList<>();

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && null != applicationInfo.splitSourceDirs) {
      // add the split apk, normally for InstantRun, and newest version.
      instantRunSourcePaths.addAll(Arrays.asList(applicationInfo.splitSourceDirs));
    } else {
      try {
        // This man is reflection from Google instant run sdk, he will tell me where the dex files go.
        Class pathsByInstantRun = Class.forName("com.android.tools.fd.runtime.Paths");
        Method getDexFileDirectory = pathsByInstantRun.getMethod("getDexFileDirectory", String.class);
        String instantRunDexPath = (String) getDexFileDirectory.invoke(null, applicationInfo.packageName);

        File instantRunFilePath = new File(instantRunDexPath);
        if (instantRunFilePath.exists() && instantRunFilePath.isDirectory()) {
          File[] dexFile = instantRunFilePath.listFiles();
          for (File file : dexFile) {
            if (null != file && file.exists() && file.isFile() && file.getName().endsWith(".dex")) {
              instantRunSourcePaths.add(file.getAbsolutePath());
            }
          }
        }

      } catch (Exception ignored) {
      }
    }

    return instantRunSourcePaths;
  }


  private static boolean multiDexAvaliable() {
    String vmName = null;
    boolean isMultidexCapable = false;
    try {
      if (isYunOS()) {    // YunOS需要特殊判断
        vmName = "'YunOS'";
        isMultidexCapable = Integer.valueOf(System.getProperty("ro.build.version.sdk")) >= 21;
      } else {    // 非YunOS原生Android
        vmName = "'Android'";
        String versionString = System.getProperty("java.vm.version");
        if (versionString != null) {
          Matcher matcher = Pattern.compile("(\\d+)\\.(\\d+)(\\.\\d+)?").matcher(versionString);
          if (matcher.matches()) {
            try {
              int major = Integer.parseInt(matcher.group(1));
              int minor = Integer.parseInt(matcher.group(2));
              isMultidexCapable = (major > VM_WITH_MULTIDEX_VERSION_MAJOR)
                || ((major == VM_WITH_MULTIDEX_VERSION_MAJOR)
                && (minor >= VM_WITH_MULTIDEX_VERSION_MINOR));
            } catch (NumberFormatException ignore) {
              // let isMultidexCapable be false
            }
          }
        }
      }
    } catch (Exception ignore) {

    }
    return isMultidexCapable;

  }

  private static boolean isYunOS() {
    try {
      String version = System.getProperty("ro.yunos.version");
      String vmName = System.getProperty("java.vm.name");
      return (vmName != null && vmName.toLowerCase().contains("lemur"))
        || (version != null && version.trim().length() > 0);
    } catch (Exception ignore) {
      return false;
    }
  }

  private static SharedPreferences getMultiDexPreferences(Context context) {
    return context.getSharedPreferences(PREFS_FILE, Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB ? Context.MODE_PRIVATE : Context.MODE_PRIVATE | Context.MODE_MULTI_PROCESS);
  }
}
