package com.voler.inject;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.voler.cutlass.FieldInject;
import com.voler.cutlass.annotation.InjectField;
import com.voler.utils.Consts;
import com.voler.utils.Logger;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import static com.voler.utils.Consts.ANNOTATION_TYPE_INJECTFIELD;
import static com.voler.utils.Consts.ARRAYLIST;
import static com.voler.utils.Consts.INTEGER;
import static com.voler.utils.Consts.PACKAGE;
import static com.voler.utils.Consts.PARCELABLE;
import static com.voler.utils.Consts.SERIALIZABLE;
import static com.voler.utils.Consts.STRING;
import static com.voler.utils.Consts.WARNING_TIPS;


@AutoService(Processor.class)
@SupportedAnnotationTypes(ANNOTATION_TYPE_INJECTFIELD)
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class FieldProcessor extends AbstractProcessor {


    private Elements elementUtils;
    private Filer filer;
    private Types typeUtils;
    private Logger logger;
    private Map<TypeElement, List<Element>> parentAndChild = new HashMap<>();

    /**
     * init()方法会被注解处理工具调用，并输入ProcessingEnviroment参数。
     * ProcessingEnviroment提供很多有用的工具类Elements, Types 和 Filer
     *
     * @param processingEnv 提供给 processor 用来访问工具框架的环境
     */
    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        elementUtils = processingEnv.getElementUtils();
        filer = processingEnv.getFiler();
        typeUtils = processingEnv.getTypeUtils();
        logger = new Logger(processingEnv.getMessager());
        logger.info(">>>>>>> FieldProcessor init");
    }

    /**
     * 这相当于每个处理器的主函数main()，你在这里写你的扫描、评估和处理注解的代码，以及生成Java文件。
     * 输入参数RoundEnviroment，可以让你查询出包含特定注解的被注解元素
     *
     * @param annotations 请求处理的注解类型
     * @param roundEnv    有关当前和以前的信息环境
     * @return 如果返回 true，则这些注解已声明并且不要求后续 Processor 处理它们；
     * 如果返回 false，则这些注解未声明并且可能要求后续 Processor 处理它们
     */
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (CollectionUtils.isEmpty(annotations)) return false;
        logger.info(">>>>>>>> processor start...");
        try {
            // roundEnv.getElementsAnnotatedWith()返回使用给定注解类型的元素
            categories(roundEnv.getElementsAnnotatedWith(InjectField.class));
            generateHelper();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e);
        }
        return true;
    }

    /**
     * 这里必须指定，这个注解处理器是注册给哪个注解的。注意，它的返回值是一个字符串的集合，包含本处理器想要处理的注解类型的合法全称
     *
     * @return 注解器所支持的注解类型集合，如果没有这样的类型，则返回一个空集合
     */
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotataions = new LinkedHashSet<String>();
        annotataions.add(InjectField.class.getCanonicalName());
        return annotataions;
    }

    /**
     * 指定使用的Java版本，通常这里返回SourceVersion.latestSupported()，默认返回SourceVersion.RELEASE_6
     *
     * @return 使用的Java版本
     */
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    /**
     * Categories field, find his papa.
     *
     * @param elements
     */
    private void categories(Set<? extends Element> elements) throws IllegalAccessException {
        if (CollectionUtils.isNotEmpty(elements)) {
            for (Element element : elements) {
                TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();

                if (element.getModifiers().contains(Modifier.PRIVATE)) {
                    throw new IllegalAccessException("The autowired fields CAN NOT BE 'private'!!! please check field ["
                            + element.getSimpleName() + "] in class [" + enclosingElement.getQualifiedName() + "]");
                }

                if (parentAndChild.containsKey(enclosingElement)) { // Has categries
                    parentAndChild.get(enclosingElement).add(element);
                } else {
                    List<Element> childs = new ArrayList<>();
                    childs.add(element);
                    parentAndChild.put(enclosingElement, childs);
                }
            }

            logger.info("categories finished.");
        }
    }

    private void generateHelper() throws Exception {

        TypeMirror activityTm = elementUtils.getTypeElement(Consts.ACTIVITY).asType();
        TypeMirror fragmentTm = elementUtils.getTypeElement(Consts.FRAGMENT).asType();
        TypeMirror fragmentTmV4 = elementUtils.getTypeElement(Consts.FRAGMENT_V4).asType();

        TypeSpec.Builder fragmentFactorySpec = TypeSpec.classBuilder("FragmentFactory")
                .addJavadoc(WARNING_TIPS)
                .addModifiers(Modifier.PUBLIC);

        TypeSpec.Builder intentFactorySpec = TypeSpec.classBuilder("IntentFactory")
                .addJavadoc(WARNING_TIPS)
                .addModifiers(Modifier.PUBLIC);


        //inject参数
        if (MapUtils.isNotEmpty(parentAndChild)) {
            for (Map.Entry<TypeElement, List<Element>> entry : parentAndChild.entrySet()) {

                TypeElement classElement = entry.getKey();
                List<Element> fieldList = entry.getValue();
                String qualifiedName = classElement.getQualifiedName().toString();
                String packageName = qualifiedName.substring(0, qualifiedName.lastIndexOf("."));
                String fileName = classElement.getSimpleName() + "$$InjectField";

                logger.info(">>> Start process " + fieldList.size() + " field in " + classElement.getSimpleName() + " ... <<<");


                TypeSpec.Builder typeSpec = TypeSpec.classBuilder(fileName)
                        .addJavadoc(WARNING_TIPS)
                        .addModifiers(Modifier.PUBLIC)
                        .addSuperinterface(ClassName.get(FieldInject.class));

                MethodSpec.Builder method = MethodSpec.methodBuilder("inject")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(TypeName.OBJECT, "target")
                        .addStatement("$T substitute = ($T)target", ClassName.get(classElement), ClassName.get(classElement));
                boolean isActivity = false;
                if (typeUtils.isSubtype(classElement.asType(), activityTm)) {
                    isActivity = true;
                    method.addStatement("$T injects = substitute.getIntent()", ClassName.get("android.content", "Intent"));
                } else if (typeUtils.isSubtype(classElement.asType(), fragmentTm) || typeUtils.isSubtype(classElement.asType(), fragmentTmV4)) {
                    method.addStatement("$T injects = substitute.getArguments()", ClassName.get("android.os", "Bundle"));
                } else {
                    throw new IllegalAccessException(fileName + "is not activity or fragment! !");
                }

                for (Element element : fieldList) {
                    InjectField injectField = element.getAnnotation(InjectField.class);
                    String fieldName = element.getSimpleName().toString();

                    String statment = "substitute." + fieldName + " = injects.get";
                    statment = buildStatement(statment, element, isActivity);
                    String parameter = StringUtils.isEmpty(injectField.value()) ? fieldName : injectField.value();
                    method.addStatement(statment, String.format("\"%s\"", parameter));
                }
                typeSpec.addMethod(method.build());
                JavaFile.builder(packageName, typeSpec.build()).build().writeTo(filer);


                if (isActivity) {
                    MethodSpec.Builder createIntentMethod = MethodSpec.methodBuilder("create" + classElement.getSimpleName())
                            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                            .returns(ClassName.get("android.content", "Intent"))
                            .addStatement("$T intent = new $T()", ClassName.get("android.content", "Intent"), ClassName.get("android.content", "Intent"));

                    for (Element element : fieldList) {
                        InjectField injectField = element.getAnnotation(InjectField.class);
                        String fieldName = element.getSimpleName().toString();

                        String statment = "intent.put";
                        statment = buildIntentStatement(statment, element);
                        String parameter = StringUtils.isEmpty(injectField.value()) ? fieldName : injectField.value();
                        TypeName typeName = ClassName.get(element.asType());
                        createIntentMethod.addParameter(typeName, parameter)
                                .addStatement(statment, parameter, parameter);
//                                .addStatement(String.format("\"%s\",%s", parameter, parameter));
                    }
                    createIntentMethod.addStatement("return intent");
                    intentFactorySpec.addMethod(createIntentMethod.build());

                } else {
                    MethodSpec.Builder createFragmentMethod = MethodSpec.methodBuilder("create" + classElement.getSimpleName())
                            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                            .returns(ClassName.get(classElement))
                            .addStatement("$T params = new $T()", ClassName.get("android.os", "Bundle"), ClassName.get("android.os", "Bundle"))
                            .addStatement("$T fragment = new $T()", ClassName.get(classElement), ClassName.get(classElement));

                    for (Element element : fieldList) {
                        InjectField injectField = element.getAnnotation(InjectField.class);
                        String fieldName = element.getSimpleName().toString();

                        String statment = "params.put";
                        statment = buildStatement(statment, element, isActivity);
                        String parameter = StringUtils.isEmpty(injectField.value()) ? fieldName : injectField.value();
                        TypeName typeName = ClassName.get(element.asType());
                        createFragmentMethod.addParameter(typeName, parameter)
                                .addStatement(statment, String.format("\"%s\",%s", parameter, parameter));
//                                .addStatement(String.format("\"%s\",%s", parameter, parameter));
                    }

                    createFragmentMethod.addStatement("fragment.setArguments(params)")
                            .addStatement("return fragment");
                    fragmentFactorySpec.addMethod(createFragmentMethod.build());
                }
            }
            JavaFile.builder(PACKAGE, fragmentFactorySpec.build()).build().writeTo(filer);
            JavaFile.builder(PACKAGE, intentFactorySpec.build()).build().writeTo(filer);
            logger.info(">>> InjectField processor stop. <<<");
        }
    }

    private String buildIntentStatement(String statment, Element element) {
        TypeMirror typeMirror = element.asType();
        TypeMirror parcelableType = elementUtils.getTypeElement(PARCELABLE).asType();
        if (isList(typeMirror)) {
            String string = typeMirror.toString();
            logger.info(string);
            string = string.substring(string.indexOf("<") + 1, string.lastIndexOf(">"));
            logger.info(string);
            TypeMirror asType = elementUtils.getTypeElement(string).asType();
            if (string.equals(INTEGER)) {
                statment += "IntegerArrayListExtra($S,$L)";
            } else if (string.equals(STRING)) {
                statment += "StringArrayListExtra($S,$L)";
            } else if (typeUtils.isSubtype(asType, parcelableType)) {
                statment += "ParcelableArrayListExtra($S,$L)";
            } else {
                statment += "CharSequenceArrayListExtra($S,$L)";
            }
        } else {
            statment += "Extra($S,$L)";
        }
        return statment;
    }

    private String buildStatement(String statment, Element element, boolean isActivity) throws IllegalAccessException {
        TypeMirror typeMirror = element.asType();
        TypeKind type = typeMirror.getKind();
        logger.info(typeMirror.toString() + "---" + type.ordinal());

        TypeMirror parcelableType = elementUtils.getTypeElement(PARCELABLE).asType();
        TypeMirror serializableType = elementUtils.getTypeElement(SERIALIZABLE).asType();
        TypeMirror stringType = elementUtils.getTypeElement(STRING).asType();

        if (type == TypeKind.BOOLEAN) {
            statment += (isActivity ? ("BooleanExtra($L, false)") : ("Boolean($L)"));
        } else if (type == TypeKind.BYTE) {
            statment += (isActivity ? ("ByteExtra($L, (byte) 0)") : ("Byte($L)"));
        } else if (type == TypeKind.SHORT) {
            statment += (isActivity ? ("ShortExtra($L, (short) 0)") : ("Short($L)"));
        } else if (type == TypeKind.INT) {
            statment += (isActivity ? ("IntExtra($L, 0)") : ("Int($L)"));
        } else if (type == TypeKind.LONG) {
            statment += (isActivity ? ("LongExtra($L, 0)") : ("Long($L)"));
        } else if (type == TypeKind.FLOAT) {
            statment += (isActivity ? ("FloatExtra($L, 0)") : ("Float($L)"));
        } else if (type == TypeKind.DOUBLE) {
            statment += (isActivity ? ("DoubleExtra($L, 0)") : ("Double($L)"));
        } else if (type == TypeKind.CHAR) {
            statment += (isActivity ? ("CharExtra($L, 0)") : ("Char($L)"));
        } else if (StringUtils.deleteWhitespace(typeMirror.toString()).equals("boolean[]")) {
            statment += (isActivity ? ("BooleanArrayExtra($L)") : ("BooleanArray($L)"));
        } else if (StringUtils.deleteWhitespace(typeMirror.toString()).equals("byte[]")) {
            statment += (isActivity ? ("ByteArrayExtra($L)") : ("ByteArray($L)"));
        } else if (StringUtils.deleteWhitespace(typeMirror.toString()).equals("short[]")) {
            statment += (isActivity ? ("ShortArrayExtra($L)") : ("ShortArray($L)"));
        } else if (StringUtils.deleteWhitespace(typeMirror.toString()).equals("int[]")) {
            statment += (isActivity ? ("IntArrayExtra($L)") : ("IntArray($L)"));
        } else if (StringUtils.deleteWhitespace(typeMirror.toString()).equals("long[]")) {
            statment += (isActivity ? ("LongArrayExtra($L)") : ("LongArray($L)"));
        } else if (StringUtils.deleteWhitespace(typeMirror.toString()).equals("float[]")) {
            statment += (isActivity ? ("tFloatArrayExtra($L)") : ("FloatArray($L)"));
        } else if (StringUtils.deleteWhitespace(typeMirror.toString()).equals("double[]")) {
            statment += (isActivity ? ("DoubleArrayExtra($L)") : ("DoubleArray($L)"));
        } else if (StringUtils.deleteWhitespace(typeMirror.toString()).equals("char[]")) {
            statment += (isActivity ? ("CharArrayExtra($L)") : ("CharArray($L)"));
        } else if (typeMirror.toString().equals(STRING + "[]")) {
            statment += (isActivity ? ("StringArrayExtra($L)") : ("StringArray($L)"));
        } else if (typeUtils.isSameType(typeMirror, stringType)) {
            statment += (isActivity ? ("StringExtra($L)") : ("String($L)"));
        } else if (typeUtils.isSubtype(typeMirror, parcelableType)) {
            statment += (isActivity ? ("ParcelableExtra($L)") : ("Parcelable($L)"));
        } else if (isList(typeMirror)) {
            String string = typeMirror.toString();
            logger.info(string);
            string = string.substring(string.indexOf("<") + 1, string.lastIndexOf(">"));
            logger.info(string);
            TypeMirror asType = elementUtils.getTypeElement(string).asType();
            if (string.equals(INTEGER)) {
                statment += (isActivity ? ("IntegerArrayListExtra($L)") : ("IntegerArrayList($L)"));
            } else if (string.equals(STRING)) {
                statment += (isActivity ? ("StringArrayListExtra($L)") : ("StringArrayList($L)"));
            } else if (typeUtils.isSubtype(asType, parcelableType)) {
                statment += (isActivity ? ("ParcelableArrayListExtra($L)") : ("ParcelableArrayList($L)"));
            } else {
                statment += (isActivity ? ("CharSequenceArrayListExtra($L)") : ("CharSequenceArrayList($L)"));
            }
        } else if (typeUtils.isSubtype(typeMirror, serializableType)) {
            statment += (isActivity ? ("SerializableExtra($L)") : ("Serializable($L)"));
        } else {
            throw new IllegalAccessException(typeMirror.toString() + "--> This type is not supported");
        }

        return statment;
    }

    boolean isList(TypeMirror typeMirror) {
        String string = typeMirror.toString();
        logger.info(string);

//        string = string.substring(0, string.indexOf("<"));
        return string.startsWith(ARRAYLIST);
    }
}
