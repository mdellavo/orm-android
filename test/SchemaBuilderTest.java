import android.content.Context;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.quuux.orm.*;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLog;

import java.util.List;

@Config(shadows = {ShadowLog.class })
@RunWith(RobolectricTestRunner.class)
public class SchemaBuilderTest {

    private static final String TAG = "SchemaBuilderTest";

    @Before
    public void setUp() {
        ShadowLog.stream = System.out;
    }

    @Table(name="users")
    public  static class User implements Entity {

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

    }
    android.content.Context context;

    @Test
    public void testSchemaBuilder() throws Exception {

        final String sql = SchemaBuilder.renderCreateTable(User.class);

    }

    @Test
    public void testCreateAll() throws Exception {
        Database.attach(User.class);
        Database db = Database.getInstance(Robolectric.application, "test.db", 1);
        db.getWritableDatabase();
    }

    @Test
    public void testAddObject() throws Exception {
        Database.attach(User.class);
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
    }

    @Test
    public void testQueryBuilder() throws Exception {

        final QueryBuilder query = new QueryBuilder(
                new QueryBuilder("foo=?", "bar=?"),
                new QueryBuilder("baz=?", "qux=?"),
                new Literal("quuux=?")
        );
        Log.d(TAG, "query = %s", query.toSql());

    }

}


