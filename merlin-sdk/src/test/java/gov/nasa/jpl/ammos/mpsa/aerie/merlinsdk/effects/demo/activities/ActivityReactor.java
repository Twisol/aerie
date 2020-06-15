package gov.nasa.jpl.ammos.mpsa.aerie.merlinsdk.effects.demo.activities;

import gov.nasa.jpl.ammos.mpsa.aerie.merlinsdk.activities.Activity;
import gov.nasa.jpl.ammos.mpsa.aerie.merlinsdk.effects.EventGraph;
import gov.nasa.jpl.ammos.mpsa.aerie.merlinsdk.effects.Projection;
import gov.nasa.jpl.ammos.mpsa.aerie.merlinsdk.effects.demo.events.DefaultEventHandler;
import gov.nasa.jpl.ammos.mpsa.aerie.merlinsdk.effects.demo.events.Event;
import gov.nasa.jpl.ammos.mpsa.aerie.merlinsdk.effects.demo.models.Querier;
import gov.nasa.jpl.ammos.mpsa.aerie.merlinsdk.effects.timeline.Time;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import org.apache.commons.lang3.tuple.Pair;
import org.pcollections.HashTreePMap;
import org.pcollections.PMap;

public final class ActivityReactor<T>
    implements DefaultEventHandler<Function<Time<T, Event>, Pair<Time<T, Event>, PMap<String, ScheduleItem>>>>
{
  private static final Map<String, Activity> activityMap = Map.of(
      "a", new ActivityA(),
      "b", new ActivityB()
  );

  private final Querier<T> querier;
  private final Projection<Event, Function<Time<T, Event>, Pair<Time<T, Event>, PMap<String, ScheduleItem>>>> reactor;
  private final Map<String, String> activityInstances = new HashMap<>();

  public ActivityReactor(
      final Querier<T> querier,
      final Projection<Event, Function<Time<T, Event>, Pair<Time<T, Event>, PMap<String, ScheduleItem>>>> reactor
  ) {
    this.querier = querier;
    this.reactor = reactor;
  }

  @Override
  public Function<Time<T, Event>, Pair<Time<T, Event>, PMap<String, ScheduleItem>>> instantiateActivity(final String activityId, final String activityType) {
    if (this.activityInstances.containsKey(activityId)) {
      throw new RuntimeException("Activity ID already in use");
    }

    this.activityInstances.put(activityId, activityType);

    return time -> Pair.of(time, HashTreePMap.empty());
  }

  @Override
  public Function<Time<T, Event>, Pair<Time<T, Event>, PMap<String, ScheduleItem>>> resumeActivity(final String activityId) {
    return time -> {
      final var activityType = this.activityInstances.get(activityId);
      final var activity = activityMap.getOrDefault(activityType, new Activity() {});

      var scheduled = HashTreePMap.<String, ScheduleItem>empty();

      // TODO: avoid using exceptions for control flow by wrapping activities in a Thread
      final var context = new ReactionContext<>(this.querier, this.reactor, List.of(time));
      try {
        ReactionContext.activeContext.setWithin(context, activity::modelEffects);
        scheduled = scheduled.plusAll(context.getScheduled());
        time = context.getCurrentTime();

        scheduled = scheduled.plus(activityId, new ScheduleItem.Complete());
      } catch (final ReactionContext.Defer request) {
        scheduled = scheduled.plusAll(context.getScheduled());
        time = context.getCurrentTime();

        scheduled = scheduled.plus(activityId, new ScheduleItem.Defer(request.duration));
      } catch (final ReactionContext.Call request) {
        scheduled = scheduled.plusAll(context.getScheduled());
        time = context.getCurrentTime();

        final var childId = UUID.randomUUID().toString();
        scheduled = scheduled.plus(activityId, new ScheduleItem.OnCompletion(childId));

        final var callGraph = EventGraph.sequentially(
            EventGraph.atom(Event.instantiateActivity(childId, request.activityType)),
            EventGraph.atom(Event.resumeActivity(childId)));
        final var result = callGraph.evaluate(this.reactor).apply(time);
        scheduled = scheduled.plusAll(result.getRight());
        time = result.getLeft();
      }

      return Pair.of(time, scheduled);
    };
  }

  @Override
  public Function<Time<T, Event>, Pair<Time<T, Event>, PMap<String, ScheduleItem>>> unhandled() {
    return ctx -> Pair.of(ctx, HashTreePMap.empty());
  }
}
