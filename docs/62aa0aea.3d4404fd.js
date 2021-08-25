(window.webpackJsonp=window.webpackJsonp||[]).push([[21],{103:function(e,t,n){"use strict";n.d(t,"a",(function(){return b})),n.d(t,"b",(function(){return m}));var a=n(0),r=n.n(a);function o(e,t,n){return t in e?Object.defineProperty(e,t,{value:n,enumerable:!0,configurable:!0,writable:!0}):e[t]=n,e}function i(e,t){var n=Object.keys(e);if(Object.getOwnPropertySymbols){var a=Object.getOwnPropertySymbols(e);t&&(a=a.filter((function(t){return Object.getOwnPropertyDescriptor(e,t).enumerable}))),n.push.apply(n,a)}return n}function c(e){for(var t=1;t<arguments.length;t++){var n=null!=arguments[t]?arguments[t]:{};t%2?i(Object(n),!0).forEach((function(t){o(e,t,n[t])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(n)):i(Object(n)).forEach((function(t){Object.defineProperty(e,t,Object.getOwnPropertyDescriptor(n,t))}))}return e}function s(e,t){if(null==e)return{};var n,a,r=function(e,t){if(null==e)return{};var n,a,r={},o=Object.keys(e);for(a=0;a<o.length;a++)n=o[a],t.indexOf(n)>=0||(r[n]=e[n]);return r}(e,t);if(Object.getOwnPropertySymbols){var o=Object.getOwnPropertySymbols(e);for(a=0;a<o.length;a++)n=o[a],t.indexOf(n)>=0||Object.prototype.propertyIsEnumerable.call(e,n)&&(r[n]=e[n])}return r}var l=r.a.createContext({}),p=function(e){var t=r.a.useContext(l),n=t;return e&&(n="function"==typeof e?e(t):c(c({},t),e)),n},b=function(e){var t=p(e.components);return r.a.createElement(l.Provider,{value:t},e.children)},d={inlineCode:"code",wrapper:function(e){var t=e.children;return r.a.createElement(r.a.Fragment,{},t)}},u=r.a.forwardRef((function(e,t){var n=e.components,a=e.mdxType,o=e.originalType,i=e.parentName,l=s(e,["components","mdxType","originalType","parentName"]),b=p(n),u=a,m=b["".concat(i,".").concat(u)]||b[u]||d[u]||o;return n?r.a.createElement(m,c(c({ref:t},l),{},{components:n})):r.a.createElement(m,c({ref:t},l))}));function m(e,t){var n=arguments,a=t&&t.mdxType;if("string"==typeof e||a){var o=n.length,i=new Array(o);i[0]=u;var c={};for(var s in t)hasOwnProperty.call(t,s)&&(c[s]=t[s]);c.originalType=e,c.mdxType="string"==typeof e?e:a,i[1]=c;for(var l=2;l<o;l++)i[l]=n[l];return r.a.createElement.apply(null,i)}return r.a.createElement.apply(null,n)}u.displayName="MDXCreateElement"},75:function(e,t,n){"use strict";n.r(t),n.d(t,"frontMatter",(function(){return i})),n.d(t,"metadata",(function(){return c})),n.d(t,"rightToc",(function(){return s})),n.d(t,"default",(function(){return p}));var a=n(2),r=n(6),o=(n(0),n(103)),i={id:"introduction",title:"Introduction",sidebar_label:"Introduction",order:1},c={unversionedId:"intro/introduction",id:"intro/introduction",isDocsHomePage:!0,title:"Introduction",description:"`nuts` stands for Network Updatable Things Services tool. It is a simple tool  for managing remote artifacts, installing these  artifacts to the current machine and executing such  artifacts on need. Each managed package  is also called a `nuts` which  is a Network Updatable Thing Service . `nuts` artifacts are  stored  into repositories. A  repository  may be local for  storing local `nuts` or remote for accessing  remote artifacts (good examples  are  remote maven  repositories). It may also be a proxy repository so that remote artifacts are fetched and cached locally to save network resources.",source:"@site/docs/intro/introduction.md",permalink:"/nuts/docs/",editUrl:"https://github.com/facebook/docusaurus/edit/master/website/docs/intro/introduction.md",sidebar_label:"Introduction",sidebar:"someSidebar",next:{title:"Nuts and Maven",permalink:"/nuts/docs/intro/nutsAndMaven"}},s=[],l={rightToc:s};function p(e){var t=e.components,n=Object(r.a)(e,["components"]);return Object(o.b)("wrapper",Object(a.a)({},l,n,{components:t,mdxType:"MDXLayout"}),Object(o.b)("p",null,Object(o.b)("strong",{parentName:"p"},Object(o.b)("inlineCode",{parentName:"strong"},"nuts"))," stands for ",Object(o.b)("strong",{parentName:"p"},"Network Updatable Things Services")," tool. It is a simple tool  for managing remote artifacts, installing these  artifacts to the current machine and executing such  artifacts on need. Each managed package  is also called a ",Object(o.b)("strong",{parentName:"p"},Object(o.b)("inlineCode",{parentName:"strong"},"nuts"))," which  is a ",Object(o.b)("strong",{parentName:"p"},"Network Updatable Thing Service")," . ",Object(o.b)("strong",{parentName:"p"},Object(o.b)("inlineCode",{parentName:"strong"},"nuts"))," artifacts are  stored  into repositories. A  ",Object(o.b)("strong",{parentName:"p"},"repository"),"  may be local for  storing local ",Object(o.b)("strong",{parentName:"p"},Object(o.b)("inlineCode",{parentName:"strong"},"nuts"))," or remote for accessing  remote artifacts (good examples  are  remote maven  repositories). It may also be a proxy repository so that remote artifacts are fetched and cached locally to save network resources."),Object(o.b)("p",null,"One manages a set of repositories called a  workspace. Managed ",Object(o.b)("strong",{parentName:"p"},Object(o.b)("inlineCode",{parentName:"strong"},"nuts")),"  (artifacts)  have descriptors that depicts dependencies between them. This dependency is seamlessly handled by  ",Object(o.b)("strong",{parentName:"p"},Object(o.b)("inlineCode",{parentName:"strong"},"nuts")),"  (tool) to resolve and download on-need dependencies over the wire."),Object(o.b)("p",null,Object(o.b)("strong",{parentName:"p"},Object(o.b)("inlineCode",{parentName:"strong"},"nuts"))," is a swiss army knife tool as it acts like (and supports) ",Object(o.b)("strong",{parentName:"p"},"maven")," build tool to have an abstract view of the the  artifacts dependency and like  ",Object(o.b)("strong",{parentName:"p"},"npm"),", ",Object(o.b)("strong",{parentName:"p"},"pip")," or ",Object(o.b)("strong",{parentName:"p"},"zypper/apt-get"),"  package manager tools to  install and uninstall artifacts allowing multiple versions of the very same artifact to  be installed."),Object(o.b)("p",null,Object(o.b)("strong",{parentName:"p"},Object(o.b)("inlineCode",{parentName:"strong"},"nuts"))," common verbs are:"),Object(o.b)("ul",null,Object(o.b)("li",{parentName:"ul"},Object(o.b)("inlineCode",{parentName:"li"},"exec"),"               : execute an artifact or a command"),Object(o.b)("li",{parentName:"ul"},Object(o.b)("inlineCode",{parentName:"li"},"install"),", ",Object(o.b)("inlineCode",{parentName:"li"},"uninstall")," : install/uninstall an artifact (using its fetched/deployed installer)"),Object(o.b)("li",{parentName:"ul"},Object(o.b)("inlineCode",{parentName:"li"},"deploy"),", ",Object(o.b)("inlineCode",{parentName:"li"},"undeploy"),"   : manage artifacts (artifact installers) on the local repositories"),Object(o.b)("li",{parentName:"ul"},Object(o.b)("inlineCode",{parentName:"li"},"update"),"             : update an artifact (using its fetched/deployed installer)"),Object(o.b)("li",{parentName:"ul"},Object(o.b)("inlineCode",{parentName:"li"},"fetch"),", ",Object(o.b)("inlineCode",{parentName:"li"},"push"),"        : download from, upload to remote repositories"),Object(o.b)("li",{parentName:"ul"},Object(o.b)("inlineCode",{parentName:"li"},"search"),"             : search for existing/installable artifacts"),Object(o.b)("li",{parentName:"ul"},Object(o.b)("inlineCode",{parentName:"li"},"welcome"),"            : a command that does nothing but bootstrapping ",Object(o.b)("strong",{parentName:"li"},Object(o.b)("inlineCode",{parentName:"strong"},"nuts"))," and showing a welcome message.")))}p.isMDXComponent=!0}}]);