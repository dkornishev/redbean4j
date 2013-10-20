/*    */ package org.redbean.adapter.mysql.fieldConverter;
/*    */ 
/*    */ import java.lang.reflect.Field;
/*    */ import org.redbean.adapter.FieldConverter;
/*    */ 
/*    */ public class EnumFieldConverter
/*    */   implements FieldConverter
/*    */ {
/*    */   public String convert(Field field, Object value)
/*    */   {
/* 25 */     FieldConverter fc = new StringFieldConverter();
/*    */ 
/* 27 */     return fc.convert(field, value.toString());
/*    */   }
/*    */ }

/* Location:           C:\Users\leigh\Downloads\redbean4j-0.1.jar
 * Qualified Name:     org.redbean.adapter.mysql.fieldConverter.EnumFieldConverter
 * JD-Core Version:    0.6.2
 */