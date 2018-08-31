/**
 * Copyright 2018, by the California Institute of Technology. ALL RIGHTS RESERVED. United States Government Sponsorship acknowledged.
 * Any commercial use must be negotiated with the Office of Technology Transfer at the California Institute of Technology.
 * This software may be subject to U.S. export control laws and regulations.
 * By accepting this document, the user agrees to comply with all applicable U.S. export laws and regulations.
 * User has the responsibility to obtain export licenses, or other export authority as may be required
 * before exporting such information to foreign countries or providing access to foreign persons
 */

import { ConfigState } from '../../../config';
import { UpdateDefaultBandSettings } from '../actions/config';
import { initialState, reducer } from './config';

describe('config reducer', () => {
  let configState: ConfigState;

  beforeEach(() => {
    configState = initialState;
  });

  it('handle default', () => {
    expect(configState).toEqual(initialState);
  });

  it('handle UpdateDefaultBandSettings activityLayout', () => {
    configState = reducer(
      configState,
      new UpdateDefaultBandSettings({ activityLayout: 2 })
    );
    expect(configState).toEqual({
      ...initialState,
      raven: {
        ...initialState.raven,
        defaultBandSettings: {
          ...initialState.raven.defaultBandSettings,
          activityLayout: 2,
        },
      },
    });
  });

  it('handle UpdateDefaultBandSettings icon', () => {
    configState = reducer(
      configState,
      new UpdateDefaultBandSettings({ icon: 'triangle' })
    );
    expect(configState).toEqual({
      ...initialState,
      raven: {
        ...initialState.raven,
        defaultBandSettings: {
          ...initialState.raven.defaultBandSettings,
          icon: 'triangle',
        },
      },
    });
  });

  it('handle UpdateDefaultBandSettings labelFont', () => {
    configState = reducer(
      configState,
      new UpdateDefaultBandSettings({ labelFont: 'Courier' })
    );
    expect(configState).toEqual({
      ...initialState,
      raven: {
        ...initialState.raven,
        defaultBandSettings: {
          ...initialState.raven.defaultBandSettings,
          labelFont: 'Courier',
        },
      },
    });
  });

  it('handle UpdateDefaultBandSettings labelFontSize', () => {
    configState = reducer(
      configState,
      new UpdateDefaultBandSettings({ labelFontSize: 11 })
    );
    expect(configState).toEqual({
      ...initialState,
      raven: {
        ...initialState.raven,
        defaultBandSettings: {
          ...initialState.raven.defaultBandSettings,
          labelFontSize: 11,
        },
      },
    });
  });

  it('handle UpdateDefaultBandSettings labelWidth', () => {
    configState = reducer(
      configState,
      new UpdateDefaultBandSettings({ labelWidth: 50 })
    );
    expect(configState).toEqual({
      ...initialState,
      raven: {
        ...initialState.raven,
        defaultBandSettings: {
          ...initialState.raven.defaultBandSettings,
          labelWidth: 50,
        },
      },
    });
  });

  it('handle UpdateDefaultBandSettings resourceColor', () => {
    configState = reducer(
      configState,
      new UpdateDefaultBandSettings({ resourceColor: '#00ff00' })
    );
    expect(configState).toEqual({
      ...initialState,
      raven: {
        ...initialState.raven,
        defaultBandSettings: {
          ...initialState.raven.defaultBandSettings,
          resourceColor: '#00ff00',
        },
      },
    });
  });

  it('handle UpdateDefaultBandSettings resourceFillColor', () => {
    configState = reducer(
      configState,
      new UpdateDefaultBandSettings({ resourceFillColor: '#ff0000' })
    );
    expect(configState).toEqual({
      ...initialState,
      raven: {
        ...initialState.raven,
        defaultBandSettings: {
          ...initialState.raven.defaultBandSettings,
          resourceFillColor: '#ff0000',
        },
      },
    });
  });

  it('handle UpdateDefaultBandSettings showTimeCursor', () => {
    configState = reducer(
      configState,
      new UpdateDefaultBandSettings({ showTimeCursor: true })
    );
    expect(configState).toEqual({
      ...initialState,
      raven: {
        ...initialState.raven,
        defaultBandSettings: {
          ...initialState.raven.defaultBandSettings,
          showTimeCursor: true,
        },
      },
    });
  });

  it('handle UpdateDefaultBandSettings showTooltip', () => {
    configState = reducer(
      configState,
      new UpdateDefaultBandSettings({ showTooltip: true })
    );
    expect(configState).toEqual({
      ...initialState,
      raven: {
        ...initialState.raven,
        defaultBandSettings: {
          ...initialState.raven.defaultBandSettings,
          showTooltip: true,
        },
      },
    });
  });
});
