package eu.f3rog.blade.weaving.interfaces.dagger;

import java.util.List;

import eu.f3rog.javassist.MethodEditor;
import javassist.CannotCompileException;
import javassist.CtMethod;
import javassist.expr.FieldAccess;
import javassist.expr.MethodCall;

final class MembersInjectorMethodEditor
        extends MethodEditor {

    private static final String PROVIDER_FIELD_NAME_SUFFIX = "Provider";
    private static final String PROVIDER_GET_METHOD_NAME = "get";

    private final List<String> mPresenterFieldNames;
    private final String mWrapMethod;
    private String mLastAccessedProviderFieldName;

    MembersInjectorMethodEditor(List<String> presenterFieldNames, String wrapMethod) {
        mPresenterFieldNames = presenterFieldNames;
        mWrapMethod = wrapMethod;
        mLastAccessedProviderFieldName = null;
    }

    @Override
    public void edit(final FieldAccess f) throws CannotCompileException {
        super.edit(f);

        final String fieldName = f.getFieldName();
        if (fieldName.endsWith(PROVIDER_FIELD_NAME_SUFFIX)) {
            //System.out.printf("field access '%s' ...\n", fieldName);
            mLastAccessedProviderFieldName = fieldName;
        }
    }

    @Override
    public void edit(final MethodCall m) throws CannotCompileException {
        super.edit(m);

        if (!m.getMethodName().equals(PROVIDER_GET_METHOD_NAME)) {
            return;
        }

        //System.out.printf("replacing %s ", m.getMethodName());

        final String providerFieldName = mLastAccessedProviderFieldName;
        final String instanceFieldName = providerFieldName.substring(0, providerFieldName.length() - PROVIDER_FIELD_NAME_SUFFIX.length());

        if (!mPresenterFieldNames.contains(instanceFieldName)) {
            return;
        }

        final String statement = String.format("{ $_ = %s(%s, \"%s\", %s); }",
                mWrapMethod, "instance", instanceFieldName, providerFieldName);

        //System.out.printf("with %s\n", statement);

        m.replace(statement);
    }

    @Override
    public void instrument(final CtMethod method) throws CannotCompileException {
        mLastAccessedProviderFieldName = null;
        method.instrument(this);
    }
}
