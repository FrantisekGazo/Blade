package eu.f3rog.blade.weaving;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.f3rog.blade.compiler.builder.annotation.WeaveParser;
import eu.f3rog.blade.core.Weave;
import eu.f3rog.blade.core.WeaveInto;
import eu.f3rog.blade.core.Weaves;
import eu.f3rog.blade.weaving.interfaces.Interfaces;
import eu.f3rog.blade.weaving.util.AWeaver;
import eu.f3rog.javassist.exception.AfterBurnerImpossibleException;
import groovy.lang.Tuple2;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.AnnotationMemberValue;
import javassist.bytecode.annotation.ArrayMemberValue;
import javassist.bytecode.annotation.MemberValue;

import static eu.f3rog.blade.weaving.util.WeavingUtil.getAnnotations;

public final class BladeWeaver
        extends AWeaver {

    @Override
    public void weave(ClassPool classPool, List<CtClass> classes) {
        final List<Tuple2<CtClass, CtClass>> processingList = new ArrayList<>();
        for (CtClass cls : classes) {
            if (cls.hasAnnotation(WeaveInto.class)) {
                CtClass intoClass;
                try {
                    intoClass = classPool.get(getWeaveClassTarget(cls));
                } catch (NotFoundException e) {
                    continue;
                }
                processingList.add(new Tuple2<>(cls, intoClass));
            }
        }

        final Map<Tuple2<CtClass, CtClass>, Integer> superClassCountMap = new HashMap<>(classes.size());
        for (Tuple2<CtClass, CtClass> toProcess : processingList) {
            int superClassCount = 0;
            try {
                CtClass checker = toProcess.getSecond();
                while (checker.getSuperclass() != null) {
                    superClassCount++;
                    checker = checker.getSuperclass();
                }
            } catch (NotFoundException e) {
                System.out.println("Why is this throwing a NotFoundException? " + e);
            }
            superClassCountMap.put(toProcess, superClassCount);
        }
        Collections.sort(processingList, new Comparator<Tuple2<CtClass, CtClass>>() {
            @Override
            public int compare(Tuple2<CtClass, CtClass> firstPair, Tuple2<CtClass, CtClass> secondPair) {
                return Integer.compare(superClassCountMap.get(firstPair), superClassCountMap.get(secondPair));
            }
        });

        for (Tuple2<CtClass, CtClass> toProcess : processingList) {
            weave(toProcess.getFirst(), toProcess.getSecond());
        }
    }

    private static class Metadata {
        String into;
        CtClass[] args;
        String statement;
    }

    private final static class MetadataComparator
            implements Comparator<Metadata> {

        @Override
        public int compare(Metadata l, Metadata r) {
            // order descending based on 'into' value - higher priority should be first
            if (l == null || l.into == null) {
                return 1;
            }
            if (r == null || r.into == null) {
                return -1;
            }
            return -l.into.compareTo(r.into);
        }
    }

    private static final String HELPER_NAME_FORMAT = "%s.%s_Helper";

    /**
     * Constructor
     */
    public BladeWeaver(boolean debug) {
        super(debug);
    }

    public void weave(CtClass helperClass, CtClass intoClass) {
        lognl("|~ Weaving start '%s'", intoClass.getName());
        try {
            ClassPool classPool = intoClass.getClassPool();

            // get field metadata
            for (CtField field : helperClass.getDeclaredFields()) {
                lognl("field '%s'", field.getName());

                List<Metadata> metadata = loadWeaveMetadata(classPool, field);
                weave(metadata, intoClass, field);
            }

            // get method metadata
            List<Metadata> allMethodMetadata = new ArrayList<>();
            for (CtMethod method : helperClass.getDeclaredMethods()) {
                lognl("method '%s'", method.getName());

                List<Metadata> metadata = loadWeaveMetadata(classPool, method);
                allMethodMetadata.addAll(metadata);
            }
            // sort metadata based on priority and weave them
            Collections.sort(allMethodMetadata, new MetadataComparator());
            for (Metadata metadata : allMethodMetadata) {
                weave(metadata, intoClass, null);
            }

            // weave interfaces
            for (CtClass interfaceClass : helperClass.getInterfaces()) {
                lognl("interface '%s'", interfaceClass.getName());
                Interfaces.weaveInterface(interfaceClass, intoClass, getJavassistHelper());
            }

            lognl("~| Weaving done '%s'", intoClass.getName());
        } catch (Exception e) {
            lognl("");
            lognl("~| Weaving failed '%s'", intoClass.getName());
            lognl("");
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }

    private void weave(List<Metadata> m, CtClass intoClass, CtField helperField) throws NotFoundException, CannotCompileException, AfterBurnerImpossibleException {
        for (int i = 0, c = m.size(); i < c; i++) {
            Metadata metadata = m.get(i);
            weave(metadata, intoClass, helperField);
        }
    }

    private void weave(Metadata metadata, CtClass intoClass, CtField helperField) throws NotFoundException, CannotCompileException, AfterBurnerImpossibleException {
        if (metadata == null) {
            lognl(" ~x nowhere");
            return;
        }

        if (helperField != null && Weave.WEAVE_FIELD.equals(metadata.into)) {
            // weave field
            CtField f = new CtField(helperField.getType(), helperField.getName(), intoClass);
            f.setModifiers(helperField.getModifiers());

            log(" ~> field '%s'", f.getName());
            if (metadata.statement != null) {
                lognl(" ~~~ %s", metadata.statement);
                intoClass.addField(f, CtField.Initializer.byExpr(metadata.statement));
            } else {
                lognl(" ~~~ without statement");
                intoClass.addField(f);
            }
        } else {
            String body = "{ " + metadata.statement + " }";

            if (Weave.WEAVE_CONSTRUCTOR.equals(metadata.into)) {
                log(" ~> constructor");
                // weave into constructor
                getJavassistHelper().insertConstructor(body, intoClass, metadata.args);
                lognl(" ~~~ %s", body);
            } else {
                // weave into method
                WeaveParser.Into into = WeaveParser.parseInto(metadata.into);

                if (into.shouldRename()) {
                    lognl(" ~> rename '%s' to '%s'", into.getMethodName(), into.getRename());
                    getJavassistHelper().renameMethod(intoClass, into.getMethodName(), into.getRename(), metadata.args);
                }

                log(" ~> method '%s' %s with %s priority", into.getMethodName(), into.getMethodWeaveType(), into.getPriority());
                lognl(" ~~~ %s", body);

                switch (into.getMethodWeaveType()) {
                    case BEFORE_BODY:
                        getJavassistHelper().insertBeforeBody(body, intoClass, into.getMethodName(), metadata.args);
                        break;
                    case AFTER_BODY:
                        getJavassistHelper().insertAfterBody(body, intoClass, into.getMethodName(), metadata.args);
                        break;
                    case BEFORE_SUPER:
                        try {
                            getJavassistHelper().insertBeforeSuper(body, intoClass, into.getMethodName(), metadata.args);
                        } catch (Exception e) { // put at beginning if super not found
                            getJavassistHelper().insertBeforeBody(body, intoClass, into.getMethodName(), metadata.args);
                        }
                        break;
                    case AFTER_SUPER:
                        try {
                            getJavassistHelper().insertAfterSuper(body, intoClass, into.getMethodName(), metadata.args);
                        } catch (Exception e) { // put at beginning if super not found
                            getJavassistHelper().insertBeforeBody(body, intoClass, into.getMethodName(), metadata.args);
                        }
                        break;
                    default:
                        throw new IllegalStateException();
                }
            }
        }
    }

    private CtClass getHelper(CtClass cls) throws NotFoundException {
        return cls.getClassPool()
                .get(String.format(HELPER_NAME_FORMAT, cls.getPackageName(), cls.getSimpleName()));
    }

    private boolean hasHelper(CtClass cls) {
        try {
            return getHelper(cls) != null;
        } catch (NotFoundException e) {
            return false;
        }
    }

    private List<Metadata> loadWeaveMetadata(ClassPool classPool, CtMethod method) throws NotFoundException {
        AnnotationsAttribute attr = getAnnotations(method);
        if (attr != null) {
            return loadWeaveMetadata(classPool, attr);
        }

        return Collections.emptyList();
    }

    private List<Metadata> loadWeaveMetadata(ClassPool classPool, CtField field) throws NotFoundException {
        AnnotationsAttribute attr = getAnnotations(field);
        if (attr != null) {
            return loadWeaveMetadata(classPool, attr);
        }

        return Collections.emptyList();
    }

    private List<Metadata> loadWeaveMetadata(ClassPool classPool, AnnotationsAttribute attr) throws NotFoundException {
        Annotation a;

        a = attr.getAnnotation(Weave.class.getName());
        if (a != null) {
            return Collections.singletonList(readWeaveAnnotation(a, classPool));
        }

        a = attr.getAnnotation(Weaves.class.getName());
        if (a != null) {
            return readWeavesAnnotation(a, classPool);
        }

        return Collections.emptyList();
    }


    private Metadata readWeaveAnnotation(Annotation weaveAnnotation, ClassPool classPool) throws NotFoundException {
        Metadata metadata = new Metadata();

        // get INTO
        MemberValue val = weaveAnnotation.getMemberValue("into");
        if (val != null) {
            metadata.into = val.toString().replaceAll("\"", "");
        }
        // get INTO ARGS
        metadata.args = readArgs(weaveAnnotation, classPool);
        // get STATEMENT
        val = weaveAnnotation.getMemberValue("statement");
        if (val != null) {
            metadata.statement = val.toString().replaceAll("\"", "");
            metadata.statement = metadata.statement.replaceAll("'", "\"");
            if (metadata.statement.length() == 0) {
                metadata.statement = null;
            }
        }

        return metadata;
    }

    private CtClass[] readArgs(Annotation weaveAnnotation, ClassPool classPool) throws NotFoundException {
        ArrayMemberValue arrayMemberValue = (ArrayMemberValue) weaveAnnotation.getMemberValue("args");
        if (arrayMemberValue != null) {
            MemberValue[] memberValues = arrayMemberValue.getValue();
            CtClass[] classes = new CtClass[memberValues.length];
            for (int i = 0; i < memberValues.length; i++) {
                String className = memberValues[i].toString().replaceAll("\"", "");
                classes[i] = classPool.get(className);
            }
            return classes;
        } else {
            return new CtClass[0];
        }
    }

    private List<Metadata> readWeavesAnnotation(Annotation weavesAnnotation, ClassPool classPool) throws NotFoundException {
        ArrayMemberValue arrayMemberValue = (ArrayMemberValue) weavesAnnotation.getMemberValue("value");
        if (arrayMemberValue != null) {
            MemberValue[] memberValues = arrayMemberValue.getValue();
            List<Metadata> annotations = new ArrayList<>(memberValues.length);
            for (int i = 0; i < memberValues.length; i++) {
                AnnotationMemberValue memberValue = (AnnotationMemberValue) memberValues[i];
                Metadata m = readWeaveAnnotation(memberValue.getValue(), classPool);
                annotations.add(m);
            }
            return annotations;
        } else {
            return Collections.emptyList();
        }
    }

    private String getWeaveClassTarget(CtClass weaveClass) {
        AnnotationsAttribute annotations = getAnnotations(weaveClass);

        Annotation a = annotations.getAnnotation(WeaveInto.class.getName());
        if (a != null) {
            return a.getMemberValue("target").toString().replaceAll("\"", "");
        }
        return "";
    }
}
