import {NbMenuItem} from '@nebular/theme';
import {icons} from 'eva-icons';

export const MENU_ITEMS: NbMenuItem[] = [
  {
    title: 'Dashboard',
    icon: 'nb-home',
    link: '/pages/dashboard',
    home: true,
  },
  {
    title: 'Workspace',
    icon: 'nb-list',
    link: '/pages/workspace',
  },
  {
    title: 'Repositories',
    icon: 'nb-list',
    link: '/pages/repository',
  },
  {
    title: 'Components',
    icon: 'nb-list',
    link: '/pages/component-list',
  },
  {
    title: 'FEATURES',
    group: true,
  },
  {
    title: 'Auth',
    icon: 'nb-locked',
    children: [
      {
        title: 'Login',
        link: '/auth/login',
      },
      {
        title: 'Register',
        link: '/auth/register',
      },
      {
        title: 'Request Password',
        link: '/auth/request-password',
      },
      {
        title: 'Reset Password',
        link: '/auth/reset-password',
      },
    ],
  },
];
