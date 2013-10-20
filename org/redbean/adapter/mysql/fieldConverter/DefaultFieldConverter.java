/*    */ package org.redbean.adapter.mysql.fieldConverter;
/*    */ 
/*    */ import java.lang.reflect.Field;
/*    */ import java.util.Date;
/*    */ import java.util.HashMap;
/*    */ import java.util.Map;
/*    */ import org.redbean.adapter.FieldConverter;
/*    */ 
/*    */ public class DefaultFieldConverter
/*    */   implements FieldConverter
/*    */ {
/* 12 */   private static final Map<Object, String> types = new HashMap();
/*    */ 
/*    */   static
/*    */   {
/* 16 */     types.put(Long.class, "BIGINT");
/* 17 */     types.put(Integer.class, "INT");
/* 18 */     types.put(Short.class, "SMALLINT");
/* 19 */     types.put(Double.class, "DOUBLE");
/* 20 */     types.put(Float.class, "FLOAT");
/* 21 */     types.put(Boolean.class, "BIT");
/* 22 */     types.put(Date.class, "DATETIME");
/*    */   }
/*    */ 
/*    */   public String convert(Field field, Object value)
/*    */   {
/* 28 */     return (String)types.get(field.getType());
/*    */   }
/*    */ }

/* Location:           C:\Users\leigh\Downloads\redbean4j-0.1.jar
 * Qualified Name:     org.redbean.adapter.mysql.fieldConverter.DefaultFieldConverter
 * JD-Core Version:    0.6.2
 */