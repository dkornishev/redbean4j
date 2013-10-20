package org.redbean.annotations;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Id
{
  public abstract boolean generated();
}

/* Location:           C:\Users\leigh\Downloads\redbean4j-0.1.jar
 * Qualified Name:     org.redbean.annotations.Id
 * JD-Core Version:    0.6.2
 */