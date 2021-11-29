(window.webpackJsonp=window.webpackJsonp||[]).push([[28],{114:function(e,n,t){"use strict";t.d(n,"a",(function(){return l})),t.d(n,"b",(function(){return b}));var a=t(0),r=t.n(a);function i(e,n,t){return n in e?Object.defineProperty(e,n,{value:t,enumerable:!0,configurable:!0,writable:!0}):e[n]=t,e}function s(e,n){var t=Object.keys(e);if(Object.getOwnPropertySymbols){var a=Object.getOwnPropertySymbols(e);n&&(a=a.filter((function(n){return Object.getOwnPropertyDescriptor(e,n).enumerable}))),t.push.apply(t,a)}return t}function o(e){for(var n=1;n<arguments.length;n++){var t=null!=arguments[n]?arguments[n]:{};n%2?s(Object(t),!0).forEach((function(n){i(e,n,t[n])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(t)):s(Object(t)).forEach((function(n){Object.defineProperty(e,n,Object.getOwnPropertyDescriptor(t,n))}))}return e}function c(e,n){if(null==e)return{};var t,a,r=function(e,n){if(null==e)return{};var t,a,r={},i=Object.keys(e);for(a=0;a<i.length;a++)t=i[a],n.indexOf(t)>=0||(r[t]=e[t]);return r}(e,n);if(Object.getOwnPropertySymbols){var i=Object.getOwnPropertySymbols(e);for(a=0;a<i.length;a++)t=i[a],n.indexOf(t)>=0||Object.prototype.propertyIsEnumerable.call(e,t)&&(r[t]=e[t])}return r}var u=r.a.createContext({}),p=function(e){var n=r.a.useContext(u),t=n;return e&&(t="function"==typeof e?e(n):o(o({},n),e)),t},l=function(e){var n=p(e.components);return r.a.createElement(u.Provider,{value:n},e.children)},d={inlineCode:"code",wrapper:function(e){var n=e.children;return r.a.createElement(r.a.Fragment,{},n)}},m=r.a.forwardRef((function(e,n){var t=e.components,a=e.mdxType,i=e.originalType,s=e.parentName,u=c(e,["components","mdxType","originalType","parentName"]),l=p(t),m=a,b=l["".concat(s,".").concat(m)]||l[m]||d[m]||i;return t?r.a.createElement(b,o(o({ref:n},u),{},{components:t})):r.a.createElement(b,o({ref:n},u))}));function b(e,n){var t=arguments,a=n&&n.mdxType;if("string"==typeof e||a){var i=t.length,s=new Array(i);s[0]=m;var o={};for(var c in n)hasOwnProperty.call(n,c)&&(o[c]=n[c]);o.originalType=e,o.mdxType="string"==typeof e?e:a,s[1]=o;for(var u=2;u<i;u++)s[u]=t[u];return r.a.createElement.apply(null,s)}return r.a.createElement.apply(null,t)}m.displayName="MDXCreateElement"},82:function(e,n,t){"use strict";t.r(n),t.d(n,"frontMatter",(function(){return s})),t.d(n,"metadata",(function(){return o})),t.d(n,"rightToc",(function(){return c})),t.d(n,"default",(function(){return p}));var a=t(2),r=t(6),i=(t(0),t(114)),s={id:"nutsDescriptorIntegration",title:"Nuts Descriptor Integration",sidebar_label:"Nuts Descriptor Integration"},o={unversionedId:"dev/nutsDescriptorIntegration",id:"dev/nutsDescriptorIntegration",isDocsHomePage:!1,title:"Nuts Descriptor Integration",description:"Nuts Descriptor Integration",source:"@site/docs/dev/nuts-maven-integration.md",permalink:"/nuts/docs/dev/nutsDescriptorIntegration",editUrl:"https://github.com/thevpc/nuts/edit/master/website/docs/dev/nuts-maven-integration.md",sidebar_label:"Nuts Descriptor Integration",sidebar:"someSidebar",previous:{title:"Welcome Command",permalink:"/nuts/docs/cmd/welcome-cmd"},next:{title:"Nuts Path",permalink:"/nuts/docs/dev/nutsPath"}},c=[{value:"Nuts Descriptor Integration",id:"nuts-descriptor-integration",children:[{value:"Nuts and Maven",id:"nuts-and-maven",children:[]},{value:"Nuts and Java MANIFEST.MF",id:"nuts-and-java-manifestmf",children:[]},{value:"Nuts and Java 9 (jdeps)",id:"nuts-and-java-9-jdeps",children:[]},{value:"Nuts and Gradle (TODO)",id:"nuts-and-gradle-todo",children:[]}]}],u={rightToc:c};function p(e){var n=e.components,t=Object(r.a)(e,["components"]);return Object(i.b)("wrapper",Object(a.a)({},u,t,{components:n,mdxType:"MDXLayout"}),Object(i.b)("h2",{id:"nuts-descriptor-integration"},"Nuts Descriptor Integration"),Object(i.b)("ul",null,Object(i.b)("li",{parentName:"ul"},"Seamless integration"),Object(i.b)("li",{parentName:"ul"},"Maven Solver")),Object(i.b)("h3",{id:"nuts-and-maven"},"Nuts and Maven"),Object(i.b)("ul",null,Object(i.b)("li",{parentName:"ul"},Object(i.b)("inlineCode",{parentName:"li"},"nuts.executable=<true|false>")," : when true the artifact is an executable (contains main class)"),Object(i.b)("li",{parentName:"ul"},Object(i.b)("inlineCode",{parentName:"li"},"nuts.application=<true|false>")," : when true the artifact is an executable application (implements NutsApplication)"),Object(i.b)("li",{parentName:"ul"},Object(i.b)("inlineCode",{parentName:"li"},"nuts.gui=<true|false>")," : when true the requires a gui environment to execute"),Object(i.b)("li",{parentName:"ul"},Object(i.b)("inlineCode",{parentName:"li"},"nuts.term=<true|false>")," : when true the artifact is a command line executable"),Object(i.b)("li",{parentName:"ul"},Object(i.b)("inlineCode",{parentName:"li"},"nuts.icons=<icon-path-string-array>")," : an array (separated with ',' or new lines) of icon paths (url in the NutsPath\nformat)"),Object(i.b)("li",{parentName:"ul"},Object(i.b)("inlineCode",{parentName:"li"},"nuts.genericName=<genericNameString>")," : a generic name for the application like 'Text Editor'",Object(i.b)("ul",{parentName:"li"},Object(i.b)("li",{parentName:"ul"},Object(i.b)("inlineCode",{parentName:"li"},"nuts.categories=<categories-string-array>")," : an array (separated with ',' or new lines) of categories. the\ncategories should be compatible with Free Desktop Menu\nspecification (",Object(i.b)("a",Object(a.a)({parentName:"li"},{href:"https://specifications.freedesktop.org/menu-spec/menu-spec-1.0.html"}),"https://specifications.freedesktop.org/menu-spec/menu-spec-1.0.html"),")"))),Object(i.b)("li",{parentName:"ul"},Object(i.b)("inlineCode",{parentName:"li"},"nuts.<os>-os-dependencies")," : list (':',';' or line separated) of short ids of dependencies that shall be appended to\nclasspath only if running on the given os (see NutsOsFamily). This is a ways more simple than using the builtin '\nprofile' concept of Maven (which is of course supported as well)"),Object(i.b)("li",{parentName:"ul"},Object(i.b)("inlineCode",{parentName:"li"},"nuts.<arch>-arch-dependencies")," : list (':',';' or line separated) of short ids of dependencies that shall be appended\nto classpath only if running on the given hardware architecture (see NutsArchFamily). This is a ways more simple than\nusing the builtin 'profile' concept of Maven (which is of course supported as well)"),Object(i.b)("li",{parentName:"ul"},Object(i.b)("inlineCode",{parentName:"li"},"nuts.<os>-os-<arch>-arch-dependencies")," : list (':',';' or line separated) of short ids of dependencies that shall be\nappended to classpath only if running on the given hardware architecture and os family")),Object(i.b)("pre",null,Object(i.b)("code",Object(a.a)({parentName:"pre"},{className:"language-xml"}),'<?xml version="1.0" encoding="UTF-8"?>\n<project xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns="http://maven.apache.org/POM/4.0.0"\n         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">\n    <modelVersion>4.0.0</modelVersion>\n    <groupId>your-group</groupId>\n    <artifactId>your-project</artifactId>\n    <version>1.2.3</version>\n    <packaging>jar</packaging>\n    <properties>\n        \x3c!--properties having special meanings in Nuts--\x3e\n        <maven.compiler.target>1.8</maven.compiler.target>\n\n        \x3c!--properties specific to nuts for developers extending nuts--\x3e\n        <nuts.runtime>true</nuts.runtime> \x3c!--if you implement a whole new runtime--\x3e\n        <nuts.extension>true</nuts.extension> \x3c!--if you implement an extension--\x3e\n\n        \x3c!--other properties specific to nuts--\x3e\n        <nuts.genericName>A Generic Name</nuts.genericName>\n        <nuts.executable>true</nuts.executable>\n        <nuts.application>true</nuts.application>\n        <nuts.gui>true</nuts.gui>\n        <nuts.term>true</nuts.term>\n\n        <nuts.categories>\n            /Settings/YourCategory\n        </nuts.categories>\n        <nuts.icons>\n            classpath://net/yourpackage/yourapp/icon.svg\n            classpath://net/yourpackage/yourapp/icon.png\n            classpath://net/yourpackage/yourapp/icon.ico\n        </nuts.icons>\n        <nuts.windows-os-dependencies>\n            org.fusesource.jansi:jansi\n            com.github.vatbub:mslinks\n        </nuts.windows-os-dependencies>\n        <nuts.windows-os-x86_32-arch-dependencies>\n            org.fusesource.jansi:jansi\n            com.github.vatbub:mslinks\n        </nuts.windows-os-x86_32-arch-dependencies>\n    </properties>\n\n    <dependencies>\n    </dependencies>\n</project>\n\n')),Object(i.b)("h3",{id:"nuts-and-java-manifestmf"},"Nuts and Java MANIFEST.MF"),Object(i.b)("pre",null,Object(i.b)("code",Object(a.a)({parentName:"pre"},{className:"language-manifest"}),"\nManifest-Version: 1.0\nArchiver-Version: Plexus Archiver\nBuilt-By: vpc\nCreated-By: Apache Maven 3.8.1\nBuild-Jdk: 1.8.0_302\n\nNuts-Id: groupid:artifactid#version\nNuts-Dependencies: org.fusesource.jansi:jansi#1.2?os=windows;com.github.vatbub:mslinks#1.3?os=windows\nNuts-Name: Your App Name\nNuts-Generic-Name: Your App Generic Name\nNuts-Description: Your App Description\nNuts-Categories: /Settings/YourCategory;/Settings/YourCategory2\nNuts-Icons: classpath://net/yourpackage/yourapp/icon.svg;classpath://net/yourpackage/yourapp/icon.png\nNuts-Property-YourProp: YourValue\n\nComment: if the Nuts-Id could not be found, best effort will be used from the following\nAutomatic-Module-Name: yourgroupid.yourartifactid.YourClass\nMain-Class: groupid.artifactid.YourClass\nImplementation-Version: 1.2.3\n\n")),Object(i.b)("h3",{id:"nuts-and-java-9-jdeps"},"Nuts and Java 9 (jdeps)"),Object(i.b)("p",null,"Nuts supports ",Object(i.b)("inlineCode",{parentName:"p"},"Automatic-Module-Name"),"."),Object(i.b)("pre",null,Object(i.b)("code",Object(a.a)({parentName:"pre"},{className:"language-manifest"}),"Automatic-Module-Name: yourgroupid.yourartifactid.YourClass\n\n")),Object(i.b)("h3",{id:"nuts-and-gradle-todo"},"Nuts and Gradle (TODO)"))}p.isMDXComponent=!0}}]);