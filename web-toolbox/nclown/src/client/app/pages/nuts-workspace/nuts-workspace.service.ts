import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {NWorkspace} from './n-workspace';
import {Observable, Subject} from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class NutsWorkspaceService {

  public currentWorkspace = 'default-workspace';

  public workspaces: NWorkspace[] = [];
  public workspacesSubject: Subject<NWorkspace[]>;
  public workspacesObservable: Observable<NWorkspace[]>;

  constructor(private http: HttpClient) {
    this.workspacesSubject = new Subject();
    this.workspacesObservable = this.workspacesSubject.asObservable();
    this.getAll();
  }


  public refreshData(workspaces) {
    this.workspaces = [...workspaces];
    this.workspacesSubject.next(workspaces);
  }

  public getAll() {
    this.http
      .get<NWorkspace[]>(`/ws/workspaces`)
      .subscribe(workspaces => this.refreshData(workspaces));
  }

  public add(name: String) {
    this.http
      .get<NWorkspace[]>(`/ws/workspaces/add?name=${name}`)
      .subscribe(workspaces => this.refreshData(workspaces));
  }

  public delete(name: String) {
    this.http
      .get<NWorkspace[]>(`/ws/workspaces/delete?name=${name}`)
      .subscribe(workspaces => this.refreshData(workspaces));
  }

  public changeWorkspace(workspace: string) {
    this.currentWorkspace = workspace;
  }

  public switchEnabled(name: string, value: boolean) {
    this.http
      .get<NWorkspace[]>(`/ws/workspaces/onOff?name=${name}&value=${value}`)
      .subscribe(workspaces => this.refreshData(workspaces));
  }
}
