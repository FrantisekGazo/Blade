package eu.f3rog.blade.compiler.builder;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;

import javax.annotation.Generated;
import javax.annotation.processing.Filer;

import eu.f3rog.blade.compiler.BladeProcessor;
import eu.f3rog.blade.compiler.name.GCN;
import eu.f3rog.blade.compiler.name.GPN;
import eu.f3rog.blade.compiler.util.ProcessorError;
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
     * @param isClass         <code>true</code> if generated should be <code>class</code>. <code>false</code> if <code>interface</code>.
     * @param genClassName    Name of generated class.
     * @param arg             Class name that will be used when formatting generated class name.
     * @param genPackageNames Package in which class will be generated.
     */
    public BaseClassBuilder(boolean isClass, GCN genClassName, ClassName arg, GPN... genPackageNames) throws ProcessorError {
        mGenClassName = genClassName;
        mGenClassNameArg = arg;
        mGenPackageName = genPackageNames;
        if (isClass) {
            mBuilder = TypeSpec.classBuilder(getClassName().simpleName());
        } else {
            mBuilder = TypeSpec.interfaceBuilder(getClassName().simpleName());
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
        this(true, genClassName, arg, genPackageNames);
    }

    /**
     * Constructor for <code>class</code> builder.
     *
     * @param genClassName    Name of generated class.
     * @param genPackageNames Package in which class will be generated.
     */
    public BaseClassBuilder(GCN genClassName, GPN... genPackageNames) throws ProcessorError {
        this(true, genClassName, null, genPackageNames);
    }

    /**
     * Constructor
     *
     * @param isClass         <code>true</code> if generated should be <code>class</code>. <code>false</code> if <code>interface</code>.
     * @param genClassName    Name of generated class.
     * @param genPackageNames Package in which class will be generated.
     */
    public BaseClassBuilder(boolean isClass, GCN genClassName, GPN... genPackageNames) throws ProcessorError {
        this(isClass, genClassName, null, genPackageNames);
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
                        .addMember("value", "$S", BladeProcessor.class.getName())
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
     *
     * @param filer File creator.
     */
    @Override
    public void build(Filer filer) throws ProcessorError, IOException {
        end();
        TypeSpec cls = mBuilder.build();
        // create file
        JavaFile javaFile = JavaFile.builder(getClassName().packageName(), cls).build();
        javaFile.writeTo(filer);
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
