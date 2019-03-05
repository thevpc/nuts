import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {NutsRepository} from './nuts-repository';
import {Observable, Subject} from 'rxjs';
import {NutsWorkspaceService} from '../nuts-workspace/nuts-workspace.service';

@Injectable({
  providedIn: 'root',
})
export class NutsRepositoryService {

  public repositories: NutsRepository[] = [];
  public repositoriesSubject: Subject<NutsRepository[]>;
  public repositoriesObservable: Observable<NutsRepository[]>;

  constructor(private http: HttpClient,
              private workspaceService: NutsWorkspaceService) {
    this.repositoriesSubject = new Subject();
    this.repositoriesObservable = this.repositoriesSubject.asObservable();
    this.getAll();
  }

  public refreshData(repositories) {
    this.repositories = [...repositories];
    this.repositoriesSubject.next(repositories);
  }

  public getAll() {
    this.http
      .get<NutsRepository[]>(`/ws/repositories?workspace=${this.workspaceService.currentWorkspace}`)
      .subscribe(repositories => this.refreshData(repositories));
  }

  public add(data: NutsRepository) {
    const dataEncoded = encodeURI(JSON.stringify(data));
    this.http
      .get<NutsRepository[]>(`/ws/repositories/add?workspace=${this.workspaceService.currentWorkspace}&data=${dataEncoded}`)
      .subscribe(repositories => this.refreshData(repositories));
  }

  public delete(name: String) {
    this.http
      .get<NutsRepository[]>(`/ws/repositories/delete?workspace=${this.workspaceService.currentWorkspace}&name=${name}`)
      .subscribe(repositories => this.refreshData(repositories));
  }

}
