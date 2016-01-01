package eu.f3rog.blade.compiler.builder.weaving;

/**
 * Class {@link IWeaveUse}
 *
 * @author FrantisekGazo
 * @version 2015-12-31
 */
public interface IWeaveUse extends IWeaveBuild {

    IWeaveBuild use(Integer... argNumbers);

}
