(window.webpackJsonp=window.webpackJsonp||[]).push([[22],{117:function(e,t,n){"use strict";n.d(t,"a",(function(){return m}));var a=n(0),c=n.n(a);function r(e,t,n){return t in e?Object.defineProperty(e,t,{value:n,enumerable:!0,configurable:!0,writable:!0}):e[t]=n,e}function o(e,t){var n=Object.keys(e);if(Object.getOwnPropertySymbols){var a=Object.getOwnPropertySymbols(e);t&&(a=a.filter((function(t){return Object.getOwnPropertyDescriptor(e,t).enumerable}))),n.push.apply(n,a)}return n}function s(e){for(var t=1;t<arguments.length;t++){var n=null!=arguments[t]?arguments[t]:{};t%2?o(Object(n),!0).forEach((function(t){r(e,t,n[t])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(n)):o(Object(n)).forEach((function(t){Object.defineProperty(e,t,Object.getOwnPropertyDescriptor(n,t))}))}return e}function i(e,t){if(null==e)return{};var n,a,c=function(e,t){if(null==e)return{};var n,a,c={},r=Object.keys(e);for(a=0;a<r.length;a++)n=r[a],t.indexOf(n)>=0||(c[n]=e[n]);return c}(e,t);if(Object.getOwnPropertySymbols){var r=Object.getOwnPropertySymbols(e);for(a=0;a<r.length;a++)n=r[a],t.indexOf(n)>=0||Object.prototype.propertyIsEnumerable.call(e,n)&&(c[n]=e[n])}return c}var l=c.a.createContext({}),u=function(e){var t=c.a.useContext(l),n=t;return e&&(n="function"==typeof e?e(t):s(s({},t),e)),n},p={inlineCode:"code",wrapper:function(e){var t=e.children;return c.a.createElement(c.a.Fragment,{},t)}},d=c.a.forwardRef((function(e,t){var n=e.components,a=e.mdxType,r=e.originalType,o=e.parentName,l=i(e,["components","mdxType","originalType","parentName"]),d=u(n),m=a,b=d["".concat(o,".").concat(m)]||d[m]||p[m]||r;return n?c.a.createElement(b,s(s({ref:t},l),{},{components:n})):c.a.createElement(b,s({ref:t},l))}));function m(e,t){var n=arguments,a=t&&t.mdxType;if("string"==typeof e||a){var r=n.length,o=new Array(r);o[0]=d;var s={};for(var i in t)hasOwnProperty.call(t,i)&&(s[i]=t[i]);s.originalType=e,s.mdxType="string"==typeof e?e:a,o[1]=s;for(var l=2;l<r;l++)o[l]=n[l];return c.a.createElement.apply(null,o)}return c.a.createElement.apply(null,n)}d.displayName="MDXCreateElement"},77:function(e,t,n){"use strict";n.r(t),n.d(t,"frontMatter",(function(){return o})),n.d(t,"metadata",(function(){return s})),n.d(t,"rightToc",(function(){return i})),n.d(t,"default",(function(){return u}));var a=n(2),c=n(6),r=(n(0),n(117)),o={id:"exec-cmd",title:"Exec and Which Commands",sidebar_label:"Exec and Which Commands"},s={unversionedId:"cmd/exec-cmd",id:"cmd/exec-cmd",isDocsHomePage:!1,title:"Exec and Which Commands",description:"exec command runs another command and which command does a dry run of it.",source:"@site/docs/cmd/exec-cmd.md",permalink:"/nuts/docs/cmd/exec-cmd",editUrl:"https://github.com/thevpc/nuts/edit/master/website/docs/cmd/exec-cmd.md",sidebar_label:"Exec and Which Commands",sidebar:"someSidebar",previous:{title:"Nuts Text Format",permalink:"/nuts/docs/concepts/doc1"},next:{title:"Fetch Command",permalink:"/nuts/docs/cmd/fetch-cmd"}},i=[{value:"2. Execution types",id:"2-execution-types",children:[{value:"2.1 spawn",id:"21-spawn",children:[]},{value:"2.2 embedded",id:"22-embedded",children:[]},{value:"2.3 syscall",id:"23-syscall",children:[]}]},{value:"3 Execution modes",id:"3-execution-modes",children:[{value:"3.1 effective execution",id:"31-effective-execution",children:[]},{value:"3.2 dry execution",id:"32-dry-execution",children:[]}]}],l={rightToc:i};function u(e){var t=e.components,n=Object(c.a)(e,["components"]);return Object(r.a)("wrapper",Object(a.a)({},l,n,{components:t,mdxType:"MDXLayout"}),Object(r.a)("p",null,Object(r.a)("strong",{parentName:"p"},"exec")," command runs another command and ",Object(r.a)("strong",{parentName:"p"},"which")," command does a dry run of it."),Object(r.a)("p",null,"When one types"),Object(r.a)("pre",null,Object(r.a)("code",Object(a.a)({parentName:"pre"},{}),"nuts netbeans-command\n")),Object(r.a)("p",null,"it is actually equivalent to"),Object(r.a)("pre",null,Object(r.a)("code",Object(a.a)({parentName:"pre"},{}),"nuts exec netbeans-command\n")),Object(r.a)("p",null,"What is helpful with ",Object(r.a)("strong",{parentName:"p"},"exec")," is that it permits passing extra parameters to application executors. In ",Object(r.a)("strong",{parentName:"p"},"nuts"),", an application executor is an artifact that can be used to run other artifacts. For instance ",Object(r.a)("strong",{parentName:"p"},"nsh"),", which is a ",Object(r.a)("strong",{parentName:"p"},"nuts"),' companion, is an executor for all "*.nsh" artifacts (yest script files are artifacts too). Some executors are specially handled such as "java" executor that is used to run all jars and basically all java based artifacts. Java executor for instance supports all java standard vm option arguments'),Object(r.a)("pre",null,Object(r.a)("code",Object(a.a)({parentName:"pre"},{}),"me@linux:~> nuts exec -Xmx1G netbeans-launcher\n")),Object(r.a)("p",null,"Here we pass -Xmx1G option argument to java executor because ",Object(r.a)("strong",{parentName:"p"},"netbeans-launcher")," will be resolved as a java based artifact.\nFor what concerns ",Object(r.a)("strong",{parentName:"p"},"which")," command, it does not really execute the command, it just resolves the command execution"),Object(r.a)("pre",null,Object(r.a)("code",Object(a.a)({parentName:"pre"},{}),"me@linux:~> nuts which version ls\nversion : internal command \nls : nuts alias (owner maven-local://net.vpc.app.nuts.toolbox:nsh#0.5.7.0 ) : maven-local://net.vpc.app.nuts.toolbox:nsh#0.5.7.0 -c ls\n")),Object(r.a)("p",null,"Here ",Object(r.a)("strong",{parentName:"p"},"which")," returns that ",Object(r.a)("strong",{parentName:"p"},"version")," is an internal command while ",Object(r.a)("strong",{parentName:"p"},"ls"),' is an alias to an artifact based command (nsh -c ls) which is called a "nuts alias". As you can see, ls is actually a sub command of nsh artifact.'),Object(r.a)("h4",{id:"12-external-commands"},"1.2 External Commands"),Object(r.a)("p",null,"External commands are commands that will invoke another artifact. for instance"),Object(r.a)("pre",null,Object(r.a)("code",Object(a.a)({parentName:"pre"},{}),"nuts netbeans-command\n")),Object(r.a)("p",null,"is running an external command which is net.vpc.app:netbeans-launcher#1.2.2 artifact."),Object(r.a)("h4",{id:"12-external-files--urls"},"1.2 External Files & URLs"),Object(r.a)("p",null,"You can run any jar file using ",Object(r.a)("strong",{parentName:"p"},"nuts")," as far as it fulfills two points : the files must contain a supported descriptor (if it is compiled with maven, it already has the supported descriptor) and the file should be typed as a path (it must contain a '/' or '\\' separator)"),Object(r.a)("pre",null,Object(r.a)("code",Object(a.a)({parentName:"pre"},{}),"wget -N https://github.com/thevpc/vpc-public-maven/raw/master/net/vpc/app/netbeans-launcher/1.2.2/netbeans-launcher-1.2.2.jar\nnuts ./netbeans-launcher-1.2.2.jar\n")),Object(r.a)("p",null,"You can even run a remove file using its url format :"),Object(r.a)("pre",null,Object(r.a)("code",Object(a.a)({parentName:"pre"},{}),"nuts https://github.com/thevpc/vpc-public-maven/raw/master/net/vpc/app/netbeans-launcher/1.2.2/netbeans-launcher-1.2.2.jar\n")),Object(r.a)("h2",{id:"2-execution-types"},"2. Execution types"),Object(r.a)("h3",{id:"21-spawn"},"2.1 spawn"),Object(r.a)("p",null,"This is the default execution type where any external command will spawn a new process to execute within.\nInternal commands are not affected by this mode and are executed, always, in the current vm process (with embedded type)."),Object(r.a)("pre",null,Object(r.a)("code",Object(a.a)({parentName:"pre"},{}),"me@linux:~> nuts --spawn ls\n")),Object(r.a)("h3",{id:"22-embedded"},"2.2 embedded"),Object(r.a)("p",null,"In this type the command will try not to spawn a new process but load in the current vm the commmand to run (as far as it is a java command)"),Object(r.a)("pre",null,Object(r.a)("code",Object(a.a)({parentName:"pre"},{}),"me@linux:~> nuts --embedded ls\n")),Object(r.a)("h3",{id:"23-syscall"},"2.3 syscall"),Object(r.a)("p",null,"In this type, the command execution is delegated to the underlying operating system end hence will also swan a new process."),Object(r.a)("pre",null,Object(r.a)("code",Object(a.a)({parentName:"pre"},{}),"me@linux:~> nuts --syscall ls\n")),Object(r.a)("h2",{id:"3-execution-modes"},"3 Execution modes"),Object(r.a)("h3",{id:"31-effective-execution"},"3.1 effective execution"),Object(r.a)("p",null,"This is the default execution mode where the command is really and effectively ran."),Object(r.a)("h3",{id:"32-dry-execution"},"3.2 dry execution"),Object(r.a)("p",null,'In this mode, the command will be ran in dry mode with no side effects which implies a "simulation" of the execution.'),Object(r.a)("pre",null,Object(r.a)("code",Object(a.a)({parentName:"pre"},{}),"me@linux:~> nuts --dry version\n[dry] internal version \n")))}u.isMDXComponent=!0}}]);