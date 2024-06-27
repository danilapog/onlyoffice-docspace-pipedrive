package com.onlyoffice.docspacepipedrive.web.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface JoinToSharedGroup {
    Execution execution() default Execution.BEFORE;
}
