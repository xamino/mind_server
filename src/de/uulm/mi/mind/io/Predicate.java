package de.uulm.mi.mind.io;

/**
 * Created by Cassio on 08.05.2014.
 */
abstract class Predicate<ExtentType> {
    private final Class<? extends ExtentType> extentType;

    public abstract boolean match(ExtentType o);

    public Predicate(Class<? extends ExtentType> extentType) {
        this.extentType = extentType;
    }

    public Class<? extends ExtentType> getClassType() {
        return extentType;
    }
}
