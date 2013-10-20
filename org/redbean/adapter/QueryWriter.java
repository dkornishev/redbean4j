package org.redbean.adapter;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.List;

public abstract interface QueryWriter
{
  public abstract String addColumn(String paramString, Field paramField, Object paramObject);

  public abstract String accomodateField(String paramString1, Field paramField, Object paramObject, String paramString2);

  public abstract String batchLoad(String paramString1, String paramString2, int paramInt);

  public abstract String createTable(String paramString, Field paramField, Object paramObject)
    throws SQLException;

  public abstract String currentSchema();

  public abstract String delete(String paramString1, String paramString2, Object paramObject);

  public abstract String dropColumn(String paramString1, String paramString2);

  public abstract String dropPrimaryKey(String paramString);

  public abstract String exists(String paramString1, String paramString2);

  public abstract String index(String paramString1, String paramString2, String paramString3);

  public abstract String insert(String paramString, List<String> paramList);

  public abstract String nextId(String paramString1, String paramString2);

  public abstract String primaryKey(String paramString1, String paramString2);

  public abstract String retrieve(String paramString1, String paramString2, Object paramObject);

  public abstract String update(String paramString, List<String> paramList, Field paramField, Object paramObject);

  public abstract String unique(String paramString1, String paramString2, String paramString3);
}

/* Location:           C:\Users\leigh\Downloads\redbean4j-0.1.jar
 * Qualified Name:     org.redbean.adapter.QueryWriter
 * JD-Core Version:    0.6.2
 */