package eu.f3rog.blade.compiler.util;

import javax.tools.JavaFileObject;

/**
 * Interface {@link IBody}
 *
 * @author FrantisekGazo
 * @version 2015-11-20
 */
public interface IBody {

    JavaFileObject body(String... lines);

}

