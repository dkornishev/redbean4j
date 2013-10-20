package org.redbean.adapter;

import java.lang.reflect.Field;

public abstract interface FieldConverter
{
  public abstract String convert(Field paramField, Object paramObject);
}

/* Location:           C:\Users\leigh\Downloads\redbean4j-0.1.jar
 * Qualified Name:     org.redbean.adapter.FieldConverter
 * JD-Core Version:    0.6.2
 */