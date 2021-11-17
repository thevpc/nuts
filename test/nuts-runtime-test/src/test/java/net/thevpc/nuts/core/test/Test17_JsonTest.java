/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 *
 * Copyright [2020] [thevpc] Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.core.test;

import net.thevpc.nuts.*;
import net.thevpc.nuts.core.test.utils.*;
import org.junit.jupiter.api.*;

import java.io.IOException;

/**
 *
 * @author thevpc
 */
public class Test17_JsonTest {

    @BeforeAll
    public static void setUpClass() throws IOException {
        TestUtils.println("####### RUNNING TEST @ " + TestUtils.getCallerClassSimpleName());
    }

    @Test
    public void test1() throws Exception {
        NutsSession session = TestUtils.openNewTestWorkspace(
                "-byZSKk"
        );

//        NutsString e = ws.elem()
//                .setContentType(NutsContentType.JSON)
//                .setValue("a@@@")
//                .setCompact(true)
//                .setSession(session)
//                .setNtf(false)
//                .format();

//        TestUtils.println(NutsTexts.of(session).forPlain("a@@@"));
//        TestUtils.println(NutsTexts.of(session).forPlain("a@@@").filteredText());

        TestUtils.println(NutsTexts.of(session).ofPlain("a##@"));
        TestUtils.println(NutsTexts.of(session).ofPlain("a##@").filteredText());

    }

