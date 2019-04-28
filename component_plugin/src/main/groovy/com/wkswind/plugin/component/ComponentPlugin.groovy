package com.wkswind.plugin.component

import org.gradle.api.Plugin
import org.gradle.api.Project

class ComponentPlugin implements Plugin<Project> {
  @Override
  void apply(Project project) {
    ComponentConfig config = ComponentConfig.fromFile(project.file(Const.FILE_CONFIG))
    applyConfig(config, project)
    if (config.asLibrary) {
      installPlugin(project, Const.LIBRARY_PLUGIN_ID)
      applyAsLibrary(project, config)
    } else {
      installPlugin(project, Const.APP_PLUGIN_ID)
      applyAsApp(project, config)
    }
    if (config.enableKotlin) {
      installPlugin(project, Const.KOTLIN_PLUGIN_ID)
      installPlugin(project, Const.KOTLIN_EXTENSION_PLUGIN_ID)
      installPlugin(project, Const.KOTLIN_KAPT_PLUGIN_ID)
    }
    project.tasks.create(Const.TASK_NAME_HELP).doLast {
      printHelpMessage()
    }.group = Const.TASK_GROUP

    project.tasks.create(Const.TASK_NAME_SHOW_CONFIG).doLast {
      println(config)
    }.group = Const.TASK_GROUP
  }

  private void printHelpMessage() {
    println("==========ComponentPlugin========插件使用说明=======")
    println("1.rootProjet下的gradle.properties必须添加")
    println("添加${Const.PREFIX}module_name的boolean值来表示对应的module是否以library方式运行，比如component_module1就需要在gradle.properties中添加${Const.PREFIX}component_module1=true/false来配置")
    println("添加${Const.PROP_APPLICATION_ID_BASE}来作为applicationId，用以给module作为application运行时添加")
    println("2.module下配置${Const.FILE_CONFIG}文件")
    println("${Const.CONFIG_KOTLIN}表示当前是否需要开启kotlin：如果设置为true，那么会apply ${Const.KOTLIN_PLUGIN_ID}、${Const.KOTLIN_EXTENSION_PLUGIN_ID}和${Const.KOTLIN_KAPT_PLUGIN_ID}")
    println("${Const.CONFIG_APPEND_APPID_SUFFIX}表示当module以app运行时是否需要设置applicationIdSuffix：true表示需要设置，applicationIdSuffix参考以下参数")
    println("${Const.CONFIG_APPID_SUFFIX}表示applicationIdSuffix：默认为module的name去掉${Const.COMPONENT_PREFIX}。比如component_module1的默认applicationIdSuffix为module1")
    println("==========ComponentPlugin========插件原理==========")
    println("首先读取gradle.properties中的${Const.PREFIX}module_name来确定对应module是否以library方式运行")
    println("然后读取对应module_name下的${Const.FILE_CONFIG}来确定对应的配置信息")
    println("如果module以application方式运行，那么会给module设置applicationId和根据配置添加applicationIdSuffix")
    //println("如果module以library运行，则会添加resourcePrefix为[module_name]_，并且设置对应的sourceSets.main.manifest.srcFile为src/main/component/AndroidManifest.xml")
  }

  private static void installPlugin(Project project, String pluginId) {
    if (project.getPlugins().hasPlugin(pluginId)) {
      return;
    }
    project.pluginManager.apply(pluginId)
  }
  /**
   * 读取gradle.properties中的cp.allApps和cp.applicationIdBase值
   * cp.allApps表示project设置为APP运行，不然就以dynamicConfig中的asLibrary为主
   * cp.applicationIdBase用于设置组件作为app运行时的applicationId
   * @param config
   * @param project
   */
  private static void applyConfig(ComponentConfig config, Project project) {
//    println("========begin applyConfig ============")
//    println("project.rootProject.properties list bellow : ")
//    println(project.rootProject.properties)
    if (project.rootProject.properties.containsKey(Const.PROP_ALL_APPS)) {
//      println("apply config with ${Const.PROP_ALL_APPS}======")
      boolean allApps = project.rootProject.getProperties().get(Const.PROP_ALL_APPS)
      if (allApps) {
        config.asLibrary = false
      }
    }else {
//      println("apply config with NO ${Const.PROP_ALL_APPS}======")
      config.asLibrary = asLibraryByProperties(project)
    }
    if (config.applicationIdSuffix == null || config.applicationIdSuffix.length() == 0) {
      config.applicationIdSuffix = project.name.replace(Const.COMPONENT_PREFIX, "")
    }
//    println("=======end applyConfig========")
  }
  /**
   * 获取cp.{project_name}属性值
   * true表示作为library运行
   * false表示作为app
   * @param project
   * @return
   */
  private static boolean asLibraryByProperties(Project project) {
    String key = Const.PREFIX + project.name
//    println("========begin asLibraryByProperties with key :${key}===========")
    if(project.rootProject.properties.containsKey(key)) {
//      println("asLibraryByProperties has key :${key}===========")
//      println("config asLibrary from project.properties")
      return project.rootProject.properties[key].toString().toBoolean()
    }
//    println("asLibraryByProperties with key :${key} not exists")
    return false
  }
  //尝试修改resourcePrefix, 但是属性是只读的，无法修改
  private void applyAsLibrary(Project project, ComponentConfig config) {

    //def androidExt = project.extensions.findByName(Const.EXT_ANDROID)
    //if (androidExt != null) {
      // androidExt.resourcePrefix = config.applicationIdSuffix+"_"
      //def sourceSetsExt = androidExt["sourceSets"]
      //def mainExt = sourceSetsExt["main"]
      //if(mainExt != null) {
      //  mainExt.manifest.srcFile = "src/main/component/AndroidManifest.xml"
      //}
    //}
  }

  private void applyAsApp(Project project, ComponentConfig config) {
    String applicationId = loadProperty(project, Const.PROP_APPLICATION_ID_BASE)
    if (applicationId == null || applicationId.length() == 0) {
      throw new IllegalArgumentException("please add dpc.applicationIdBase in gradle.properties. " +
        "dpc.applicationIdBase will be the appliationId when component work as app")
    }
    def androidExt = project.extensions.findByName(Const.EXT_ANDROID)
    if (androidExt != null) {
      def defExt = androidExt[Const.EXT_DCONFIG]
      if (defExt != null) {
        defExt.applicationId = applicationId
        if (config.appendApplicationIdSuffix) {
          defExt.applicationIdSuffix = config.applicationIdSuffix
        }

      }
    }
  }

  private static String loadProperty(Project project, String name) {
    def value = project.rootProject.properties[name]
    return value == null ? null : value.toString()
  }
}