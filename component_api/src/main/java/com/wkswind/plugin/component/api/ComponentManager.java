package com.wkswind.plugin.component.api;

import android.app.Application;

import com.wkswind.plugin.component.annotation.ComponentLoader;
import com.wkswind.plugin.component.annotation.ComponentMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ComponentManager {
  private static List<ComponentMeta> allComponents = new ArrayList<>();
  private static List<Class> behaviorClasses = new ArrayList<>();
  private static boolean manual = false;
  public static void init(Application app) {
    loadComponentByTransformer();
    for (Class clz : behaviorClasses) {
      try {
        if (clz == ComponentBehavior.class) {
          ComponentBehavior behavior = (ComponentBehavior) clz.newInstance();
          allComponents.add(behavior.provideInfo());
          behavior.injectAsComponent(app);
        }
      } catch (IllegalAccessException e) {
        e.printStackTrace();
      } catch (InstantiationException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * 手动添加ComponentMeta
   * 调用该方法后不会调用{@link #loadComponentByTransformer()}
   * 必须在init方法之前调用
   * @param loaders 自动生成的ComponentRegister$$xx
   */
  public static void loadComponentBehaviorClasses(ComponentLoader... loaders) {
    for (ComponentLoader loader : loaders) {
      loader.loadComponent(behaviorClasses);
    }
//    Collections.addAll(behaviorClasses, classes);
    manual = true;
  }

  private static void loadComponentByTransformer() {
    if(manual) {
      return;
    }
    //auto-generate by transform
  }

  public List<ComponentMeta> getAllComponents() {
    return allComponents;
  }
}
