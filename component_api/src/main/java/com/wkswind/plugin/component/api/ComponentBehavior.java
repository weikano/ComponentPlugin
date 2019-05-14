package com.wkswind.plugin.component.api;

import android.app.Application;

import com.wkswind.plugin.component.annotation.ComponentMeta;

public interface ComponentBehavior {
  ComponentMeta provideInfo();
  void injectAsComponent(Application app);
  void injectAsApp(Application app);
}
