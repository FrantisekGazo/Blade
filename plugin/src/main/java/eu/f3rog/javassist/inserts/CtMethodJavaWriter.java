package eu.f3rog.javassist.inserts;

import javassist.CtClass;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.NotFoundException;

/**
 * Deals with some aspect of code generation regarding a {@link CtMethod};
 *
 * @author SNI and FrantisekGazo
 */
public final class CtMethodJavaWriter {

    /**
     * Returns the signature of a method like "public abstract foo(Object o) throws Exception, Throwable".
     *
     * @param overriddenMethod the method to generate the signature of.
     * @return the signature of overridenMethod like "public abstract foo(Object o) throws Exception, Throwable".
     * @throws NotFoundException if a type is not found (like parameter types).
     */
    public String createJavaSignature(CtMethod overriddenMethod) throws NotFoundException {
        return extractModifier(overriddenMethod) + " "
                + extractReturnType(overriddenMethod) + " "
                + overriddenMethod.getName() + "("
                + extractParametersAndTypes(overriddenMethod) + ")"
                + extractThrowClause(overriddenMethod);
    }

    /**
     * Invokes the super implemntation of a method like "super.foo(o);".
     *
     * @param method the method to generate the super impl invocation of.
     * @return the super implemntation of a method like "super.foo(o)".
     * @throws NotFoundException if a type is not found (like parameter types).
     */
    public String createSuperCall(CtMethod method) throws NotFoundException {
        return "super." + method.getName() + "(" + extractParameters(method) + ");";
    }

    private String extractThrowClause(CtMethod overriddenMethod) throws NotFoundException {
        StringBuilder builder = new StringBuilder();

        CtClass[] exceptionTypes = overriddenMethod.getExceptionTypes();
        for (int i = 0, count = exceptionTypes.length; i < count; i++) {
            CtClass exceptionType = exceptionTypes[i];

            if (i > 0) {
                builder.append(", ");
            }
            builder.append(exceptionType.getName());
        }

        if (builder.length() > 0) {
            builder.insert(0, " throws ");
        }

        return builder.toString();
    }

    private String extractParametersAndTypes(CtMethod overriddenMethod) throws NotFoundException {
        StringBuilder builder = new StringBuilder();

        CtClass[] paramTypes = overriddenMethod.getParameterTypes();
        for (int i = 0, count = paramTypes.length; i < count; i++) {
            CtClass paramType = paramTypes[i];

            if (i > 0) {
                builder.append(", ");
            }
            builder.append(paramType.getName()).append(" ").append(getParamName(i));
        }

        return builder.toString();
    }

    private String extractParameters(CtMethod overriddenMethod) throws NotFoundException {
        StringBuilder builder = new StringBuilder();

        CtClass[] paramTypes = overriddenMethod.getParameterTypes();
        for (int i = 0, count = paramTypes.length; i < count; i++) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append(getParamName(i));
        }

        return builder.toString();
    }

    private String getParamName(final int index) {
        return "p" + index;
    }

    private String extractReturnType(CtMethod overriddenMethod) throws NotFoundException {
        return overriddenMethod.getReturnType().getName();
    }

    private String extractModifier(CtMethod overriddenMethod) {
        return Modifier.toString(overriddenMethod.getModifiers());
    }
}
