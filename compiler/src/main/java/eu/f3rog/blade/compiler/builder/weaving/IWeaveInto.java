package eu.f3rog.blade.compiler.builder.weaving;

/**
 * Class {@link eu.f3rog.blade.compiler.builder.weaving.IWeaveInto}
 *
 * @author FrantisekGazo
 * @version 2015-12-31
 */
public interface IWeaveInto extends IWeaveBuild {

    IWeaveUse into(String methodName, Class... args);

}
