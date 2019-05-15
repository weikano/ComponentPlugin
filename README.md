### 组件化开发的助手

主要功能包括

- 通过组件化开发的gradle插件component_plugin_adv动态配置项目中的MODULE是作为standalone方式区里运行,还是component方式集成到宿主app中
- 通过annotation processor和transform修改ComponentManager手动/自动添加添加组件入口

#### 模块说明

可以分为两个类型:

1. annotation processor相关: component_annotation, component_api, component_compiler
2. 组件化开发自动配置插件: component_plugin

**二者可独立使用,也可以一起使用**

- component_annotation: 将组件的app通过ComponentInfo方式注解, 会通过annotation processor自动生成ComponentRegister$$xx.java类. 如果使用了component_plugin_adv,那么会通过transform方式修改ComponentManager的loadComponentByTransform自动调用生成的ComponentLoader类
- component_api: 组件的app需要实现ComponentBehavior接口, ComponentManager.init方法会调用ComponentLoader.injectAsComponent方法
- component_compiler: 将ComponentInfo注解的类生成ComponentRegister$$xx.java文件
- component_plugin: 组件化开发自动化的gradle plugin 

#### 使用之前

**上述模块都可以通过uploadArchive发布至mavenLocal然后使用, 发布顺序为component_annotation, component_api, component_compiler. 因为三者有依赖关系, 注意顺序**. 

#### 使用说明

##### 一、注解相关

单独使用时，需要配置

```groovy
//以component_1为例，修改component_1目录下的buid.gradle
android {
  defaultConfig {
    ...
      javaCompileOptions {
        annotationPRocessorOptions {
          arguments = ["COMPONENT_NAME": project.name] //value可以设置为其他值
        }
      }
  }
}
//如果是kotlin
kapt {
  argument {
    arg("COMPONENT_NAME", project.getName()) //value可以设置为其他的值
  }
}

dependencies {
  implementation "com.wkswind.plugin:component_api:latestversion"
  kapt "com.wkswind.plugin:component_compiler:latestversion"
  annotationProcessor "com.wkswind.plugin:component_compiler:latestversion"
}
```

然后在对应的MODULE的application中使用ComponentInfo注解

```kotlin
@ComponentInfo class ModuleApp : Application(), ComponentBehavior {
  override fun injectAsApp(app:Application) {
    //xxx
  }
  override fun provideInfo(app:Application) : ComponentMeta {
    //xxx
  }
}
```

然后assemble一下，就能看到ComponentRegister$$module1.java类

接下来在主程序的application中调用

```kotlin
class MainApp : Application() {
  fun onCreate() {
    ComponentManager.init(this)
    //想要获得所有接入的组件
    ComponentManager.getAllComponents();
  }
}
```

##### 二、gradle 插件

gradle插件集成在projectA的build.gradle中

```groovy
//projectA下的build.gradle
buildscript {
  dependencies {
    ...
    classpath "com.wkswind.plugin:component_plugin:latestVersion"  
  }
}
//接入插件
apply plugin:"component_plugin_adv"
cpHostConfig {
  hostModuleName = ":app"//设置为宿主程序对应的MODULE名字加上:，默认值为:app
  kotlin = true //默认为false
  kapt = true //默认为false
  baseApplicationId = "com.aaa.bbb" //作为宿主程序的applicationId。当组件作为单独app运行时的applicationId，applicationIdSuffix由componentName设定
  ignores = [
    ":library1", ":library2" //不参与组件化的module
  ]
  components {
    component_1 {//component_1为当前projectA目录下的MODULE的名字，不能写错，写错会找不到
      componentName = "test1" //设置别名，用在applicationIdSuffix和resourcePrefx中，也用在ARouter和component_compiler中的kapt和annotationProcessorOptions的参数
      runAsApp = false //如果为true，那么就是单独的app运行，false就为组件，并且宿主程序会自动集成
    }
    component_2 {
      componentName = "test2"
      runAsApp = false
    }
    component_3 {
      componentName = "test3"
      runAsApp = true
    }
    ....
  }

}
```

**插件做了什么？**

> 1. 将component_1和component_2作为android library，设置resourcePrefix = componentName +“__”，并将当前生效的AndroidManifest这是为src/component/AndroidManifest.xml
> 2. 将component_3设置为com.android.application，applicationId=“com.aaa.bbb”, applicationIdSuffix = “.test3”
> 3. 将app作为宿主app，自动依赖component_test1, component_test2
> 4. 设置ARouter和component_compiler的annotationProcessor参数，对应的value都是componentName
> 5. 将component对应的MODULE中的src/main/AndroidManifest.xml复制一份到src/component/AndroidManifest.xml中

#### 待完成

- [ ] 组件之间的相互依赖
- [ ] 参考ARouter的init方法，在dex中查找对应的Component信息进行注册
- [ ] 参考ARouter的register插件，在transform时动态修改class文件来注册ComponentRegister