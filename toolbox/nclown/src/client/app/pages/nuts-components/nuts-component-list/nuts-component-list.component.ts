import {Component, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {Router} from '@angular/router';
import {NutsComponentService} from '../nuts-component.service';
import {LocalDataSource} from 'ng2-smart-table';
import {Ng2SmartTableComponent} from 'ng2-smart-table/ng2-smart-table.component';
import {NutsComponent} from '../nuts-component';
import {Subscription} from 'rxjs';


@Component({
  selector: 'ngx-nuts-component-list',
  templateUrl: './nuts-component-list.component.html',
  styleUrls: [
    './nuts-component-list.component.scss',
  ],
})
export class NutsComponentListComponent implements OnInit, OnDestroy {

  @ViewChild('repositoryTable')
  repositoryTable: Ng2SmartTableComponent;

  subscription: Subscription;
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
      namespace: {
        title: 'Namespace',
        type: 'string',
        filter: false,
      },
      group: {
        title: 'Group',
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
              private router: Router) {
    this.source.load(componentService.searchData);
  }

  ngOnInit() {
    this.search();
    this.subscription = this.componentService.componentsObservable.subscribe(components => {
      this.search();
    });
  }

  ngOnDestroy() {
    if (this.subscription) {
      this.subscription.unsubscribe();
    }
  }

  onUserRowSelect(event) {
    this.componentService.setSelectedComponent(NutsComponentService.cleanComponent(event.data));
    this.router.navigateByUrl('pages/component');
  }

  search() {
    this.componentService.search().then(() => {
      this.source.load(this.componentService.searchData);
    });
  }

  clearSearchForm() {
    this.componentService.componentFormModel = new NutsComponent();
    this.search();
  }
}

