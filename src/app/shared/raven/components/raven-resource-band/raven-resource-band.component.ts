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
  Component,
  EventEmitter,
  Input,
  OnChanges,
  OnDestroy,
  OnInit,
  Output,
  SimpleChanges,
} from '@angular/core';

import {
  RavenEpoch,
  RavenResourcePoint,
} from './../../../models';

import {
  getInterpolatedTooltipText,
  getTooltipText,
} from './../../../util/tooltip';

import {
  dateToTimestring,
  toDuration,
} from './../../../util/time';

import {
  colorHexToRgbArray,
} from './../../../util';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'raven-resource-band',
  styleUrls: ['./raven-resource-band.component.css'],
  templateUrl: './raven-resource-band.component.html',
})
export class RavenResourceBandComponent implements OnChanges, OnDestroy, OnInit {
  @Input() autoTickValues: boolean;
  @Input() color: string;
  @Input() ctlTimeAxis: any;
  @Input() ctlViewTimeAxis: any;
  @Input() dayCode: string;
  @Input() earthSecToEpochSec: number;
  @Input() epoch: RavenEpoch | null;
  @Input() fill: boolean;
  @Input() fillColor: string;
  @Input() font: string;
  @Input() height: number;
  @Input() heightPadding: number;
  @Input() icon: string;
  @Input() id: string;
  @Input() interpolation: string;
  @Input() isDuration: boolean;
  @Input() isTime: boolean;
  @Input() label: string;
  @Input() labelFont: string;
  @Input() labelFontSize: number;
  @Input() labelPin: string;
  @Input() labelUnit: string;
  @Input() name: string;
  @Input() points: RavenResourcePoint[];
  @Input() rescale: boolean;
  @Input() showIcon: boolean;
  @Input() showLabelPin: boolean;
  @Input() showLabelUnit: boolean;
  @Input() type: string;

  @Output() addSubBand: EventEmitter<any> = new EventEmitter<any>();
  @Output() removeSubBand: EventEmitter<string> = new EventEmitter<string>();
  @Output() updateInterpolation: EventEmitter<any> = new EventEmitter<any>();
  @Output() updateIntervals: EventEmitter<any> = new EventEmitter<any>();
  @Output() updateSubBand: EventEmitter<any> = new EventEmitter<any>();

  ctlResourceBand: any;

  ngOnChanges(changes: SimpleChanges) {
    // Auto Tick Values.
    if (changes.autoTickValues && !changes.autoTickValues.firstChange) {
      this.updateSubBand.emit({ subBandId: this.id, prop: 'autoTickValues', value: this.autoTickValues });
    }

    // Color.
    if (changes.color && !changes.color.firstChange && this.ctlResourceBand) {
      this.color = changes.color.currentValue;
      // update color of intervals
      const { intervals } = this.getIntervals();
      this.ctlResourceBand.setIntervals(intervals);
      this.updateSubBand.emit({ subBandId: this.id, subObject: 'painter', prop: 'color', value: this.color });
    }

    // Fill.
    if (changes.fill && !changes.fill.firstChange) {
      this.fill = changes.fill.currentValue;
      this.updateSubBand.emit({ subBandId: this.id, subObject: 'painter', prop: 'fill', value: this.fill });
    }

    // Fill Color.
    if (changes.fillColor && !changes.fillColor.firstChange) {
      this.fillColor = changes.fillColor.currentValue;
      this.updateSubBand.emit({ subBandId: this.id, subObject: 'painter', prop: 'fillColor', value: colorHexToRgbArray(this.fillColor) });
    }

    // Font.
    // TODO.

    // Interpolation.
    if (changes.interpolation && !changes.interpolation.firstChange) {
      this.updateInterpolation.emit({ subBandId: this.id, interpolation: this.interpolation });
    }

    // Label.
    if (changes.label && !changes.label.firstChange) {
      this.updateSubBand.emit({ subBandId: this.id, prop: 'label', value: this.getLabel() });
    }

    // Label Font Size.
    if (changes.labelFontSize && !changes.labelFontSize.firstChange) {
      this.updateSubBand.emit({ subBandId: this.id, subObject: 'decorator', prop: 'labelFontSize', value: this.labelFontSize });
    }

    // Label Pin.
    if (changes.labelPin && !changes.labelPin.firstChange) {
      this.updateSubBand.emit({ subBandId: this.id, prop: 'label', value: this.getLabel() });
    }

    // Label Unit.
    if (changes.labelUnit && !changes.labelUnit.firstChange) {
      this.updateSubBand.emit({ subBandId: this.id, prop: 'label', value: this.getLabel() });
    }

    // Points.
    if (changes.points && !changes.points.firstChange) {
      this.updateIntervals.emit({ subBandId: this.id, ...this.getIntervals() });
    }

    // Show Icon.
    if (changes.showIcon && !changes.showIcon.firstChange) {
      this.updateSubBand.emit({ subBandId: this.id, subObject: 'painter', prop: 'showIcon', value: this.showIcon });
    }

    // Show Label Pin.
    if (changes.showLabelPin && !changes.showLabelPin.firstChange) {
      this.updateSubBand.emit({ subBandId: this.id, prop: 'label', value: this.getLabel() });
    }

    // Show Label Unit.
    if (changes.showLabelUnit && !changes.showLabelUnit.firstChange) {
      this.updateSubBand.emit({ subBandId: this.id, prop: 'label', value: this.getLabel() });
    }
  }

