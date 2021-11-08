package gov.nasa.jpl.aerie.merlin.server.remotes.postgres;

import org.intellij.lang.annotations.Language;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/*package-local*/ final class CreateProfileSegmentPartitionTableAction implements AutoCloseable {
  private final @Language("SQL") String sql = "create table %s partition of profile_segment for values in (%d)";
  private final PreparedStatement statement;

  public CreateProfileSegmentPartitionTableAction(
      final Connection connection,
      final long datasetId,
      final String partitionName
  ) throws SQLException {
    this.statement = connection.prepareStatement(String.format(sql, partitionName, datasetId));
  }

  public void apply() throws SQLException {
    statement.execute();
  }

  @Override
  public void close() throws SQLException {
    this.statement.close();
  }
}
