/**
 * Copyright 2018, by the California Institute of Technology. ALL RIGHTS RESERVED. United States Government Sponsorship acknowledged.
 * Any commercial use must be negotiated with the Office of Technology Transfer at the California Institute of Technology.
 * This software may be subject to U.S. export control laws and regulations.
 * By accepting this document, the user agrees to comply with all applicable U.S. export laws and regulations.
 * User has the responsibility to obtain export licenses, or other export authority as may be required
 * before exporting such information to foreign countries or providing access to foreign persons
 */

import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import {
  MatButtonModule,
  MatCardModule,
  MatDividerModule,
  MatFormFieldModule,
  MatIconModule,
  MatInputModule,
  MatMenuModule,
  MatProgressBarModule,
  MatSidenavModule,
  MatTabsModule,
  MatToolbarModule,
  MatTooltipModule,
} from '@angular/material';
import { AngularSplitModule } from 'angular-split';
import { NestAppHeaderModule } from '../../../shared/components';
import { SeqCommandListModule, SeqCommandLoaderModule } from '../../components';
import { SeqCommandFormEditorModule } from '../../components/seq-command-form-editor/seq-command-form-editor.module';
import { EditorWorkspaceModule } from '../editor-workspace/editor-workspace.module';
import { FileExplorerModule } from '../file-explorer/file-explorer.module';
import { FalconAppComponent } from './falcon-app.component';

@NgModule({
  declarations: [FalconAppComponent],
  exports: [FalconAppComponent],
  imports: [
    AngularSplitModule.forChild(),
    CommonModule,
    FormsModule,
    MatButtonModule,
    MatCardModule,
    MatDividerModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatMenuModule,
    MatProgressBarModule,
    MatSidenavModule,
    MatTabsModule,
    MatToolbarModule,
    MatTooltipModule,
    NestAppHeaderModule,
    EditorWorkspaceModule,
    FileExplorerModule,
    SeqCommandFormEditorModule,
    SeqCommandListModule,
    SeqCommandLoaderModule,
  ],
})
export class FalconAppModule {}