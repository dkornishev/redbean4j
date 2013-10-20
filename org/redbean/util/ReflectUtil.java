/*    */ package org.redbean.util;
/*    */ 
/*    */ import java.lang.reflect.Field;
/*    */ import java.util.ArrayList;
/*    */ import java.util.Arrays;
/*    */ import java.util.List;
/*    */ 
/*    */ public class ReflectUtil
/*    */ {
/*    */   public static List<Field> getAllFields(Class<?> cl)
/*    */   {
/* 18 */     List fields = new ArrayList();
/*    */     do
/*    */     {
/* 22 */       fields.addAll(Arrays.asList(cl.getDeclaredFields()));
/* 23 */       cl = cl.getSuperclass();
/*    */     }
/* 20 */     while (!
/* 25 */       cl.equals(Object.class));
/*    */ 
/* 27 */     return fields;
/*    */   }
/*    */ }

/* Location:           C:\Users\leigh\Downloads\redbean4j-0.1.jar
 * Qualified Name:     org.redbean.util.ReflectUtil
 * JD-Core Version:    0.6.2
 */