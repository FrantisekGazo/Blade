# Bl@De
Android library for boilerplate destruction - "Just code what is worth coding"

* Generates boilerplate code based on used annotations and lets you focus on what matters.
* Generated code is fully traceable.
* Everything is generated during compile time.
* No reflection used!

# Usage
Available annotations:
* [@Arg](https://github.com/FrantisekGazo/Blade#arg)
* [@Extra](https://github.com/FrantisekGazo/Blade#extra)

## @Arg
Annotation for generating `newInstance()` methods for your [Fragment](http://developer.android.com/reference/android/app/Fragment.html) classes.

Without using this library you would have to write this:

```Java
public class MyFragment extends Fragment {

    private static final String EXTRA_TEXT = "arg_text";
    private static final String EXTRA_DATA = "arg_data";

    public static MyFragment newInstance(String text, MyData data) {
        MyFragment frag = new MyFragment();
        Bundle args = new Bundle();
        args.putString(EXTRA_TEXT, text);
        args.putParcelable(EXTRA_DATA, data);
        frag.setArguments(args);
        return frag;
    }

    private String mText;
    private MyData mData;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    
        mText = getArguments().getString(EXTRA_TEXT);
        mData = (MyData) getArguments().getParcelable(EXTRA_DATA);
    }
}
```

But with this library you can write this:

```Java
public class MyFragment extends Fragment {

    @Arg 
    String mText;
    @Arg 
    MyData mData;
  
}
```

Class named `F` (= Fragment) is automatically generated for you. 
This class contains 1 method for each `Fragment`class with annotated arguments:
* ` X newX(Context c, T1 arg1[, T2 arg2, ...]) ` - Creates new instance of class `X`.


e.g. for `MyFragment` class it contains method named `newMyFragment` with 2 parameters: `String` and `MyData`. 
So you can easily create new fragment by calling:
```Java
F.newMyFragment("some-string", new MyData());
```
And given values will be set to coresponding attributes annotated with `@Arg`.

## @Extra
Annotation for generating `newIntent()` methods for your [Activity](http://developer.android.com/reference/android/app/Activity.html) classes.

Without using this library you would have to write this:

```Java
public class MyActivity extends Activity {

    private static final String EXTRA_TEXT = "extra_text";
    private static final String EXTRA_DATA = "arg_data";

    public static Intent newIntent(Contaxt context, String text, MyData data) {
        Intent intent = new Intent(context, MyActivity.class);
        intent.putExtra(EXTRA_TEXT, text);
        intent.putExtra(EXTRA_DATA, data);
        return intent;
    }

    private String mText;
    private MyData mData;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    
        Bundle extras = getIntent().getExtras();
        mText = extras.getString(EXTRA_TEXT);
        mData = (MyData) extras.getParcelable(EXTRA_DATA);
    }
}
```

But with this library you can write this:

```Java
public class MyActivity extends Activity {

    @Extra 
    String mText;
    @Extra
    MyData mData;
  
}
```

Class named `I` (= Intent) is automatically generated for you. 
This class contains 2 methods for each `Activity` class with annotated arguments:
* ` Intent forX(Context c, T1 extra1[, T2 extra2, ...]) ` - Creates new `Intent` which can be used for starting new activity. This lets you add additional flags to this intent.
* ` void startX(Context c, T1 extra1[, T2 extra2, ...]) ` - Creates new `Intent` and starts new activity. 

e.g. for `MyActivity` class it contains methods named `forMyActivity` and `startMyActivity` with 2 parameters: `String` and `MyData`. 
So you can easily start new Activity by calling:
```Java
I.startMyActivity("some-string", new MyData());
```
And given values will be set to coresponding attributes annotated with `@Extra`.

# Download

Gradle plugin:
```Gradle
buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:1.3.0'
        // Add Blade plugin
        classpath 'eu.f3rog.blade:plugin:1.0.0'
    }
}

apply plugin: 'com.android.application'
// Apply Blade plugin
apply plugin: 'blade'
```

This library uses Annotation Processor so you have to apply also this Gradle plugin, if not already used:
```Gradle
classpath 'com.neenbedankt.gradle.plugins:android-apt:1.4'
apply plugin: 'com.neenbedankt.android-apt'
```
