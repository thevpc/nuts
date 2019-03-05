import {NgModule} from '@angular/core';

import {PagesComponent} from './pages.component';
import {DashboardModule} from './dashboard/dashboard.module';
import {PagesRoutingModule} from './pages-routing.module';
import {ThemeModule} from '../@theme/theme.module';
import {MiscellaneousModule} from './miscellaneous/miscellaneous.module';
import {NutsRepositoryModule} from './nuts-repository/nuts-repository.module';
import {NutsComponentModule} from './nuts-components/nuts-component.module';
import {NutsWorkspaceModule} from './nuts-workspace/nuts-workspace.module';

const PAGES_COMPONENTS = [
  PagesComponent,
];

@NgModule({
  imports: [
    PagesRoutingModule,
    ThemeModule,
    DashboardModule,
    NutsRepositoryModule,
    NutsComponentModule,
    NutsWorkspaceModule,
    MiscellaneousModule,
  ],
  declarations: [
    ...PAGES_COMPONENTS,
  ],
})
export class PagesModule {
}
