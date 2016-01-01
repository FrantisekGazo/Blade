package eu.f3rog.afterburner.inserts;

import javassist.CtClass;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.NotFoundException;

/**
 * Deals with some aspect of code generation regarding a {@link CtMethod};
 * @author SNI
 */
public class CtMethodJavaWriter {
    /**
     * Returns the signature of a method like "public abstract foo(Object o) throws Exception, Throwable".
     * @param overridenMethod the method to generate the signature of.
     * @return the signature of overridenMethod like "public abstract foo(Object o) throws Exception, Throwable".
     * @throws NotFoundException if a type is not found (like parameter types).
     */
    public String createJavaSignature(CtMethod overridenMethod) throws NotFoundException {
        return extractModifier(overridenMethod) + " "
                + extractReturnType(overridenMethod) + " "
                + overridenMethod.getName() + "("
                + extractParametersAndTypes(overridenMethod) + ")"
                + extractThrowClause(overridenMethod);
    }

    /**
     * Invokes the super implemntation of a method like "super.foo(o)".
     * @param method the method to generate the super impl invocation of.
     * @return the super implemntation of a method like "super.foo(o)".
     * @throws NotFoundException if a type is not found (like parameter types).
     */
    public String invokeSuper(CtMethod method) throws NotFoundException {
        return "super."
                + method.getName() + "("
                + extractParameters(method) + ");";
    }

    private String extractThrowClause(CtMethod overridenMethod) throws NotFoundException {
        int indexException = 0;
        StringBuilder builder = new StringBuilder();
        for (CtClass paramType : overridenMethod.getExceptionTypes()) {
            builder.append(paramType.getName());
            if (indexException < overridenMethod.getExceptionTypes().length - 1) {
                builder.append(", ");
            }   
            indexException++;
        }
        if (builder.length() != 0) {
            builder.insert(0, " throws ");
        }
        return builder.toString();
    }

    private String extractParametersAndTypes(CtMethod overridenMethod) throws NotFoundException {
        StringBuilder builder = new StringBuilder();
        int indexParam = 0;
        for (CtClass paramType : overridenMethod.getParameterTypes()) {
            builder.append(paramType.getName());
            builder.append(" ");
            builder.append("p" + indexParam);
            if (indexParam < overridenMethod.getParameterTypes().length - 1) {
                builder.append(", ");
            }
            indexParam++;
        }
        return builder.toString();
    }

    private String extractParameters(CtMethod overridenMethod) throws NotFoundException {
        StringBuilder builder = new StringBuilder();
        for (int indexParam = 0; indexParam < overridenMethod.getParameterTypes().length; indexParam++) {
            builder.append("p" + indexParam);
            if (indexParam < overridenMethod.getParameterTypes().length - 1) {
                builder.append(", ");
            }
        }
        return builder.toString();
    }

    private String extractReturnType(CtMethod overridenMethod) throws NotFoundException {
        return overridenMethod.getReturnType().getName();
    }

    private String extractModifier(CtMethod overridenMethod) {
        return Modifier.toString(overridenMethod.getModifiers());
    }
}
