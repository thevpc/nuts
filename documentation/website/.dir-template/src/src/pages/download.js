// THIS FILE IS GENERATED
import React from 'react';
import clsx from 'clsx';
import Layout from '@theme/Layout';
import Link from '@docusaurus/Link';
import useDocusaurusContext from '@docusaurus/useDocusaurusContext';
import useBaseUrl from '@docusaurus/useBaseUrl';
import styles from './styles.module.css';
        const features = [
        {
        title: <>Download Installer</>,
imageUrl: 'img/run.png',
description: (
<>
<p>
Download a graphical installer that will make it simple to install nuts package manager.
After downloading the installer, just double click the file and follow the installation wizard instructions.
A valid <img src="img/java.png" alt=""  width="16"/> java 1.8+ runtime is required.
</p>
<ul>
<li><img src="img/java.png" alt=""  width="32"/> &nbsp;
<Link
                    className={clsx(
                        'button button--secondary--lg b2 ',
                        styles.getStarted,
                        )}
                    href={'https://thevpc.net/nuts-installer.jar'}
                    target="_blank"
                    >
Portable Installer
</Link>
</li>
<li><img src="img/linux.png" alt=""  width="32" /> &nbsp;
<Link
                    className={clsx(
                        'button button--secondary--lg b2 ',
                        styles.getStarted,
                        )}
                    href={'https://thevpc.net/nuts-installer-linux-x64'}
                    target="_blank"
                    >
Linux x64 Installer
</Link>
</li>
<li><img src="img/windows.png" alt=""  width="32"/> &nbsp;
<Link
                    className={clsx(
                        'button button--secondary--lg b2 ',
                        styles.getStarted,
                        )}
                    href={'https://thevpc.net/nuts-installer-windows-x64.exe'}
                    target="_blank"
                    >
Windows x64 Installer
</Link>
</li>
<li><img src="img/macos.png" alt=""  width="32"/> &nbsp;
<Link
                    className={clsx(
                        'button button--secondary--lg b2 ',
                        styles.getStarted,
                        )}
                    href={'https://thevpc.net/nuts-installer-macos-x64'}
                    target="_blank"
                    disabled
                    >
MacOS x64 Installer
</Link>
</li>
</ul>
</>
),
},
{
                title: <>Download Raw Jar Package</>,
imageUrl: 'img/jar2.png',
description: (
<>
<p>
Download raw jar file to perform installation using your favourite shell.
After downloading the installer, follow the documentation to install the package manager.
Use 'Portable' version for production and 'Preview' for all other cases.
A valid <img src="img/java.png" alt=""  width="16"/> java 1.8+ runtime is required.
</p>
<ul>
<li><img src="img/java.png" alt=""  width="32"/> &nbsp;
<Link
                    className={clsx(
                        'button button--secondary--lg b2 ',
                        styles.getStarted,
                        )}
                    href={'https://thevpc.net/nuts-stable.jar'}
                    target="_blank"
                    >

Stable ${{stableApiVersion}} Jar
</Link>
</li>
<li><img src="img/java.png" alt=""  width="32"/> &nbsp;

    <Link
                        className={clsx(
                            'button button--secondary b3',
                            styles.getStarted,
                            )}
                        href={'https://thevpc.net/nuts-preview.jar'}
                        target="_blank"
                        >

                    Preview ${{latestApiVersion}} Jar
                    </Link>

</li>
</ul>
</>
),
},
,
{
                title: <>Install Manually</>,
imageUrl: 'img/terminal.png',
description: (
<>
<p>
Use one commandline to download and install Nuts package manager with the help of cUrl command.
Use 'Portable' version for production and 'Preview' version for all other cases.
A valid <img src="img/java.png" alt=""  width="16"/> java 1.8+ runtime is required.
</p>
<Link
                    className={clsx(
                        'button button--secondary--lg b2 ',
                        styles.getStarted,
                        )}
                    >Stable</Link>
<pre>
curl -sOL https://repo.maven.apache.org/maven2/net/thevpc/nuts/nuts/${{stableApiVersion}}/nuts-${{stableApiVersion}}.jar -o nuts.jar && java -jar nuts.jar -Zy
</pre>

<Link
                    className={clsx(
                            'button button--secondary b3',
                        styles.getStarted,
                        )}
                    >Preview</Link>
<pre>
curl -sOL https://thevpc.net/nuts-preview.jar -o nuts.jar && java -jar nuts.jar -Zy
</pre>

</>
),
},

];

function Feature({imageUrl, title, description}) {
                const imgUrl = useBaseUrl(imageUrl);
                return (
                        <div className={clsx('col col--4', styles.feature)}>
    {imgUrl && (
                        <div className="text--center">
        <img className={styles.featureImage} src={imgUrl} alt={title} />
    </div>
                        )}
    <h3>{title}</h3>
    <p>{description}</p>
</div>
                        );
}

function Download() {
                const context = useDocusaurusContext();
                const {siteConfig = {}} = context;
                return (
                        <Layout
    title={`${siteConfig.title}` + ', the Java Package Manager'}
    description="Description will go into a meta tag in <head />">
    <header className={clsx('hero hero--primary', styles.heroBanner)}>
        <div className="container">
            <h1 className="hero__title"><img src="img/nuts-icon.png" alt=""  width="64"/>{siteConfig.title}</h1>
            <p className="hero__subtitle">{siteConfig.tagline}</p>
            <img src="https://thevpc.net/nuts/images/pixel.gif?q=nuts-gsite" alt="" />
            Choose your download format...
        </div>
    </header>
    <main>
        {features && features.length > 0 && (
                        <section className={styles.features}>
            <div className="container">
                <div className="row">
                    {features.map((props, idx) => (
                                <Feature key={idx} {...props} />
                                ))}
                </div>
            </div>
        </section>
                        )}
    </main>
</Layout>
                        );
}

export default Download;
