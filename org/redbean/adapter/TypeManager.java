package org.redbean.adapter;

import java.lang.reflect.Field;

public abstract interface TypeManager
{
  public abstract String getSQLType(Field paramField, Object paramObject);
}

/* Location:           C:\Users\leigh\Downloads\redbean4j-0.1.jar
 * Qualified Name:     org.redbean.adapter.TypeManager
 * JD-Core Version:    0.6.2
 */