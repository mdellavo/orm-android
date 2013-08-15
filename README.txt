ORM for Android

Mostly an experimental/work in progress ORM inspired by SQLAlchemy.

There is a very basic smoke test in the test directory which serves as an example of use.

TODO
 - a better name
 - Flesh out the rest of the Connection/Session/etc
 - Build out query and some query builder utils (func stuff)
 - Clean up and refactor SchemaBuilder
 - Serializer, deserializer interface
 
 - Figure out mapper.
   
   Are we doing something analagous? 

 - Relations
   - see: http://square.github.io/retrofit/

   Mapped classes are abstract where abstract annotated
   getters that take a Query/Fetch lisener. Object will be proxied, if
   an abstract getter is invoked, fetch and call listener. 

   abstract class Score implements Entity {

       @Column(primaryKey=true)
       Private int id;
       private date;  
       private int score;
      
       @Column(foreignKey=User.class, useList=false)
       private int userId;

       @Relation(User.class)
       public abstract void getUser(FetchListener<User> listener);              
      
       ...
   }
   
   abstract class User implements Entity {
   
       @Column(primaryKey=true)
       private int id;
       private String name;

       @Relation(Score.class)
       public abstract void getScores(QueryListener<Score> listener);

       ...
   }


 - Actual unit tests (ie - not just using junit as a test harness for a smoke test)
