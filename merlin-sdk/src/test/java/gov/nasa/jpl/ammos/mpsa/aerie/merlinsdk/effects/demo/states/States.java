package gov.nasa.jpl.ammos.mpsa.aerie.merlinsdk.effects.demo.states;

import static gov.nasa.jpl.ammos.mpsa.aerie.merlinsdk.effects.demo.models.Querier.ctx;
import static gov.nasa.jpl.ammos.mpsa.aerie.merlinsdk.effects.demo.models.Querier.dataQuerier;

public final class States {
  private static final DataBinsResource bins = new DataBinsResource(dataQuerier, ctx::emit);

  public static final DataBinResource binA = bins.bin("bin A");

  public static final CumulableResource<String> log = new LogResource(ctx::emit);
}
