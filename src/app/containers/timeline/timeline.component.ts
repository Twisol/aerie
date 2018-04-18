/**
 * Copyright 2018, by the California Institute of Technology. ALL RIGHTS RESERVED. United States Government Sponsorship acknowledged.
 * Any commercial use must be negotiated with the Office of Technology Transfer at the California Institute of Technology.
 * This software may be subject to U.S. export control laws and regulations.
 * By accepting this document, the user agrees to comply with all applicable U.S. export laws and regulations.
 * User has the responsibility to obtain export licenses, or other export authority as may be required
 * before exporting such information to foreign countries or providing access to foreign persons
 */

import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  HostListener,
  OnDestroy,
} from '@angular/core';

import { MatTabChangeEvent } from '@angular/material';

import { Store } from '@ngrx/store';

import { takeUntil } from 'rxjs/operators';
import { Subject } from 'rxjs/Subject';

import * as fromConfig from './../../reducers/config';
import * as fromLayout from './../../reducers/layout';
import * as fromTimeline from './../../reducers/timeline';

import * as layoutActions from './../../actions/layout';
import * as sourceExplorerActions from './../../actions/source-explorer';
import * as timelineActions from './../../actions/timeline';

import {
  RavenBandLeftClick,
  RavenCompositeBand,
  RavenDefaultSettings,
  RavenPoint,
  RavenSettingsUpdate,
  RavenSortMessage,
  RavenSubBand,
  RavenTimeRange,
  StringTMap,
} from './../../shared/models';

import {
  toCompositeBand,
  toDividerBand,
} from './../../shared/util';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'app-timeline',
  styleUrls: ['./timeline.component.css'],
  templateUrl: './timeline.component.html',
})
export class TimelineComponent implements OnDestroy {
  // Config state.
  itarMessage: string;

  // Layout state.
  rightDrawerSelectedTabIndex: number | null;
  showActivityPointMetadata: boolean;
  showActivityPointParameters: boolean;
  showDetailsDrawer: boolean;
  showLeftDrawer: boolean;
  showRightDrawer: boolean;
  showSouthBandsDrawer: boolean;
  timelinePanelSize: number;

  // Timeline state.
  bands: RavenCompositeBand[];
  defaultSettings: RavenDefaultSettings;
  labelWidth: number;
  maxTimeRange: RavenTimeRange;
  selectedBandId: string;
  selectedPoint: RavenPoint | null;
  selectedSubBandId: string;
  viewTimeRange: RavenTimeRange;

  private ngUnsubscribe: Subject<{}> = new Subject();

  constructor(
    private changeDetector: ChangeDetectorRef,
    private store: Store<fromTimeline.TimelineState | fromConfig.ConfigState>,
  ) {
    // Config state.
    this.store.select(fromConfig.getItarMessage).pipe(
      takeUntil(this.ngUnsubscribe),
    ).subscribe(itarMessage => {
      this.itarMessage = itarMessage;

      // TODO. Find out how to remove this checking.
      this.changeDetector.markForCheck();
      setTimeout(() =>
        this.changeDetector.detectChanges(),
      );
    });

    // Layout state.
    this.store.select(fromLayout.getLayoutState).pipe(
      takeUntil(this.ngUnsubscribe),
    ).subscribe(state => {
      this.rightDrawerSelectedTabIndex = state.rightDrawerSelectedTabIndex;
      this.showActivityPointMetadata = state.showActivityPointMetadata;
      this.showActivityPointParameters = state.showActivityPointParameters;
      this.showDetailsDrawer = state.showDetailsDrawer;
      this.showLeftDrawer = state.showLeftDrawer;
      this.showRightDrawer = state.showRightDrawer;
      this.showSouthBandsDrawer = state.showSouthBandsDrawer;
      this.timelinePanelSize = state.timelinePanelSize;

      // TODO. Find out how to remove this checking.
      this.changeDetector.markForCheck();
      setTimeout(() => {
        this.changeDetector.detectChanges();
        dispatchEvent(new Event('resize')); // Trigger a window resize to make sure bands properly resize anytime our layout changes.
      });
    });

    // Timeline state.
    this.store.select(fromTimeline.getTimelineState).pipe(
      takeUntil(this.ngUnsubscribe),
    ).subscribe(state => {
      this.bands = state.bands;
      this.defaultSettings = state.defaultSettings;
      this.labelWidth = state.labelWidth;
      this.maxTimeRange = state.maxTimeRange;
      this.selectedBandId = state.selectedBandId;
      this.selectedPoint = state.selectedPoint;
      this.selectedSubBandId = state.selectedSubBandId;
      this.viewTimeRange = state.viewTimeRange;

      // TODO. Find out how to remove this checking.
      this.changeDetector.markForCheck();
      setTimeout(() =>
        this.changeDetector.detectChanges(),
      );
    });
  }

