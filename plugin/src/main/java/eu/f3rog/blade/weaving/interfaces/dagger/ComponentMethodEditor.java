package eu.f3rog.blade.weaving.interfaces.dagger;

import org.gradle.internal.impldep.aQute.bnd.build.Run;

import java.util.ArrayList;
import java.util.List;

import eu.f3rog.javassist.MethodEditor;
import javassist.CannotCompileException;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.expr.MethodCall;

final class ComponentMethodEditor
        extends MethodEditor {

    private static final String PM = "blade.mvp.PresenterManager.getInstance()";
    private static final String INJECT_METHOD_NAME_PREFIX = "inject";

    private final List<String> mPresenterFieldNames;
    private final List<String> mPresenterInjectMethodNames;
    private final String mWrapMethod;
    private final List<String> mAccessedMethodNames;
    private boolean mIsSearchPhase;
    private int mAccessedMethodIndex;

    ComponentMethodEditor(final List<String> presenterFieldNames, final String wrapMethod) {
        mPresenterFieldNames = presenterFieldNames;
        mPresenterInjectMethodNames = new ArrayList<>();
        for (final String presenterFieldName : mPresenterFieldNames) {
            final String methodName = INJECT_METHOD_NAME_PREFIX + presenterFieldName.substring(0, 1).toUpperCase() + presenterFieldName.substring(1);
            mPresenterInjectMethodNames.add(methodName);
        }
        mWrapMethod = wrapMethod;
        mAccessedMethodNames = new ArrayList<>();
    }

    @Override
    public void edit(final MethodCall m) throws CannotCompileException {
        super.edit(m);

        final String methodName = m.getMethodName();
        if (mIsSearchPhase) {
            mAccessedMethodNames.add(methodName);
        } else {
            if (mAccessedMethodIndex + 1 >= mAccessedMethodNames.size()) {
                return;
            }

            final String nextMethodName = mAccessedMethodNames.get(mAccessedMethodIndex + 1);
            final int presenterIndex = mPresenterInjectMethodNames.indexOf(nextMethodName);
            if (presenterIndex < 0) {
                return; // next method is not an inject method
            }

            System.out.printf(" replacing %s() ", m.getMethodName());

            final String presenterFieldName = mPresenterFieldNames.get(presenterIndex);
            final String presenterType;
            try {
                presenterType = m.getMethod().getReturnType().getName();
            } catch (NotFoundException e) {
                throw new RuntimeException(e);
            }
            final String statement = String.format("{ $_ = (%s) (%s.exists(%s, \"%s\") ? %s.getInitialized(%s, \"%s\") : %s.initialize(%s, \"%s\", %s())); }",
                    presenterType, PM, "instance", presenterFieldName, PM, "instance", presenterFieldName, PM, "instance", presenterFieldName, methodName);

            System.out.printf(" with %s\n", statement);

            m.replace(statement);

            mAccessedMethodIndex++;
        }
    }

    @Override
    public void instrument(final CtMethod method) throws CannotCompileException {
        mIsSearchPhase = true;
        method.instrument(this);

        mIsSearchPhase = false;
        mAccessedMethodIndex = 0;
        method.instrument(this);
    }
}
