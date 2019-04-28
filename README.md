### 组件化开发的助手
#### 项目说明
- component_plugin：插件的实现
- app：组件化的主工程
- component_test1：组件化的library
- component_test2：组件化的library

#### 首次使用
component_plugin未上传至maven或者jcenter，所以第一次使用需要在将component_plugin发布至mavenLocal。
1. 首先修改settings.gradle，将除了component_plugin之外的module全部去掉
2. 修改project目录下的build.gradle，注释掉classpath "com.wkswind.plugin:component_plugin:1.0.0"
3. 调用component_plugin中的gradle任务uploadArchives，即命令行执行./gradlelw :component_plugin:uploadArchives
4. 查看本地maven仓库中是否能找到该插件
5. 还原123所做的操作

#### 插件说明
命令行调用./gradlew :component_test1:cpHelpConfig查看帮助

#### Todo 

- [ ] 插件中动态配置sourceSet
- [ ] 插件中动态配置resourcePrefix