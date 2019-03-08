import {
  AfterViewInit,
  Component,
  ElementRef,
  EventEmitter,
  HostListener,
  Input,
  OnChanges,
  OnInit,
  Output,
  SimpleChanges,
} from '@angular/core';

import { RavenGuidePoint, RavenTimeRange } from '../../models';

@Component({
  selector: 'raven-guide-band',
  styles: [
    `
      :host {
        display: block;
      }
    `,
  ],
  template: ``,
})
export class RavenGuideBandComponent
  implements AfterViewInit, OnChanges, OnInit {
  @Input()
  guides: number[];

  @Input()
  labelFontSize = 9;

  @Input()
  labelWidth = 150;

  @Input()
  lastClickTime: number | null;

  @Input()
  maxTimeRange: RavenTimeRange = { end: 0, start: 0 };

  @Input()
  showLastClick = true;

  @Input()
  sideMenuDivSize: number;

  @Input()
  viewTimeRange: RavenTimeRange = { end: 0, start: 0 };

  @Output()
  toggleGuide: EventEmitter<RavenGuidePoint> = new EventEmitter<
    RavenGuidePoint
  >();

  ctlTimeAxis: any;
  ctlViewTimeAxis: any;
  ctlGuideBand: any;

  constructor(public elementRef: ElementRef) {}

  ngAfterViewInit() {
    this.resize();
  }

  ngOnChanges(changes: SimpleChanges) {
    // Guides.
    if (changes.guides && !changes.guides.firstChange) {
      // Update Intervals.
      this.ctlGuideBand.setIntervals(this.createEvents());
      this.redraw();
    }

    // Last click time.
    if (changes.lastClickTime && !changes.lastClickTime.firstChange) {
      this.ctlTimeAxis.lastClickTime = this.showLastClick
        ? this.lastClickTime
        : null;
      this.redraw();
    }

    // Max Time Range.
    if (changes.maxTimeRange && !changes.maxTimeRange.firstChange) {
      const currentMaxTimeRange = changes.maxTimeRange.currentValue;
      const previousMaxTimeRange = changes.maxTimeRange.previousValue;

      // Make sure we don't redraw or update times unless the times actually changed.
      if (
        previousMaxTimeRange.start !== currentMaxTimeRange.start ||
        previousMaxTimeRange.end !== currentMaxTimeRange.end
      ) {
        this.ctlTimeAxis.updateTimes(
          currentMaxTimeRange.start,
          currentMaxTimeRange.end,
        );
        this.redraw();
      }
    }

    // ViewTimeRange.
    if (changes.viewTimeRange && !changes.viewTimeRange.firstChange) {
      const currentViewTimeRange = changes.viewTimeRange.currentValue;
      const previousViewTimeRange = changes.viewTimeRange.previousValue;

      console.log('guide band detect timeRange change');
      // Make sure we don't redraw or update times unless the times actually changed.
      if (
        previousViewTimeRange.start !== currentViewTimeRange.start ||
        previousViewTimeRange.end !== currentViewTimeRange.end
      ) {
        this.ctlViewTimeAxis.updateTimes(
          currentViewTimeRange.start,
          currentViewTimeRange.end,
        );
        this.redraw();
      }
    }
  }

  ngOnInit() {
    console.log('in createGuideBand');
    this.ctlTimeAxis = new (window as any).TimeAxis({ end: 0, start: 0 });
    this.ctlViewTimeAxis = new (window as any).TimeAxis({ end: 0, start: 0 });
    // Create Guide Band.
    this.ctlGuideBand = new (window as any).ActivityBand({
      activityHeight: 8,
      autoFit: null,
      borderWidth: 0,
      height: 15,
      id: '__guide_band',
      intervals: [],
      label: '',
      name: 'guide_band',
      onLeftClick: this.onLeftClick.bind(this),
      showLabel: false,
      style: 3,
      timeAxis: this.ctlTimeAxis,
      trimLabel: false,
      viewTimeAxis: this.ctlViewTimeAxis,
    });

    console.log('this.maxTimeRange: ' + JSON.stringify(this.maxTimeRange));
    console.log('this.viewTimeRange: ' + JSON.stringify(this.viewTimeRange));
    this.ctlTimeAxis.updateTimes(
      this.maxTimeRange.start,
      this.maxTimeRange.end,
    );
    this.ctlViewTimeAxis.updateTimes(
      this.viewTimeRange.start,
      this.viewTimeRange.end,
    );

    // Create Intervals.
    this.ctlGuideBand.setIntervals(this.createEvents()); // This resets interpolation in CTL so we must re-set it on the next line.
    // this.ctlGuideBand.type = 'activity';
    this.elementRef.nativeElement.appendChild(this.ctlGuideBand.div);
  }

  /**
   * Global Event. Called on window resize.
   */
  @HostListener('window:resize', ['$event'])
  onResize(e: Event): void {
    this.resize();
  }

  /**
   * CTL Event. Called when user left-click on this band.
   */
  onLeftClick(e: MouseEvent, ctlData: any) {
    this.toggleGuide.emit({
      guideTime: ctlData.time,
      timePerPixel: this.ctlTimeAxis.getTimePerPixel(),
    });
  }
  /**
   * Helper. Call when a composite-band should be resized.
   * Note that this triggers a redraw.
   */
  resize() {
    console.log('guide band resize');
    this.updateTimeAxisXCoordinates();
    this.redraw();
  }

  /**
   * Helper. Recalculates x-coordinates of the band based on the label width.
   */
  updateTimeAxisXCoordinates() {
    const offsetWidth = this.elementRef.nativeElement.offsetWidth;
    this.ctlTimeAxis.updateXCoordinates(
      this.labelWidth,
      offsetWidth + this.sideMenuDivSize,
    );
    this.ctlViewTimeAxis.updateXCoordinates(
      this.labelWidth,
      offsetWidth + this.sideMenuDivSize,
    );
  }

  /**
   * Helper. Call when a composite-band should be redrawn.
   */
  redraw() {
    this.ctlGuideBand.revalidate();
    this.ctlGuideBand.repaint();
  }

  createEvents() {
    const intervals = [];

    for (let i = 0, l = this.guides.length; i < l; ++i) {
      const point = this.guides[i];

      const interval = new (window as any).DrawableInterval({
        color: [124, 191, 183],
        end: point,
        icon: 'circleCross',
        id: `guide_${i}`,
        label: '',
        opacity: 1.0,
        start: point,
      });

      // Set the sub-band ID and unique ID separately since they are not a DrawableInterval prop.
      interval.subBandId = '__guide_band';
      interval.uniqueId = `guide_${i}`;
      intervals.push(interval);
    }

    intervals.sort((window as any).DrawableInterval.earlyStartEarlyEnd);

    return intervals;
  }
}
