"use strict";(self.webpackChunknuts=self.webpackChunknuts||[]).push([[9936],{9252:(e,n,t)=>{t.r(n),t.d(n,{assets:()=>r,contentTitle:()=>l,default:()=>h,frontMatter:()=>a,metadata:()=>o,toc:()=>c});var i=t(4848),s=t(8453);const a={id:"running",title:"Running Nuts",sidebar_label:"Running Nuts",order:1},l=void 0,o={id:"info/running",title:"Running Nuts",description:"Running a deployed artifact",source:"@site/docs/info/running.md",sourceDirName:"info",slug:"/info/running",permalink:"/nuts/docs/info/running",draft:!1,unlisted:!1,editUrl:"https://github.com/facebook/docusaurus/tree/main/packages/create-docusaurus/templates/shared/docs/info/running.md",tags:[],version:"current",frontMatter:{id:"running",title:"Running Nuts",sidebar_label:"Running Nuts",order:1},sidebar:"tutorialSidebar",previous:{title:"Frequently Asked Questions",permalink:"/nuts/docs/info/faq"},next:{title:"Troubleshooting",permalink:"/nuts/docs/info/troubleshooting"}},r={},c=[{value:"Running a deployed artifact",id:"running-a-deployed-artifact",level:2},{value:"Artifact Long Ids",id:"artifact-long-ids",level:2},{value:"Artifact Installation",id:"artifact-installation",level:2},{value:"Multiple Artifact version Installation",id:"multiple-artifact-version-installation",level:2},{value:"Searching artifacts",id:"searching-artifacts",level:2},{value:"Running local jar file with its dependencies",id:"running-local-jar-file-with-its-dependencies",level:2}];function d(e){const n={code:"code",h2:"h2",li:"li",p:"p",pre:"pre",strong:"strong",ul:"ul",...(0,s.R)(),...e.components};return(0,i.jsxs)(i.Fragment,{children:[(0,i.jsx)(n.h2,{id:"running-a-deployed-artifact",children:"Running a deployed artifact"}),"\n",(0,i.jsxs)(n.p,{children:["You can run any jar using ",(0,i.jsx)(n.strong,{children:(0,i.jsx)(n.code,{children:"nuts"})})," as far as the jar is accessible from one of the supported repositories.\nBy default, ",(0,i.jsx)(n.strong,{children:(0,i.jsx)(n.code,{children:"nuts"})})," supports:"]}),"\n",(0,i.jsxs)(n.ul,{children:["\n",(0,i.jsx)(n.li,{children:"maven central"}),"\n",(0,i.jsx)(n.li,{children:"local maven folder (~/.m2)"}),"\n"]}),"\n",(0,i.jsx)(n.p,{children:"You can configure other repositories or even implement your own if you need to."}),"\n",(0,i.jsx)(n.p,{children:"The jar will be parsed to check form maven descriptor so that dependencies will be resolved and downloaded on the fly.\nThen, all executable classes (public with static void main method) are enumerated. You can actually run any of them when prompted. Any jar built using maven should be well described and can be run using its artifact long id."}),"\n",(0,i.jsx)(n.h2,{id:"artifact-long-ids",children:"Artifact Long Ids"}),"\n",(0,i.jsxs)(n.p,{children:[(0,i.jsx)(n.strong,{children:(0,i.jsx)(n.code,{children:"nuts"})})," long ids are a string representation of a unique identifier of the artifact. It has the following form :"]}),"\n",(0,i.jsx)(n.pre,{children:(0,i.jsx)(n.code,{className:"language-bash",children:"groupId:artifactId#version\n"})}),"\n",(0,i.jsxs)(n.p,{children:["for instance, to install ",(0,i.jsx)(n.code,{children:"netbeans-launcher"})," (which is a simple UI helping launch of multiple instances of netbeans), you can issue"]}),"\n",(0,i.jsx)(n.pre,{children:(0,i.jsx)(n.code,{className:"language-bash",children:"  nuts net.vpc.app:netbeans-launcher#1.2.2\n"})}),"\n",(0,i.jsx)(n.p,{children:"You do agree that this can be of some cumbersome to type. So you can simplify it to :"}),"\n",(0,i.jsx)(n.pre,{children:(0,i.jsx)(n.code,{className:"language-bash",children:"  nuts netbeans-launcher\n"})}),"\n",(0,i.jsxs)(n.p,{children:["In this form, ",(0,i.jsx)(n.strong,{children:(0,i.jsx)(n.code,{children:"nuts"})})," will auto-detect both the ",(0,i.jsx)(n.code,{children:"groupId"})," and the ",(0,i.jsx)(n.code,{children:"version"}),". The group id is detected if it is already imported (we will see later how to import a groupId).\nBy default, there is a couple of groupIds that are automatically imported :"]}),"\n",(0,i.jsxs)(n.ul,{children:["\n",(0,i.jsxs)(n.li,{children:[(0,i.jsx)(n.code,{children:"net.thevpc"})," (contains various applications of the author)"]}),"\n",(0,i.jsxs)(n.li,{children:[(0,i.jsx)(n.code,{children:"net.thevpc.nuts.toolbox"})," (contains various companion tools of ",(0,i.jsx)(n.strong,{children:(0,i.jsx)(n.code,{children:"nuts"})}),", such as ",(0,i.jsx)(n.code,{children:"nsh"}),", ...)"]}),"\n"]}),"\n",(0,i.jsxs)(n.p,{children:["And it turns out, hopefully, that netbeans-launcher belongs to an imported groupId, so we can omit it.\nBesides, if no version is provided, ",(0,i.jsx)(n.strong,{children:(0,i.jsx)(n.code,{children:"nuts"})})," will also auto-detect the best version to execute. If the application is already installed, the version you choose to install will be resolved. If you have not installed any, the most recent version, obviously, will be detected for you."]}),"\n",(0,i.jsx)(n.h2,{id:"artifact-installation",children:"Artifact Installation"}),"\n",(0,i.jsxs)(n.p,{children:["Any java application can run using ",(0,i.jsx)(n.strong,{children:(0,i.jsx)(n.code,{children:"nuts"})})," but it has to be installed first. If you try to run the application before installing it, you will be prompted to confirm installation.\nTo install our favorite application here we could have issued :"]}),"\n",(0,i.jsx)(n.pre,{children:(0,i.jsx)(n.code,{className:"language-bash",children:"  nuts install netbeans-launcher\n"})}),"\n",(0,i.jsx)(n.p,{children:"But as we have tried to run the application first, it has been installed for us (after confirmation)."}),"\n",(0,i.jsx)(n.h2,{id:"multiple-artifact-version-installation",children:"Multiple Artifact version Installation"}),"\n",(0,i.jsxs)(n.p,{children:["One of the key features of ",(0,i.jsx)(n.strong,{children:(0,i.jsx)(n.code,{children:"nuts"})})," is the ability to install multiple versions of the same application.\nWe can for instance type :"]}),"\n",(0,i.jsx)(n.pre,{children:(0,i.jsx)(n.code,{className:"language-bash",children:"  nuts install netbeans-launcher#1.2.2\n  # then\n  nuts install netbeans-launcher#1.2.0\n"})}),"\n",(0,i.jsx)(n.p,{children:"Now we have two versions installed, the last one always is considered default one.\nyou can run either, using it's version"}),"\n",(0,i.jsx)(n.pre,{children:(0,i.jsx)(n.code,{className:"language-bash",children:"  nuts netbeans-launcher#1.2.2 &\n  # or\n  nuts netbeans-launcher#1.2.0 &\n"})}),"\n",(0,i.jsx)(n.p,{children:"Actually, when you have many versions installed for the same artifact and you try to run it without specifying the version, the last one installed will be considered. To be more precise, an artifact has a default version when it is installed. This default version is considered when no explicit version is typed.\nIn our example, when we type"}),"\n",(0,i.jsx)(n.pre,{children:(0,i.jsx)(n.code,{children:"  nuts netbeans-launcher &\n"})}),"\n",(0,i.jsx)(n.p,{children:"the 1.2.0 version will be invoked because the artifact is already installed and the default version points to the last one installed. So if you want to switch back to version 1.2.2 you just have to re-install it. Don't worry, no file will be downloaded again, nuts will detect that the version is not marked as default and will switch it to."}),"\n",(0,i.jsx)(n.h2,{id:"searching-artifacts",children:"Searching artifacts"}),"\n",(0,i.jsx)(n.p,{children:"Now let's take a look at installed artifacts. We will type :"}),"\n",(0,i.jsx)(n.pre,{children:(0,i.jsx)(n.code,{className:"language-bash",children:"  nuts search --installed\n"})}),"\n",(0,i.jsx)(n.p,{children:"This will list all installed artifacts. We can get a better listing using long format :"}),"\n",(0,i.jsx)(n.pre,{children:(0,i.jsx)(n.code,{className:"language-bash",children:"  nuts search --installed -l\n"})}),"\n",(0,i.jsx)(n.p,{children:"you will see something like"}),"\n",(0,i.jsx)(n.pre,{children:(0,i.jsx)(n.code,{children:"I-X 2019-08-21 04:54:22.951 anonymous vpc-public-maven net.vpc.app:netbeans-launcher#1.2.0\ni-X 2019-08-21 04:54:05.196 anonymous vpc-public-maven net.vpc.app:netbeans-launcher#1.2.2\n"})}),"\n",(0,i.jsxs)(n.p,{children:["The first column here is a the artifact status that helps getting zipped information of the artifact. the 'I' stands for 'installed and default' whereas, 'i' is simply 'installed'. The 'X' stands for 'executable application', where 'x' is simply 'executable'. Roughly said, executable applications are executables aware of (or depends on) ",(0,i.jsx)(n.strong,{children:"nuts"}),", as they provide a special api that helps nuts to get more information and more features for the application. As an example, executable applications have special OnInstall and OnUninstall hooks called by nuts.\nThe second and the third columns are date and time of installation. The fourth column points to the installation user. When Secure mode has not been enabled (which is the default), you are running nuts as 'anonymous'.\nThe fifth column shows the repository from which the package was installed. And the last column depicts the artifact long id."]}),"\n",(0,i.jsx)(n.h2,{id:"running-local-jar-file-with-its-dependencies",children:"Running local jar file with its dependencies"}),"\n",(0,i.jsxs)(n.p,{children:["Let's suppose that my-app.jar is a maven created jar (contains META-INF/maven files) with a number of dependencies. ",(0,i.jsx)(n.strong,{children:(0,i.jsx)(n.code,{children:"nuts"})})," is able to download on the fly needed dependencies, detect the Main class (no need for MANIFEST.MF) and run the\napplication. If a Main-Class Attribute was detected in a valid MANIFEST.MF, il will be considered.\nIf more than one class is detected with a main method, ",(0,i.jsx)(n.strong,{children:(0,i.jsx)(n.code,{children:"nuts"})})," will ask for the current class to run."]}),"\n",(0,i.jsxs)(n.p,{children:["When you run a local file, ",(0,i.jsx)(n.strong,{children:(0,i.jsx)(n.code,{children:"nuts"})})," will behave as if the app is installed (in the given path, an no need to invoke install command). Local files are detected if they are denoted by a valid path (containing '/' or '' depending on the underlying operating system).\nDependencies will be downloaded as well (and cached in the workspace)"]}),"\n",(0,i.jsx)(n.pre,{children:(0,i.jsx)(n.code,{className:"language-bash",children:"nuts ./my-app.jar some-argument-of-my-app\n"})}),"\n",(0,i.jsx)(n.p,{children:'If you need to pass JVM arguments you have to prefix them with "--exec". So if you want to fix maximum heap size use'}),"\n",(0,i.jsx)(n.pre,{children:(0,i.jsx)(n.code,{className:"language-bash",children:"nuts --exec -Xms1G -Xmx2G ./my-app.jar argument-1 argument-2\n"})})]})}function h(e={}){const{wrapper:n}={...(0,s.R)(),...e.components};return n?(0,i.jsx)(n,{...e,children:(0,i.jsx)(d,{...e})}):d(e)}},8453:(e,n,t)=>{t.d(n,{R:()=>l,x:()=>o});var i=t(6540);const s={},a=i.createContext(s);function l(e){const n=i.useContext(a);return i.useMemo((function(){return"function"==typeof e?e(n):{...n,...e}}),[n,e])}function o(e){let n;return n=e.disableParentContext?"function"==typeof e.components?e.components(s):e.components||s:l(e.components),i.createElement(a.Provider,{value:n},e.children)}}}]);