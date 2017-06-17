package cs.bms.dao.util;





public class PGSqlUtil
{
  private static JDBCPostGresSQL jdbcPostGresSQL;
  




  public static JDBCPostGresSQL getJdbcPostGresSQL()
  {
    if (jdbcPostGresSQL == null) {
      jdbcPostGresSQL = new JDBCPostGresSQL();
    }
    return jdbcPostGresSQL;
  }
  
  public static void setJdbcPostGresSQL(JDBCPostGresSQL jdbcPostGresSQL) {
    jdbcPostGresSQL = jdbcPostGresSQL;
  }
}
