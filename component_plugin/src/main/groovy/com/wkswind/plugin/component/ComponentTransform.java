package com.wkswind.plugin.component;

import com.android.build.api.transform.DirectoryInput;
import com.android.build.api.transform.Format;
import com.android.build.api.transform.JarInput;
import com.android.build.api.transform.QualifiedContent;
import com.android.build.api.transform.Transform;
import com.android.build.api.transform.TransformException;
import com.android.build.api.transform.TransformInput;
import com.android.build.api.transform.TransformInvocation;
import com.android.build.gradle.internal.pipeline.TransformManager;

import org.gradle.api.Project;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * 用来在编译时修改ComponentManager中的loadComponent方法,类似ARouter
 */
class ComponentTransform extends Transform {
  private static final String PKG = "com/wkswind/plugin/component/gen";
  private static final String MANAGER = "com/wkswind/plugin/component/api";
  private List<File> componentLoaders = new ArrayList<>();
  private final Project project;

  ComponentTransform(Project project) {
    this.project = project;
  }

  @Override
  public String getName() {
    return "com.wkswind.plugin.component";
  }

  @Override
  public Set<QualifiedContent.ContentType> getInputTypes() {
    return TransformManager.CONTENT_CLASS;
  }

  @Override
  public Set<? super QualifiedContent.Scope> getScopes() {
    return TransformManager.SCOPE_FULL_PROJECT;
  }

  @Override
  public boolean isIncremental() {
    return false;
  }

  @Override
  public void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
    super.transform(transformInvocation);
    Logger.i("Start scan registere info in jar file.");
    boolean leftSlash = File.separatorChar == '/';
    for (TransformInput input : transformInvocation.getInputs()) {
      for (JarInput jarInput : input.getJarInputs()) {
        scanJar(jarInput.getFile());
      }
      for (DirectoryInput dir : input.getDirectoryInputs()) {
        File dest = transformInvocation.getOutputProvider().getContentLocation(dir.getName(),dir.getContentTypes(), dir.getScopes(), Format.DIRECTORY);
        String root = dir.getFile().getAbsolutePath();
        if(!root.endsWith(File.separator)) {
          root += File.separator;
        }
        scanDir(dir.getFile());
      }
    }
  }

  private static void scanDir(File dir) {
    if(dir.isFile()) {
      if(available(dir.getAbsolutePath())) {
        Logger.i(dir.getAbsolutePath());
      }
    }else if(dir.isDirectory()) {
      for (File file : dir.listFiles()) {
        scanDir(file);
      }
    }
  }

  private static boolean available(String name) {
    return name.startsWith(PKG) || name.startsWith(MANAGER);
  }

  private static void scanJar(File jarFile) {
    try {
      JarFile jar = new JarFile(jarFile);
      Enumeration<JarEntry> entries = jar.entries();
      while (entries.hasMoreElements()) {
        JarEntry item = entries.nextElement();
        String name = item.getName();
        if(name.startsWith(PKG)) {
          Logger.i(name);
        }else if(name.startsWith(MANAGER)) {
          Logger.i(name);
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}