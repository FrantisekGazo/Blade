package eu.f3rog.automat.weaving.util;

import java.io.File;

import javassist.build.IClassTransformer;

/**
 * Interface {@link IWeaver} adds method for setting destination directory
 * of transformed classes to {@link IClassTransformer}.
 *
 * @author FrantisekGazo
 * @version 2015-11-08
 */
public interface IWeaver extends IClassTransformer {

    void setDestinationDirectory(File dst);

}

