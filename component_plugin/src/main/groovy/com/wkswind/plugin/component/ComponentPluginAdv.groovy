package com.wkswind.plugin.component

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.api.AndroidSourceSet
import com.android.build.gradle.internal.dsl.AnnotationProcessorOptions
import com.google.common.collect.Lists
import kotlin.Unit
import kotlin.jvm.functions.Function1
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.internal.AndroidExtensionsSubpluginIndicator
import org.jetbrains.kotlin.gradle.internal.Kapt3GradleSubplugin
import org.jetbrains.kotlin.gradle.plugin.KaptAnnotationProcessorOptions
import org.jetbrains.kotlin.gradle.plugin.KaptExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinAndroidPluginWrapper

import java.util.function.Predicate

class ComponentPluginAdv implements Plugin<Project> {
  private static final String APT_KEY = "COMPONENT_NAME";
  private static final String APT_KEY_AROUTER = "AROUTER_MODULE_NAME"
  private static final String EXT_NAME = "cpHostConfig"
  private static final String COMPONENT = "component"
  private static final String TASK_NAME = "cpHelp"
  private static final String TASK_GROUP = "componentPlugin"
//  private Logger logger;
  @Override
  void apply(Project project) {
//    logger = Logger.make(project)
    def hostExt = project.extensions.create(EXT_NAME, ComponentHostExtension, project);
    config(project)
    //帮助任务
    project.tasks.create(TASK_NAME).doLast {
      println(Const.HELP_MESSAGE)
    }.group = TASK_GROUP
  }

  void config(Project project) {
//    List<String> modules = Lists.newArrayList()
//    recursive(project, modules);
//    NamedDomainObjectContainer<ComponentExtension> components;
    def hostExt = project.extensions.getByType(ComponentHostExtension)
    project.afterEvaluate {
//      components = hostExt.components
      setupComponents(project, hostExt)
    }
  }

//  void recursive(Project project, List<String> modules) {
//    if (!project.childProjects.isEmpty()) {
//      project.childProjects.each {
//        modules.add(it.value.name)
//      }
//    }
//  }
  /**
   * 设置MODULE
   * 1. 将{@link ComponentHostExtension}中的{@link ComponentHostExtension#components}分为独立运行的app和集成的library, 过滤掉在ignores中的</br>
   * 2. 先处理libraries<br>
   * 3. 处理独立运行额apps<br>
   * 4. 遍历libraries,将其作为hostApp的依赖<br>
   * 5. 处理hostApp
   * @param project
   * @param ext
   */
  void setupComponents(Project project, ComponentHostExtension ext) {
    def baseApplicationId = ext.baseApplicationId
    def allApps = ext.allApps
    def ignores = ext.ignores;
    Set<ComponentExtension> libraries = new HashSet<>();
    Set<ComponentExtension> apps = new HashSet<>();
    if (!ext.components.empty) {
      ext.components.each {
        if (!ignores.contains(it.componentName)) {
          if (allApps || it.runAsApp) {
            apps.add(it)
          } else {
            libraries.add(it)
          }
        }
      }
      libraries.each {
        componentAsLibrary(project, it, ext.kotlin, ext.kapt)
      }
      apps.each {
        componentAsApp(project, it, baseApplicationId, ext.kotlin, ext.kapt)
      }
    }

    configureHostApp(project, ext, ext.kotlin, ext.kapt, libraries)
  }
  /**
   * 设置宿主app, 依赖所有作为library运行的组件
   * @param project
   * @param ext
   * @param kotlin
   * @param kapt
   * @param components
   */
  private void configureHostApp(Project project, ComponentHostExtension ext, boolean kotlin, boolean kapt, Set<ComponentExtension> components) {
    Project host = project.project(ext.hostModuleName)
    host.plugins.removeIf(new Predicate<Plugin>() {
      @Override
      boolean test(Plugin e) {
        return e.class == LibraryPlugin
      }
    })
    AppPlugin plugin = host.plugins.apply(AppPlugin)
    plugin.extension.defaultConfig.applicationId = ext.baseApplicationId
    handleAppAnnotationProcessor(plugin, ext.hostModuleName)
    handleKotlinPlugin(host, ext.hostModuleName, kotlin, kapt)
    List<ComponentDependency> dependencies = Lists.newArrayList()
    components.each {
      ComponentDependency dependency = new ComponentDependency(it.name)
      if (!dependency.name.startsWith(":")) {
        dependency.name += ":"
      }
      dependency.scope = "implementation"
//      dependency.name = it.componentName
      dependencies.add(dependency)
    }
    makeDependencies(host, dependencies)
    plugin.extension.registerTransform(new ComponentTransform(project))
  }

