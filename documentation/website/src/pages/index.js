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
        title: <>No Fat Jar</>,
imageUrl: 'img/slim-jar.png',
description: (
<>
Developers, forget about big jars, uber jars and fats jars hassle.
You do not need to bother about packaging your dependencies along with your application.
</>
),
},
{
                title: <>Any application, All applications</>,
imageUrl: 'img/package.png',
description: (
<>
install any java application with a simple `nuts install your-package`. dependencies are shared across all
installed applications and you still can install multiple versions of the same application.
</>
),
},
{
                title: <>Automate your operations</>,
imageUrl: 'img/gear.png',
description: (
<>
Take advantage of the Nuts toolbox that offers GNU binutils equivalent tools (bash,ls, cp, and more), and extend them to 
support json and xml outputs to help automation.
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

function Home() {
                const context = useDocusaurusContext();
                const {siteConfig = {}} = context;
                return (
                        <Layout
    title={`${siteConfig.title}` + ', the Java Package Manager'}
    description="Description will go into a meta tag in <head />">
    <header className={clsx('hero hero--primary', styles.heroBanner)}>
        <div className="container">
            <h1 className="hero__title">{siteConfig.title}</h1>
            <p className="hero__subtitle">{siteConfig.tagline}</p>
            <img src="https://thevpc.net/nuts/images/pixel.gif?q=nuts-gsite" alt="" />
            <div className={styles.buttons}>
                <Link
                    className={clsx(
                        'button b1 button--secondary b1 ',
                        styles.getStarted,
                        )}
                    to={useBaseUrl('docs/')}>
                Get Started
                </Link>
                &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                <Link
                    className={clsx(
                        'button button--secondary--lg b2 ',
                        styles.getStarted,
                        )}
                    href={'https://thevpc.net/nuts-stable.jar'}
                    target="_blank"
                    >

                Download stable 0.8.2
                </Link>
                &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                <Link
                    className={clsx(
                        'button button--secondary b3',
                        styles.getStarted,
                        )}
                    href={'https://thevpc.net/nuts-preview.jar'}
                    target="_blank"
                    >

                Download preview 0.8.3-rc1
                </Link>
            </div>
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

export default Home;
