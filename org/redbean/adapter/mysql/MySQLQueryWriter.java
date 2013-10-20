/*     */ package org.redbean.adapter.mysql;
/*     */ 
/*     */ import java.lang.reflect.Field;
/*     */ import java.sql.SQLException;
/*     */ import java.util.List;
/*     */ import org.redbean.adapter.QueryWriter;
/*     */ import org.redbean.adapter.TypeManager;
/*     */ 
/*     */ public class MySQLQueryWriter
/*     */   implements QueryWriter
/*     */ {
/*  12 */   private TypeManager typeManager = new MySQLTypeManager();
/*     */ 
/*     */   public String addColumn(String tableName, Field field, Object value)
/*     */   {
/*  17 */     return "ALTER TABLE " + quote(tableName) + " ADD COLUMN " + quote(field.getName()) + " " + this.typeManager.getSQLType(field, value);
/*     */   }
/*     */ 
/*     */   public String accomodateField(String tableName, Field field, Object value, String oldType)
/*     */   {
/*  23 */     String ddl = "";
/*     */ 
/*  26 */     String newType = this.typeManager.getSQLType(field, value);
/*     */ 
/*  29 */     if ((!newType.equalsIgnoreCase(oldType)) && (!isWider(oldType, newType)))
/*     */     {
/*  31 */       ddl = "ALTER IGNORE TABLE " + quote(tableName) + " MODIFY " + quote(field.getName()) + " " + this.typeManager.getSQLType(field, value);
/*     */     }
/*     */ 
/*  34 */     return ddl;
/*     */   }
/*     */ 
/*     */   public String batchLoad(String tableName, String primaryKeyName, int count)
/*     */   {
/*  40 */     String sql = "SELECT * FROM " + quote(tableName) + " where " + quote(primaryKeyName) + " IN (";
/*     */ 
/*  42 */     for (int i = count; i > 0; i--)
/*     */     {
/*  44 */       sql = sql + "?,";
/*     */     }
/*     */ 
/*  47 */     sql = sql.replaceAll(",$", ")");
/*     */ 
/*  49 */     return sql;
/*     */   }
/*     */ 
/*     */   public String createTable(String tableName, Field primaryKey, Object primaryKeyValue)
/*     */     throws SQLException
/*     */   {
/*  55 */     String keyName = quote(primaryKey.getName());
/*  56 */     String keyType = this.typeManager.getSQLType(primaryKey, primaryKeyValue);
/*  57 */     String keyOptions = "";
/*     */ 
/*  59 */     String sql = "CREATE TABLE " + quote(tableName) + "(@id @type @options) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci ";
/*  60 */     sql = sql.replaceAll("@id", keyName).replaceAll("@type", keyType).replaceAll("@options", keyOptions);
/*     */ 
/*  62 */     return sql;
/*     */   }
/*     */ 
/*     */   public String delete(String tableName, String primaryKeyName, Object primaryKeyValue)
/*     */   {
/*  68 */     return "DELETE FROM " + quote(tableName) + " WHERE " + quote(primaryKeyName) + "=?";
/*     */   }
/*     */ 
/*     */   public String dropColumn(String tableName, String column)
/*     */   {
/*  74 */     return "ALTER TABLE " + quote(tableName) + " DROP COLUMN " + quote(column);
/*     */   }
/*     */ 
/*     */   public String currentSchema()
/*     */   {
/*  80 */     return "SELECT SCHEMA()";
/*     */   }
/*     */ 
/*     */   public String primaryKey(String tableName, String columnName)
/*     */   {
/*  86 */     return "ALTER IGNORE TABLE " + quote(tableName) + " ADD PRIMARY KEY(" + quote(columnName) + ")";
/*     */   }
/*     */ 
/*     */   public String dropPrimaryKey(String tableName)
/*     */   {
/*  92 */     return "ALTER TABLE " + quote(tableName) + " DROP PRIMARY KEY";
/*     */   }
/*     */ 
/*     */   public String update(String tableName, List<String> columns, Field primaryKey, Object primaryKeyValue)
/*     */   {
/*  98 */     String sql = "UPDATE " + quote(tableName) + " SET ";
/*     */ 
/* 100 */     for (String column : columns)
/*     */     {
/* 102 */       sql = sql + quote(column) + "=?,";
/*     */     }
/*     */ 
/* 105 */     sql = sql.replaceAll(",$", "");
/*     */ 
/* 107 */     sql = sql + " WHERE " + quote(primaryKey.getName()) + "=?";
/*     */ 
/* 109 */     return sql;
/*     */   }
/*     */ 
/*     */   public String insert(String tableName, List<String> columns)
/*     */   {
/* 115 */     String sql = "INSERT INTO " + quote(tableName) + " (@columns) VALUES(@values)";
/*     */ 
/* 117 */     String columnStr = "";
/* 118 */     String valueStr = "";
/*     */ 
/* 120 */     for (String column : columns)
/*     */     {
/* 122 */       columnStr = columnStr + quote(column) + ",";
/* 123 */       valueStr = valueStr + "?,";
/*     */     }
/*     */ 
/* 127 */     columnStr = columnStr.replaceAll(",$", "");
/* 128 */     valueStr = valueStr.replaceAll(",$", "");
/*     */ 
/* 130 */     return sql.replaceAll("@columns", columnStr).replaceAll("@values", valueStr);
/*     */   }
/*     */ 
/*     */   public String retrieve(String tableName, String primaryKeyName, Object primaryKeyValue)
/*     */   {
/* 136 */     return "SELECT * FROM " + quote(tableName) + " WHERE " + quote(primaryKeyName) + "=?";
/*     */   }
/*     */ 
/*     */   public String exists(String tableName, String primaryKeyName)
/*     */   {
/* 142 */     return "SELECT count(" + quote(primaryKeyName) + ") FROM " + quote(tableName) + " where " + quote(primaryKeyName) + "=?";
/*     */   }
/*     */ 
/*     */   public String nextId(String tableName, String primaryKeyName)
/*     */   {
/* 148 */     return "SELECT MAX(" + quote(primaryKeyName) + ")+1 FROM " + quote(tableName);
/*     */   }
/*     */ 
/*     */   private boolean isWider(String oldType, String newType)
/*     */   {
/* 159 */     boolean verdict = false;
/*     */ 
/* 161 */     if ((isStringType(newType)) && (isStringType(oldType)))
/*     */     {
/* 163 */       verdict = getStringTypeWidth(oldType) > getStringTypeWidth(newType);
/*     */     }
/*     */ 
/* 166 */     return verdict;
/*     */   }
/*     */ 
/*     */   private boolean isStringType(String type)
/*     */   {
/* 171 */     boolean verdict = false;
/*     */ 
/* 173 */     if ((type.startsWith("VARCHAR")) || (type.startsWith("TEXT")) || (type.startsWith("LONGTEXT")))
/*     */     {
/* 175 */       verdict = true;
/*     */     }
/*     */ 
/* 178 */     return verdict;
/*     */   }
/*     */ 
/*     */   private int getStringTypeWidth(String type)
/*     */   {
/* 183 */     int rank = 0;
/* 184 */     if (type.startsWith("VARCHAR"))
/*     */     {
/* 186 */       rank = 1;
/*     */     }
/* 188 */     else if (type.startsWith("TEXT"))
/*     */     {
/* 190 */       rank = 2;
/*     */     }
/* 192 */     else if (type.startsWith("LONGTEXT"))
/*     */     {
/* 194 */       rank = 3;
/*     */     }
/*     */ 
/* 197 */     return rank;
/*     */   }
/*     */ 
/*     */   private String quote(String name)
/*     */   {
/* 208 */     return "`" + name + "`";
/*     */   }
/*     */ 
/*     */   public String index(String tableName, String columnName, String indexName)
/*     */   {
/* 215 */     return "ALTER TABLE " + quote(tableName) + " ADD INDEX " + quote(indexName) + " (" + quote(columnName) + ")";
/*     */   }
/*     */ 
/*     */   public String unique(String tableName, String columnName, String indexName)
/*     */   {
/* 222 */     return "ALTER TABLE " + quote(tableName) + " ADD UNIQUE INDEX " + quote(indexName) + " (" + quote(columnName) + ")";
/*     */   }
/*     */ }

/* Location:           C:\Users\leigh\Downloads\redbean4j-0.1.jar
 * Qualified Name:     org.redbean.adapter.mysql.MySQLQueryWriter
 * JD-Core Version:    0.6.2
 */