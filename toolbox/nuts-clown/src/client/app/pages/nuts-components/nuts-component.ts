export class NutsComponent {
  stringId: string;
  name: string;
  namespace: string;
  group: string;
  version: string;
  scope: string;
  arch: string;
  os: string;
  osdist: string;
  face: string;
  alternative: string;
  dependencies: NutsComponent[];
  allDependencies: NutsComponent[];

  constructor() {
    this.stringId = '';
    this.name = '';
    this.namespace = '';
    this.group = '';
    this.version = '';
    this.scope = '';
    this.arch = '';
    this.os = '';
    this.osdist = '';
    this.face = '';
    this.alternative = '';
    this.dependencies = [];
  }

}
