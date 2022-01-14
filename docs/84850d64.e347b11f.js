(window.webpackJsonp=window.webpackJsonp||[]).push([[39],{119:function(e,t,n){"use strict";n.d(t,"a",(function(){return b}));var r=n(0),o=n.n(r);function a(e,t,n){return t in e?Object.defineProperty(e,t,{value:n,enumerable:!0,configurable:!0,writable:!0}):e[t]=n,e}function i(e,t){var n=Object.keys(e);if(Object.getOwnPropertySymbols){var r=Object.getOwnPropertySymbols(e);t&&(r=r.filter((function(t){return Object.getOwnPropertyDescriptor(e,t).enumerable}))),n.push.apply(n,r)}return n}function l(e){for(var t=1;t<arguments.length;t++){var n=null!=arguments[t]?arguments[t]:{};t%2?i(Object(n),!0).forEach((function(t){a(e,t,n[t])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(n)):i(Object(n)).forEach((function(t){Object.defineProperty(e,t,Object.getOwnPropertyDescriptor(n,t))}))}return e}function s(e,t){if(null==e)return{};var n,r,o=function(e,t){if(null==e)return{};var n,r,o={},a=Object.keys(e);for(r=0;r<a.length;r++)n=a[r],t.indexOf(n)>=0||(o[n]=e[n]);return o}(e,t);if(Object.getOwnPropertySymbols){var a=Object.getOwnPropertySymbols(e);for(r=0;r<a.length;r++)n=a[r],t.indexOf(n)>=0||Object.prototype.propertyIsEnumerable.call(e,n)&&(o[n]=e[n])}return o}var c=o.a.createContext({}),u=function(e){var t=o.a.useContext(c),n=t;return e&&(n="function"==typeof e?e(t):l(l({},t),e)),n},d={inlineCode:"code",wrapper:function(e){var t=e.children;return o.a.createElement(o.a.Fragment,{},t)}},p=o.a.forwardRef((function(e,t){var n=e.components,r=e.mdxType,a=e.originalType,i=e.parentName,c=s(e,["components","mdxType","originalType","parentName"]),p=u(n),b=r,m=p["".concat(i,".").concat(b)]||p[b]||d[b]||a;return n?o.a.createElement(m,l(l({ref:t},c),{},{components:n})):o.a.createElement(m,l({ref:t},c))}));function b(e,t){var n=arguments,r=t&&t.mdxType;if("string"==typeof e||r){var a=n.length,i=new Array(a);i[0]=p;var l={};for(var s in t)hasOwnProperty.call(t,s)&&(l[s]=t[s]);l.originalType=e,l.mdxType="string"==typeof e?e:r,i[1]=l;for(var c=2;c<a;c++)i[c]=n[c];return o.a.createElement.apply(null,i)}return o.a.createElement.apply(null,n)}p.displayName="MDXCreateElement"},94:function(e,t,n){"use strict";n.r(t),n.d(t,"frontMatter",(function(){return i})),n.d(t,"metadata",(function(){return l})),n.d(t,"rightToc",(function(){return s})),n.d(t,"default",(function(){return u}));var r=n(2),o=n(6),a=(n(0),n(119)),i={id:"troubleshooting",title:"Troubleshooting",sidebar_label:"Troubleshooting",order:5},l={unversionedId:"info/troubleshooting",id:"info/troubleshooting",isDocsHomePage:!1,title:"Troubleshooting",description:"Whenever installation fails, it is more likely there is a mis-configuration or invalid libraries bundles used. You may have to options",source:"@site/docs/info/troubleshooting.md",permalink:"/nuts/docs/info/troubleshooting",editUrl:"https://github.com/thevpc/nuts/edit/master/website/docs/info/troubleshooting.md",sidebar_label:"Troubleshooting",sidebar:"someSidebar",previous:{title:"Frequently Asked Questions",permalink:"/nuts/docs/info/faq"},next:{title:"Aliases, Imports & Launchers",permalink:"/nuts/docs/concepts/aliases"}},s=[{value:"recover mode",id:"recover-mode",children:[]},{value:"newer mode",id:"newer-mode",children:[]},{value:"reset mode",id:"reset-mode",children:[]},{value:"kill mode",id:"kill-mode",children:[]},{value:"After invoking reset mode",id:"after-invoking-reset-mode",children:[]}],c={rightToc:s};function u(e){var t=e.components,n=Object(o.a)(e,["components"]);return Object(a.a)("wrapper",Object(r.a)({},c,n,{components:t,mdxType:"MDXLayout"}),Object(a.a)("p",null,"Whenever installation fails, it is more likely there is a mis-configuration or invalid libraries bundles used. You may have to options\nto circumvent this which are two levels or workspace reinitialization."),Object(a.a)("h2",{id:"recover-mode"},"recover mode"),Object(a.a)("p",null,Object(a.a)("strong",{parentName:"p"},"recover mode")," will apply best efforts to correct configuration without losing them. It will delete all cached data and\nlibraries for them to be downloaded later and searches for a valid nuts installation binaries to run (it will actually\ndo a forced update). To run nuts in recover mode type :"),Object(a.a)("pre",null,Object(a.a)("code",Object(r.a)({parentName:"pre"},{}),"nuts -z\n")),Object(a.a)("h2",{id:"newer-mode"},"newer mode"),Object(a.a)("p",null,Object(a.a)("strong",{parentName:"p"},"newer mode")," will apply best efforts to reload cached files and libraries. to run nuts in 'newer mode' type:"),Object(a.a)("pre",null,Object(a.a)("code",Object(r.a)({parentName:"pre"},{}),"nuts -N\n")),Object(a.a)("h2",{id:"reset-mode"},"reset mode"),Object(a.a)("p",null,Object(a.a)("strong",{parentName:"p"},"reset mode")," will apply all efforts to correct configuration by, actually, ",Object(a.a)("strong",{parentName:"p"},"deleting")," them\n(and all of workspace files!!) to create a new fresh workspace. This is quite a radical action to run. Do not ever\ninvoke this unless your are really knowing what you are doing.\nTo run nuts in reset mode type :"),Object(a.a)("pre",null,Object(a.a)("code",Object(r.a)({parentName:"pre"},{}),"nuts -Z\n")),Object(a.a)("h2",{id:"kill-mode"},"kill mode"),Object(a.a)("p",null,Object(a.a)("strong",{parentName:"p"},"kill mode")," is a special variant of reset mode where workspace will not be recreated after deletion.\nThis can be achieved by using a combination of reset mode and --skip-boot (-Q) option. Do not ever\ninvoke it unless you are really knowing what you are doing. To run nuts in kill mode type :"),Object(a.a)("p",null,"To run nuts in kill mode type :"),Object(a.a)("pre",null,Object(a.a)("code",Object(r.a)({parentName:"pre"},{}),"nuts -ZQ\n")),Object(a.a)("h2",{id:"after-invoking-reset-mode"},"After invoking reset mode"),Object(a.a)("p",null,"After invoking reset mode, nuts shell launchers (installed by nuts settings) will not be available anymore.\nPATH environment will point (temporarily) to a non existing folder. You should use the jar based invocation at least once to reinstall these commands."),Object(a.a)("pre",null,Object(a.a)("code",Object(r.a)({parentName:"pre"},{}),"java -jar nuts-0.8.3.jar\n")))}u.isMDXComponent=!0}}]);