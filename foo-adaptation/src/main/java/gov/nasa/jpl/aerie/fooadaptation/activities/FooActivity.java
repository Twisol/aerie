package gov.nasa.jpl.aerie.fooadaptation.activities;

import gov.nasa.jpl.aerie.fooadaptation.Mission;
import gov.nasa.jpl.aerie.fooadaptation.generated.Task;
import gov.nasa.jpl.aerie.fooadaptation.models.ImagerMode;
import gov.nasa.jpl.aerie.merlin.framework.annotations.ActivityType;
import gov.nasa.jpl.aerie.merlin.framework.annotations.ActivityType.Parameter;
import gov.nasa.jpl.aerie.merlin.framework.annotations.ActivityType.Validation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.util.List;

import static gov.nasa.jpl.aerie.time.Duration.SECOND;

@ActivityType("foo")
public final class FooActivity {
  @Parameter
  public int x = 0;

  @Parameter
  public String y = "test";

  @Parameter
  public List<Vector3D> vecs = List.of(new Vector3D(0.0, 0.0, 0.0));

  @Validation("x cannot be exactly 99")
  public boolean validateX() {
    return (x != 99);
  }

  @Validation("y cannot be 'bad'")
  public boolean validateY() {
    return !y.equals("bad");
  }

  public final class EffectModel extends Task {
    public void run(final Mission mission) {
      final var data = mission.data;
      final var complexData = mission.complexData;

      complexData.beginImaging(ImagerMode.HI_RES, 60);

      if (y.equals("test")) {
        data.rate.add(x);
      } else if (y.equals("spawn")) {
        call(new FooActivity());
      }

      data.rate.add(1.0);
      delay(1, SECOND);
      waitUntil(data.isBetween(50.0, 100.0));

      mission.simpleData.downlinkData();

      data.rate.add(2.0);
      data.rate.add(data.rate.get());
      delay(10, SECOND);

      complexData.endImaging();

      mission.simpleData.a.deactivate();
      mission.simpleData.b.deactivate();
      delay(1, SECOND);

      mission.activitiesExecuted.add(1);
    }
  }
}