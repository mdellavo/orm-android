package org.quuux.orm;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Column {
    String name() default "";
    boolean primaryKey() default false;
    boolean nullable() default true;
    boolean unique() default false;
    String check() default "";
    String defaultValue() default "";
    Class<? extends Entity> foreignKey() default Entity.class;
}
