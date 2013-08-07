package org.quuux.orm;

import java.util.ArrayList;
import java.util.List;

public class QueryBuilder implements Clause {

    public enum Operator {
        AND,
        OR,
        NOT
    };

    private Operator mOperator = Operator.AND;
    private List<Clause> mClauses = new ArrayList<Clause>();

    public QueryBuilder() {}

    public QueryBuilder(final Clause... clauses) {
        for (final Clause clause : clauses)
            add(clause);
    }

    public QueryBuilder(final String... clauses) {
        for (final String clause : clauses)
            add(clause);
    }

    public QueryBuilder add(final Clause clause) {
        mClauses.add(clause);
        return this;
    }

    public QueryBuilder add(final String clause) {
        return add(new Literal(clause));
    }

    @Override
    public String toSql() {
        final StringBuilder sb = new StringBuilder();

        for (int i=0; i<mClauses.size(); i++) {
            sb.append("(");
            sb.append(mClauses.get(i).toSql());
            sb.append(")");

            if (i < mClauses.size() - 1) {
                sb.append(" ");
                sb.append(mOperator);
                sb.append(" ");
            }
        }

        return sb.toString();
    }

}
