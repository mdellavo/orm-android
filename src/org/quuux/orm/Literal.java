package org.quuux.orm;

import java.io.Serializable;

public class Literal implements Clause, Serializable {
    private String mClause;

    public Literal(final String clause) {
        mClause = clause;
    }

    @Override
    public String toSql() {
        return mClause;
    }
}