  ngOnDestroy() {
    this.ngUnsubscribe.next();
    this.ngUnsubscribe.complete();
  }

  /**
   * Event. Called when a `add-divider-band` event is fired from the raven-settings component.
   */
  onAddDividerBand(): void {
    this.store.dispatch(new timelineActions.AddBand(null, toCompositeBand(toDividerBand())));
  }

  /**
   * Event. Called when a band is left clicked in a raven-bands component.
   */
  onBandLeftClick(e: RavenBandLeftClick): void {
    this.store.dispatch(new timelineActions.SelectBand(e.bandId));

    if (e.subBandId && e.pointId) {
      this.store.dispatch(new timelineActions.SelectPoint(e.bandId, e.subBandId, e.pointId));
    }
  }

  /**
   * Event. Called when a `delete-sub-band` event is fired from the raven-settings component.
   */
  onDeleteSubBand(subBand: RavenSubBand): void {
    this.store.dispatch(new timelineActions.RemoveSubBand(subBand.id));
    this.store.dispatch(new sourceExplorerActions.SubBandIdRemove(subBand.sourceIds, subBand.id));
  }

  /**
   * Event. Called when a tab is changed.
   */
  onSelectedTabChange(e: MatTabChangeEvent) {
    this.store.dispatch(new layoutActions.UpdateLayout({ rightDrawerSelectedTabIndex: e.index }));
  }

  /**
   * Event. Called when a `new-sort` event is fired from raven-bands.
   */
  onSort(sort: StringTMap<RavenSortMessage>): void {
    this.store.dispatch(new timelineActions.SortBands(sort));
  }

  /**
   * Event. Called when a `toggle-show-activity-point-metadata` event is fired from a raven-activity-point.
   */
  onToggleShowActivityPointMetadata(show: boolean) {
    this.store.dispatch(new layoutActions.UpdateLayout({ showActivityPointMetadata: show }));
  }

  /**
   * Event. Called when a `toggle-show-activity-point-parameters` event is fired from a raven-activity-point.
   */
  onToggleShowActivityPointParameters(show: boolean) {
    this.store.dispatch(new layoutActions.UpdateLayout({ showActivityPointParameters: show }));
  }

  /**
   * Event. Called when an `update-band` event is fired from the raven-settings component.
   */
  onUpdateBand(e: RavenSettingsUpdate): void {
    if (e.bandId) {
      this.store.dispatch(new timelineActions.UpdateBand(e.bandId, e.update));
    }
  }

  /**
   * Event. Called when an `update-sub-band` event is fired from the raven-settings component.
   */
  onUpdateSubBand(e: RavenSettingsUpdate): void {
    if (e.bandId && e.subBandId) {
      this.store.dispatch(new timelineActions.UpdateSubBand(e.bandId, e.subBandId, e.update));
    }
  }

  /**
   * Event. Called when an `update-timeline` event is fired from the raven-settings component.
   */
  onUpdateTimeline(e: RavenSettingsUpdate): void {
    this.store.dispatch(new timelineActions.UpdateTimeline(e.update));
  }

  /**
   * Event. Called when catching an `update-view-time-range` event.
   */
  onUpdateViewTimeRange(viewTimeRange: RavenTimeRange): void {
    this.store.dispatch(new timelineActions.UpdateTimeline({ viewTimeRange }));
  }

  /**
   * Event. After a split pane drag, trigger a window resize event so the bands are properly sized.
   */
  onDragEnd(): void {
    dispatchEvent(new Event('resize'));
  }

  /**
   * Event. Called when a keyboard key is pressed.
   */
  @HostListener('window:keyup', ['$event'])
  keyEvent(event: KeyboardEvent) {
    if (event.keyCode === 49) { // 1 key.
      this.store.dispatch(new layoutActions.UpdateLayout({ rightDrawerSelectedTabIndex: 0 }));
    }

    if (event.keyCode === 50) { // 2 key.
      this.store.dispatch(new layoutActions.UpdateLayout({ rightDrawerSelectedTabIndex: 1 }));
    }
  }
}