    @Test
    public void test2()  {
        NutsSession ws = TestUtils.openNewTestWorkspace(
                "-byZSKk"
        );
        NutsElement a = NutsElements.of(ws).json()
                .parse(
                        "{\n" +
                                "    title: 'Some Title',\n" +
                                "    tagline: 'The official coding standards for main programming languages',\n" +
                                "    url: 'https://something.there',\n" +
                                "    baseUrl: '/',\n" +
                                "    onBrokenLinks: 'throw',\n" +
                                "    favicon: 'img/favicon.ico',\n" +
                                "    organizationName: 'ksa-nca', // Usually your GitHub org/user name.\n" +
                                "    projectName: 'ksa-nca-coding-standards', // Usually your repo name.\n" +
                                "    themeConfig: {\n" +
                                "        navbar: {\n" +
                                "            title: 'Any Title',\n" +
                                "            logo: {\n" +
                                "                alt: 'Logo',\n" +
                                "                src: 'img/logo-small.jpeg',\n" +
                                "            },\n" +
                                "            items: [\n" +
                                "                {\n" +
                                "                    to: 'docs/',\n" +
                                "                    activeBasePath: 'docs',\n" +
                                "                    label: 'Docs',\n" +
                                "                    position: 'left',\n" +
                                "                }/*,\n" +
                                "                 {to: 'blog', label: 'Blog', position: 'left'},\n" +
                                "                 {\n" +
                                "                 href: 'https://hello.temp',\n" +
                                "                 label: 'Jello',\n" +
                                "                 position: 'right',\n" +
                                "                 },*/\n" +
                                "            ],\n" +
                                "        },\n" +
                                "        footer: {\n" +
                                "            style: 'dark',\n" +
                                "            links: [\n" +
                                "                {\n" +
                                "                    title: 'Docs',\n" +
                                "                    items: [\n" +
                                "                        {\n" +
                                "                            label: 'Guide',\n" +
                                "                            to: 'docs/',\n" +
                                "                        }\n" +
                                "                    ],\n" +
                                "                },\n" +
                                "//                {\n" +
                                "//                    title: 'Community',\n" +
                                "//                    items: [\n" +
                                "//                        {\n" +
                                "//                            label: 'Stack Overflow',\n" +
                                "//                            href: 'https://stackoverflow.com/questions/tagged/docusaurus',\n" +
                                "//                        },\n" +
                                "//                        {\n" +
                                "//                            label: 'Discord',\n" +
                                "//                            href: 'https://discordapp.com/invite/docusaurus',\n" +
                                "//                        },\n" +
                                "//                        {\n" +
                                "//                            label: 'Twitter',\n" +
                                "//                            href: 'https://twitter.com/docusaurus',\n" +
                                "//                        },\n" +
                                "//                    ],\n" +
                                "//                },\n" +
                                "//                {\n" +
                                "//                    title: 'More',\n" +
                                "//                    items: [\n" +
                                "//                        /*{\n" +
                                "//                         label: 'Blog',\n" +
                                "//                         to: 'blog',\n" +
                                "//                         },*/\n" +
                                "//                        {\n" +
                                "//                            label: 'GitHub',\n" +
                                "//                            href: 'https://github.com/facebook/docusaurus',\n" +
                                "//                        },\n" +
                                "//                    ],\n" +
                                "//                },\n" +
                                "            ],\n" +
                                "            copyright: `Copyright © ${new Date().getFullYear()} 2020.`,\n" +
                                "        },\n" +
                                "        prism: {\n" +
                                "            additionalLanguages: ['powershell', 'csharp', 'scala'],\n" +
                                "        }\n" +
                                "    },\n" +
                                "    presets: [\n" +
                                "        [\n" +
                                "            '@docusaurus/preset-classic',\n" +
                                "            {\n" +
                                "                docs: {\n" +
                                "                    // It is recommended to set document id as docs home page (`docs/` path).\n" +
                                "                    homePageId: 'IntroAboutTheDocument',\n" +
                                "                    sidebarPath: require.resolve('./sidebars.js'),\n" +
                                "                    // Please change this to your repo.\n" +
                                "//                    editUrl:\n" +
                                "//                            'https://github.com/facebook/docusaurus/edit/master/website/',\n" +
                                "                },\n" +
                                "                blog: {\n" +
                                "                    showReadingTime: true,\n" +
                                "//                    // Please change this to your repo.\n" +
                                "//                    editUrl:\n" +
                                "//                            'https://github.com/facebook/docusaurus/edit/master/website/blog/',\n" +
                                "                },\n" +
                                "                theme: {\n" +
                                "                    customCss: require.resolve('./src/css/custom.css'),\n" +
                                "                },\n" +
                                "            },\n" +
                                "        ],\n" +
                                "    ],\n" +
                                "    customFields: {\n" +
                                "        docusaurus:{\n" +
                                "            generateSidebarMenu:false\n" +
                                "        },\n" +
                                "        asciidoctor: {\n" +
                                "            path: 'pdf',\n" +
                                "            pdf: {\n" +
                                "                output:'pdf/',\n" +
                                "                headers: [\n" +
                                "                    ':source-highlighter: pygments',\n" +
                                "                    ':icons: font',\n" +
                                "                    ':icon-set: pf',\n" +
                                "                    ':doctype: book',\n" +
                                "                    ':revnumber: v1.4',\n" +
                                "                    ':revdate: 2020-11-11',\n" +
                                "                    ':revremark: RESTRICTED - INTERNAL',\n" +
                                "                    ':toc:',\n" +
                                "                    ':toclevels: 3',\n" +
                                "                    ':appendix-caption: Appx',\n" +
                                "                ],\n" +
                                "                command: {\n" +
                                "                    bin: 'asciidoctor-pdf.ruby2.7',\n" +
                                "                    args: [\n" +
                                "                        '-a',\n" +
                                "                        'pdf-themesdir=${asciidoctor.baseDir}/resources/themes',\n" +
                                "                        '-a',\n" +
                                "                        'pdf-theme=nca',\n" +
                                "                        '-a',\n" +
                                "                        'pdf-fontsdir=${asciidoctor.baseDir}/resources/fonts/;GEM_FONTS_DIR',\n" +
                                "                    ]\n" +
                                "                }\n" +
                                "            }\n" +
                                "        }\n" +
                                "    },\n" +
                                "}"
                );
        TestUtils.println(a);
//        NutsString e = ws.elem()
//                .setContentType(NutsContentType.JSON)
//                .setValue("a@@@")
//                .setCompact(true)
//                .setSession(session)
//                .setNtf(false)
//                .format();

//        TestUtils.println(NutsTexts.of(session).forPlain("a@@@"));
//        TestUtils.println(NutsTexts.of(session).forPlain("a@@@").filteredText());

        TestUtils.println(NutsTexts.of(ws).ofPlain("a##@"));
        TestUtils.println(NutsTexts.of(ws).ofPlain("a##@").filteredText());

    }

    @AfterAll
    public static void tearUpClass() throws IOException {
        //CoreIOUtils.delete(null,new File(baseFolder));
    }

    @BeforeEach
    public void startup() throws IOException {
//        Assumptions.assumeTrue(NutsOsFamily.getCurrent() == NutsOsFamily.LINUX);
        TestUtils.unsetNutsSystemProperties();
    }

    @AfterEach
    public void cleanup() {
        TestUtils.unsetNutsSystemProperties();
    }
}
