import {RouterModule, Routes} from '@angular/router';
import {NgModule} from '@angular/core';

import {PagesComponent} from './pages.component';
import {DashboardComponent} from './dashboard/dashboard.component';
import {NutsRepositoryComponent} from './nuts-repository/nuts-repository.component';
import {NutsComponentListComponent} from './nuts-components/nuts-component-list/nuts-component-list.component';
import {NutsWorkspaceComponent} from './nuts-workspace/nuts-workspace.component';
import {NutsComponentComponent} from './nuts-components/nuts-component/nuts-component.component';

const routes: Routes = [{
  path: '',
  component: PagesComponent,
  children: [
    {
      path: 'dashboard',
      component: DashboardComponent,
    },
    {
      path: 'workspace',
      component: NutsWorkspaceComponent,
    },
    {
      path: 'repository',
      component: NutsRepositoryComponent,
    },
    {
      path: 'component',
      component: NutsComponentComponent,
    },
    {
      path: 'component-list',
      component: NutsComponentListComponent,
    },
    {
      path: '',
      redirectTo: 'dashboard',
      pathMatch: 'full',
    },
  ],
}];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class PagesRoutingModule {
}
