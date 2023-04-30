package org.example;

import java.lang.annotation.*;

@Documented
@Target(ElementType.FIELD)
@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface DBField {
    String column_name();

    Class<?> type();

    boolean isPrimaryKey() default false;

    boolean ignore() default false;
}