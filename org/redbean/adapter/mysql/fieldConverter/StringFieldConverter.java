/*    */ package org.redbean.adapter.mysql.fieldConverter;
/*    */ 
/*    */ import java.lang.reflect.Field;
/*    */ import org.redbean.adapter.FieldConverter;
/*    */ 
/*    */ public class StringFieldConverter
/*    */   implements FieldConverter
/*    */ {
/*    */   public String convert(Field field, Object value)
/*    */   {
/* 13 */     String str = (String)value;
/*    */ 
/* 15 */     String typeString = "";
/*    */ 
/* 17 */     if (str.length() <= 255)
/*    */     {
/* 19 */       typeString = "VARCHAR(255)";
/*    */     }
/* 21 */     else if (str.length() < 65536)
/*    */     {
/* 23 */       typeString = "TEXT";
/*    */     }
/*    */     else
/*    */     {
/* 27 */       typeString = "LONGTEXT";
/*    */     }
/*    */ 
/* 30 */     return typeString;
/*    */   }
/*    */ }

/* Location:           C:\Users\leigh\Downloads\redbean4j-0.1.jar
 * Qualified Name:     org.redbean.adapter.mysql.fieldConverter.StringFieldConverter
 * JD-Core Version:    0.6.2
 */