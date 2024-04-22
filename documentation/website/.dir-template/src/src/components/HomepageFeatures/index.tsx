import React from 'react';
import clsx from 'clsx';
import Heading from '@theme/Heading';
import styles from './styles.module.css';

type FeatureItem = {
  title: string;
  Svg: React.ComponentType<React.ComponentProps<'svg'>>;
  description: JSX.Element;
};

const FeatureList: FeatureItem[] = [
  {
    title: 'No Fat Jar',
    Svg: require('@site/static/img/undraw_docusaurus_mountain.svg').default,
    description: (
      <>
          Developers, forget about big jars, uber jars and fats jars hassle.
          You do not need to bother about packaging your dependencies along with your application.

      </>
    ),
  },
  {
    title: 'Any application',
    Svg: require('@site/static/img/undraw_docusaurus_tree.svg').default,
    description: (
      <>
          install any java application with a simple `nuts install your-package`. dependencies are shared across all
          installed applications and you still can install multiple versions of the same application.

      </>
    ),
  },
  {
    title: 'Automate your Ops',
    Svg: require('@site/static/img/undraw_docusaurus_react.svg').default,
    description: (
      <>
          Take advantage of the Nuts toolbox that offers GNU binutils equivalent tools (bash,ls, cp, and more), and extend them to
          support json and xml outputs to help automation.
      </>
    ),
  },
];

function Feature({title, Svg, description}: FeatureItem) {
  return (
    <div className={clsx('col col--4')} style={{textAlign:"center"}}>
      <div className="text--center">
        <Svg className={styles.featureSvg} role="img" />
      </div>
      <div className="text--center padding-horiz--md">
        <Heading as="h3"  style={{textAlign:"center"}}>{title}</Heading>
        <p>{description}</p>
      </div>
    </div>
  );
}

export default function HomepageFeatures(): JSX.Element {
  return (
    <section className={styles.features}>
      <div className="container">
        <div className="row">
          {FeatureList.map((props, idx) => (
            <Feature key={idx} {...props} />
          ))}
        </div>
      </div>
    </section>
  );
}
