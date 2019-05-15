package com.wkswind.plugin.component.annotation;

import java.util.Set;

public interface ComponentLoader {
  void loadComponent(Set<Class> metas);
}
