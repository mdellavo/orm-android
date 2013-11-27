package org.quuux.orm;


public class DeleteTask extends ScalarTask {
    public DeleteTask(final Connection connection, final Query query, final ScalarListener listener) {
        super(connection, query, listener);
    }


    @Override
    protected Object doInBackground(final Class... params) {
        final Connection connection = getConnection();
        connection.beginTransaction();
        final Long rv = Long.valueOf(connection.delete(getQuery()));
        connection.commit();
        return rv;
    }
}
