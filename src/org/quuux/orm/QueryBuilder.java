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

    public QueryBuilder(final Operator operator, final Object... clauses) {
        mOperator = operator;
        for (final Object clause : clauses)
            add(clause);
    }

    public QueryBuilder(final Object... clauses) {
        this(Operator.AND, clauses);
    }

    public QueryBuilder add(final Object object) {
        Clause clause = null;

        if (object instanceof Clause)
            clause = (Clause) object;
        else if (object instanceof String)
            clause = new Literal((String)object);
        else
            throw new IllegalArgumentException("Unknown object type added to query builder");

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
