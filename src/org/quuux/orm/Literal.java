package org.quuux.orm;

public class Literal implements Clause {
    private String mClause;

    public Literal(final String clause) {
        mClause = clause;
    }

    @Override
    public String toSql() {
        return mClause;
    }
}
