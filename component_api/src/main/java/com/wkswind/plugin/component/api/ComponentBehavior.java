package com.wkswind.plugin.component.api;

import android.app.Application;

public interface ComponentBehavior {
  void injectAsComponent(Application app);
  void injectAsApp(Application app);
}
