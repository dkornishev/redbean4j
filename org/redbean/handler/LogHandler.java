/*    */ package org.redbean.handler;
/*    */ 
/*    */ import java.io.PrintStream;
/*    */ import java.util.ArrayList;
/*    */ import java.util.List;
/*    */ 
/*    */ public class LogHandler
/*    */ {
/*  9 */   private List<PrintStream> loggers = new ArrayList();
/*    */ 
/*    */   public void addLogger(PrintStream stream)
/*    */   {
/* 18 */     this.loggers.add(stream);
/*    */   }
/*    */ 
/*    */   public void removeLogger(PrintStream stream)
/*    */   {
/* 27 */     this.loggers.remove(stream);
/*    */   }
/*    */ 
/*    */   public void clearLoggers()
/*    */   {
/* 35 */     this.loggers.clear();
/*    */   }
/*    */ 
/*    */   public void write(String msg)
/*    */   {
/* 45 */     for (PrintStream st : this.loggers)
/*    */     {
/* 47 */       st.println(msg);
/*    */     }
/*    */   }
/*    */ 
/*    */   public void error(String msg, Exception x)
/*    */   {
/* 58 */     for (PrintStream st : this.loggers)
/*    */     {
/* 60 */       st.println(msg);
/* 61 */       x.printStackTrace(st);
/*    */     }
/*    */   }
/*    */ }

/* Location:           C:\Users\leigh\Downloads\redbean4j-0.1.jar
 * Qualified Name:     org.redbean.handler.LogHandler
 * JD-Core Version:    0.6.2
 */