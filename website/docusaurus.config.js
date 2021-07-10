module.exports = {
  title: 'Nuts, the Java Package Manager',
  tagline: 'The Java Package Manager',
  url: 'https://thevpc.github.io/nuts',
  baseUrl: '/nuts/',
  onBrokenLinks: 'log',
  favicon: 'img/favicon.ico',
  organizationName: 'facebook', // Usually your GitHub org/user name.
  projectName: 'docusaurus', // Usually your repo name.
  themeConfig: {
    navbar: {
      title: 'Nuts Package Manager',
      logo: {
        alt: 'Nuts Package Manager',
        src: 'img/shuriken.png',
      },
      items: [
        {
          to: 'docs/intro/introduction',
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
              to: 'docs/intro/introduction',
            },
            {
              label: 'FAQ',
              to: 'docs/info/faq',
            },
            {
              label: 'Change Log',
              to: 'docs/info/changelog',
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
              label: 'Features Request',
              href: 'https://github.com/thevpc/nuts/issues',
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
    customFields: {
        copyBuildPath:'../docs',
        docusaurus: {
            generateSidebarMenu: true
        },
        asciidoctor: {
            path: 'asciidoctor',
            pdf: {
                headers: [
                    ':source-highlighter: pygments',
                    ':icons: font',
                    ':icon-set: pf',
                    ':doctype: book',
                    ':revnumber: v${apiVersion}',
                    ':revdate: 2021-07-01',
                    ':toc:',
                    ':toclevels: 3',
                    ':appendix-caption: Appx',
                ],
                command: {
                    bin: 'asciidoctor-pdf.ruby2.7',
                    args: [
                        '-a', 'pdf-themesdir=${asciidoctor.baseDir}/resources/themes',
                        '-a', 'pdf-theme=custom',
                        '-a', 'pdf-fontsdir=${asciidoctor.baseDir}/resources/fonts/;GEM_FONTS_DIR',
                    ]
                },
                output:'static/pdf/',
            }
        }
    },
};
