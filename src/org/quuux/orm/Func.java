package org.quuux.orm;

// yuck  - how do AND OR NOT work
public class Func extends Literal {

    public Func(final String name, final Object... args) {
        super(String.format(name, args));
    }

    public static Func COUNT = new Func("COUNT(*)");
    public static Func RANDOM = new Func("RANDOM()");

}
