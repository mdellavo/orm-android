package org.quuux.orm;

public class Func extends Literal {

    public Func(final String name, final Object... args) {
        super(String.format(name, args));
    }

    public static Func COUNT = new Func("COUNT(*)");
}
