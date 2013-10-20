/*     */ package org.redbean;
/*     */ 
/*     */ import java.lang.reflect.Field;
/*     */ import java.lang.reflect.Modifier;
/*     */ import java.sql.Connection;
/*     */ import java.sql.DatabaseMetaData;
/*     */ import java.sql.PreparedStatement;
/*     */ import java.sql.ResultSet;
/*     */ import java.sql.SQLException;
/*     */ import java.sql.Statement;
/*     */ import java.util.ArrayList;
/*     */ import java.util.HashMap;
/*     */ import java.util.Iterator;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import org.redbean.adapter.QueryWriter;
/*     */ import org.redbean.annotations.Id;
/*     */ import org.redbean.annotations.Index;
/*     */ import org.redbean.annotations.Table;
/*     */ import org.redbean.annotations.Unique;
/*     */ import org.redbean.exception.EntityException;
/*     */ import org.redbean.handler.LogHandler;
/*     */ import org.redbean.util.ReflectUtil;
/*     */ import org.redbean.util.StringUtil;
/*     */ 
/*     */ public class EntityManager
/*     */ {
/*     */   private Connection conn;
/*     */   private String catalog;
/*     */   private String schema;
/*     */   private boolean frozen;
/*     */   private QueryWriter queryWriter;
/*     */   private LogHandler log;
/*     */ 
/*     */   public EntityManager(Connection conn, boolean frozen)
/*     */     throws SQLException
/*     */   {
/*  55 */     this.conn = conn;
/*  56 */     this.frozen = frozen;
/*     */ 
/*  59 */     this.queryWriter = QueryWriterFactory.getQueryWriter(conn.getMetaData().getDatabaseProductName());
/*     */ 
/*  61 */     if (this.queryWriter == null)
/*     */     {
/*  63 */       throw new IllegalArgumentException("No adapter registered for " + conn.getMetaData().getDatabaseProductName());
/*     */     }
/*     */ 
/*  67 */     this.catalog = conn.getCatalog();
/*     */ 
/*  70 */     ResultSet rs = conn.createStatement().executeQuery(this.queryWriter.currentSchema());
/*  71 */     rs.next();
/*  72 */     this.schema = rs.getString(1);
/*     */ 
/*  75 */     this.log = new LogHandler();
/*  76 */     this.log.addLogger(System.out);
/*     */   }
/*     */ 
/*     */   public <T> void store(T data)
/*     */     throws EntityException
/*     */   {
/*  82 */     validate(data.getClass());
/*     */ 
/*  84 */     if (this.frozen)
/*     */     {
/*  86 */       storeFrozen(data);
/*     */     }
/*     */     else
/*     */     {
/*  90 */       storeFluid(data);
/*     */     }
/*     */   }
/*     */ 
/*     */   private <T> void storeFluid(T data)
/*     */     throws EntityException
/*     */   {
/*     */     try
/*     */     {
/* 105 */       String tableName = getTableName(data.getClass());
/*     */ 
/* 107 */       DatabaseMetaData dbMeta = this.conn.getMetaData();
/*     */ 
/* 109 */       Map dbColumns = new HashMap();
/* 110 */       Map dbColumnsCopy = new HashMap();
/*     */ 
/* 112 */       List fields = ReflectUtil.getAllFields(data.getClass());
/* 113 */       List structureQueries = new ArrayList();
/* 114 */       List indexQueries = new ArrayList();
/* 115 */       List columnNames = new ArrayList();
/* 116 */       List columnValues = new ArrayList();
/* 117 */       List indicies = new ArrayList();
/*     */ 
/* 119 */       ResultSet trs = dbMeta.getTables(this.catalog, this.schema, tableName, null);
/* 120 */       boolean tableExists = trs.next();
/*     */ 
/* 122 */       Field primaryKey = null;
/* 123 */       Object primaryKeyValue = null;
/*     */ 
/* 126 */       ResultSet rsCol = dbMeta.getColumns(this.catalog, this.schema, tableName, "%");
/*     */       int nullibility;
/* 127 */       while (rsCol.next())
/*     */       {
/* 129 */         String name = rsCol.getString("COLUMN_NAME");
/* 130 */         String columnType = rsCol.getString("TYPE_NAME");
/* 131 */         nullibility = rsCol.getInt("NULLABLE");
/* 132 */         int size = rsCol.getInt("COLUMN_SIZE");
/*     */ 
/* 134 */         if (columnType.equals("VARCHAR"))
/*     */         {
/* 136 */           columnType = columnType + "(" + size + ")";
/*     */         }
/*     */ 
/* 139 */         if (nullibility == 0)
/*     */         {
/* 141 */           columnType = columnType + " NOT NULL";
/*     */         }
/*     */ 
/* 144 */         dbColumns.put(name, columnType);
/*     */       }
/*     */ 
/* 148 */       dbColumnsCopy.putAll(dbColumns);
/*     */ 
/* 151 */       ResultSet rsIdx = dbMeta.getIndexInfo(this.catalog, this.schema, tableName, false, true);
/* 152 */       while (rsIdx.next())
/*     */       {
/* 154 */         indicies.add(rsIdx.getString("INDEX_NAME"));
/*     */       }
/*     */       String ddlString;
/* 158 */       for (Field field : fields)
/*     */       {
/* 160 */         field.setAccessible(true);
/*     */ 
/* 163 */         if ((!Modifier.isTransient(field.getModifiers())) && (!Modifier.isStatic(field.getModifiers())))
/*     */         {
/* 168 */           Object value = field.get(data);
/*     */ 
/* 171 */           if ((field.isAnnotationPresent(Id.class)) && (((Id)field.getAnnotation(Id.class)).generated()) && (value == null))
/*     */           {
/* 173 */             value = Integer.valueOf(1);
/* 174 */             if (dbColumns.containsKey(field.getName()))
/*     */             {
/* 176 */               value = getNextId(tableName, field);
/* 177 */               field.set(data, value);
/*     */             }
/*     */           }
/*     */ 
/* 181 */           columnNames.add(field.getName());
/* 182 */           columnValues.add(getValue(value));
/*     */ 
/* 185 */           if (field.isAnnotationPresent(Id.class))
/*     */           {
/* 187 */             primaryKey = field;
/* 188 */             primaryKeyValue = value;
/*     */ 
/* 190 */             if (!tableExists)
/*     */             {
/* 192 */               continue;
/*     */             }
/*     */           }
/* 195 */           else if (field.isAnnotationPresent(Index.class))
/*     */           {
/* 197 */             String indexName = getIndexName(field.getName(), false);
/*     */ 
/* 199 */             if (!indicies.contains(indexName))
/*     */             {
/* 201 */               indexQueries.add(this.queryWriter.index(tableName, field.getName(), indexName));
/*     */             }
/*     */           }
/* 204 */           else if (field.isAnnotationPresent(Unique.class))
/*     */           {
/* 206 */             String indexName = getIndexName(field.getName(), true);
/*     */ 
/* 208 */             if (!indicies.contains(indexName))
/*     */             {
/* 210 */               indexQueries.add(this.queryWriter.unique(tableName, field.getName(), indexName));
/*     */             }
/*     */ 
/*     */           }
/*     */ 
/* 217 */           if (dbColumns.containsKey(field.getName()))
/*     */           {
/* 219 */             ddlString = this.queryWriter.accomodateField(tableName, field, value, (String)dbColumns.get(field.getName()));
/*     */ 
/* 221 */             if (!ddlString.equals(""))
/*     */             {
/* 223 */               structureQueries.add(ddlString);
/*     */             }
/*     */ 
/* 226 */             dbColumns.remove(field.getName());
/*     */           }
/*     */           else
/*     */           {
/* 230 */             structureQueries.add(this.queryWriter.addColumn(tableName, field, value));
/*     */           }
/*     */         }
/*     */       }
/*     */       Field f;
/* 235 */       if (!tableExists)
/*     */       {
/* 237 */         structureQueries.add(0, this.queryWriter.createTable(tableName, primaryKey, primaryKeyValue));
/* 238 */         indexQueries.add(this.queryWriter.primaryKey(tableName, primaryKey.getName()));
/*     */       }
/*     */       else
/*     */       {
/* 244 */         ResultSet mrs = dbMeta.getPrimaryKeys(this.catalog, this.schema, tableName);
/*     */ 
/* 246 */         if (mrs.next())
/*     */         {
/* 248 */           String oldPrimary = mrs.getString("COLUMN_NAME");
/* 249 */           if (!primaryKey.getName().equals(oldPrimary))
/*     */           {
/* 253 */             indexQueries.add(this.queryWriter.dropPrimaryKey(tableName));
/* 254 */             indexQueries.add(this.queryWriter.primaryKey(tableName, primaryKey.getName()));
/*     */ 
/* 258 */             for (ddlString = fields.iterator(); ddlString.hasNext(); ) { f = (Field)ddlString.next();
/*     */ 
/* 260 */               if (oldPrimary.equals(f.getName()))
/*     */               {
/* 262 */                 indexQueries.add(this.queryWriter.accomodateField(tableName, f, f.get(data), (String)dbColumnsCopy.get(oldPrimary)));
/* 263 */                 break;
/*     */               }
/*     */             }
/*     */           }
/*     */ 
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/* 272 */       Statement st = this.conn.createStatement();
/*     */ 
/* 275 */       for (String ddl : structureQueries)
/*     */       {
/* 277 */         this.log.write(ddl);
/* 278 */         st.addBatch(ddl);
/*     */       }
/*     */ 
/* 282 */       for (String index : indexQueries)
/*     */       {
/* 284 */         this.log.write(index);
/* 285 */         st.addBatch(index);
/*     */       }
/*     */ 
/* 289 */       for (String column : dbColumns.keySet())
/*     */       {
/* 291 */         String dropQuery = this.queryWriter.dropColumn(tableName, column);
/* 292 */         this.log.write(dropQuery);
/* 293 */         st.addBatch(dropQuery);
/*     */       }
/*     */ 
/* 297 */       st.executeBatch();
/*     */ 
/* 301 */       String update = this.queryWriter.insert(tableName, columnNames);
/*     */ 
/* 304 */       if (primaryKeyValue != null)
/*     */       {
/* 306 */         String existsQuery = this.queryWriter.exists(tableName, primaryKey.getName());
/* 307 */         PreparedStatement ps = this.conn.prepareStatement(existsQuery);
/* 308 */         ps.setObject(1, primaryKeyValue);
/*     */ 
/* 310 */         this.log.write(StringUtil.sqlString(existsQuery, new Object[] { primaryKeyValue }));
/*     */ 
/* 312 */         ResultSet rs = ps.executeQuery();
/* 313 */         rs.next();
/*     */ 
/* 315 */         this.log.write("Found " + rs.getInt(1));
/*     */ 
/* 317 */         if (rs.getInt(1) > 0)
/*     */         {
/* 319 */           update = this.queryWriter.update(tableName, columnNames, primaryKey, primaryKeyValue);
/* 320 */           columnValues.add(primaryKeyValue);
/*     */         }
/*     */       }
/*     */ 
/* 324 */       this.log.write(StringUtil.sqlString(update, columnValues));
/*     */ 
/* 326 */       PreparedStatement ps = this.conn.prepareStatement(update);
/*     */ 
/* 328 */       for (int i = 0; i < columnValues.size(); i++)
/*     */       {
/* 330 */         ps.setObject(i + 1, columnValues.get(i));
/*     */       }
/*     */ 
/* 334 */       ps.execute();
/*     */     }
/*     */     catch (SQLException e)
/*     */     {
/* 338 */       this.log.error("Error Writing to DB", e);
/* 339 */       throw new EntityException("Error writing bean to database", e);
/*     */     }
/*     */     catch (IllegalArgumentException e)
/*     */     {
/* 343 */       this.log.error("Error reading object", e);
/* 344 */       throw new EntityException("Error reading object", e);
/*     */     }
/*     */     catch (IllegalAccessException e)
/*     */     {
/* 348 */       this.log.error("Error reading object", e);
/* 349 */       throw new EntityException("Error reading object", e);
/*     */     }
/*     */   }
/*     */ 
/*     */   private <T> void storeFrozen(T data)
/*     */     throws EntityException
/*     */   {
/*     */     try
/*     */     {
/* 367 */       String tableName = getTableName(data.getClass());
/*     */ 
/* 369 */       List columnNames = new ArrayList();
/* 370 */       List columnValues = new ArrayList();
/*     */ 
/* 372 */       List fields = ReflectUtil.getAllFields(data.getClass());
/*     */ 
/* 374 */       Field primaryKey = null;
/* 375 */       Object primaryKeyValue = null;
/*     */ 
/* 380 */       for (Field field : fields)
/*     */       {
/* 382 */         field.setAccessible(true);
/*     */ 
/* 385 */         if ((!Modifier.isTransient(field.getModifiers())) && (!Modifier.isStatic(field.getModifiers())))
/*     */         {
/* 388 */           Object value = getValue(field.get(data));
/* 389 */           if ((field.isAnnotationPresent(Id.class)) && (primaryKeyValue == null))
/*     */           {
/* 391 */             primaryKey = field;
/*     */ 
/* 393 */             if (((Id)primaryKey.getAnnotation(Id.class)).generated())
/*     */             {
/* 395 */               value = getNextId(tableName, primaryKey);
/* 396 */               field.set(data, value);
/*     */             }
/*     */ 
/* 399 */             primaryKeyValue = value;
/*     */           }
/*     */ 
/* 402 */           columnNames.add(field.getName());
/* 403 */           columnValues.add(value);
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/* 409 */       String update = this.queryWriter.insert(tableName, columnNames);
/* 410 */       if (primaryKeyValue != null)
/*     */       {
/* 412 */         String existsQuery = this.queryWriter.exists(tableName, primaryKey.getName());
/* 413 */         PreparedStatement ps = this.conn.prepareStatement(existsQuery);
/* 414 */         ps.setObject(1, primaryKeyValue);
/*     */ 
/* 416 */         this.log.write(StringUtil.sqlString(existsQuery, new Object[] { primaryKeyValue }));
/*     */ 
/* 418 */         ResultSet rs = ps.executeQuery();
/* 419 */         rs.next();
/*     */ 
/* 421 */         this.log.write("Found " + rs.getInt(1));
/*     */ 
/* 423 */         if (rs.getInt(1) > 0)
/*     */         {
/* 425 */           update = this.queryWriter.update(tableName, columnNames, primaryKey, primaryKeyValue);
/* 426 */           columnValues.add(primaryKeyValue);
/*     */         }
/*     */       }
/*     */ 
/* 430 */       this.log.write(StringUtil.sqlString(update, columnValues));
/*     */ 
/* 432 */       PreparedStatement ps = this.conn.prepareStatement(update);
/*     */ 
/* 434 */       for (int i = 0; i < columnValues.size(); i++)
/*     */       {
/* 436 */         ps.setObject(i + 1, columnValues.get(i));
/*     */       }
/*     */ 
/* 440 */       ps.execute();
/*     */     }
/*     */     catch (SecurityException e)
/*     */     {
/* 444 */       this.log.error("Error reading object", e);
/* 445 */       throw new EntityException("Error reading object", e);
/*     */     }
/*     */     catch (IllegalArgumentException e)
/*     */     {
/* 449 */       this.log.error("Error reading object", e);
/* 450 */       throw new EntityException("Error reading object", e);
/*     */     }
/*     */     catch (IllegalAccessException e)
/*     */     {
/* 454 */       this.log.error("Error reading object", e);
/* 455 */       throw new EntityException("Error reading object", e);
/*     */     }
/*     */     catch (SQLException e)
/*     */     {
/* 459 */       this.log.error("Error Writing to DB", e);
/* 460 */       throw new EntityException("Error writing bean to database", e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public <T> T load(Class<T> cl, Object id)
/*     */     throws EntityException
/*     */   {
/*     */     try
/*     */     {
/* 477 */       validate(cl);
/*     */ 
/* 479 */       String tableName = getTableName(cl);
/* 480 */       Field primaryKeyName = findPrimaryKey(cl);
/*     */ 
/* 482 */       String retrieveQuery = this.queryWriter.retrieve(tableName, primaryKeyName.getName(), id);
/*     */ 
/* 484 */       this.log.write(StringUtil.sqlString(retrieveQuery, new Object[] { id }));
/*     */ 
/* 486 */       PreparedStatement ps = this.conn.prepareStatement(retrieveQuery);
/*     */ 
/* 488 */       ps.setObject(1, id);
/*     */ 
/* 490 */       ResultSet rs = ps.executeQuery();
/*     */ 
/* 492 */       return entityFromResultSet(cl, rs);
/*     */     }
/*     */     catch (SecurityException e)
/*     */     {
/* 496 */       this.log.error("Error reading object", e);
/* 497 */       throw new EntityException("Error reading object", e);
/*     */     }
/*     */     catch (IllegalArgumentException e)
/*     */     {
/* 501 */       this.log.error("Error reading object", e);
/* 502 */       throw new EntityException("Error reading object", e);
/*     */     }
/*     */     catch (SQLException e)
/*     */     {
/* 506 */       this.log.error("Error reading DB", e);
/* 507 */       throw new EntityException("Error reading from DB", e);
/*     */     }
/*     */     catch (InstantiationException e)
/*     */     {
/* 511 */       this.log.error("Error instantiating object", e);
/* 512 */       throw new EntityException("Error instantiating the object", e);
/*     */     }
/*     */     catch (IllegalAccessException e)
/*     */     {
/* 516 */       this.log.error("Error reading object", e);
/* 517 */       throw new EntityException("Error reading object", e);
/*     */     }
/*     */     catch (NoSuchFieldException e)
/*     */     {
/* 521 */       this.log.error("Error reading object", e);
/* 522 */       throw new EntityException("Error reading object", e);
/*     */     }
/*     */   }
/*     */ 
/*     */   private <T> T entityFromResultSet(Class<T> cl, ResultSet rs)
/*     */     throws InstantiationException, IllegalAccessException, SQLException, NoSuchFieldException
/*     */   {
/* 538 */     Object loaded = cl.newInstance();
/* 539 */     if (rs.next())
/*     */     {
/* 541 */       List fields = ReflectUtil.getAllFields(cl);
/*     */ 
/* 543 */       for (Field field : fields)
/*     */       {
/* 546 */         if ((!Modifier.isTransient(field.getModifiers())) && (!Modifier.isStatic(field.getModifiers())))
/*     */         {
/* 551 */           field.setAccessible(true);
/* 552 */           Object loadedField = rs.getObject(field.getName());
/*     */ 
/* 554 */           if (field.getType().isEnum())
/*     */           {
/* 556 */             loadedField = field.getType().getField(loadedField.toString()).get(null);
/*     */           }
/*     */ 
/* 559 */           field.set(loaded, loadedField);
/*     */         }
/*     */       }
/*     */     }
/*     */     else {
/* 564 */       loaded = null;
/*     */     }
/*     */ 
/* 567 */     return loaded;
/*     */   }
/*     */ 
/*     */   public <T> List<T> batchLoad(Class<T> cl, List<Object> ids)
/*     */     throws EntityException
/*     */   {
/* 580 */     validate(cl);
/*     */ 
/* 582 */     String tableName = getTableName(cl);
/*     */ 
/* 585 */     Field primaryKey = findPrimaryKey(cl);
/*     */ 
/* 587 */     String sql = this.queryWriter.batchLoad(tableName, primaryKey.getName(), ids.size());
/* 588 */     this.log.write(StringUtil.sqlString(sql, ids));
/*     */     try
/*     */     {
/* 592 */       PreparedStatement ps = this.conn.prepareStatement(sql);
/*     */ 
/* 594 */       for (int i = 0; i < ids.size(); i++)
/*     */       {
/* 596 */         ps.setObject(i + 1, ids.get(i));
/*     */       }
/*     */ 
/* 599 */       ResultSet rs = ps.executeQuery();
/*     */ 
/* 601 */       List list = new ArrayList();
/* 602 */       for (int i = ids.size(); i > 0; i--)
/*     */       {
/* 604 */         list.add(entityFromResultSet(cl, rs));
/*     */       }
/*     */ 
/* 607 */       return list;
/*     */     }
/*     */     catch (SQLException e)
/*     */     {
/* 611 */       this.log.error("Error reading DB", e);
/* 612 */       throw new EntityException("Error reading from DB", e);
/*     */     }
/*     */     catch (InstantiationException e)
/*     */     {
/* 616 */       this.log.error("Error reading object", e);
/* 617 */       throw new EntityException("Error reading object", e);
/*     */     }
/*     */     catch (IllegalAccessException e)
/*     */     {
/* 621 */       this.log.error("Error reading object", e);
/* 622 */       throw new EntityException("Error reading object", e);
/*     */     }
/*     */     catch (NoSuchFieldException e)
/*     */     {
/* 626 */       this.log.error("Error reading object", e);
/* 627 */       throw new EntityException("Error reading object", e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public boolean delete(Class<?> cl, Object id)
/*     */     throws EntityException
/*     */   {
/*     */     try
/*     */     {
/* 643 */       validate(cl);
/*     */ 
/* 645 */       String primaryKeyName = findPrimaryKey(cl).getName();
/* 646 */       String sql = this.queryWriter.delete(getTableName(cl), primaryKeyName, id);
/*     */ 
/* 648 */       this.log.write(StringUtil.sqlString(sql, new Object[] { id }));
/*     */ 
/* 650 */       PreparedStatement ps = this.conn.prepareStatement(sql);
/* 651 */       ps.setObject(1, id);
/*     */ 
/* 653 */       int result = ps.executeUpdate();
/*     */ 
/* 655 */       this.log.write("Deleted: " + result);
/*     */ 
/* 657 */       return result > 0;
/*     */     }
/*     */     catch (SQLException e)
/*     */     {
/* 661 */       this.log.error("Error writing to DB", e);
/* 662 */       throw new EntityException("Error writing to DB", e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public String getTableName(Class<?> cl)
/*     */   {
/* 671 */     String name = cl.getSimpleName();
/* 672 */     if (cl.isAnnotationPresent(Table.class))
/*     */     {
/* 674 */       name = ((Table)cl.getAnnotation(Table.class)).name();
/*     */     }
/*     */ 
/* 677 */     return name;
/*     */   }
/*     */ 
/*     */   private <T> void validate(Class<?> cl)
/*     */     throws EntityException
/*     */   {
/* 693 */     String errMsg = "";
/*     */ 
/* 696 */     List fields = ReflectUtil.getAllFields(cl);
/*     */ 
/* 698 */     int primaryKeyNum = 0;
/* 699 */     for (Field f : fields)
/*     */     {
/* 701 */       if (f.getType().isPrimitive())
/*     */       {
/* 703 */         errMsg = errMsg + "[ERROR]" + f.getName() + " is primitive\n";
/*     */       }
/*     */ 
/* 706 */       if (f.isAnnotationPresent(Id.class))
/*     */       {
/* 708 */         primaryKeyNum++;
/*     */ 
/* 710 */         Id an = (Id)f.getAnnotation(Id.class);
/*     */ 
/* 712 */         if ((an.generated()) && (!Number.class.isAssignableFrom(f.getType())))
/*     */         {
/* 714 */           errMsg = errMsg + "[ERROR] Id with generated option can only be assigned to a numeric type";
/*     */         }
/*     */ 
/* 717 */         if ((f.isAnnotationPresent(Index.class)) || (f.isAnnotationPresent(Unique.class)))
/*     */         {
/* 719 */           errMsg = errMsg + "[ERROR] Primary Key cannot have additional Index or Unique annotations";
/*     */         }
/*     */       }
/*     */ 
/* 723 */       if ((f.isAnnotationPresent(Index.class)) && (f.isAnnotationPresent(Unique.class)))
/*     */       {
/* 725 */         errMsg = errMsg + "[ERROR] " + f.getName() + " has both Index and Unique annotations.  Only one is allowed.";
/*     */       }
/*     */     }
/*     */ 
/* 729 */     if (primaryKeyNum != 1)
/*     */     {
/* 731 */       errMsg = "[ERROR] Class must annotate exactly one field with @Id annotation";
/*     */     }
/*     */ 
/* 734 */     if (errMsg != "")
/*     */     {
/* 736 */       throw new EntityException(errMsg);
/*     */     }
/*     */   }
/*     */ 
/*     */   private Object getValue(Object value)
/*     */   {
/* 743 */     if (value != null)
/*     */     {
/* 747 */       if (value.getClass().isEnum())
/*     */       {
/* 749 */         value = value.toString();
/*     */       }
/*     */     }
/* 752 */     return value;
/*     */   }
/*     */ 
/*     */   private String getIndexName(String columnName, boolean unique)
/*     */   {
/* 757 */     String indexName = columnName;
/* 758 */     if (unique)
/*     */     {
/* 760 */       indexName = indexName + "_unique_idx";
/*     */     }
/*     */     else
/*     */     {
/* 764 */       indexName = indexName + "_idx";
/*     */     }
/*     */ 
/* 767 */     return indexName;
/*     */   }
/*     */ 
/*     */   private Object getNextId(String tableName, Field primaryKey) throws SQLException
/*     */   {
/* 772 */     Object value = Integer.valueOf(1);
/*     */ 
/* 774 */     String nextIdQuery = this.queryWriter.nextId(tableName, primaryKey.getName());
/* 775 */     ResultSet rs = this.conn.createStatement().executeQuery(nextIdQuery);
/*     */ 
/* 777 */     if (rs.next())
/*     */     {
/* 779 */       if (primaryKey.getType() == Short.class)
/*     */       {
/* 781 */         value = Short.valueOf(rs.getShort(1));
/*     */       }
/* 783 */       else if (primaryKey.getType() == Integer.class)
/*     */       {
/* 785 */         value = Integer.valueOf(rs.getInt(1));
/*     */       }
/* 787 */       else if (primaryKey.getType() == Long.class)
/*     */       {
/* 789 */         value = Long.valueOf(rs.getLong(1));
/*     */       }
/*     */ 
/* 793 */       if (value == null)
/*     */       {
/* 795 */         value = Integer.valueOf(1);
/*     */       }
/*     */     }
/*     */ 
/* 799 */     return value;
/*     */   }
/*     */ 
/*     */   private Field findPrimaryKey(Class<?> cl) {
/* 803 */     List fields = ReflectUtil.getAllFields(cl);
/*     */ 
/* 805 */     for (Field field : fields)
/*     */     {
/* 807 */       if (field.isAnnotationPresent(Id.class))
/*     */       {
/* 809 */         return field;
/*     */       }
/*     */     }
/*     */ 
/* 813 */     throw new IllegalArgumentException("NO PRIMARY KEY FOUND");
/*     */   }
/*     */ }

/* Location:           C:\Users\leigh\Downloads\redbean4j-0.1.jar
 * Qualified Name:     org.redbean.EntityManager
 * JD-Core Version:    0.6.2
 */