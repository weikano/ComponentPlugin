package com.wkswind.plugin.component.api;

import android.app.Application;

import com.wkswind.plugin.component.annotation.ComponentLoader;
import com.wkswind.plugin.component.annotation.ComponentMeta;
import com.wkswind.plugin.component.api.utils.ClassUtils;
import com.wkswind.plugin.component.api.utils.PackageUtils;

import java.util.HashSet;
import java.util.Set;

import static com.wkswind.plugin.component.api.utils.Consts.COMPONENT_GEN_PACKAGENAME;

public final class ComponentManager {
  private static Set<ComponentMeta> allComponents = new HashSet<>();
  private static Set<Class> behaviorClasses = new HashSet<>();
  private static boolean registerByPlugin = false;

  private static boolean debuggable = false;
  public static void openDebug() {
    debuggable = true;
  }

  public static void init(Application app) {

    try {
      if (!registerByPlugin) {
        Set<String> gens;
        if (debuggable || PackageUtils.isNewVersion(app)) {
          gens = ClassUtils.getFileNameByPackageName(app, COMPONENT_GEN_PACKAGENAME);
          if (!gens.isEmpty()) {
            PackageUtils.cacheGens(app,gens);
          }
          PackageUtils.updateVersion(app);
        } else {
          gens = PackageUtils.gensFromSp(app);
        }

        for (String gen : gens) {
          Class clz = Class.forName(gen);
          if(ComponentLoader.class.isAssignableFrom(clz)) {
            ComponentLoader loader = (ComponentLoader) clz.newInstance();
            loader.loadComponent(behaviorClasses);
          }
        }
      }
      for (Class clz : behaviorClasses) {
        if(ComponentBehavior.class.isAssignableFrom(clz)) {
          ComponentBehavior behavior = (ComponentBehavior) clz.newInstance();
          allComponents.add(behavior.provideInfo());
          behavior.injectAsComponent(app);
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static void loadByPlugin() {
    registerByPlugin = false;
  }

//  /**
//   * 手动添加ComponentMeta
//   * 调用该方法后不会调用{@link #loadComponentByTransformer()}
//   * 必须在init方法之前调用
//   * @param loaders 自动生成的ComponentRegister$$xx
//   */
//  public static void loadComponentBehaviorClasses(ComponentLoader... loaders) {
//    for (ComponentLoader loader : loaders) {
//      loader.loadComponent(behaviorClasses);
//    }
////    Collections.addAll(behaviorClasses, classes);
//    registerByPlugin = true;
//  }
//
//  private static void loadComponentByTransformer() {
//    if(registerByPlugin) {
//      return;
//    }
//    //auto-generate by transform
//  }

  public Set<ComponentMeta> getAllComponents() {
    return allComponents;
  }
}
