/*    */ package org.redbean.util;
/*    */ 
/*    */ import java.util.Arrays;
/*    */ import java.util.Iterator;
/*    */ import java.util.List;
/*    */ 
/*    */ public class StringUtil
/*    */ {
/*    */   public static String sqlString(String sql, List<?> values)
/*    */   {
/* 10 */     for (Iterator localIterator = values.iterator(); localIterator.hasNext(); ) { Object value = localIterator.next();
/*    */ 
/* 12 */       sql = sql.replaceFirst("\\?", value == null ? "NULL" : value.toString());
/*    */     }
/*    */ 
/* 15 */     return sql;
/*    */   }
/*    */ 
/*    */   public static String sqlString(String sql, Object[] values)
/*    */   {
/* 20 */     return sqlString(sql, Arrays.asList(values));
/*    */   }
/*    */ }

/* Location:           C:\Users\leigh\Downloads\redbean4j-0.1.jar
 * Qualified Name:     org.redbean.util.StringUtil
 * JD-Core Version:    0.6.2
 */