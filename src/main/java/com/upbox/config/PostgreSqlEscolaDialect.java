package com.upbox.config;

import java.sql.Types;
import org.hibernate.community.dialect.PostgreSQLLegacyDialect;
import org.hibernate.dialect.identity.IdentityColumnSupport;
import org.hibernate.dialect.identity.PostgreSQLIdentityColumnSupport;
import org.hibernate.engine.jdbc.dialect.spi.DialectResolutionInfo;

/** SQL compatível com o laboratório: PostgreSQL 9 usa serial, e não identity do PostgreSQL 10. */
public class PostgreSqlEscolaDialect extends PostgreSQLLegacyDialect {
  public PostgreSqlEscolaDialect() {
    super();
  }

  public PostgreSqlEscolaDialect(DialectResolutionInfo info) {
    super(info);
  }

  @Override
  public IdentityColumnSupport getIdentityColumnSupport() {
    if (getVersion().isSameOrAfter(10)) return super.getIdentityColumnSupport();
    // O dialect comunitário 6.5 ainda emite identity. Serial mantém as mesmas
    // sequências tabela_id_seq e a recuperação automática do ID pelo JDBC.
    return new PostgreSQLIdentityColumnSupport() {
      @Override
      public boolean hasDataTypeInIdentityColumn() {
        return false;
      }

      @Override
      public String getIdentityColumnString(int type) {
        return type == Types.BIGINT ? "bigserial" : "serial";
      }
    };
  }
}
