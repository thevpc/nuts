import {NgModule} from '@angular/core';


import {ThemeModule} from '../../@theme/theme.module';
import {NutsComponentListComponent} from './nuts-component-list/nuts-component-list.component';
import {NutsComponentComponent} from './nuts-component/nuts-component.component';
import {Ng2SmartTableModule} from 'ng2-smart-table';

@NgModule({
  imports: [
    ThemeModule,
    Ng2SmartTableModule,
  ],
  declarations: [
    NutsComponentListComponent,
    NutsComponentComponent,
  ],
})
export class NutsComponentModule {
}
