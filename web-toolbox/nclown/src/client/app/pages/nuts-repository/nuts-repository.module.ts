import {NgModule} from '@angular/core';


import {ThemeModule} from '../../@theme/theme.module';
import {NutsRepositoryComponent} from './nuts-repository.component';
import {Ng2SmartTableModule} from 'ng2-smart-table';

@NgModule({
  imports: [
    ThemeModule,
    Ng2SmartTableModule,
  ],
  declarations: [
    NutsRepositoryComponent,
  ],
})
export class NutsRepositoryModule {
}
