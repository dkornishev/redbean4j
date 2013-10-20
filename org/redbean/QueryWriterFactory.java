/*    */ package org.redbean;
/*    */ 
/*    */ import java.util.HashMap;
/*    */ import java.util.Map;
/*    */ import org.redbean.adapter.QueryWriter;
/*    */ import org.redbean.adapter.mysql.MySQLQueryWriter;
/*    */ 
/*    */ public class QueryWriterFactory
/*    */ {
/* 12 */   private static Map<String, Class<? extends QueryWriter>> registry = new HashMap();
/*    */ 
/*    */   static
/*    */   {
/* 16 */     register("MySQL", MySQLQueryWriter.class);
/*    */   }
/*    */ 
/*    */   public static QueryWriter getQueryWriter(String dbType)
/*    */   {
/* 28 */     Class queryWriterCl = (Class)registry.get(dbType);
/*    */ 
/* 30 */     QueryWriter qw = null;
/*    */ 
/* 32 */     if (queryWriterCl != null)
/*    */     {
/*    */       try
/*    */       {
/* 36 */         qw = (QueryWriter)queryWriterCl.newInstance();
/*    */       }
/*    */       catch (InstantiationException e)
/*    */       {
/* 40 */         e.printStackTrace();
/*    */       }
/*    */       catch (IllegalAccessException e)
/*    */       {
/* 44 */         e.printStackTrace();
/*    */       }
/*    */     }
/*    */ 
/* 48 */     return qw;
/*    */   }
/*    */ 
/*    */   public static void register(String dbType, Class<? extends QueryWriter> cl)
/*    */   {
/* 58 */     registry.put(dbType, cl);
/*    */   }
/*    */ }

/* Location:           C:\Users\leigh\Downloads\redbean4j-0.1.jar
 * Qualified Name:     org.redbean.QueryWriterFactory
 * JD-Core Version:    0.6.2
 */