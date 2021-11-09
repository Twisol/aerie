package gov.nasa.jpl.aerie.merlin.driver.engine;

import gov.nasa.jpl.aerie.merlin.driver.timeline.CausalEventSource;
import gov.nasa.jpl.aerie.merlin.driver.timeline.Event;
import gov.nasa.jpl.aerie.merlin.driver.timeline.EventGraph;
import gov.nasa.jpl.aerie.merlin.driver.timeline.LiveCells;
import gov.nasa.jpl.aerie.merlin.driver.timeline.Query;
import gov.nasa.jpl.aerie.merlin.protocol.types.Duration;
import org.apache.commons.lang3.tuple.Triple;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * A TaskFrame is a record of a completed task and its remaining branches.
 *
 * <p>
 * <pre>
 *   |-> branches[0].left |-> branches[1].left   ... |-> branches[n].left     |-> tip
 *   +-> sibling          +-> branches[0].right      +-> branches[n-1].right  +-> branches[n].right
 * </pre>
 * </p>
 *
 * <p>
 * When there are no branches, this simplifies to:
 * <pre>
 *   |-> tip
 *   +-> sibling
 * </pre>
 * In which case, the frame can evolve no further; we can bind the tip and sibling concurrently
 * and commit it to a parent timeline.
 * </p>
*/
public final class TaskFrame<Signal> {
  private final Optional<TaskFrame<Signal>> continuation;
  private final EventGraph<Event> sibling;

  private CausalEventSource events;
  private final Deque<Triple<CausalEventSource, LiveCells, Signal>> branches;

  private TaskFrame(
      final Optional<TaskFrame<Signal>> continuation,
      final EventGraph<Event> sibling,
      final FrameBuilder<Signal> builder
  ) {
    this.continuation = continuation;
    this.sibling = sibling;

    this.events = builder.events;
    this.branches = builder.branches;
  }

  public static <Signal>
  TaskFrame<Signal> of(final LiveCells context, final Consumer<FrameBuilder<Signal>> body) {
    final var builder = new FrameBuilder<Signal>(context);
    body.accept(builder);
    return new TaskFrame<>(Optional.empty(), EventGraph.empty(), builder);
  }

  public static <Signal>
  EventGraph<Event> runToCompletion(
      final Iterable<Signal> jobs,
      final LiveCells context,
      final BiConsumer<Signal, FrameBuilder<Signal>> executor
  ) {
    var frame = TaskFrame.<Signal>of(context, builder -> jobs.forEach(builder::signal));

    while (!frame.isDone()) frame = frame.step(executor);

    return frame.commit();
  }


  public EventGraph<Event> commit() {
    return EventGraph.concurrently(this.sibling, this.events.commit());
  }

  public boolean isDone() {
    return this.branches.isEmpty() && this.continuation.isEmpty();
  }

  public TaskFrame<Signal> step(final BiConsumer<Signal, FrameBuilder<Signal>> executor) {
    if (!this.branches.isEmpty()) {
      // We have uncommitted child branches.
      // Start wandering down the next one.
      final var branch = this.branches.pop();
      final var branchBase = branch.getLeft();
      final var branchContext = branch.getMiddle();
      final var signal = branch.getRight();

      final var builder = new FrameBuilder<Signal>(branchContext);
      executor.accept(signal, builder);

      final var sibling = this.events.commit();
      this.events = branchBase;

      return new TaskFrame<>(Optional.of(this), sibling, builder);
    } else if (this.continuation.isPresent()) {
      // We're done here, but a parent frame is waiting for us to finish.
      // Commit our tip back up to the parent.
      final var parent = this.continuation.orElseThrow();
      parent.events.add(this.commit());
      return parent;
    } else {
      // There's nothing at all left to do.
      assert this.isDone();
      return this;
    }
  }

  public static final class FrameBuilder<Signal> {
    private CausalEventSource events;
    private LiveCells cells;
    private final Deque<Triple<CausalEventSource, LiveCells, Signal>> branches = new ArrayDeque<>();

    private FrameBuilder(final LiveCells context) {
      this.events = new CausalEventSource();
      this.cells = new LiveCells(this.events, context);
    }

    public <State> Optional<State> getState(final Query<State> query) {
      return this.cells.getState(query);
    }

    public Optional<Duration> getExpiry(final Query<?> query) {
      return this.cells.getExpiry(query);
    }

    public void emit(final Event event) {
      this.events.add(EventGraph.atom(event));
    }

    public void signal(final Signal target) {
      // If we haven't emitted any events, subscribe the target to the previous branch point instead.
      // This avoids making long chains of LiveCells over segments where no events have actually been accumulated.
      if (this.events.points().isEmpty() && !this.branches.isEmpty()) {
        this.branches.push(Triple.of(new CausalEventSource(), this.branches.peek().getMiddle(), target));
      } else {
        this.branches.push(Triple.of(this.events, this.cells, target));

        this.events = new CausalEventSource();
        this.cells = new LiveCells(this.events, this.cells);
      }
    }
  }
}
