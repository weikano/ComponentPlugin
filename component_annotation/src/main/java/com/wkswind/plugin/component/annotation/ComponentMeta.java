package com.wkswind.plugin.component.annotation;

/**
 * Component信息
 */
public class ComponentMeta {
  private int title;
  private int icon;
  private String entryPoint;

  private ComponentMeta(int title, int icon, String entryPoint) {
    this.title = title;
    this.icon = icon;
    this.entryPoint = entryPoint;
  }

  public static ComponentMeta build(int title, int icon, String entry) {
    return new ComponentMeta(title, icon, entry);
  }

  public int getTitle() {
    return title;
  }

  public int getIcon() {
    return icon;
  }

  public String getEntryPoint() {
    return entryPoint;
  }

}
