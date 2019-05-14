package com.wkswind.plugin.component

class Const {
  public static final String HELP_MESSAGE = "参考项目地址:https://github.com/weikano/ComponentPlugin\n" +
    "假设当前项目结构如下:\n" +
    "projectA\n" +
    "---  app:宿主程序\n" +
    "---  component_1:组件1\n" +
    "---  component_2:组件2\n" +
    "---  component_3:组件3\n" +
    "---  ....\n" +
    "---  library1:其他的library,比如一些通用的类,不作为component使用\n" +
    "---  library2:同library1\n" +
    "\n" +
    "使用步骤:\n" +
    "1. 修改projectA/build.gradle,修改内容如下:\n" +
    "buildscript {\n" +
    "  //...原来的内容不变\n" +
    "  repositories {\n" +
    "    //建议clone项目然后通过uploadArchive发布到自己的mavenLocal中,这样如果需要更改plugin,可以自己修改发布\n" +
    "    //...其他repository\n" +
    "    mavenLocal()\n" +
    "  }\n" +
    "  dependencies {\n" +
    "    //其他classpath\n" +
    "    classpath 'com.wkswind.plugin:component_plugin:latestversion'\n" +
    "  }\n" +
    "}\n" +
    "\n" +
    "apply plugin:\"component_plugin_adv\"\n" +
    "//配置组件化\n" +
    "cpHostConfig {\n" +
    "  hostModuleName = \":app\" //默认为:app,如果你的宿住MODULE是app1,那么修改为:app1\n" +
    "  kotlin = true //使用kotlin,默认为false\n" +
    "  kapt = true //使用了kapt,默认为false\n" +
    "  baseApplicationId = \"com.wkswind.plugin.test\" // 你自己宿主程序的applicationId,必须配置\n" +
    "  ignores = [ //添加不参与组件化的MODULE,比如上述中的library1和library2,注意要以:开头\n" +
    "    \":library1\", \":library2\"\n" +
    "  ]\n" +
    "  //接下来配置组件\n" +
    "  components {\n" +
    "    component_1 { //component_1对应组件MODULE的名字\n" +
    "      runAsApp = true //是否作为单独的app运行\n" +
    "      componentName = \"media\" //用来作为applicationIdSuffix和resourcePrefix\n" +
    "    }\n" +
    "    component_2 {\n" +
    "      runAsApp = false\n" +
    "      componentName = \"pay\"\n" +
    "    }\n" +
    "    //...其他组件\n" +
    "  }\n" +
    "}\n" +
    "\n" +
    "2. 组件中的修改,以component_1为例:\n" +
    "首先去掉其中的com.android.library或者com.android.application插件.去掉applicationId\n" +
    "然后新建目录src/component,将AndroidManifest.xml复制一份到该目录.当组件作为组件运行时,使用的是该目录下的AndroidManifest.xml"
}