  private void componentAsApp(Project project, ComponentExtension it, String baseApplicationId, boolean kotlin, boolean kapt) {
    def thiz = findProjectByComponentExtension(project, it.name)
    thiz.plugins.removeIf(new Predicate<Plugin>() {
      @Override
      boolean test(Plugin e) {
        return e.class == LibraryPlugin
      }
    })
    AppPlugin plugin = thiz.plugins.apply(AppPlugin)
    //设置applicationId和applicationIdSuffix
    plugin.extension.defaultConfig.applicationId = baseApplicationId
    plugin.extension.defaultConfig.applicationIdSuffix = it.applicationIdSuffix()
    handleAppAnnotationProcessor(plugin, it.componentName)
    handleKotlinPlugin(thiz, it.componentName, kotlin, kapt)
    makeDependencies(thiz, it.dependencies)
    plugin.extension.registerTransform(new ComponentTransform(project))
  }
  /**
   * 修改resourcePrefx 为 _componentName
   * 修改buildType中main的AndroidManifest.xml路径为src/component/AndroidManifest.xml
   * @param project
   * @param it
   * @param kotlin
   * @param kapt
   */
  private void componentAsLibrary(Project project, ComponentExtension it, boolean kotlin, boolean kapt) {
    def thiz = findProjectByComponentExtension(project,it.name)
    thiz.plugins.removeIf(new Predicate<Plugin>() {
      @Override
      boolean test(Plugin e) {
        return e.class == AppPlugin
      }
    })
    LibraryPlugin ext = thiz.plugins.apply(LibraryPlugin)
    ext.extension.resourcePrefix(it.resourcePrefix())
    applyManifestChange(thiz, ext.extension.sourceSets)
    handleLibraryAnnotationProcessor(ext, it.componentName)
    handleKotlinPlugin(thiz, it.componentName, kotlin, kapt)
    makeDependencies(thiz, it.dependencies)

  }

  /**
   * annotationProcessor 中的arguments设置的Java班, kapt在{@link #handleKotlinPlugin}中
   * @param plugin
   * @param componentName
   */
  private void handleLibraryAnnotationProcessor(LibraryPlugin plugin, String componentName) {
    handleApt(plugin.extension.defaultConfig.javaCompileOptions.annotationProcessorOptions, componentName)
  }

  private void handleApt(AnnotationProcessorOptions options, String componentName) {
    aptArgs(componentName).each {
      options.argument(it.key, it.value)
    }
  }

  private void assertProjectNotNull(Project project, String it) {
    if(project == null) {
      throw new IllegalArgumentException(">>>>>component_plugin_adv:<<<<<\\nCannot find module ${it}")
    }
  }

  private Project findProjectByComponentExtension(Project project, String ext) {
    String name = ext.startsWith(":") ?: ":" + ext
    Project target = project.project(name)
    if(target == null) {
      target = project.rootProject.project(name)
    }
    assertProjectNotNull(target, ext)
    return target;
  }

  private void handleAppAnnotationProcessor(AppPlugin plugin, String componentName) {
    handleApt(plugin.extension.defaultConfig.javaCompileOptions.annotationProcessorOptions, componentName)

  }
  /**
   * 配置kotlin-android, kotlin-android-extension和kapt插件, 顺便设置kapt arguments
   * @param project
   * @param componentName
   * @param kotlin
   * @param kapt
   */
  private void handleKotlinPlugin(Project project, String componentName, boolean kotlin, boolean kapt) {
    if (kotlin) {
      applyPlugin(project, KotlinAndroidPluginWrapper)
      applyPlugin(project, AndroidExtensionsSubpluginIndicator)
      if (kapt) {
        Kapt3GradleSubplugin plugin = project.plugins.apply(Kapt3GradleSubplugin)
        KaptExtension ext = project.extensions.getByType(KaptExtension)
        if (ext) {
          def args = aptArgs(componentName)
          ext.arguments(new Function1<KaptAnnotationProcessorOptions, Unit>() {
            @Override
            Unit invoke(KaptAnnotationProcessorOptions opt) {
              args.each {entry->
                opt.arg(entry.key, entry.value)
              }
              return null
            }
          })
        }
      }
    }

  }
  /**
   * 当作为library运行时,修改buildType为main的AndroidManifest.xml的指向
   * @param project
   * @param sourceSets
   */
  private void applyManifestChange(Project project, NamedDomainObjectContainer<AndroidSourceSet> sourceSets) {
    sourceSets.find {
      it.name == "main"
    }.manifest.srcFile(project.file("src/${COMPONENT}/AndroidManifest.xml"))
  }
  /**
   * 添加插件,如果有了就不管
   * @param project
   * @param pluginClass
   */
  private void applyPlugin(Project project, Class<Plugin> pluginClass) {
    if (!project.plugins.hasPlugin(pluginClass)) {
      project.plugins.apply(pluginClass)
    }
  }
  /**
   * 添加依赖. 组件之间的相互依赖暂时不管,现在只有host app有用
   * @param project
   * @param dependencies
   */
  private void makeDependencies(Project project, List<ComponentDependency> dependencies) {
    if (dependencies.empty) {
      return
    }
    dependencies.each {
      Project dep = findProjectByComponentExtension(project, it.name)
      project.dependencies.add(it.scope, dep)
    }

  }

  private Map<String, String> aptArgs(String componentName) {
    Map<String, String> args = new HashMap<>()
    String value = componentName.replaceAll(":","")
    args.put(APT_KEY, value)
    args.put(APT_KEY_AROUTER, value)
    return args;
  }
}