package com.voler.cutlass.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 三尺春光驱我寒，一生戎马为长安
 * Created by Voler on 17/8/5.
 */


@Target(ElementType.FIELD)
@Retention(RetentionPolicy.CLASS)
public @interface InjectField {
    String value() default "";
}
