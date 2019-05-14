package com.wkswind.plugin.component

import com.google.common.collect.Lists
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project;

class ComponentHostExtension {
  String hostModuleName = ":app"
  boolean kotlin = false
  boolean kapt = false

  List<String> ignores = Lists.newArrayList();

  NamedDomainObjectContainer<ComponentExtension> components;

  ComponentHostExtension(Project project) {
    components = project.container(ComponentExtension);
  }
  /**
   * 控制所有的组件是否全部为app模式
   */
  boolean allApps = false;
  /**
   * applicationId,用于给component作为APP运行时设置
   */
  String baseApplicationId;

  def hostModuleName(String hostModuleName) {
    if(!hostModuleName.startsWith(":")) {
      hostModuleName = ":"+hostModuleName
    }
    this.hostModuleName = hostModuleName
  }

  def components(Closure closure) {
    components.configure(closure)
  }

  def components(Action<NamedDomainObjectContainer<ComponentExtension>> action) {
    action.execute(components)
  }

  def ignores(String... ignores) {
    this.ignores.addAll(ignores)
  }

  @Override
  public String toString() {
    return "ComponentHostExtension{" +
      "hostModuleName='" + hostModuleName + '\'' +
      ", kotlin=" + kotlin +
      ", kapt=" + kapt +
      ", ignores=" + ignores +
      ", components=" + components +
      ", allApps=" + allApps +
      ", baseApplicationId='" + baseApplicationId + '\'' +
      '}';
  }
}