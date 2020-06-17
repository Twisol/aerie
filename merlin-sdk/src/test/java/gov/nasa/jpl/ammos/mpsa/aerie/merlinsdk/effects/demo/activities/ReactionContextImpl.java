package gov.nasa.jpl.ammos.mpsa.aerie.merlinsdk.effects.demo.activities;

import gov.nasa.jpl.ammos.mpsa.aerie.merlinsdk.effects.timeline.Time;
import gov.nasa.jpl.ammos.mpsa.aerie.merlinsdk.time.Duration;
import org.apache.commons.lang3.tuple.Triple;
import org.pcollections.ConsPStack;
import org.pcollections.PStack;
import org.pcollections.PVector;
import org.pcollections.TreePVector;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class ReactionContextImpl<T, Activity, Event> implements ReactionContext<T, Activity, Event> {
  private PStack<Triple<String, Activity, PVector<ActivityBreadcrumb<T, Event>>>> spawns = ConsPStack.empty();
  private PVector<ActivityBreadcrumb<T, Event>> breadcrumbs;
  private int nextBreadcrumbIndex;

  private Time<T, Event> currentTime;
  private final Set<String> children = new HashSet<>();

  public ReactionContextImpl(final PVector<ActivityBreadcrumb<T, Event>> breadcrumbs) {
    this.currentTime = ((ActivityBreadcrumb.Advance<T, Event>) breadcrumbs.get(0)).next;
    this.breadcrumbs = breadcrumbs;
    this.nextBreadcrumbIndex = 1;
  }

  public final Time<T, Event> getCurrentTime() {
    return this.currentTime;
  }

  public final PVector<ActivityBreadcrumb<T, Event>> getBreadcrumbs() {
    return this.breadcrumbs;
  }

  public final PStack<Triple<String, Activity, PVector<ActivityBreadcrumb<T, Event>>>> getSpawns() {
    return this.spawns;
  }

  @Override
  public Time<T, Event> now() {
    return this.currentTime;
  }

  @Override
  public final void emit(final Event event) {
    this.currentTime = this.currentTime.emit(event);
  }

  public final void delay(final Duration duration) {
    if (this.nextBreadcrumbIndex >= breadcrumbs.size()) {
      throw new Defer(duration);
    } else {
      final var breadcrumb = this.breadcrumbs.get(this.nextBreadcrumbIndex++);
      if (!(breadcrumb instanceof ActivityBreadcrumb.Advance)) {
        throw new RuntimeException("Unexpected breadcrumb on delay(): " + breadcrumb.getClass().getName());
      }

      this.currentTime = ((ActivityBreadcrumb.Advance<T, Event>) breadcrumb).next;
    }
  }

  @Override
  public final void waitForActivity(final String activityId) {
    if (this.nextBreadcrumbIndex >= breadcrumbs.size()) {
      throw new Await(activityId);
    } else {
      final var breadcrumb = this.breadcrumbs.get(this.nextBreadcrumbIndex++);
      if (!(breadcrumb instanceof ActivityBreadcrumb.Advance)) {
        throw new RuntimeException("Unexpected breadcrumb on waitForActivity(): " + breadcrumb.getClass().getName());
      }

      this.currentTime = ((ActivityBreadcrumb.Advance<T, Event>) breadcrumb).next;
    }
  }

  public final void waitForChildren() {
    for (final var child : this.children) this.waitForActivity(child);
  }

  @Override
  public final String spawn(final Activity activity) {
    final String childId;
    if (this.nextBreadcrumbIndex >= breadcrumbs.size()) {
      this.currentTime = this.currentTime.fork();

      childId = UUID.randomUUID().toString();
      this.spawns = this.spawns.plus(Triple.of(childId, activity, TreePVector.singleton(new ActivityBreadcrumb.Advance<>(this.currentTime))));

      this.breadcrumbs = this.breadcrumbs.plus(new ActivityBreadcrumb.Spawn<>(childId));
      this.nextBreadcrumbIndex += 1;
    } else {
      final var breadcrumb = this.breadcrumbs.get(this.nextBreadcrumbIndex++);
      if (!(breadcrumb instanceof ActivityBreadcrumb.Spawn)) {
        throw new RuntimeException("Unexpected breadcrumb; expected spawn, got " + breadcrumb.getClass().getName());
      }

      childId = ((ActivityBreadcrumb.Spawn<T, Event>) breadcrumb).activityId;
    }

    this.children.add(childId);
    return childId;
  }

  public static final class Defer extends RuntimeException {
    public final Duration duration;

    private Defer(final Duration duration) {
      this.duration = duration;
    }
  }

  public static final class Await extends RuntimeException {
    public final String activityId;

    private Await(final String activityId) {
      this.activityId = activityId;
    }
  }
}
