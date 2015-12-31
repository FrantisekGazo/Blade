package eu.f3rog.blade.compiler.builder;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;

import java.util.ArrayList;
import java.util.List;

import eu.f3rog.blade.compiler.name.GCN;
import eu.f3rog.blade.compiler.name.GPN;
import eu.f3rog.blade.compiler.util.ProcessorError;

/**
 * Class {@link WeaverGuideBuilder}
 *
 * @author FrantisekGazo
 * @version 2015-12-15
 */
public class WeaverGuideBuilder
        extends BaseClassBuilder {

    private List<String> mHelpedClasses = new ArrayList<>();

    public WeaverGuideBuilder() throws ProcessorError {
        super(BuilderType.INTERFACE, GCN.WEAVER_GUIDE, GPN.BLADE);
    }

    public void addHelpedClassName(ClassName className) {
        mHelpedClasses.add(String.format("%s.%s", className.packageName(), className.simpleName()));
    }

    @Override
    public void end() throws ProcessorError {
        super.end();

        StringBuilder format = new StringBuilder();
        Object[] args = mHelpedClasses.toArray(new Object[mHelpedClasses.size()]);

        format.append("{\n");
        for (int i = 0; i < args.length; i++) {
            if (i > 0) {
                format.append(",\n");
            }
            format.append("$S");
        }
        format.append("\n}");

        getBuilder().addAnnotation(
                AnnotationSpec.builder(ClassName.get(GPN.BLADE.getName(), GCN.WEAVE.getName()))
                        .addMember("value", format.toString(), args)
                        .build()
        );
    }
}
