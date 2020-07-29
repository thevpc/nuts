import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {NutsComponent} from './nuts-component';
import {Observable, Subject} from 'rxjs';
import {Promise} from 'es6-promise';
import {NutsWorkspaceService} from '../nuts-workspace/nuts-workspace.service';

@Injectable({
  providedIn: 'root',
})
export class NutsComponentService {

  components: NutsComponent[] = [];
  componentsSubject: Subject<NutsComponent[]>;
  componentsObservable: Observable<NutsComponent[]>;
  searchData: NutsComponent[] = [];
  componentFormModel: NutsComponent = new NutsComponent();

  selectedComponent: NutsComponent = null;

  constructor(private http: HttpClient,
              private workspaceService: NutsWorkspaceService) {
    this.componentsSubject = new Subject();
    this.componentsObservable = this.componentsSubject.asObservable();
    this.getAll();
  }

  refreshData(components) {
    this.components = [...components];
    this.componentsSubject.next(components);
  }

  getAll() {
    this.http
      .get<NutsComponent[]>(`/ws/components?workspace=${this.workspaceService.currentWorkspace}`)
      .subscribe(components => this.refreshData(components), () => {
      });
  }

  getAllDependencies(data: NutsComponent, callback) {
    return this.http.get<NutsComponent[]>(`/ws/components/dependencies?workspace=${this.workspaceService.currentWorkspace}`
      + `&name=${data.name}&namespace=${data.namespace}&group=${data.group}&version=${data.version}&face=${data.face}`
      + `&os=${data.os}&osdist=${data.osdist}&scope=${data.scope}&alternative=${data.alternative}&arch=${data.arch}&all=true`)
      .subscribe(dependencies => {
        this.selectedComponent.allDependencies = [...dependencies];
        callback();
      });
  }

  search() {
    return Promise.all(this.components.filter(component => {
      return component.name.includes(this.componentFormModel.name)
        && component.namespace.includes(this.componentFormModel.namespace)
        && component.group.includes(this.componentFormModel.group)
        && component.version.includes(this.componentFormModel.version);
    })).then((res) => {
      this.searchData = [...res];
    });
  }

  setSelectedComponent(data: NutsComponent) {
    this.selectedComponent = data;
  }

  download(data: NutsComponent) {
    this.http.get<any>(`/ws/components/download?workspace=${this.workspaceService.currentWorkspace}`
      + `&name=${data.name}&namespace=${data.namespace}&group=${data.group}&version=${data.version}&face=${data.face}`
      + `&os=${data.os}&osdist=${data.osdist}&scope=${data.scope}&alternative=${data.alternative}`,
      {responseType: 'blob' as 'json'})
      .subscribe(response => {
        const dataType = response.type;
        const binaryData = [];
        binaryData.push(response);
        const downloadLink = document.createElement('a');
        downloadLink.href = window.URL.createObjectURL(new Blob(binaryData, {type: dataType}));
        downloadLink.setAttribute('download', data.name + '-' + data.version + '.zip');
        document.body.appendChild(downloadLink);
        downloadLink.click();
      }, () => {
      });
  }

  delete(data: NutsComponent, callback) {
    this.http.get<NutsComponent[]>(`/ws/components/delete?workspace=${this.workspaceService.currentWorkspace}`
      + `&name=${data.name}&namespace=${data.namespace}&group=${data.group}&version=${data.version}&face=${data.face}`
      + `&os=${data.os}&osdist=${data.osdist}&scope=${data.scope}&alternative=${data.alternative}`)
      .subscribe(components => {
        this.selectedComponent = null;
        this.refreshData(components);
        callback();
      }, () => {
      });
  }

  static cleanComponent(component: NutsComponent): NutsComponent {
    component.stringId = component.stringId || '';
    component.name = component.name || '';
    component.namespace = component.namespace || '';
    component.group = component.group || '';
    component.version = component.version || '';
    component.scope = component.scope || '';
    component.arch = component.arch || '';
    component.os = component.os || '';
    component.osdist = component.osdist || '';
    component.face = component.face || '';
    component.alternative = component.alternative || '';
    component.dependencies = component.dependencies || [];
    component.allDependencies = component.allDependencies || [];
    return component;
  }
}
