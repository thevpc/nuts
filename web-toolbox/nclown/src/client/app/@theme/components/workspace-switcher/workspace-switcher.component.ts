import {Component, Input, ViewChild} from '@angular/core';
import {NbPopoverDirective} from '@nebular/theme';

import {WorkspaceSwitcherListComponent} from './workspace-switcher-list/workspace-switcher-list.component';
import {NWorkspace} from '../../../pages/nuts-workspace/n-workspace';

@Component({
  selector: 'ngx-workspace-switcher',
  templateUrl: './workspace-switcher.component.html',
  styleUrls: ['./workspace-switcher.component.scss'],
})
export class WorkspaceSwitcherComponent {
  @ViewChild(NbPopoverDirective) popover: NbPopoverDirective;

  @Input() showTitle: boolean = true;

  switcherListComponent = WorkspaceSwitcherListComponent;
  workspace: NWorkspace;
}
