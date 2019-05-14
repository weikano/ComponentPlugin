package com.wkswind.plugin.component.annotation;

/**
 * Component信息
 */
public class ComponentMeta {
  private int title;
  private int icon;
  private String entryPoint;
  private Class<?> clazz;

  private ComponentMeta(int title, int icon, String entryPoint, Class<?> clazz) {
    this.title = title;
    this.icon = icon;
    this.entryPoint = entryPoint;
    this.clazz = clazz;
  }

  public static ComponentMeta build(int title, int icon, String entry, Class<?> clazz) {
    return new ComponentMeta(title,icon,entry,clazz);
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

  public Class<?> getClazz() {
    return clazz;
  }
}
