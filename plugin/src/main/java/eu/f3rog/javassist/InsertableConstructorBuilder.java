package eu.f3rog.javassist;

import eu.f3rog.javassist.exception.AfterBurnerImpossibleException;
import eu.f3rog.javassist.inserts.InsertableConstructor;
import eu.f3rog.javassist.inserts.AllInsertableConstructor;
import eu.f3rog.javassist.inserts.OneInsertableConstructor;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;

/**
 * Almost a DSL/builder to ease creating an {@link InsertableConstructor}.
 * Needs more intermediate states.
 *
 * @author SNI and FrantisekGazo
 */
public class InsertableConstructorBuilder {

    private CtClass mClassToInsertInto;
    private CtClass[] mTargetConstructorParams;
    protected String mBody;

    public InsertableConstructorBuilder() {
    }

    public StateTargetClassSet insertIntoClass(Class<?> clazzToInsertInto) throws NotFoundException {
        this.mClassToInsertInto = ClassPool.getDefault().get(clazzToInsertInto.getName());
        return new StateTargetClassSet();
    }

    public StateTargetClassSet insertIntoClass(CtClass clazzToInsertInto) {
        this.mClassToInsertInto = clazzToInsertInto;
        return new StateTargetClassSet();
    }

    protected void checkFields() throws AfterBurnerImpossibleException {
        if (mClassToInsertInto == null || mBody == null) {
            throw new AfterBurnerImpossibleException("Builder was not used as intended. A field is null.");
        }
    }

    //**********************************************
    //******* FLUENT DSL STATE CLASSES
    //**********************************************

    public final class StateTargetClassSet {

        public StateTargetConstructorSet intoConstructor(CtClass... targetConstructorParams) {
            InsertableConstructorBuilder.this.mTargetConstructorParams = targetConstructorParams;
            return new StateTargetConstructorSet();
        }

        public StateComplete withBody(String body) {
            InsertableConstructorBuilder.this.mBody = body;
            return new StateComplete();
        }
    }

    public final class StateTargetConstructorSet {

        public StateComplete withBody(String body) {
            InsertableConstructorBuilder.this.mBody = body;
            return new StateComplete();
        }
    }

    public final class StateComplete {

        public InsertableConstructor createInsertableConstructor() throws AfterBurnerImpossibleException {
            checkFields();

            if (mTargetConstructorParams != null) {
                return new OneInsertableConstructor(mBody, mClassToInsertInto, mTargetConstructorParams);
            } else {
                return new AllInsertableConstructor(mBody, mClassToInsertInto);
            }
        }
    }
}
