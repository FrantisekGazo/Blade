package blade;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * If field is annotated with @{@link ParcelIgnore}
 * then the generated {@link android.os.Parcelable} implementation will ignore this field.
 *
 * @author FrantisekGazo
 */
@Target(FIELD)
@Retention(SOURCE)
public @interface ParcelIgnore {
}
