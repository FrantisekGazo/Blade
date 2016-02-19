package eu.f3rog.blade.compiler.mvp;

import android.app.Activity;
import android.os.Bundle;

import com.squareup.javapoet.FieldSpec;

import java.util.UUID;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

import blade.mvp.PresenterManager;
import eu.f3rog.blade.compiler.builder.helper.BaseHelperModule;
import eu.f3rog.blade.compiler.builder.helper.HelperClassBuilder;
import eu.f3rog.blade.compiler.builder.weaving.WeaveBuilder;
import eu.f3rog.blade.compiler.name.EClass;
import eu.f3rog.blade.compiler.util.ProcessorError;

import static eu.f3rog.blade.compiler.util.ProcessorUtils.isSubClassOf;

/**
 * Class {@link PresenterScopeHelperModule}
 *
 * @author FrantisekGazo
 * @version 2015-12-15
 */
public class PresenterScopeHelperModule extends BaseHelperModule {

    private static final String FIELD_NAME_ACTIVITY_ID = "mActivityId";
    private static final String SAVE_TAG_ACTIVITY_ID = "blade:activity_id";

    @Override
    public void checkClass(TypeElement e) throws ProcessorError {
        if (!isSubClassOf(e, Activity.class) && !isSubClassOf(e, EClass.AppCompatActivity.getName())) {
            throw new IllegalStateException();
        }
    }

    @Override
    public boolean implement(ProcessingEnvironment processingEnvironment, HelperClassBuilder builder) throws ProcessorError {
        FieldSpec field = FieldSpec.builder(String.class, FIELD_NAME_ACTIVITY_ID, Modifier.PRIVATE)
                .addAnnotation(
                        WeaveBuilder.weave()
                                .field().withStatement("null")
                                .and()
                                .method("getSystemService", String.class)
                                .withStatement("if (%s.ACTIVITY_ID.equals($1)) { return this.mActivityId; }",
                                        PresenterManager.class.getCanonicalName())
                                .and()
                                .method("onDestroy")
                                .withStatement("if (this.isFinishing()) { %s.removePresentersFor(this); }",
                                        PresenterManager.class.getCanonicalName())
                                .and()
                                .method("onCreate", Bundle.class)
                                .withStatement("if ($1 != null) {")
                                .withStatement(" this.%s = $1.getString('%s');", FIELD_NAME_ACTIVITY_ID, SAVE_TAG_ACTIVITY_ID)
                                .withStatement(" %s.restorePresentersFor(this, $1);", PresenterManager.class.getCanonicalName())
                                .withStatement(" } else {")
                                .withStatement(" this.%s = %s.randomUUID().toString();", FIELD_NAME_ACTIVITY_ID, UUID.class.getCanonicalName())
                                .withStatement(" }")
                                .and()
                                .method("onSaveInstanceState", Bundle.class)
                                .withStatement("$1.putString('%s', this.%s);", SAVE_TAG_ACTIVITY_ID, FIELD_NAME_ACTIVITY_ID)
                                .withStatement(" %s.savePresentersFor(this, $1);", PresenterManager.class.getCanonicalName())
                                .build()
                )
                .build();

        builder.getBuilder().addField(field);

        return true;
    }

}
