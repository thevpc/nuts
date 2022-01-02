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

  source: LocalDataSource = new LocalDataSource();

  directDependenciesToggle: boolean = true;

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

  constructor(private componentService: NutsComponentService,
              private router: Router,
              private location: Location) {
    if (componentService.selectedComponent === null) {
      router.navigateByUrl('pages/component-list');
      return;
    }
    this.component = componentService.selectedComponent;
    this.source.load(this.component.dependencies);
  }

  ngOnInit() {
  }

  ngOnDestroy() {
  }

  returnBack() {
    this.componentService.selectedComponent = null;
    this.location.back();
  }

  allDependencies() {
    if (this.directDependenciesToggle) {
      this.source.load([]);
      this.componentService.getAllDependencies(this.component, () => {
        this.component = this.componentService.selectedComponent;
        this.source.load(this.component.allDependencies);
      });
      this.directDependenciesToggle = false;
    }
  }

  directDependencies() {
    if (!this.directDependenciesToggle) {
      this.source.load(this.component.dependencies);
      this.directDependenciesToggle = true;
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
