Change Log
==========

Version 2.7.2 *(2018-12-06)*
----------------------------

 * New: Add support for androidx library.

Version 2.7.1 *(2017-10-29)*
----------------------------

 * New: Add support for `com.android.tools.build:gradle:3.+`.
 * New: Show a better error message if `dagger2` dependency is missing while using `mvp` module.

Version 2.7.0 *(2017-10-27)*
----------------------------

 * New: Add support for a `launchMode:singleTop` Activity into the `extra` module.

Version 2.6.4 *(2017-10-16)*
----------------------------

 * Fix: Superclass Presenter injection.
 * New: Add more unit tests.

Version 2.6.3 *(2017-04-16)*
----------------------------

 * Fix: Presenter state restoration.
 * New: Add more unit tests.

Version 2.6.2 *(2017-03-11)*
----------------------------

 * Fix: Processing of `@Inject` annotations for `mvp` module.

Version 2.6.1 *(2017-01-28)*
----------------------------

 * Fix: `mvp` generated Activity classes.

Version 2.6.0 *(2017-01-01)*
----------------------------

 * New: Add support for custom `Bundler` for `@Arg`, `@Extra` and `@State`.

Version 2.5.1 *(2016-12-26)*
----------------------------

 * Change: Configuration file is now required.

Version 2.5.0 *(2016-12-26)*
----------------------------

 * Change: Add support for android gradle plugin 2.2+ `annotationProcessor`. (android-apt is no longer added automatically).
 * New: Add support for **YAML** format of configuration file `blade.yaml`.

Version 2.4.0 *(2016-12-07)*
----------------------------

 * Change: Refactor **mvp** module to use Dagger2 and support Fragments.
 * Fix: `@State` inside android View subclasses.

Version 2.3.0 *(2016-10-28)*
----------------------------

 * New: Add more plugin tests
 * Fix: Refactor code for bytecode weaving.
 * Fix: Handle order of weaved code correctly.

Version 2.2.0 *(2016-05-21)*
----------------------------

 * New: Added module for Parcelable implementation - using `@Parcel` annotation.
 * Optimization: Support of generic and inner classes.

Version 2.1.0 *(2016-03-23)*
----------------------------

 * Optimization: Used google's **Transform API** for bytecode weaving.
 * Optimization: **APT** plugin is automatically applied.

Version 2.0.0 *(2016-03-07)*
----------------------------

 * New: Blade library divided into modules.
 * New: Added module for simple MVP implementation - using `@Presenter` annotation.
 * New: Added `@GeneratedFor` inside `blade.I` class for simpler navigation.

Version 1.2.0 *(2016-01-07)*
----------------------------

 * Fix: Fragment `@Arg` attributes injected in `onCreate(Bundle)`.
 * Fix: Generated `blade.F` and `blade.I` non-final.
 * New: `@Blade` for generating code for classes without Blade annotations.
 * New: Support for `@Extra` in `Service` and `IntentService` subclasses.


Version 1.1.0 *(2016-01-01)*
----------------------------

 * New: `@State` for State management.
 * Optimizations of generated Code.

Version 1.0.0 *(2015-12-06)*
----------------------------

 * Initial release.