  ngOnInit() {
    // Create Resource Band.
    this.ctlResourceBand = new (window as any).ResourceBand({
      autoScale: (window as any).ResourceBand.VISIBLE_INTERVALS,
      autoTickValues: this.autoTickValues,
      height: this.height,
      heightPadding: this.heightPadding,
      hideTicks: false,
      icon: this.icon,
      id: this.id,
      interpolation: this.interpolation,
      intervals: [],
      label: this.getLabel(),
      labelColor: colorHexToRgbArray(this.color),
      labelFont: this.labelFont,
      labelFontSize: this.labelFontSize,
      name: this.name,
      onFormatTickValue: this.onFormatTickValue.bind(this),
      onGetInterpolatedTooltipText: this.onGetInterpolatedTooltipText.bind(this),
      painter: new (window as any).ResourcePainter({
        color: colorHexToRgbArray(this.color),
        fill: this.fill,
        fillColor: colorHexToRgbArray(this.fillColor),
        icon: this.icon,
        showIcon: this.showIcon,
      }),
      rescale: this.rescale,
      tickValues: [],
      timeAxis: this.ctlTimeAxis,
      viewTimeAxis: this.ctlViewTimeAxis,
    });

    // Create Intervals.
    const { intervals, intervalsById } = this.getIntervals();

    this.ctlResourceBand.setIntervals(intervals);
    this.ctlResourceBand.intervalsById = intervalsById;
    this.ctlResourceBand.type = 'resource';
    this.ctlResourceBand.isDuration = this.isDuration;
    this.ctlResourceBand.isTime = this.isTime;

    // Send the newly created resource band to the parent composite band so it can be added.
    // All subsequent updates should be made to the parent composite sub-band via events.
    this.addSubBand.emit(this.ctlResourceBand);
  }

  ngOnDestroy() {
    this.removeSubBand.emit(this.id);
  }

  /**
   * Helper. Creates CTL intervals for a resource band.
   */
  getIntervals() {
    const intervals = [];
    const intervalsById = {};

    for (let i = 0, l = this.points.length; i < l; ++i) {
      const point = this.points[i];

      const interval = new (window as any).DrawableInterval({
        color: colorHexToRgbArray(this.color),
        end: point.start,
        endValue: point.value,
        icon: this.icon,
        id: point.id,
        onGetTooltipText: this.onGetTooltipText.bind(this),
        opacity: 0.9,
        properties: {
          Value: point.value,
        },
        start: point.start,
        startValue: point.value,
      });

      // Set the sub-band ID and unique ID separately since they are not a DrawableInterval prop.
      interval.subBandId = this.id;
      interval.uniqueId = point.uniqueId;

      intervals.push(interval);
      intervalsById[interval.uniqueId] = interval;
    }

    intervals.sort((window as any).DrawableInterval.earlyStartEarlyEnd);

    return {
      intervals,
      intervalsById,
    };
  }

  /**
   * Helper. Builds a label from the base label, pins, and units.
   */
  getLabel() {
    let labelPin = '';
    let labelUnit = '';

    if (this.showLabelPin && this.labelPin !== '') {
      labelPin = ` (${this.labelPin})`;
    }

    if (this.showLabelUnit && this.labelUnit !== '') {
      labelUnit = ` (${this.labelUnit})`;
    }

    return `${this.label}${labelPin}${labelUnit}`;
  }

  /**
   * CTL Event. Called when we want to format a tick value.
   */
  onFormatTickValue(tick: any, showMilliseconds: boolean) {
    if (this.isDuration) {
      // Format duration resources ticks.
      return toDuration(tick, false);
    } else if (this.isTime) {
      // Format time resources ticks.
      return dateToTimestring(new Date(tick * 1000), showMilliseconds);
    }

    return tick;
  }

  /**
   * CTL Event. Called when we want to get tooltip text for an interpolated interval.
   */
  onGetInterpolatedTooltipText(e: Event, obj: any) {
    return getInterpolatedTooltipText(obj, this.earthSecToEpochSec, this.epoch, this.dayCode);
  }

  /**
   * CTL Event. Called when we want to get tooltip text.
   */
  onGetTooltipText(e: Event, obj: any) {
    return getTooltipText(obj, this.earthSecToEpochSec, this.epoch, this.dayCode);
  }
}
