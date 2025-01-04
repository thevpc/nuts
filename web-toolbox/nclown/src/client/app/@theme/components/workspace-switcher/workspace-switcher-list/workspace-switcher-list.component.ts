import {Component, Input, OnDestroy, OnInit} from '@angular/core';
import {NbPopoverDirective} from '@nebular/theme';
import {NWorkspace} from '../../../../pages/nuts-workspace/n-workspace';
import {NutsWorkspaceService} from '../../../../pages/nuts-workspace/nuts-workspace.service';
import {Subscription} from 'rxjs';

@Component({
  selector: 'ngx-workspace-switcher-list',
  template: `
    <ul class="workspace-switcher-list">
      <li class="workspace-switcher-item"
          *ngFor="let workspace of workspaces"
          (click)="onToggleWorkspace(workspace.name)">
        <span
          [style.color]="workspaceService.currentWorkspace === workspace.name?'#73a1ff':'black'"
          style="font-size: 15px">
          {{ workspace.name }}
        </span>
      </li>
    </ul>
  `,
  styleUrls: ['./workspace-switcher-list.component.scss'],
})
export class WorkspaceSwitcherListComponent implements OnInit, OnDestroy {

  @Input() popover: NbPopoverDirective;

  subscription: Subscription;

  workspace: NWorkspace;

  workspaces = [];

  constructor(public workspaceService: NutsWorkspaceService) {
    this.workspaces = [...this.workspaceService.workspaces];
  }

  ngOnInit() {
    this.subscription = this.workspaceService.workspacesObservable.subscribe(workspaces => {
      this.workspaces = [...workspaces];
    });
  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }

  onToggleWorkspace(themeKey: string) {
    this.workspaceService.changeWorkspace(themeKey);
    this.popover.hide();
  }
}
