package blade;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Class annotated with @{@link Parcel} has to
 * implement {@link android.os.Parcelable} (empty methods are enough)
 * and contain constructor with parameter of type {@link android.os.Parcel}.
 * <p>
 * Then the {@link android.os.Parcelable} implementation will be generated automatically.
 *
 * @author FrantisekGazo
 */
@Target(TYPE)
@Retention(SOURCE)
public @interface Parcel {
}
