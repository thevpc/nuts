import {Component, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {Router} from '@angular/router';
import {Location} from '@angular/common';
import {NutsComponentService} from '../nuts-component.service';
import {LocalDataSource} from 'ng2-smart-table';
import {Ng2SmartTableComponent} from 'ng2-smart-table/ng2-smart-table.component';
import {NutsComponent} from '../nuts-component';
import {Subscription} from 'rxjs';

@Component({
  selector: 'ngx-nuts-component-list',
  templateUrl: './nuts-component.component.html',
  styleUrls: [
    './nuts-component.component.scss',
  ],
})
export class NutsComponentComponent implements OnInit, OnDestroy {

  @ViewChild('repositoryTable')
  repositoryTable: Ng2SmartTableComponent;

  component: NutsComponent;
  dependencies: any;

  subscription: Subscription = new Subscription();
  source: LocalDataSource = new LocalDataSource();

  settings = {
    hideSubHeader: true,
    actions: {
      add: false,
      edit: false,
      delete: false,
    },
    columns: {
      name: {
        title: 'Name',
        type: 'string',
        filter: false,
      },
      group: {
        title: 'Group',
        type: 'string',
        filter: false,
      },
      scope: {
        title: 'Scope',
        type: 'string',
        filter: false,
      },
      version: {
        title: 'Version',
        type: 'string',
        filter: false,
      },
    },
  };

  constructor(public componentService: NutsComponentService,
              private router: Router,
              private location: Location) {
    if (componentService.selectedComponent === null) {
      router.navigateByUrl('pages/component-list');
      return;
    }
    this.component = componentService.selectedComponent;
    this.componentService.getDependencies(this.component);
  }

  ngOnInit() {
    this.subscription = this.componentService.selectedComponentDependenciesObservable.subscribe(dependencies => {
      this.dependencies = dependencies;
      this.source.load(dependencies);
    });
  }

  ngOnDestroy() {
    if (this.subscription) {
      this.subscription.unsubscribe();
    }
  }

  returnBack() {
    this.componentService.selectedComponent = null;
    this.location.back();
  }

  allDependencies() {
    if (this.componentService.directDependenciesToggle) {
      this.componentService.directDependenciesToggle = false;
      this.source.load(this.dependencies);
    }
  }

  directDependencies() {
    if (!this.componentService.directDependenciesToggle) {
      this.componentService.directDependenciesToggle = true;
      this.source.load(this.dependencies);
    }
  }

  download() {
    this.componentService.download(this.component);
  }

  deleteFromDisk() {
    if (window.confirm('Are you sure you want to delete?')) {
      this.componentService.delete(this.component, () => {
        this.location.back();
      });
    }
  }
}
