package org.example;
import java.lang.annotation.*;
@Documented
@Target({ElementType.FIELD})
@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface DBFieldTableName {
    String table_name();
    boolean ignore() default false;
}