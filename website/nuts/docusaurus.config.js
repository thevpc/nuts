module.exports = {
  title: 'Nuts',
  tagline: 'The Java Package Manager',
  url: 'https://thevpc.github.io/nuts',
  baseUrl: '/nuts/',
  onBrokenLinks: 'log',
  favicon: 'img/favicon.ico',
  organizationName: 'facebook', // Usually your GitHub org/user name.
  projectName: 'docusaurus', // Usually your repo name.
  customFields: {
    appApiVersion: '0.7.0',
    appCoreVersion: '0.7.0.0',
  },  
  themeConfig: {
    navbar: {
      title: 'Nuts Package Manager',
      logo: {
        alt: 'Nuts Package Manager',
        src: 'img/shuriken.png',
      },
      items: [
        {
          to: 'docs/',
          activeBasePath: 'docs',
          label: 'Docs',
          position: 'left',
        },
        {to: 'blog', label: 'Blog', position: 'left'},
        {
          href: 'https://github.com/thevpc/nuts',
          label: 'GitHub',
          position: 'right',
        },
      ],
    },
    footer: {
      style: 'dark',
      links: [
        {
          title: 'Docs',
          items: [
            {
              label: 'Style Guide',
              to: 'docs/',
            },
            {
              label: 'Second Doc',
              to: 'docs/doc2/',
            },
          ],
        },
        {
          title: 'Community',
          items: [
            {
              label: 'Stack Overflow',
              href: 'https://stackoverflow.com/questions/tagged/nuts',
            },
            {
              label: 'Discord',
              href: 'https://discordapp.com/invite/nuts',
            },
            {
              label: 'Twitter',
              href: 'https://twitter.com/nuts',
            },
          ],
        },
        {
          title: 'More',
          items: [
            {
              label: 'Blog',
              to: 'blog',
            },
            {
              label: 'GitHub',
              href: 'https://github.com/thevpc/nuts',
            },
          ],
        },
      ],
      copyright: `Copyright © ${new Date().getFullYear()} Nuts Package Manager Team.`,
    },
  },
  presets: [
    [
      '@docusaurus/preset-classic',
      {
        docs: {
          // It is recommended to set document id as docs home page (`docs/` path).
          homePageId: 'get-started/installation',
          sidebarPath: require.resolve('./sidebars.js'),
          // Please change this to your repo.
          editUrl:
            'https://github.com/facebook/docusaurus/edit/master/website/',
        },
        blog: {
          showReadingTime: true,
          // Please change this to your repo.
          editUrl:
            'https://github.com/facebook/docusaurus/edit/master/website/blog/',
        },
        theme: {
          customCss: require.resolve('./src/css/custom.css'),
        },
      },
    ],
  ],
};
