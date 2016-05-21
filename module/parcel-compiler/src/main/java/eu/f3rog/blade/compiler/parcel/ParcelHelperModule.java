package eu.f3rog.blade.compiler.parcel;

import android.os.Parcel;
import android.os.Parcelable;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.sun.tools.javac.util.Pair;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

import blade.ParcelIgnore;
import eu.f3rog.blade.compiler.builder.annotation.WeaveBuilder;
import eu.f3rog.blade.compiler.builder.helper.BaseHelperModule;
import eu.f3rog.blade.compiler.builder.helper.HelperClassBuilder;
import eu.f3rog.blade.compiler.parcel.p.CallFormat;
import eu.f3rog.blade.compiler.parcel.p.Parceler;
import eu.f3rog.blade.compiler.util.ProcessorError;
import eu.f3rog.blade.compiler.util.ProcessorUtils;
import eu.f3rog.blade.compiler.util.StringUtils;

import static eu.f3rog.blade.compiler.util.ProcessorUtils.addClassAsParameter;
import static eu.f3rog.blade.compiler.util.ProcessorUtils.fullName;
import static eu.f3rog.blade.compiler.util.ProcessorUtils.hasSomeModifier;

/**
 * Class {@link ParcelHelperModule}
 *
 * @author FrantisekGazo
 * @version 2015-12-19
 */
