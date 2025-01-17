package gov.nasa.jpl.aerie.constraints.tree;

import gov.nasa.jpl.aerie.constraints.model.EvaluationEnvironment;
import gov.nasa.jpl.aerie.constraints.model.SimulationResults;
import gov.nasa.jpl.aerie.constraints.time.Interval;
import gov.nasa.jpl.aerie.constraints.time.Windows;
import gov.nasa.jpl.aerie.merlin.protocol.types.Duration;

import java.util.Objects;
import java.util.Set;

public final class ShiftWindowsEdges implements Expression<Windows> {
  public final Expression<Windows> windows;
  public final Expression<Duration> fromStart;
  public final Expression<Duration> fromEnd;

  public ShiftWindowsEdges(final Expression<Windows> left, final Expression<Duration> fromStart, final Expression<Duration> fromEnd) {
    this.windows = left;
    this.fromStart = fromStart;
    this.fromEnd = fromEnd;
  }

  @Override
  public Windows evaluate(final SimulationResults results, final Interval bounds, final EvaluationEnvironment environment) {
    final var shiftRising = this.fromStart.evaluate(results, bounds, environment);
    final var shiftFalling = this.fromEnd.evaluate(results, bounds, environment);

    final var newBounds = Interval.between(
        Duration.min(bounds.start.minus(shiftRising), bounds.start.minus(shiftFalling)),
        bounds.startInclusivity,
        Duration.max(bounds.end.minus(shiftRising), bounds.end.minus(shiftFalling)),
        bounds.endInclusivity
    );

    final var windows = this.windows.evaluate(results, newBounds, environment);
    return windows.shiftEdges(shiftRising, shiftFalling).select(bounds);
  }

  @Override
  public void extractResources(final Set<String> names) {
    this.windows.extractResources(names);
  }

  @Override
  public String prettyPrint(final String prefix) {
    return String.format(
        "\n%s(shiftWindowsEdges %s by %s %s)",
        prefix,
        this.windows.prettyPrint(prefix + "  "),
        this.fromStart.toString(),
        this.fromEnd.toString()
    );
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof ShiftWindowsEdges)) return false;
    final var o = (ShiftWindowsEdges)obj;

    return Objects.equals(this.windows, o.windows) &&
           Objects.equals(this.fromStart, o.fromStart) &&
           Objects.equals(this.fromEnd, o.fromEnd);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.windows, this.fromStart, this.fromEnd);
  }
}
