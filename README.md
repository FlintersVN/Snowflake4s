# Snowflake4s
**A k-sortable unique ID generation lib using Scala.**

It using the same encoded ID format as Twitter's Snowflake. Snowflake4s can generate the roughly-sorted 64 bit ids as Long type

##Get Started
You should add the following dependency.
```sbtshell
libraryDependencies += "com.septech" %% "Snowflake4s" % "0.0.1-ALPHA"
```
##How to use
to generate id:
```scala
val id = Snowflake4s.generator.generate()
```

You also could bulk generate 10 ids with the following snippet: 
```scala
val ids = Snowflake4s.generator.bulkGenerate(10)
```

##License
The Snowflake4s is released under version 2.0 of the Apache License.

##References
- https://github.com/twitter/snowflake/tree/scala_28
- https://blog.twitter.com/2010/announcing-snowflake
