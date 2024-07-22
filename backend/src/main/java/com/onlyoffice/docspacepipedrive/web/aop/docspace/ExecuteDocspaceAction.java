package com.onlyoffice.docspacepipedrive.web.aop.docspace;

import com.onlyoffice.docspacepipedrive.web.aop.Execution;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExecuteDocspaceAction {
    DocspaceAction action();
    Execution execution() default Execution.BEFORE;
}
