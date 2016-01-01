package eu.f3rog.afterburner.inserts;

import javassist.CtClass;
import lombok.Getter;

public final class SimpleInsertableMethod extends InsertableMethod {

    @Getter
    private String fullMethod;
    @Getter
    private String body;
    @Getter
    private String targetMethodName;
    @Getter
    private CtClass[] targetMethodParams;
    @Getter
    private String insertionBeforeMethod;
    @Getter
    private String insertionAfterMethod;

    public SimpleInsertableMethod(CtClass classToInsertInto,
                                  String targetMethodName, CtClass[] targetMethodParams,
                                  String insertionBeforeMethod,
                                  String insertionAfterMethod, String body, String fullMethod) {
        super(classToInsertInto);
        this.targetMethodName = targetMethodName;
        this.targetMethodParams = targetMethodParams;
        this.insertionBeforeMethod = insertionBeforeMethod;
        this.insertionAfterMethod = insertionAfterMethod;
        this.body = body;
        this.fullMethod = fullMethod;
    }


}
