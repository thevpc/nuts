module.exports = {
  title: 'Nuts',
  tagline: 'The Java Package Manager',
  url: 'https://thevpc.github.io/nuts',
  baseUrl: '/nuts/',
  onBrokenLinks: 'log',
  favicon: 'img/favicon.ico',
  organizationName: 'thevpc',
  projectName: 'nuts',
  themeConfig: {
    navbar: {
      title: 'Nuts Package Manager',
      logo: {
        alt: 'Nuts Package Manager',
        src: 'img/nuts-icon.png',
      },
      items: [
        {
          to: 'docs',
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
              label: 'Documentation',
              to: 'docs/',
            },
            {
              label: 'FAQ',
              to: 'docs/info/faq',
            },
            {
              label: 'Change Log',
              to: 'docs/info/changelog',
            },
            {
              label: 'PDF Documentation',
              to: 'pdf/nuts-documentation.pdf',
              target: "_blank"
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
              label: 'Issues Tracker',
              href: 'https://github.com/thevpc/nuts/issues',
            },
//            {
//              label: 'Discord',
//              href: 'https://discordapp.com/invite/nuts',
//            },
//            {
//              label: 'Twitter',
//              href: 'https://twitter.com/nuts',
//            },
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
          homePageId: 'intro/introduction',
          sidebarPath: require.resolve('./sidebars.js'),
          // Please change this to your repo.
          editUrl:
            'https://github.com/thevpc/nuts/edit/master/website/',
        },
        blog: {
          showReadingTime: true,
          // Please change this to your repo.
          editUrl:
            'https://github.com/thevpc/nuts/edit/master/website/blog/',
        },
        theme: {
          customCss: require.resolve('./src/css/custom.css'),
        },
      },
    ],
  ]
};
