/*    */ package org.redbean.adapter.mysql;
/*    */ 
/*    */ import java.lang.reflect.Field;
/*    */ import org.redbean.adapter.FieldConverter;
/*    */ import org.redbean.adapter.TypeManager;
/*    */ import org.redbean.adapter.mysql.fieldConverter.DefaultFieldConverter;
/*    */ import org.redbean.adapter.mysql.fieldConverter.EnumFieldConverter;
/*    */ import org.redbean.adapter.mysql.fieldConverter.StringFieldConverter;
/*    */ import org.redbean.annotations.Id;
/*    */ import org.redbean.annotations.NotNull;
/*    */ 
/*    */ public class MySQLTypeManager
/*    */   implements TypeManager
/*    */ {
/*    */   public String getSQLType(Field field, Object value)
/*    */   {
/* 20 */     FieldConverter conv = null;
/*    */ 
/* 22 */     if (field.getType().isEnum())
/*    */     {
/* 24 */       conv = new EnumFieldConverter();
/*    */     }
/* 26 */     else if (field.getType() == String.class)
/*    */     {
/* 28 */       conv = new StringFieldConverter();
/*    */     }
/*    */     else
/*    */     {
/* 32 */       conv = new DefaultFieldConverter();
/*    */     }
/*    */ 
/* 35 */     String sqlType = conv.convert(field, value);
/*    */ 
/* 37 */     if ((field.isAnnotationPresent(NotNull.class)) || (field.isAnnotationPresent(Id.class)))
/*    */     {
/* 39 */       sqlType = sqlType + " NOT NULL";
/*    */     }
/*    */ 
/* 42 */     if (sqlType == null)
/*    */     {
/* 44 */       throw new IllegalArgumentException("No mapping available for field: " + field.getName() + " of type: " + field.getType());
/*    */     }
/*    */ 
/* 47 */     return sqlType;
/*    */   }
/*    */ }

/* Location:           C:\Users\leigh\Downloads\redbean4j-0.1.jar
 * Qualified Name:     org.redbean.adapter.mysql.MySQLTypeManager
 * JD-Core Version:    0.6.2
 */