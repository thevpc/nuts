"use strict";(self.webpackChunknuts=self.webpackChunknuts||[]).push([[5206],{359:(e,t,n)=>{n.r(t),n.d(t,{assets:()=>c,contentTitle:()=>i,default:()=>h,frontMatter:()=>o,metadata:()=>a,toc:()=>d});var s=n(4848),r=n(8453);const o={id:"introduction",title:"Introduction",sidebar_label:"Introduction",order:1},i=void 0,a={id:"intro/introduction",title:"Introduction",description:"`nuts` stands for Network Updatable Things Services tool and is a portable package manager for java (mainly) that handles remote artifacts, installs these artifacts to the current machine and executes such artifacts on need.",source:"@site/docs/intro/introduction.md",sourceDirName:"intro",slug:"/intro/introduction",permalink:"/nuts/docs/intro/introduction",draft:!1,unlisted:!1,editUrl:"https://github.com/facebook/docusaurus/tree/main/packages/create-docusaurus/templates/shared/docs/intro/introduction.md",tags:[],version:"current",frontMatter:{id:"introduction",title:"Introduction",sidebar_label:"Introduction",order:1},sidebar:"tutorialSidebar",previous:{title:"Introduction",permalink:"/nuts/docs/category/introduction"},next:{title:"License",permalink:"/nuts/docs/intro/license"}},c={},d=[];function l(e){const t={code:"code",li:"li",p:"p",strong:"strong",ul:"ul",...(0,r.R)(),...e.components};return(0,s.jsxs)(s.Fragment,{children:[(0,s.jsxs)(t.p,{children:[(0,s.jsx)(t.strong,{children:(0,s.jsx)(t.code,{children:"nuts"})})," stands for ",(0,s.jsx)(t.strong,{children:"Network Updatable Things Services"})," tool and is a portable package manager for java (mainly) that handles remote artifacts, installs these artifacts to the current machine and executes such artifacts on need.\n",(0,s.jsx)(t.strong,{children:(0,s.jsx)(t.code,{children:"nuts"})})," solves the ",(0,s.jsx)(t.strong,{children:"fatjar"})," problem delegating the dependency resolution to the time when the application is to be executed and\nsimplifies the packaging process while being transparent to the build process. Actually, nuts uses ",(0,s.jsx)(t.strong,{children:"maven"})," ",(0,s.jsx)(t.strong,{children:"pom"})," descriptors to resolve\ndependencies when the artifact is installed on the target machine, and it can use also other types of descriptors for other types of packages."]}),"\n",(0,s.jsxs)(t.p,{children:[(0,s.jsx)(t.strong,{children:(0,s.jsx)(t.code,{children:"nuts"})})," artifacts are  stored  into repositories. A  ",(0,s.jsx)(t.strong,{children:"repository"}),"  may be local for  storing local artifacts or remote for accessing remote artifacts (good examples  are  remote maven  repositories). It may also be a proxy repository so that remote artifacts are fetched and cached locally to save network resources."]}),"\n",(0,s.jsxs)(t.p,{children:["One manages a set of repositories called a  workspace (like ",(0,s.jsx)(t.strong,{children:"virtualenv"})," in ",(0,s.jsx)(t.strong,{children:(0,s.jsx)(t.code,{children:"pip"})}),"). Managed ",(0,s.jsx)(t.strong,{children:(0,s.jsx)(t.code,{children:"nuts"})}),"  (artifacts)  have descriptors that depicts dependencies between them. This dependency is seamlessly handled by  ",(0,s.jsx)(t.strong,{children:(0,s.jsx)(t.code,{children:"nuts"})}),"  (tool) to resolve and download on-need dependencies over the wire."]}),"\n",(0,s.jsxs)(t.p,{children:[(0,s.jsx)(t.strong,{children:(0,s.jsx)(t.code,{children:"nuts"})})," is a swiss army knife tool as it acts like (and supports) ",(0,s.jsx)(t.strong,{children:"maven"})," build tool to have an abstract view of the artifacts\ndependency and like  ",(0,s.jsx)(t.strong,{children:"npm"})," and ",(0,s.jsx)(t.strong,{children:"pip"})," language package managers to  install and uninstall artifacts allowing multiple versions of the very\nsame artifact to  be installed. ",(0,s.jsx)(t.strong,{children:(0,s.jsx)(t.code,{children:"nuts"})})," is not exclusive for Java/Scala/Kotlin and other Java Platform Languages, by design it supports\nmultiple artifact formats other than jars and wars and is able to select the appropriate artifacts and dependencies according to the current OS, architecture and even Desktop Environment."]}),"\n",(0,s.jsxs)(t.p,{children:[(0,s.jsx)(t.strong,{children:(0,s.jsx)(t.code,{children:"nuts"})})," common verbs are:"]}),"\n",(0,s.jsxs)(t.ul,{children:["\n",(0,s.jsxs)(t.li,{children:[(0,s.jsx)(t.code,{children:"exec"}),"               : execute an artifact or a command"]}),"\n",(0,s.jsxs)(t.li,{children:[(0,s.jsx)(t.code,{children:"which"}),"              : detect the proper artifact or system command to execute"]}),"\n",(0,s.jsxs)(t.li,{children:[(0,s.jsx)(t.code,{children:"install"}),", ",(0,s.jsx)(t.code,{children:"uninstall"})," : install/uninstall an artifact (using its fetched/deployed installer)"]}),"\n",(0,s.jsxs)(t.li,{children:[(0,s.jsx)(t.code,{children:"update"}),",",(0,s.jsx)(t.code,{children:"check-updates"}),"  : search for updates"]}),"\n",(0,s.jsxs)(t.li,{children:[(0,s.jsx)(t.code,{children:"deploy"}),", ",(0,s.jsx)(t.code,{children:"undeploy"}),"   : manage artifacts (artifact installers) on the local repositories"]}),"\n",(0,s.jsxs)(t.li,{children:[(0,s.jsx)(t.code,{children:"fetch"}),", ",(0,s.jsx)(t.code,{children:"push"}),"        : download from, upload to remote repositories"]}),"\n",(0,s.jsxs)(t.li,{children:[(0,s.jsx)(t.code,{children:"search"}),"             : search for existing/installable artifacts"]}),"\n",(0,s.jsxs)(t.li,{children:[(0,s.jsx)(t.code,{children:"welcome"}),"            : a command that does nothing but bootstrapping ",(0,s.jsx)(t.strong,{children:(0,s.jsx)(t.code,{children:"nuts"})})," and showing a welcome message."]}),"\n"]})]})}function h(e={}){const{wrapper:t}={...(0,r.R)(),...e.components};return t?(0,s.jsx)(t,{...e,children:(0,s.jsx)(l,{...e})}):l(e)}},8453:(e,t,n)=>{n.d(t,{R:()=>i,x:()=>a});var s=n(6540);const r={},o=s.createContext(r);function i(e){const t=s.useContext(o);return s.useMemo((function(){return"function"==typeof e?e(t):{...t,...e}}),[t,e])}function a(e){let t;return t=e.disableParentContext?"function"==typeof e.components?e.components(r):e.components||r:i(e.components),s.createElement(o.Provider,{value:t},e.children)}}}]);