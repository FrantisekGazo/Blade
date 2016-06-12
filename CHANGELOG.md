Change Log
==========

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
