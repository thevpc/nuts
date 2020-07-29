import {NgModule} from '@angular/core';


import {ThemeModule} from '../../@theme/theme.module';
import {NutsWorkspaceComponent} from './nuts-workspace.component';
import {Ng2SmartTableModule} from 'ng2-smart-table';

@NgModule({
  imports: [
    ThemeModule,
    Ng2SmartTableModule,
  ],
  declarations: [
    NutsWorkspaceComponent,
  ],
})
export class NutsWorkspaceModule {
}
