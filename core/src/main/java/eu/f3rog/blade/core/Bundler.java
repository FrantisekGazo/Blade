package eu.f3rog.blade.core;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;


/**
 * Defines custom serialization and deserialization of given type.
 *
 * @param <T> Type that will be de/serialized.
 * @author FrantisekGazo
 */
public interface Bundler<T> {

    /**
     * Saves given <code>value</code> into given <code>state</code>.
     *
     * @param value Value that will be saved.
     * @param state State {@link Bundle} that is used only by this {@link Bundler}.
     *              Feel free to use any keys for storing values.
     */
    void save(@Nullable final T value, @NonNull final Bundle state);

    /**
     * Restores a value from given <code>state</code>.
     *
     * @param state State {@link Bundle} that is used only by this {@link Bundler}.
     */
    @Nullable
    T restore(@NonNull final Bundle state);
}
