package com.wkswind.plugin.component

class ComponentConfig {
  /**
   * 是否添加kotlin的plugin，包含android-ktx、android-ktx-extenions、kotlin-apt
   */
  boolean enableKotlin = true
  /**
   * 是否作为library运行
   */
  boolean asLibrary = false
  /**
   * 作为app运行时，是否需要添加applicationIdSuffix
   */
  boolean appendApplicationIdSuffix = true
  /**
   * appliactionIdSuffix
   */
  String applicationIdSuffix

  @Override
  public String toString() {
    return "ComponentConfig{" +
      "enableKotlin=" + enableKotlin +
      ", asLibrary=" + asLibrary +
      ", appendApplicationIdSuffix=" + appendApplicationIdSuffix +
      ", applicationIdSuffix='" + applicationIdSuffix + '\'' +
      '}';
  }

  static ComponentConfig fromFile(File file) {
    ComponentConfig result = new ComponentConfig()
    if(file.exists()) {

      Properties p = new Properties()
      p.load(new FileInputStream(file))
      if(p.containsKey(Const.CONFIG_APPID_SUFFIX)) {
        result.applicationIdSuffix = p.get(Const.CONFIG_APPID_SUFFIX).toString()
      }
      if(p.containsKey(Const.CONFIG_APPEND_APPID_SUFFIX)) {
        result.appendApplicationIdSuffix = p.get(Const.CONFIG_APPEND_APPID_SUFFIX).toString().toBoolean()
      }
      if(p.containsKey(Const.CONFIG_KOTLIN)) {
        result.enableKotlin = p.get(Const.CONFIG_KOTLIN).toString().toBoolean()
      }
    }
    return result
  }

}