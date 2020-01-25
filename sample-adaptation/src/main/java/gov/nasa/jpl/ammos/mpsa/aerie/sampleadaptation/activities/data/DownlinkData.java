package gov.nasa.jpl.ammos.mpsa.aerie.sampleadaptation.activities.data;

import gov.nasa.jpl.ammos.mpsa.aerie.merlinsdk.activities.Activity;
import gov.nasa.jpl.ammos.mpsa.aerie.merlinsdk.activities.annotations.ActivityType;
import gov.nasa.jpl.ammos.mpsa.aerie.merlinsdk.activities.annotations.Parameter;
import gov.nasa.jpl.ammos.mpsa.aerie.merlinsdk.engine.SimulationContext;
import gov.nasa.jpl.ammos.mpsa.aerie.sampleadaptation.states.SampleMissionStates;

import java.util.ArrayList;
import java.util.List;


/**
 * DownlinkData an amount of data from a specific bin.
 *
 * @subsystem Data
 * @contact mkumar
 */

@ActivityType(name="DownlinkData", states=SampleMissionStates.class)
public class DownlinkData implements Activity<SampleMissionStates> {

    @Parameter
    public boolean downlinkAll = true;

    @Parameter
    public double downlinkAmount = 0.0;

    @Override
    public List<String> validateParameters() {
        final List<String> failures = new ArrayList<>();

        if (this.downlinkAmount <= 0  && !downlinkAll) {
            failures.add("downlinked amount must be positive");
        }

        return failures;
    }

    @Override
    public void modelEffects(SimulationContext ctx, SampleMissionStates states){
        if (downlinkAll){
            states.dataBin.downlink();
        }
        else {
            states.dataBin.downlink(downlinkAmount);
        }
    }


}