package admin.http.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Http Patch
 *
 * @author admin
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@HttpMapping
public @interface PatchMapping {
  String value() default "";
}
