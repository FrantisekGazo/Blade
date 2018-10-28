package eu.f3rog.blade.weaving.interfaces.dagger;

import java.util.List;

import eu.f3rog.javassist.JavassistHelper;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;

/**
 * Changes classes generated by Dagger2 using bytecode weaving
 *
 * @author FrantisekGazo
 */
public final class DaggerMiddleMan {

    private static DaggerComponentsManager sComponents;

    public static void init(final ClassPool classPool, final List<CtClass> classes) {
        sComponents = new DaggerComponentsManager(classPool, classes);
    }

    public void weaveFor(final CtClass injectedClass,
                         final List<String> presenterFieldNames,
                         final String wrapMethod,
                         final JavassistHelper javassistHelper) {
        final ClassPool classPool = injectedClass.getClassPool();

        CtClass injectorClass;
        try {
            injectorClass = classPool.get(injectedClass.getName() + "_MembersInjector");
        } catch (NotFoundException e) {
            return;
        }

        try {
            //System.out.printf(" ~> %s\n", injectorClass.getName());

            MembersInjectorMethodEditor editor = new MembersInjectorMethodEditor(presenterFieldNames, wrapMethod);
            javassistHelper.editMethod(editor, injectorClass, "injectMembers", injectedClass);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // this is needed for dagger 2.12 and above because it skips the _MembersInjector and calls directly Module
        sComponents.weaveFor(injectedClass, presenterFieldNames, wrapMethod, javassistHelper);
    }
}
