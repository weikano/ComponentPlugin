package com.wkswind.plugin.component.compiler;

import com.google.auto.service.AutoService;
import com.google.common.reflect.TypeToken;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import com.wkswind.plugin.component.annotation.ComponentInfo;
import com.wkswind.plugin.component.annotation.ComponentLoader;
import com.wkswind.plugin.component.annotation.ComponentMeta;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Types;

@AutoService(Processor.class)
@SupportedAnnotationTypes("com.wkswind.plugin.component.annotation.ComponentInfo")
public class ComponentProcessor extends AbstractProcessor {
  private static final String PKG = "com.wkswind.plugin.component.gen";
  private static final String CLS_PREFIX = "ComponentRegister$$";
  private Filer filer;
  private Types types;
  private Logger logger;
  private String component;

  @Override
  public synchronized void init(ProcessingEnvironment processingEnvironment) {
    super.init(processingEnvironment);
    filer = processingEnvironment.getFiler();
    types = processingEnvironment.getTypeUtils();
    logger = new Logger(processingEnvironment.getMessager());
    Map<String, String> options = processingEnvironment.getOptions();
    if (MapUtils.isNotEmpty(options)) {
      component = options.get("COMPONENT_NAME");
    }
    if (StringUtils.isNotEmpty(component)) {
      component = component.replaceAll("[^0-9a-zA-Z_]+", "");
    } else {
      logger.error("COMPONENT_NAME is not set");
      throw new RuntimeException("ARouter::Compiler >>> No component name, for more information, look at gradle log.");
    }
  }

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }

  @Override
  public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
    if (set != null && !set.isEmpty()) {
      Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(ComponentInfo.class);
      parseElements(elements);
      return true;
    }
    return false;
  }

  private void parseElements(Set<? extends Element> elements) {
    try {

      String className = CLS_PREFIX + component;
      logger.info(">>> Generate file " + PKG +"."+className);
      TypeSpec.Builder typeBuilder = TypeSpec.classBuilder(className).addModifiers(Modifier.PUBLIC, Modifier.FINAL)
        .addSuperinterface(ComponentLoader.class);
      MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("loadComponent")
        .returns(void.class)
        .addModifiers(Modifier.PUBLIC)
        .addAnnotation(Override.class)
        .addParameter(new TypeToken<List<Class>>(){}.getType(), "metas");
      if (CollectionUtils.isNotEmpty(elements)) {
        logger.info(">>> Found ComponentInfo, size is " + elements.size() + "<<<");
        for (Element element : elements) {
          logger.info(">>> " + element + " <<<");
          ComponentInfo info = element.getAnnotation(ComponentInfo.class);
          if (info != null) {
            methodBuilder.addStatement("metas.add($L.class)", element.toString());
//            methodBuilder.addStatement("metas.add($T.build($L,$L,$S,$L.class))", ComponentMeta.class, info.title(), info.icon(), info.entry(), element.toString());
          }
        }
      }
      typeBuilder.addMethod(methodBuilder.build());
//      typeBuilder.addMethod(methodBuilder.build());
      JavaFile javaFile = JavaFile.builder(PKG, typeBuilder.build()).build();
      javaFile.writeTo(filer);
    } catch (IOException e) {
      e.printStackTrace();
    }

  }
}
