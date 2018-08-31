/**
 * Copyright 2018, by the California Institute of Technology. ALL RIGHTS RESERVED. United States Government Sponsorship acknowledged.
 * Any commercial use must be negotiated with the Office of Technology Transfer at the California Institute of Technology.
 * This software may be subject to U.S. export control laws and regulations.
 * By accepting this document, the user agrees to comply with all applicable U.S. export laws and regulations.
 * User has the responsibility to obtain export licenses, or other export authority as may be required
 * before exporting such information to foreign countries or providing access to foreign persons
 */

import { Injectable } from '@angular/core';
import { Actions, Effect, ofType } from '@ngrx/effects';
import { Action, Store } from '@ngrx/store';
import { Observable } from 'rxjs';
import { map, switchMap, withLatestFrom } from 'rxjs/operators';
import { LayoutActionTypes, Resize, ToggleRightPanel } from '../actions/layout';
import { RavenAppState } from '../raven-store';

import * as layoutActions from '../actions/layout';

@Injectable()
export class LayoutEffects {
  /**
   * Effect for Resize.
   */
  @Effect({ dispatch: false })
  resize$: Observable<Action> = this.actions$.pipe(
    ofType<Resize>(LayoutActionTypes.Resize),
    switchMap(() => {
      setTimeout(() => dispatchEvent(new Event('resize')));
      return [];
    })
  );

  /**
   * Effect for ToggleRightPanel.
   */
  @Effect()
  toggleRightPanel$: Observable<Action> = this.actions$.pipe(
    ofType<ToggleRightPanel>(LayoutActionTypes.ToggleRightPanel),
    withLatestFrom(this.store$),
    map(([, state]) => state.raven),
    map(raven => {
      if (raven.layout.showRightPanel && raven.layout.showLeftPanel) {
        return new layoutActions.UpdateLayout({ timelinePanelSize: 50 });
      } else {
        return new layoutActions.UpdateLayout({ timelinePanelSize: 75 });
      }
    })
  );

  constructor(
    private actions$: Actions,
    private store$: Store<RavenAppState>
  ) {}
}
