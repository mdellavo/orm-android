ORM for Android

Mostly an experimental/work in progress ORM inspired by SQLAlchemy.

There is a very basic smoke test in the test directory which serves as an example of use.

TODO
 - a better name
 - Flesh out the rest of the Connection/Session/etc
 - Build out query and some query builder utils (func stuff)
 - Clean up and refactor SchemaBuilder
 - Serializer, deserializer interface

 - Query Task refactor
     continue refactor of query task so can use it as a generic query interface

 - Query cache for inserts
   - keep a query cache of compiled sql

 - Figure out mapper.
   New annotation @MappedQuery which implements the Mappable interface (wip).

   Need to factor out some column set abstraction

   interface Mappable {
       Query buildMappingQuery()
   }

 - Relations
   Document, See test for now

 - Actual unit tests (ie - not just using junit as a test harness for a smoke test)
