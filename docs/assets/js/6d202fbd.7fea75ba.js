"use strict";(self.webpackChunknuts=self.webpackChunknuts||[]).push([[7077],{2357:(e,n,t)=>{t.r(n),t.d(n,{assets:()=>c,contentTitle:()=>a,default:()=>l,frontMatter:()=>r,metadata:()=>o,toc:()=>u});var s=t(4848),i=t(8453);const r={id:"nutsDescriptorIntegration",title:"Nuts Descriptor Integration",sidebar_label:"Nuts Descriptor Integration"},a=void 0,o={id:"dev/nutsDescriptorIntegration",title:"Nuts Descriptor Integration",description:"Nuts Descriptor Integration",source:"@site/docs/dev/nuts-maven-integration.md",sourceDirName:"dev",slug:"/dev/nutsDescriptorIntegration",permalink:"/nuts/docs/dev/nutsDescriptorIntegration",draft:!1,unlisted:!1,editUrl:"https://github.com/facebook/docusaurus/tree/main/packages/create-docusaurus/templates/shared/docs/dev/nuts-maven-integration.md",tags:[],version:"current",frontMatter:{id:"nutsDescriptorIntegration",title:"Nuts Descriptor Integration",sidebar_label:"Nuts Descriptor Integration"},sidebar:"tutorialSidebar",previous:{title:"Your first Application using nuts",permalink:"/nuts/docs/dev/nutsApp"},next:{title:"Nuts Path",permalink:"/nuts/docs/dev/nutsPath"}},c={},u=[{value:"Nuts Descriptor Integration",id:"nuts-descriptor-integration",level:2},{value:"Nuts and Maven",id:"nuts-and-maven",level:3},{value:"Nuts and Java MANIFEST.MF",id:"nuts-and-java-manifestmf",level:3},{value:"Nuts and Java 9 (jdeps)",id:"nuts-and-java-9-jdeps",level:3},{value:"Nuts and Gradle (TODO)",id:"nuts-and-gradle-todo",level:3}];function d(e){const n={a:"a",code:"code",h2:"h2",h3:"h3",li:"li",p:"p",pre:"pre",ul:"ul",...(0,i.R)(),...e.components};return(0,s.jsxs)(s.Fragment,{children:[(0,s.jsx)(n.h2,{id:"nuts-descriptor-integration",children:"Nuts Descriptor Integration"}),"\n",(0,s.jsxs)(n.ul,{children:["\n",(0,s.jsx)(n.li,{children:"Seamless integration"}),"\n",(0,s.jsx)(n.li,{children:"Maven Solver"}),"\n"]}),"\n",(0,s.jsx)(n.h3,{id:"nuts-and-maven",children:"Nuts and Maven"}),"\n",(0,s.jsxs)(n.ul,{children:["\n",(0,s.jsxs)(n.li,{children:[(0,s.jsx)(n.code,{children:"nuts.executable=<true|false>"})," : when true the artifact is an executable (contains main class)"]}),"\n",(0,s.jsxs)(n.li,{children:[(0,s.jsx)(n.code,{children:"nuts.application=<true|false>"})," : when true the artifact is an executable application (implements NutsApplication)"]}),"\n",(0,s.jsxs)(n.li,{children:[(0,s.jsx)(n.code,{children:"nuts.gui=<true|false>"})," : when true the requires a gui environment to execute"]}),"\n",(0,s.jsxs)(n.li,{children:[(0,s.jsx)(n.code,{children:"nuts.term=<true|false>"})," : when true the artifact is a command line executable"]}),"\n",(0,s.jsxs)(n.li,{children:[(0,s.jsx)(n.code,{children:"nuts.icons=<icon-path-string-array>"})," : an array (separated with ',' or new lines) of icon paths (url in the NPath\nformat)"]}),"\n",(0,s.jsxs)(n.li,{children:[(0,s.jsx)(n.code,{children:"nuts.genericName=<genericNameString>"})," : a generic name for the application like 'Text Editor'","\n",(0,s.jsxs)(n.ul,{children:["\n",(0,s.jsxs)(n.li,{children:[(0,s.jsx)(n.code,{children:"nuts.categories=<categories-string-array>"})," : an array (separated with ',' or new lines) of categories. the\ncategories should be compatible with Free Desktop Menu\nspecification (",(0,s.jsx)(n.a,{href:"https://specifications.freedesktop.org/menu-spec/menu-spec-1.0.html",children:"https://specifications.freedesktop.org/menu-spec/menu-spec-1.0.html"}),")"]}),"\n"]}),"\n"]}),"\n",(0,s.jsxs)(n.li,{children:[(0,s.jsx)(n.code,{children:"nuts.<os>-os-dependencies"})," : list (':',';' or line separated) of short ids of dependencies that shall be appended to\nclasspath only if running on the given os (see NutsOsFamily). This is a ways more simple than using the builtin '\nprofile' concept of Maven (which is of course supported as well)"]}),"\n",(0,s.jsxs)(n.li,{children:[(0,s.jsx)(n.code,{children:"nuts.<arch>-arch-dependencies"})," : list (':',';' or line separated) of short ids of dependencies that shall be appended\nto classpath only if running on the given hardware architecture (see NutsArchFamily). This is a ways more simple than\nusing the builtin 'profile' concept of Maven (which is of course supported as well)"]}),"\n",(0,s.jsxs)(n.li,{children:[(0,s.jsx)(n.code,{children:"nuts.<os>-os-<arch>-arch-dependencies"})," : list (':',';' or line separated) of short ids of dependencies that shall be\nappended to classpath only if running on the given hardware architecture and os family"]}),"\n"]}),"\n",(0,s.jsx)(n.pre,{children:(0,s.jsx)(n.code,{className:"language-xml",children:'<?xml version="1.0" encoding="UTF-8"?>\n<project xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns="http://maven.apache.org/POM/4.0.0"\n         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">\n    <modelVersion>4.0.0</modelVersion>\n    <groupId>your-group</groupId>\n    <artifactId>your-project</artifactId>\n    <version>1.2.3</version>\n    <packaging>jar</packaging>\n    <properties>\n        \x3c!--properties having special meanings in Nuts--\x3e\n        <maven.compiler.target>1.8</maven.compiler.target>\n\n        \x3c!--properties specific to nuts for developers extending nuts--\x3e\n        <nuts.runtime>true</nuts.runtime> \x3c!--if you implement a whole new runtime--\x3e\n        <nuts.extension>true</nuts.extension> \x3c!--if you implement an extension--\x3e\n\n        \x3c!--other properties specific to nuts--\x3e\n        <nuts.genericName>A Generic Name</nuts.genericName>\n        <nuts.executable>true</nuts.executable>\n        <nuts.application>true</nuts.application>\n        <nuts.gui>true</nuts.gui>\n        <nuts.term>true</nuts.term>\n\n        <nuts.categories>\n            /Settings/YourCategory\n        </nuts.categories>\n        <nuts.icons>\n            classpath://net/yourpackage/yourapp/icon.svg\n            classpath://net/yourpackage/yourapp/icon.png\n            classpath://net/yourpackage/yourapp/icon.ico\n        </nuts.icons>\n        <nuts.windows-os-dependencies>\n            org.fusesource.jansi:jansi\n            com.github.vatbub:mslinks\n        </nuts.windows-os-dependencies>\n        <nuts.windows-os-x86_32-arch-dependencies>\n            org.fusesource.jansi:jansi\n            com.github.vatbub:mslinks\n        </nuts.windows-os-x86_32-arch-dependencies>\n    </properties>\n\n    <dependencies>\n    </dependencies>\n</project>\n\n'})}),"\n",(0,s.jsx)(n.h3,{id:"nuts-and-java-manifestmf",children:"Nuts and Java MANIFEST.MF"}),"\n",(0,s.jsx)(n.pre,{children:(0,s.jsx)(n.code,{className:"language-manifest",children:"\nManifest-Version: 1.0\nArchiver-Version: Plexus Archiver\nBuilt-By: vpc\nCreated-By: Apache Maven 3.8.1\nBuild-Jdk: 1.8.0_302\n\nNuts-Id: groupid:artifactid#version\nNuts-Dependencies: org.fusesource.jansi:jansi#1.2?os=windows;com.github.vatbub:mslinks#1.3?os=windows\nNuts-Name: Your App Name\nNuts-Generic-Name: Your App Generic Name\nNuts-Description: Your App Description\nNuts-Categories: /Settings/YourCategory;/Settings/YourCategory2\nNuts-Icons: classpath://net/yourpackage/yourapp/icon.svg;classpath://net/yourpackage/yourapp/icon.png\nNuts-Property-YourProp: YourValue\n\nComment: if the Nuts-Id could not be found, best effort will be used from the following\nAutomatic-Module-Name: yourgroupid.yourartifactid.YourClass\nMain-Class: groupid.artifactid.YourClass\nImplementation-Version: 1.2.3\n\n"})}),"\n",(0,s.jsx)(n.h3,{id:"nuts-and-java-9-jdeps",children:"Nuts and Java 9 (jdeps)"}),"\n",(0,s.jsxs)(n.p,{children:["Nuts supports ",(0,s.jsx)(n.code,{children:"Automatic-Module-Name"}),"."]}),"\n",(0,s.jsx)(n.pre,{children:(0,s.jsx)(n.code,{className:"language-manifest",children:"Automatic-Module-Name: yourgroupid.yourartifactid.YourClass\n\n"})}),"\n",(0,s.jsx)(n.h3,{id:"nuts-and-gradle-todo",children:"Nuts and Gradle (TODO)"})]})}function l(e={}){const{wrapper:n}={...(0,i.R)(),...e.components};return n?(0,s.jsx)(n,{...e,children:(0,s.jsx)(d,{...e})}):d(e)}},8453:(e,n,t)=>{t.d(n,{R:()=>a,x:()=>o});var s=t(6540);const i={},r=s.createContext(i);function a(e){const n=s.useContext(r);return s.useMemo((function(){return"function"==typeof e?e(n):{...n,...e}}),[n,e])}function o(e){let n;return n=e.disableParentContext?"function"==typeof e.components?e.components(i):e.components||i:a(e.components),s.createElement(r.Provider,{value:n},e.children)}}}]);