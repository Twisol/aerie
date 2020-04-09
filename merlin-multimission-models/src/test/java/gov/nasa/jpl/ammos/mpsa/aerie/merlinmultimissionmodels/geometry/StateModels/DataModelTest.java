package gov.nasa.jpl.ammos.mpsa.aerie.merlinmultimissionmodels.geometry.StateModels;

import gov.nasa.jpl.ammos.mpsa.aerie.merlinmultimissionmodels.data.StateModels.BinModel;
import gov.nasa.jpl.ammos.mpsa.aerie.merlinmultimissionmodels.data.StateModels.InstrumentModel;
import gov.nasa.jpl.ammos.mpsa.aerie.merlinsdk.activities.Activity;
import gov.nasa.jpl.ammos.mpsa.aerie.merlinsdk.engine.DynamicCell;
import gov.nasa.jpl.ammos.mpsa.aerie.merlinsdk.engine.SimulationEngine;
import gov.nasa.jpl.ammos.mpsa.aerie.merlinsdk.engine.SimulationInstant;
import gov.nasa.jpl.ammos.mpsa.aerie.merlinsdk.states.StateContainer;
import gov.nasa.jpl.ammos.mpsa.aerie.merlinsdk.states.interfaces.State;
import gov.nasa.jpl.ammos.mpsa.aerie.merlinsdk.time.TimeUnit;
import org.junit.Test;

import java.util.List;

import static gov.nasa.jpl.ammos.mpsa.aerie.merlinsdk.engine.SimulationEffects.delay;
import static gov.nasa.jpl.ammos.mpsa.aerie.merlinsdk.engine.SimulationEffects.spawn;

public class DataModelTest {

    public static final class DataModelStates implements StateContainer {
        public final InstrumentModel instrument_a_data_rate = new InstrumentModel("instrument 1", 0.0);
        public final InstrumentModel instrument_b_data_rate = new InstrumentModel("instrument 2", 0.0);
        public final InstrumentModel instrument_c_data_rate = new InstrumentModel("instrument 3", 0.0);
        public final InstrumentModel instrument_d_data_rate = new InstrumentModel("instrument 4", 0.0);
        public final InstrumentModel instrument_e_data_rate = new InstrumentModel("instrument 4", 0.0);
        public final InstrumentModel instrument_f_data_rate = new InstrumentModel("instrument 4", 0.0);
        public final InstrumentModel instrument_g_data_rate = new InstrumentModel("instrument 4", 0.0);
        public final BinModel bin_1 = new BinModel("Bin 1", instrument_a_data_rate, instrument_b_data_rate);
        public final BinModel bin_2 = new BinModel("Bin 2", instrument_c_data_rate, instrument_d_data_rate);
        public final BinModel bin_3 = new BinModel("Bin 3", instrument_e_data_rate, instrument_f_data_rate, instrument_g_data_rate);

        public List<State<?>> getStateList() {
            return List.of(instrument_a_data_rate, instrument_b_data_rate, instrument_c_data_rate, instrument_d_data_rate,
                    instrument_e_data_rate, instrument_f_data_rate, instrument_g_data_rate, bin_1, bin_2, bin_3);
        }
    }

    private static final DynamicCell<DataModelStates> statesRef = DynamicCell.inheritableCell();

    /* --------------------------------- DATA MODEL SAMPLE ACTIVITIES ------------------------------------*/
    public static class InitBinDataVolumes implements Activity {


        @Override
        public void modelEffects() {

            final var states = statesRef.get();
            states.bin_1.initializeBinData();
            states.bin_2.initializeBinData();
            states.bin_3.initializeBinData();
        }
    }

    public static class TurnInstrumentAOn implements Activity {

        @Override
        public void modelEffects() {

            final var states = statesRef.get();
            delay(1, TimeUnit.HOURS);

            InstrumentModel some_a_instrument = states.instrument_a_data_rate;
            some_a_instrument.set(10.0);
        }
    }

    public static class DownlinkData implements Activity {

        @Override
        public void modelEffects(){

            final var states = statesRef.get();
            delay(2, TimeUnit.HOURS);

            states.bin_1.downlink();

        }

    }


    /*----------------------------------------------------------------------------------------------------*/

    @Test
    public void bin_initialization() {
        final var simStart = SimulationInstant.ORIGIN;
        final var states = new DataModelStates();

        statesRef.setWithin(states, () -> {
            SimulationEngine.simulate(simStart, states.getStateList(), () -> {
                spawn(new InitBinDataVolumes());
            });
        });
    }

    @Test
    public void turn_instrument_on(){
        final var simStart = SimulationInstant.ORIGIN;
        final var states = new DataModelStates();

        statesRef.setWithin(states, () -> {
            SimulationEngine.simulate(simStart, states.getStateList(), () -> {
                spawn(new InitBinDataVolumes());
                spawn(new TurnInstrumentAOn());
            });
        });
    }

    @Test
    public void downlink_data(){
        final var simStart = SimulationInstant.ORIGIN;
        final var states = new DataModelStates();

        statesRef.setWithin(states, () -> {
            SimulationEngine.simulate(simStart, states.getStateList(), () -> {
                spawn(new InitBinDataVolumes());
                spawn(new TurnInstrumentAOn());
                spawn(new DownlinkData());
            });
        });
    }
}
