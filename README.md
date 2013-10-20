_WARNING_
_WARNING_
*This is an abandoned project that has not been in development for many years*
_WARNING_

RedBean4j is a "fire and forget" ORM for Java 6+ that performs automatic database management based on Java code.

It is based on wonderful pioneering work done by Gabor de Mooij in his RedBeanPHP

Key Features

Very easy to use
Works with POJO's
Creates and Alters tables as needed
Adds and Drops Indices as needed
Completely eliminates the hassle of synchronize code to DB
Usage

Usage

Create database schema that will hold ORM tables 

Create user with ALL privileges on that schema

Create your domain Plain Old Java Object as normal and annotate exactly ONE field with @Id
```java
public class User
{
   public User(Integer ssn)
   {
      this.ssn=ssn;
   }
   ...
   @Id
   private Integer ssn;
   ...
}
```
Note: Primitives are not supported, so use Integer instead of int and so on 



Create EntityManager:

EntityManager manager = new EntityManager(conn,false);

Note: conn is a JDBC Connection object. Creation of one of those is beyond the scope of this guide Note: conn must be created with a user that has ALL privileges within the schema

Create an instance:
```java
User user = new User(333334444);

Save:

manager.store(user);

To Load:

User user = manager.load(333334444);

Annotations

@Id

Defines primary id. Must annotate exactly one field in an object

@id(generated=true)

Defines primary id and asks RedBean to manage ids. Only works on Integral types

@Index

Tells RedBean to create an index on the annotated field

@Unique

Tells RedBean to create a unique index on annotated field

@NotNull

Tells RedBean to declare Database Column NOT NULL

@Table(name="$name")
```
Tells RedBean to use $name for table name for this Class. Scope: Class

Notes

Multi-column indices are not supported
Default table name is same as simple class name clazz.getSimpleName();

MySQL (5.x)
Supported Java Types

Short
Integer
Long
Float
Double
String
Date
Boolean
Enums (supported as string)
Note: Primitives are not supported because they cannot have null value
