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
                    ':revdate: 2020-09-10',
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
