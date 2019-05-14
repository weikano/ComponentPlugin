package com.wkswind.plugin.component

class ComponentDependency {
  /**
   * 依赖的component
   */
  String name;
  /**
   * 依赖方式,implement or provider or else
   */
  String scope;

  ComponentDependency(String name) {
    this.name = name
  }
  @Override
  public String toString() {
    return "ComponentDependency{" +
      "name='" + name + '\'' +
      ", scope='" + scope + '\'' +
      '}';
  }
}