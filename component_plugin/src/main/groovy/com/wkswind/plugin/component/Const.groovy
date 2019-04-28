package com.wkswind.plugin.component

class Const {
  public static final String FILE_CONFIG = "cp.config.properties"
  static final String PREFIX = "cp_"
//  static final String CONFIG_NAME = "dynamicComponent";
  static final String TASK_GROUP = "cp"
  static final String TASK_NAME_SHOW_CONFIG = TASK_GROUP+"ShowConfig"
  static final String TASK_NAME_HELP = TASK_GROUP+ "HelpConfig"
  static final String TASK_NAME_CONFIG = TASK_GROUP+ "ConfigTest"

  static final String CONFIG_LIBRARY = PREFIX +"asLibrary";
  static final String CONFIG_APPID_SUFFIX = PREFIX + "appIdSuffix";
  static final String CONFIG_APPEND_APPID_SUFFIX = PREFIX + "appendAppIdSuffix";
  static final String CONFIG_KOTLIN = PREFIX+"enableKotlin";
  static final String TASKS_PREFIX = "dynamicComponent:"
  static final String COMPONENT_PREFIX = "component_";

  static final String APP_PLUGIN_ID = "com.android.application"
  static final String LIBRARY_PLUGIN_ID = "com.android.library"
  static final String KOTLIN_PLUGIN_ID = "kotlin-android"
  static final String KOTLIN_EXTENSION_PLUGIN_ID = "kotlin-android-extensions"
  static final String KOTLIN_KAPT_PLUGIN_ID = "kotlin-kapt"

  static final String PROP_ALL_APPS = PREFIX+"allApps"
  static final String PROP_APPLICATION_ID_BASE = PREFIX+"applicationIdBase"

  static final String EXT_ANDROID = "android"
  static final String EXT_DCONFIG = "defaultConfig"
}