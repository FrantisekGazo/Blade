package eu.f3rog.blade.compiler.builder;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;

import javax.annotation.Generated;

import eu.f3rog.blade.compiler.name.GCN;
import eu.f3rog.blade.compiler.name.GPN;
import eu.f3rog.blade.compiler.util.ProcessorError;
import eu.f3rog.blade.compiler.util.ProcessorUtils;
import eu.f3rog.blade.compiler.util.StringUtils;

/**
 * Class {@link BaseClassBuilder} is used for building a Java class file.
 *
 * @author FrantisekGazo
 * @version 2015-10-17
 */
public abstract class BaseClassBuilder
        implements IBuildable {

    private static final boolean LOG_SUCCESS = false;

    /**
     * Format of full class name.
     */
    private static final String FORMAT_FULL_CLASS_NAME = "%s.%s";
    /**
     * Format of instance field.
     */
    private static final String FORMAT_INSTANCE_FIELD_NAME = "m%s";
    /**
     * Format of class field.
     */
    private static final String FORMAT_CLASS_FIELD_NAME = "s%s";

    private TypeSpec.Builder mBuilder;
    private ClassName mClassName = null;
    private final GCN mGenClassName;
    private final ClassName mGenClassNameArg;
    private final GPN[] mGenPackageName;

    /**
     * Constructor
     *
     * @param builderType     Builder Type
     * @param genClassName    Name of generated class.
     * @param arg             Class name that will be used when formatting generated class name.
     * @param genPackageNames Package in which class will be generated.
     */
    public BaseClassBuilder(BuilderType builderType, GCN genClassName, ClassName arg, GPN... genPackageNames) throws ProcessorError {
        mGenClassName = genClassName;
        mGenClassNameArg = arg;
        mGenPackageName = genPackageNames;
        switch (builderType) {
            case CLASS:
                mBuilder = TypeSpec.classBuilder(getClassName().simpleName());
                break;
            case INTERFACE:
                mBuilder = TypeSpec.interfaceBuilder(getClassName().simpleName());
                break;
            case ANNOTATION:
                mBuilder = TypeSpec.annotationBuilder(getClassName().simpleName());
                break;
        }
        start();
    }

    /**
     * Constructor for <code>class</code> builder.
     *
     * @param genClassName    Name of generated class.
     * @param arg             Class name that will be used when formatting generated class name.
     * @param genPackageNames Package in which class will be generated.
     */
    public BaseClassBuilder(GCN genClassName, ClassName arg, GPN... genPackageNames) throws ProcessorError {
        this(BuilderType.CLASS, genClassName, arg, genPackageNames);
    }

    /**
     * Constructor for <code>class</code> builder.
     *
     * @param genClassName    Name of generated class.
     * @param genPackageNames Package in which class will be generated.
     */
    public BaseClassBuilder(GCN genClassName, GPN... genPackageNames) throws ProcessorError {
        this(BuilderType.CLASS, genClassName, null, genPackageNames);
    }

    /**
     * Constructor
     *
     * @param builderType     Builder Type
     * @param genClassName    Name of generated class.
     * @param genPackageNames Package in which class will be generated.
     */
    public BaseClassBuilder(BuilderType builderType, GCN genClassName, GPN... genPackageNames) throws ProcessorError {
        this(builderType, genClassName, null, genPackageNames);
    }

    /**
     * Returns {@link TypeSpec.Builder} of generated class.
     */
    public TypeSpec.Builder getBuilder() {
        return mBuilder;
    }

    public ClassName getArgClassName() {
        return mGenClassNameArg;
    }

    /**
     * Returns {@link ClassName} of generated class.
     */
    public ClassName getClassName() {
        if (mClassName == null) {
            // build CLASS name
            String name = mGenClassName.formatName(mGenClassNameArg);
            // save
            mClassName = ClassName.get(getPackage(), name);
        }
        return mClassName;
    }

    private String getPackage() {
        if (mGenPackageName.length == 0) {
            return getArgClassName().packageName();
        } else {
            return GPN.toString(mGenPackageName);
        }
    }

    /**
     * Returns full name of generated class.
     */
    protected String getFullName() {
        return String.format(FORMAT_FULL_CLASS_NAME, getClassName().packageName(), getClassName().simpleName());
    }

    /**
     * Called in the constructor.
     */
    public void start() throws ProcessorError {
        // add @Generated
        getBuilder().addAnnotation(
                AnnotationSpec.builder(Generated.class)
                        .addMember("value", "$S", "by Bl@de")
                        .build()
        );
    }

    /**
     * Called before building java file.
     */
    public void end() throws ProcessorError {
    }

    /**
     * Builds Java file for generated class.
     */
    @Override
    public void build() throws ProcessorError, IOException {
        end();
        TypeSpec cls = mBuilder.build();
        // create file
        JavaFile javaFile = JavaFile.builder(getClassName().packageName(), cls).build();
        javaFile.writeTo(ProcessorUtils.getFiler());
        //javaFile.writeTo(System.out);

        if (LOG_SUCCESS) {
            System.out.println(String.format("Class <%s> successfully generated.", getFullName()));
        }
    }

    /**
     * Creates instance field name.
     */
    protected String createInstanceFieldName(String name) {
        return String.format(FORMAT_INSTANCE_FIELD_NAME, StringUtils.startUpperCase(name));
    }

    /**
     * Creates class field name.
     */
    protected String createClassFieldName(String name) {
        return String.format(FORMAT_CLASS_FIELD_NAME, StringUtils.startUpperCase(name));
    }

}
