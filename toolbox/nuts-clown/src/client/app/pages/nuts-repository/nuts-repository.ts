export class NutsRepository {
  name: string;
  type: string;
  location: string;
  enabled: boolean;
  speed: number;
  parents: NutsRepository;
  mirrors: NutsRepository[];
}
