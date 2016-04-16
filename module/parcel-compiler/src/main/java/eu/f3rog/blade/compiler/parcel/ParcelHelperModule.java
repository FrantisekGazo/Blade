package eu.f3rog.blade.compiler.parcel;

import android.os.Parcel;
import android.os.Parcelable;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;

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
import eu.f3rog.blade.compiler.parcel.p.Parceler;
import eu.f3rog.blade.compiler.util.ProcessorError;
import eu.f3rog.blade.compiler.util.ProcessorUtils;

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

    private static final String FIELD_NAME_CREATOR = "CREATOR";
    private static final String METHOD_NAME_WRITE_TO_PARCEL = "writeToParcel";
    private static final String METHOD_NAME_READ_FROM_PARCEL = "readFromParcel";

    private final List<VariableElement> mAttributeNames = new ArrayList<>();

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
                if (hasSomeModifier(ee, Modifier.STATIC, Modifier.PRIVATE, Modifier.PROTECTED)) {
                    continue;
                }

                // do not process if ignored
                if (ee.getAnnotation(ParcelIgnore.class) != null) {
                    continue;
                }

                mAttributeNames.add((VariableElement) ee);
            }
        }
    }

    @Override
    public boolean implement(HelperClassBuilder builder) throws ProcessorError {
        if (mAttributeNames.isEmpty()) {
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

        for (int i = 0, c = mAttributeNames.size(); i < c; i++) {
            VariableElement ve = mAttributeNames.get(i);
            Parceler.write(ve, method, parcel, target);
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

        for (int i = 0, c = mAttributeNames.size(); i < c; i++) {
            VariableElement ve = mAttributeNames.get(i);
            Parceler.read(ve, method, parcel, target);
        }

        builder.getBuilder().addMethod(method.build());
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
