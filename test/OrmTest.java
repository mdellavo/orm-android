import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.quuux.orm.*;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLog;

import java.util.Date;
import java.util.List;

@Config(shadows = {ShadowLog.class })
@RunWith(RobolectricTestRunner.class)
public class OrmTest {

    private static final String TAG = "SchemaBuilderTest";

    @Before
    public void setUp() {
        ShadowLog.stream = System.out;
    }

    @Table(name="users")
    public static class User implements Entity {

        public User() {}

        @Column(primaryKey = true)
        private long id = -1;

        @Column(name="user_name", unique = true, nullable = false)
        private String userName;

        public User(final String name) {
            userName = name;
        }

        public long getId() {
            return id;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(final String username) {
            userName = username;
        }

        void getScores(final Session session, final QueryListener<Score> listener) {
            RelationBuilder.forEntity(this).other(User.class).query(session, listener);
        }

    }

    @Table(name="scores")
    public static class Score implements Entity {

        public Score() {}

        @Column(primaryKey = true)
        private long id = -1;
        @Column(nullable = false)
        private int score;
        @Column
        private Date date = new Date();

        @Column(foreignKey = User.class)
        private long userId;

        public Score(final User user, int score) {
            userId = user.getId();
            this.score = score;
        }

        public void getUser(final Session session, final FetchListener<User> listener) {
            RelationBuilder.forEntity(this).other(User.class).get(session, listener);
        }

    }

    @Test
    public void testSchemaBuilder() throws Exception {
        final String sql = SchemaBuilder.renderCreateTable(User.class);

    }

    @Test
    public void testCreateAll() throws Exception {
        Database.attach(User.class);
        Database.attach(Score.class);
        Database db = Database.getInstance(Robolectric.application, "test.db", 1);
        db.getWritableDatabase();
        Database.release();
    }

    @Test
    public void testAddObject() throws Exception {
        Database.attach(User.class);
        Database.attach(Score.class);
        Database db = Database.getInstance(Robolectric.application, "test.db", 1);

        final Session session = db.createSession();
        final User u = new User("foo");
        session.add(u);
        session.commit();

        Log.d(TAG, "user id -> " + u.getId());

        u.setUserName("bar");
        session.commit();

        Log.d(TAG, "user id -> " + u.getId());

        final Query query = session.query(User.class).filter("user_name like ?", "f%");

        Log.d(TAG, query.toSql());

        query.all(new QueryListener<User>() {
            @Override
            public void onResult(final List<User> result) {
                Log.d(TAG, "result = %s", result);
            }
        });

        session.query(User.class).get(1, new FetchListener<User>() {
            @Override
            public void onResult(final User result) {
                Log.d(TAG, "result = %s", result);
            }
        });

        session.delete(u);
        session.commit();

        Database.release();
    }

    @Test
    public void testQueryBuilder() throws Exception {

        final QueryBuilder query = new QueryBuilder(
                new QueryBuilder("foo=?", "bar=?"),
                new QueryBuilder("baz=?", "qux=?"),
                "quuux=?"
        );
        Log.d(TAG, "query = %s", query.toSql());

    }

    @Test
    public void testRelationBuilder() throws Exception {

        Database.attach(User.class);
        Database.attach(Score.class);
        Database.release();
        Database db = Database.getInstance(Robolectric.application, "test.db", 1);
        db.getWritableDatabase();

        final Session session = db.createSession();

        final User u = new User("foo");
        session.add(u);

        session.commit();

        Score s = null;
        for (int i=0; i<20; i++) {
            s = new Score(u, i * 100);
            session.add(s);
        }

        session.commit();

        RelationBuilder.forEntity(u).other(Score.class).query(session, new QueryListener<Score>() {
            @Override
            public void onResult(final List<Score> scores) {
                Log.d(TAG, "found %d scores", scores.size());
            }
        });

        RelationBuilder.forEntity(s).other(User.class).get(session, new FetchListener<User>() {
            @Override
            public void onResult(final User user) {
                Log.d(TAG, "found user %s", user.getUserName());
            }
        });


        Database.release();
    }

}


