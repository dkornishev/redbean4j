/*    */ package org.redbean.exception;
/*    */ 
/*    */ public class EntityException extends Exception
/*    */ {
/*    */   private static final long serialVersionUID = 1L;
/*    */ 
/*    */   public EntityException()
/*    */   {
/*    */   }
/*    */ 
/*    */   public EntityException(String message, Throwable cause)
/*    */   {
/* 15 */     super(message, cause);
/*    */   }
/*    */ 
/*    */   public EntityException(String message)
/*    */   {
/* 20 */     super(message);
/*    */   }
/*    */ 
/*    */   public EntityException(Throwable cause)
/*    */   {
/* 25 */     super(cause);
/*    */   }
/*    */ }

/* Location:           C:\Users\leigh\Downloads\redbean4j-0.1.jar
 * Qualified Name:     org.redbean.exception.EntityException
 * JD-Core Version:    0.6.2
 */