public class ParcelHelperModule
        extends BaseHelperModule {

    private static class Field {

        VariableElement element;
        ExecutableElement getter;
        ExecutableElement setter;

        public Field(VariableElement element) {
            this.element = element;
        }

        public void setMethods(ExecutableElement getter, ExecutableElement setter) {
            this.getter = getter;
            this.setter = setter;
        }

        public boolean isAccessible() {
            return getter == null;
        }
    }


    private static final String FIELD_NAME_CREATOR = "CREATOR";
    private static final String METHOD_NAME_WRITE_TO_PARCEL = "writeToParcel";
    private static final String METHOD_NAME_READ_FROM_PARCEL = "readFromParcel";

    private final List<Field> mFields = new ArrayList<>();

    @Override
    public void checkClass(TypeElement e) throws ProcessorError {
        // support any class that implements Parcelable
        if (!ProcessorUtils.isSubClassOf(e, Parcelable.class)) {
            throw new ProcessorError(e, ParcelErrorMsg.Invalid_Parcel_class);
        }

        // check constructor
        if (!hasParcelableConstructor(e)) {
            throw new ProcessorError(e, ParcelErrorMsg.Parcel_class_without_constructor);
        }
    }

    @Override
    public void add(TypeElement e) throws ProcessorError {
        for (Element ee : e.getEnclosedElements()) {
            if (ee.getKind() == ElementKind.FIELD) {
                if (hasSomeModifier(ee, Modifier.STATIC, Modifier.FINAL)) {
                    continue;
                }

                // do not process if ignored
                if (ee.getAnnotation(ParcelIgnore.class) != null) {
                    continue;
                }

                Field field = new Field((VariableElement) ee);

                if (hasSomeModifier(ee, Modifier.PRIVATE, Modifier.PROTECTED)) {
                    // use setter/getter
                    ExecutableElement[] gs = findGetterAndSetter(e, field.element);

                    if (gs[0] == null) {
                        throw new ProcessorError(ee, ParcelErrorMsg.Missing_Access_Method, ee.getSimpleName(), "getter");
                    } else if (gs[1] == null) {
                        throw new ProcessorError(ee, ParcelErrorMsg.Missing_Access_Method, ee.getSimpleName(), "setter");
                    }

                    field.setMethods(gs[0], gs[1]);

                    // TODO : maybe can use @Named for specifying name without prefix (e.g. 'm') ??
                }

                mFields.add(field);
            }
        }
    }

    private ExecutableElement[] findGetterAndSetter(TypeElement cls, VariableElement variable) {
        ExecutableElement[] gs = new ExecutableElement[]{null, null};

        List<? extends Element> enclosedElements = cls.getEnclosedElements();
        String capitalizedVarName = StringUtils.startUpperCase(variable.getSimpleName().toString());
        String getterName = String.format("get%s", capitalizedVarName);
        String setterName = String.format("set%s", capitalizedVarName);

        for (int i = 0, c = enclosedElements.size(); i < c; i++) {
            Element e = enclosedElements.get(i);
            if (e.getKind() == ElementKind.METHOD) {
                if (getterName.equals(e.getSimpleName().toString())) {
                    gs[0] = (ExecutableElement) e;
                } else if (setterName.equals(e.getSimpleName().toString())) {
                    gs[1] = (ExecutableElement) e;
                } else {
                    continue;
                }

                if (gs[0] != null && gs[1] != null) {
                    break;
                }
            }
        }

        return gs;
    }

    @Override
    public boolean implement(HelperClassBuilder builder) throws ProcessorError {
        if (mFields.isEmpty()) {
            return false;
        }

        addCreatorField(builder);
        addWriteToParcelMethod(builder);
        addReadFromParcelMethod(builder);

        return true;
    }

    private void addCreatorField(HelperClassBuilder builder) {
        FieldSpec.Builder field = FieldSpec.builder(
                ParameterizedTypeName.get(ClassName.get(Parcelable.Creator.class), builder.getArgClassName()),
                FIELD_NAME_CREATOR,
                Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL
        );

        field.addAnnotation(
                WeaveBuilder.weave().field()
                        .withStatement("%s.%s", fullName(builder.getClassName()), FIELD_NAME_CREATOR)
                        .build()
        );
        field.initializer("new $T<$T>() {\n" +
                        "\t@$T\n" +
                        "\tpublic $T createFromParcel($T in) {\n" +
                        "\t\treturn new $T(in);\n" +
                        "\t}\n" +
                        "\t@$T\n" +
                        "\tpublic $T[] newArray(int size) {\n" +
                        "\t\treturn new $T[size];\n" +
                        "\t}\n" +
                        "}",
                Parcelable.Creator.class, builder.getArgClassName(),
                Override.class, builder.getArgClassName(), Parcel.class, builder.getArgClassName(),
                Override.class, builder.getArgClassName(), builder.getArgClassName()
        );

        builder.getBuilder().addField(field.build());
    }

    private void addWriteToParcelMethod(HelperClassBuilder builder) throws ProcessorError {
        ClassName targetClassName = builder.getArgClassName();
        String target = "target";
        String parcel = "parcel";

        MethodSpec.Builder method = MethodSpec.methodBuilder(METHOD_NAME_WRITE_TO_PARCEL)
                .addAnnotation(
                        WeaveBuilder.weave().method(METHOD_NAME_WRITE_TO_PARCEL, Parcel.class, int.class)
                                .placed(WeaveBuilder.MethodWeaveType.AFTER_SUPER)
                                .withStatement("%s.%s(this, $1);", fullName(builder.getClassName()), METHOD_NAME_WRITE_TO_PARCEL)
                                .build()
                )
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC);

        addClassAsParameter(method, targetClassName, target);
        method.addParameter(Parcel.class, parcel);

        for (int i = 0, c = mFields.size(); i < c; i++) {
            Field field = mFields.get(i);
            CallFormat writeCall = Parceler.writeCall(field.element);

            if (writeCall == null) {
                continue; // TODO : should throw exception or ignore field ?!
            }

            Pair<String, List<Object>> mappedWriteCall = mapCall(field, writeCall, target, parcel);
            method.addStatement(mappedWriteCall.fst, mappedWriteCall.snd.toArray(new Object[mappedWriteCall.snd.size()]));
        }

        builder.getBuilder().addMethod(method.build());
    }

    private void addReadFromParcelMethod(HelperClassBuilder builder) throws ProcessorError {
        ClassName targetClassName = builder.getArgClassName();
        String target = "target";
        String parcel = "parcel";

        MethodSpec.Builder method = MethodSpec.methodBuilder(METHOD_NAME_READ_FROM_PARCEL)
                .addAnnotation(
                        WeaveBuilder.weave().constructor(Parcel.class)
                                .withStatement("%s.%s(this, $1);", fullName(builder.getClassName()), METHOD_NAME_READ_FROM_PARCEL)
                                .build()
                )
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC);

        addClassAsParameter(method, targetClassName, target);
        method.addParameter(Parcel.class, parcel);

        for (int i = 0, c = mFields.size(); i < c; i++) {
            Field field = mFields.get(i);
            CallFormat readCall = Parceler.readCall(field.element);

            if (readCall == null) {
                continue; // TODO : should throw exception or ignore field ?!
            }

            Pair<String, List<Object>> mappedReadCall = mapCall(field, readCall, target, parcel);
            String format = mappedReadCall.fst;
            List<Object> args = mappedReadCall.snd;
            if (field.isAccessible()) {
                format = String.format("$N.$N = %s", format);
                args.add(0, field.element.getSimpleName());
                args.add(0, target);
            } else {
                format = String.format("$N.$N(%s)", format);
                args.add(0, field.setter.getSimpleName());
                args.add(0, target);
            }

            method.addStatement(format, args.toArray(new Object[args.size()]));
        }

        builder.getBuilder().addMethod(method.build());
    }

    private Pair<String, List<Object>> mapCall(Field field, CallFormat call, String target, String parcel) {
        Object[] formatArgs = new Object[call.getArgs().length];
        List<Object> args = new ArrayList<>(call.getArgs().length);
        for (int j = 0, d = call.getArgs().length; j < d; j++) {
            CallFormat.Arg arg = call.getArgs()[j];

            switch (arg) {
                case PARCEL: {
                    formatArgs[j] = "$N";
                    args.add(parcel);
                    break;
                }
                case TARGET_GETTER: {
                    if (field.isAccessible()) {
                        formatArgs[j] = "$N.$N";
                        args.add(target);
                        args.add(field.element.getSimpleName());
                    } else {
                        formatArgs[j] = "$N.$N()";
                        args.add(target);
                        args.add(field.getter.getSimpleName());
                    }
                    break;
                }
                case RAW_TYPE: {
                    TypeName rawType = ProcessorUtils.getRawType(field.element.asType());
                    formatArgs[j] = "$T";
                    args.add(rawType);
                    break;
                }
                case CLASS_LOADER_OR_NULL: {
                    TypeName rawType = ProcessorUtils.getRawType(field.element.asType());
                    if (rawType != null) {
                        formatArgs[j] = "$T.class.getClassLoader()";
                        args.add(rawType);
                    } else {
                        formatArgs[j] = "null";
                    }
                    break;
                }
                case TYPE: {
                    formatArgs[j] = "$T";
                    args.add(field.element.asType());
                    break;
                }
                default:
                    throw new IllegalStateException();
            }
        }

        String format = String.format(call.getFormat(), formatArgs);

        return new Pair<>(format, args);
    }

    private boolean hasParcelableConstructor(TypeElement typeElement) {
        for (Element e : typeElement.getEnclosedElements()) {
            if (e.getKind() == ElementKind.CONSTRUCTOR && hasSomeModifier(e, Modifier.PUBLIC)) {
                ExecutableElement c = (ExecutableElement) e;
                List<? extends VariableElement> params = c.getParameters();
                if (params.size() == 1 && ClassName.get(params.get(0).asType()).equals(ClassName.get(Parcel.class))) {
                    return true;
                }
            }
        }
        return false;
    }

}
