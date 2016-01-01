package eu.f3rog.afterburner;

import eu.f3rog.afterburner.exception.AfterBurnerImpossibleException;
import eu.f3rog.afterburner.inserts.InsertableConstructor;
import eu.f3rog.afterburner.inserts.SimpleInsertableConstructor;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;

/**
 * Almost a DSL/builder to ease creating an {@link InsertableConstructor}.
 * Needs more intermediate states.
 * @author SNI
 */
public class InsertableConstructorBuilder {

    private CtClass classToInsertInto;
    protected String body;
    private AfterBurner afterBurner;
    
    public InsertableConstructorBuilder(AfterBurner afterBurner) {
        this.afterBurner = afterBurner;
    }

    public StateTargetClassSet insertIntoClass(Class<?> clazzToInsertInto) throws NotFoundException {
        this.classToInsertInto = ClassPool.getDefault().get(clazzToInsertInto.getName());
        return new StateTargetClassSet();
    }

    public StateTargetClassSet insertIntoClass(CtClass clazzToInsertInto) {
        this.classToInsertInto = clazzToInsertInto;
        return new StateTargetClassSet();
    }

    protected void checkFields() throws AfterBurnerImpossibleException {
        if (classToInsertInto == null || body == null) {
            throw new AfterBurnerImpossibleException(
                    "Builder was not used as intended. A field is null.");
        }
    }
    
    //**********************************************
    //******* FLUENT DSL STATE CLASSES
    //**********************************************

    public class StateTargetClassSet {
        public StateComplete withBody(String body) {
            InsertableConstructorBuilder.this.body = body;
            return new StateComplete();
        }
    }

    public class StateComplete {

        public void doIt() throws CannotCompileException, AfterBurnerImpossibleException, NotFoundException {
            InsertableConstructor method = createInsertableConstructor();
            afterBurner.insertConstructor(method);
        }

        public InsertableConstructor createInsertableConstructor() throws AfterBurnerImpossibleException {
            checkFields();

            InsertableConstructor constructor = new SimpleInsertableConstructor(classToInsertInto, body, true);
            return constructor;
        }
    }
}
