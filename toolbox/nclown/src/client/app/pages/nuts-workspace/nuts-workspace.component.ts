import {Component, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {NutsWorkspaceService} from './nuts-workspace.service';
import {LocalDataSource} from 'ng2-smart-table';
import {Ng2SmartTableComponent} from 'ng2-smart-table/ng2-smart-table.component';
import {Subscription} from 'rxjs';
import {WorkspaceEnableSwitcherComponent} from "../../@theme/components";

@Component({
  selector: 'ngx-workspace',
  templateUrl: './nuts-workspace.component.html',
  styleUrls: [
    './nuts-workspace.component.scss',
  ],
})
export class NutsWorkspaceComponent implements OnInit, OnDestroy {

  @ViewChild('repositoryTable')
  repositoryTable: Ng2SmartTableComponent;

  subscription: Subscription;

  source: LocalDataSource = new LocalDataSource();

  settings = {
    actions: {
      edit: false,
    },
    add: {
      addButtonContent: '<i class="nb-plus"></i>',
      createButtonContent: '<i class="nb-checkmark"></i>',
      cancelButtonContent: '<i class="nb-close"></i>',
      confirmCreate: true,
    },
    delete: {
      deleteButtonContent: '<i class="nb-trash"></i>',
      confirmDelete: true,
    },
    columns: {
      name: {
        title: 'Name',
        type: 'string',
      },
      location: {
        title: 'Folder',
        type: 'string',
      },
      enabled: {
        title: 'Enabled',
        type: 'custom',
        renderComponent: WorkspaceEnableSwitcherComponent,
        onComponentInitFunction(instance) {
          instance.save.subscribe(row => {
            alert(`${row.name} saved!`)
          });
        }
      },
    },
  };

  constructor(private repositoryService: NutsWorkspaceService) {
    this.source.load(repositoryService.workspaces);
  }

  ngOnInit() {
    this.subscription = this.repositoryService.workspacesObservable.subscribe(workspaces => {
      this.source.load(workspaces);
    });
  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }

  deleteRepository(event) {
    if (window.confirm('Are you sure you want to delete?')) {
      this.repositoryService.delete(event.data.name);
      event.confirm.resolve();
    } else {
      event.confirm.reject();
    }
  }

  addRepository(event) {
    if (window.confirm('Are you sure you want to create?')) {
      this.repositoryService.add(event.newData.name);
      event.confirm.resolve();
    } else {
      event.confirm.reject();
    }
  }

}
