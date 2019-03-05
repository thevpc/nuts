import {Component, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {NutsRepositoryService} from './nuts-repository.service';
import {NutsRepository} from './nuts-repository';
import {LocalDataSource} from 'ng2-smart-table';
import {Ng2SmartTableComponent} from 'ng2-smart-table/ng2-smart-table.component';
import {Subscription} from 'rxjs';

@Component({
  selector: 'ngx-dashboard',
  templateUrl: './nuts-repository.component.html',
  styleUrls: [
    './nuts-repository.component.scss',
  ],
})
export class NutsRepositoryComponent implements OnInit, OnDestroy {

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
      type: {
        title: 'Type',
        type: 'string',
      },
      location: {
        title: 'Folder',
        type: 'string',
      },
    },
  };

  constructor(private repositoryService: NutsRepositoryService) {
    this.source.load(repositoryService.repositories);
  }

  ngOnInit() {
    this.subscription = this.repositoryService.repositoriesObservable.subscribe(repositories => {
      this.source.load(repositories);
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
      const data: NutsRepository = new NutsRepository();
      data.name = event.newData.name.length === 0 ? null : event.newData.name;
      data.type = event.newData.type.length === 0 ? null : event.newData.type;
      data.location = event.newData.location.length === 0 ? null : event.newData.location;
      this.repositoryService.add(data);
      event.confirm.resolve();
    } else {
      event.confirm.reject();
    }
  }

}
