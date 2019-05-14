package com.wkswind.plugin.component

import com.google.common.collect.Lists

class ComponentExtension {
  public ComponentExtension(String name) {
    this.name = name
  }
  String name;
  String componentName;
  boolean runAsApp = false;
  List<ComponentDependency> dependencies = Lists.newArrayList();
  def componentName(String componentName) {
    this.componentName = componentName.toLowerCase();
  }
  def runAsApp(boolean runAsApp) {
    this.runAsApp = runAsApp
  }
  def dependencies(Map<String, String> dependencies) {
    dependencies.each {key, value->
      ComponentDependency dependency = new ComponentDependency()
      dependency.name = key
      dependency.scope = value
      this.dependencies.add(dependency)
    }
  }

  @Override
  public String toString() {
    return "ComponentExtension{" +
      "componentName='" + componentName + '\'' +
      ", runAsApp=" + runAsApp +
      ", dependencies=" + dependencies +
      '}';
  }

  def String resourcePrefix() {
    return componentName.endsWith("_")?:(componentName +"_")
  }

  def String applicationIdSuffix() {
    return componentName.startsWith(".")?:("."+componentName);
  }
}