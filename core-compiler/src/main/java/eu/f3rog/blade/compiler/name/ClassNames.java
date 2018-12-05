package eu.f3rog.blade.compiler.name;

import com.squareup.javapoet.ClassName;

/**
 * Class {@link ClassNames}.
 *
 * @author Frantisek Gazo
 */
public enum ClassNames {

   Context("android.content", "Context"),
   AndroidActivity("android.app", "Activity"),
   Bundle("android.os", "Bundle"),
   Parcel("android.os", "Parcel"),
   Parcelable("android.os", "Parcelable"),
   Intent("android.content", "Intent"),
   IntentService("android.app", "IntentService"),
   Service("android.app", "Service"),
   SparseArray("android.util", "SparseArray"),
   SparseBooleanArray("android.util", "SparseBooleanArray"),
   View("android.view", "View"),
   SupportActivity("android.support.v7.app", "AppCompatActivity"),
   AndroidxActivity("androidx.appcompat.app", "AppCompatActivity"),
   SupportFragment("android.support.v4.app", "Fragment"),
   AndroidxFragment("androidx.fragment.app", "Fragment");

   private final String packageName;
   private final String className;
   private ClassName cn = null;

   ClassNames(String packageName, String name) {
      this.packageName = packageName;
      this.className = name;
   }

   public ClassName get() {
      if (cn == null) {
         cn = ClassName.get(packageName, className);
      }
      return cn;
   }

   public String getPackageName() {
      return packageName;
   }

   public String getClassName() {
      return className;
   }
}
