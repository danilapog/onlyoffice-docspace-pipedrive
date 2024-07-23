package com.onlyoffice.docspacepipedrive.web.aop.pipedrive;

import com.onlyoffice.docspacepipedrive.web.aop.Execution;
import com.onlyoffice.docspacepipedrive.web.aop.docspace.DocspaceAction;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExecutePipedriveAction {
    PipedriveAction action();
    Execution execution() default Execution.BEFORE;
}
