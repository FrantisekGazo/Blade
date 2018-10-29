package eu.f3rog.blade.compiler

import android.app.Activity
import android.app.Fragment
import eu.f3rog.blade.compiler.name.ClassNames
import eu.f3rog.blade.compiler.util.JavaFile

abstract class MockClass {

    public static final ANDROIDX_FRAGMENT = fragment(
            ClassNames.AndroidxFragment.packageName(),
            ClassNames.AndroidxFragment.simpleName()
    )
    public static final SUPPORT_FRAGMENT = fragment(
            ClassNames.SupportFragment.packageName(),
            ClassNames.SupportFragment.simpleName()
    )

    public static final ANDROIDX_ACTIVITY = activity(
            ClassNames.AndroidxActivity.packageName(),
            ClassNames.AndroidxActivity.simpleName()
    )
    public static final SUPPORT_ACTIVITY = activity(
            ClassNames.SupportActivity.packageName(),
            ClassNames.SupportActivity.simpleName()
    )

    public static final fragmentClasses = [
            ["Fragment", Fragment.class],
            ["Support Fragment", SUPPORT_FRAGMENT],
            ["AndroidX Fragment", ANDROIDX_FRAGMENT]
    ]

    public static final activityClasses = [
            ["Activity", Activity.class],
            ["Support Activity", SUPPORT_ACTIVITY],
            ["AndroidX Activity", ANDROIDX_ACTIVITY]
    ]

    private static def fragment(String packageName, String name) {
        return JavaFile.newFile(packageName, name, """
            public class #T {
                public final android.os.Bundle getArguments() {
                    throw new RuntimeException("Stub!");
                }
                
                public void setArguments(android.os.Bundle args) {
                    throw new RuntimeException("Stub!");
                }
            }
            """
        )
    }

    private static def activity(String packageName, String name) {
        return JavaFile.newFile(packageName, name, """
            public class #T {
            }
            """
        )
    }
}