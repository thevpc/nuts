(window.webpackJsonp=window.webpackJsonp||[]).push([[45],{116:function(e,t,a){"use strict";a.d(t,"a",(function(){return o})),a.d(t,"b",(function(){return m}));var n=a(0),l=a.n(n);function r(e,t,a){return t in e?Object.defineProperty(e,t,{value:a,enumerable:!0,configurable:!0,writable:!0}):e[t]=a,e}function b(e,t){var a=Object.keys(e);if(Object.getOwnPropertySymbols){var n=Object.getOwnPropertySymbols(e);t&&(n=n.filter((function(t){return Object.getOwnPropertyDescriptor(e,t).enumerable}))),a.push.apply(a,n)}return a}function c(e){for(var t=1;t<arguments.length;t++){var a=null!=arguments[t]?arguments[t]:{};t%2?b(Object(a),!0).forEach((function(t){r(e,t,a[t])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(a)):b(Object(a)).forEach((function(t){Object.defineProperty(e,t,Object.getOwnPropertyDescriptor(a,t))}))}return e}function u(e,t){if(null==e)return{};var a,n,l=function(e,t){if(null==e)return{};var a,n,l={},r=Object.keys(e);for(n=0;n<r.length;n++)a=r[n],t.indexOf(a)>=0||(l[a]=e[a]);return l}(e,t);if(Object.getOwnPropertySymbols){var r=Object.getOwnPropertySymbols(e);for(n=0;n<r.length;n++)a=r[n],t.indexOf(a)>=0||Object.prototype.propertyIsEnumerable.call(e,a)&&(l[a]=e[a])}return l}var i=l.a.createContext({}),s=function(e){var t=l.a.useContext(i),a=t;return e&&(a="function"==typeof e?e(t):c(c({},t),e)),a},o=function(e){var t=s(e.components);return l.a.createElement(i.Provider,{value:t},e.children)},j={inlineCode:"code",wrapper:function(e){var t=e.children;return l.a.createElement(l.a.Fragment,{},t)}},p=l.a.forwardRef((function(e,t){var a=e.components,n=e.mdxType,r=e.originalType,b=e.parentName,i=u(e,["components","mdxType","originalType","parentName"]),o=s(a),p=n,m=o["".concat(b,".").concat(p)]||o[p]||j[p]||r;return a?l.a.createElement(m,c(c({ref:t},i),{},{components:a})):l.a.createElement(m,c({ref:t},i))}));function m(e,t){var a=arguments,n=t&&t.mdxType;if("string"==typeof e||n){var r=a.length,b=new Array(r);b[0]=p;var c={};for(var u in t)hasOwnProperty.call(t,u)&&(c[u]=t[u]);c.originalType=e,c.mdxType="string"==typeof e?e:n,b[1]=c;for(var i=2;i<r;i++)b[i]=a[i];return l.a.createElement.apply(null,b)}return l.a.createElement.apply(null,a)}p.displayName="MDXCreateElement"},99:function(e,t,a){"use strict";a.r(t),a.d(t,"frontMatter",(function(){return b})),a.d(t,"metadata",(function(){return c})),a.d(t,"rightToc",(function(){return u})),a.d(t,"default",(function(){return s}));var n=a(2),l=a(6),r=(a(0),a(116)),b={id:"javadoc_Elements",title:"Elements",sidebar_label:"Elements"},c={unversionedId:"javadocs/javadoc_Elements",id:"javadocs/javadoc_Elements",isDocsHomePage:!1,title:"Elements",description:"\u2615 NutsArrayElement",source:"@site/docs/javadocs/Elements.md",permalink:"/nuts/docs/javadocs/javadoc_Elements",editUrl:"https://github.com/facebook/docusaurus/edit/master/website/docs/javadocs/Elements.md",sidebar_label:"Elements"},u=[{value:"\u2615 NutsArrayElement",id:"-nutsarrayelement",children:[{value:"\u2699 Instance Methods",id:"-instance-methods",children:[]}]},{value:"\u2615 NutsArrayElementBuilder",id:"-nutsarrayelementbuilder",children:[{value:"\u2699 Instance Methods",id:"-instance-methods-1",children:[]}]},{value:"\u2615 NutsElement",id:"-nutselement",children:[{value:"\u2699 Instance Methods",id:"-instance-methods-2",children:[]}]},{value:"\u2615 NutsElementBuilder",id:"-nutselementbuilder",children:[{value:"\u2699 Instance Methods",id:"-instance-methods-3",children:[]}]},{value:"\u2615 NutsElementPath",id:"-nutselementpath",children:[{value:"\u2699 Instance Methods",id:"-instance-methods-4",children:[]}]},{value:"\u2615 NutsObjectElement",id:"-nutsobjectelement",children:[{value:"\u2699 Instance Methods",id:"-instance-methods-5",children:[]}]},{value:"\u2615 NutsPrimitiveElement",id:"-nutsprimitiveelement",children:[{value:"\ud83c\udf9b Instance Properties",id:"-instance-properties",children:[]}]}],i={rightToc:u};function s(e){var t=e.components,a=Object(l.a)(e,["components"]);return Object(r.b)("wrapper",Object(n.a)({},i,a,{components:t,mdxType:"MDXLayout"}),Object(r.b)("h2",{id:"-nutsarrayelement"},"\u2615 NutsArrayElement"),Object(r.b)("pre",null,Object(r.b)("code",Object(n.a)({parentName:"pre"},{className:"language-java"}),"public  net.vpc.app.nuts.NutsArrayElement\n")),Object(r.b)("p",null,"Array implementation of Nuts Element type.\nNuts Element types are generic JSON like parsable objects."),Object(r.b)("h3",{id:"-instance-methods"},"\u2699 Instance Methods"),Object(r.b)("h4",{id:"-children"},"\u2699 children()"),Object(r.b)("p",null,"array items"),Object(r.b)("pre",null,Object(r.b)("code",Object(n.a)({parentName:"pre"},{className:"language-java"}),"Collection\\<NutsElement\\> children()\n")),Object(r.b)("h4",{id:"-getindex"},"\u2699 get(index)"),Object(r.b)("p",null,"element at index"),Object(r.b)("pre",null,Object(r.b)("code",Object(n.a)({parentName:"pre"},{className:"language-java"}),"NutsElement get(int index)\n")),Object(r.b)("p",null,"index"),Object(r.b)("h4",{id:"-size"},"\u2699 size()"),Object(r.b)("p",null,"element count"),Object(r.b)("pre",null,Object(r.b)("code",Object(n.a)({parentName:"pre"},{className:"language-java"}),"int size()\n")),Object(r.b)("h2",{id:"-nutsarrayelementbuilder"},"\u2615 NutsArrayElementBuilder"),Object(r.b)("pre",null,Object(r.b)("code",Object(n.a)({parentName:"pre"},{className:"language-java"}),"public  net.vpc.app.nuts.NutsArrayElementBuilder\n")),Object(r.b)("p",null,"Array element Builder is a mutable NutsArrayElement that helps\nmanipulating arrays."),Object(r.b)("h3",{id:"-instance-methods-1"},"\u2699 Instance Methods"),Object(r.b)("h4",{id:"-addelement"},"\u2699 add(element)"),Object(r.b)("p",null,"add new element to the end of the array."),Object(r.b)("pre",null,Object(r.b)("code",Object(n.a)({parentName:"pre"},{className:"language-java"}),"NutsArrayElementBuilder add(NutsElement element)\n")),Object(r.b)("p",null,"element to add, should no be null"),Object(r.b)("h4",{id:"-addallvalue"},"\u2699 addAll(value)"),Object(r.b)("p",null,"all all elements in the given array"),Object(r.b)("pre",null,Object(r.b)("code",Object(n.a)({parentName:"pre"},{className:"language-java"}),"NutsArrayElementBuilder addAll(NutsArrayElement value)\n")),Object(r.b)("p",null,"value"),Object(r.b)("h4",{id:"-addallvalue-1"},"\u2699 addAll(value)"),Object(r.b)("p",null,"all all elements in the given array builder"),Object(r.b)("pre",null,Object(r.b)("code",Object(n.a)({parentName:"pre"},{className:"language-java"}),"NutsArrayElementBuilder addAll(NutsArrayElementBuilder value)\n")),Object(r.b)("p",null,"value"),Object(r.b)("h4",{id:"-build"},"\u2699 build()"),Object(r.b)("p",null,"create array with this instance elements"),Object(r.b)("pre",null,Object(r.b)("code",Object(n.a)({parentName:"pre"},{className:"language-java"}),"NutsArrayElement build()\n")),Object(r.b)("h4",{id:"-children-1"},"\u2699 children()"),Object(r.b)("p",null,"array items"),Object(r.b)("pre",null,Object(r.b)("code",Object(n.a)({parentName:"pre"},{className:"language-java"}),"List\\<NutsElement\\> children()\n")),Object(r.b)("h4",{id:"-clear"},"\u2699 clear()"),Object(r.b)("p",null,"remove all elements from this array."),Object(r.b)("pre",null,Object(r.b)("code",Object(n.a)({parentName:"pre"},{className:"language-java"}),"NutsArrayElementBuilder clear()\n")),Object(r.b)("h4",{id:"-getindex-1"},"\u2699 get(index)"),Object(r.b)("p",null,"element at index"),Object(r.b)("pre",null,Object(r.b)("code",Object(n.a)({parentName:"pre"},{className:"language-java"}),"NutsElement get(int index)\n")),Object(r.b)("p",null,"index"),Object(r.b)("h4",{id:"-insertindex-element"},"\u2699 insert(index, element)"),Object(r.b)("p",null,"insert new element at the given index."),Object(r.b)("pre",null,Object(r.b)("code",Object(n.a)({parentName:"pre"},{className:"language-java"}),"NutsArrayElementBuilder insert(int index, NutsElement element)\n")),Object(r.b)("p",null,"index to insert into\nelement to add, should no be null"),Object(r.b)("h4",{id:"-removeindex"},"\u2699 remove(index)"),Object(r.b)("p",null,"add new element to the end of the array."),Object(r.b)("pre",null,Object(r.b)("code",Object(n.a)({parentName:"pre"},{className:"language-java"}),"NutsArrayElementBuilder remove(int index)\n")),Object(r.b)("p",null,"index to remove"),Object(r.b)("h4",{id:"-setother"},"\u2699 set(other)"),Object(r.b)("p",null,"reset this instance with the given array"),Object(r.b)("pre",null,Object(r.b)("code",Object(n.a)({parentName:"pre"},{className:"language-java"}),"NutsArrayElementBuilder set(NutsArrayElementBuilder other)\n")),Object(r.b)("p",null,"array"),Object(r.b)("h4",{id:"-setother-1"},"\u2699 set(other)"),Object(r.b)("p",null,"reset this instance with the given array"),Object(r.b)("pre",null,Object(r.b)("code",Object(n.a)({parentName:"pre"},{className:"language-java"}),"NutsArrayElementBuilder set(NutsArrayElement other)\n")),Object(r.b)("p",null,"array builder"),Object(r.b)("h4",{id:"-setindex-element"},"\u2699 set(index, element)"),Object(r.b)("p",null,"update element at the given index."),Object(r.b)("pre",null,Object(r.b)("code",Object(n.a)({parentName:"pre"},{className:"language-java"}),"NutsArrayElementBuilder set(int index, NutsElement element)\n")),Object(r.b)("p",null,"index to update\nelement to add, should no be null"),Object(r.b)("h4",{id:"-size-1"},"\u2699 size()"),Object(r.b)("p",null,"element count"),Object(r.b)("pre",null,Object(r.b)("code",Object(n.a)({parentName:"pre"},{className:"language-java"}),"int size()\n")),Object(r.b)("h2",{id:"-nutselement"},"\u2615 NutsElement"),Object(r.b)("pre",null,Object(r.b)("code",Object(n.a)({parentName:"pre"},{className:"language-java"}),"public  net.vpc.app.nuts.NutsElement\n")),Object(r.b)("p",null,"Nuts Element types are generic JSON like parsable objects."),Object(r.b)("h3",{id:"-instance-methods-2"},"\u2699 Instance Methods"),Object(r.b)("h4",{id:"-array"},"\u2699 array()"),Object(r.b)("p",null,"convert this element to ",Object(r.b)("inlineCode",{parentName:"p"}," NutsArrayElement")," or throw ClassCastException"),Object(r.b)("pre",null,Object(r.b)("code",Object(n.a)({parentName:"pre"},{className:"language-java"}),"NutsArrayElement array()\n")),Object(r.b)("h4",{id:"-object"},"\u2699 object()"),Object(r.b)("p",null,"convert this element to ",Object(r.b)("inlineCode",{parentName:"p"}," NutsObjectElement")," or throw ClassCastException"),Object(r.b)("pre",null,Object(r.b)("code",Object(n.a)({parentName:"pre"},{className:"language-java"}),"NutsObjectElement object()\n")),Object(r.b)("h4",{id:"-primitive"},"\u2699 primitive()"),Object(r.b)("p",null,"convert this element to ",Object(r.b)("inlineCode",{parentName:"p"}," NutsPrimitiveElement")," or throw ClassCastException"),Object(r.b)("pre",null,Object(r.b)("code",Object(n.a)({parentName:"pre"},{className:"language-java"}),"NutsPrimitiveElement primitive()\n")),Object(r.b)("h4",{id:"-type"},"\u2699 type()"),Object(r.b)("p",null,"element type"),Object(r.b)("pre",null,Object(r.b)("code",Object(n.a)({parentName:"pre"},{className:"language-java"}),"NutsElementType type()\n")),Object(r.b)("h2",{id:"-nutselementbuilder"},"\u2615 NutsElementBuilder"),Object(r.b)("pre",null,Object(r.b)("code",Object(n.a)({parentName:"pre"},{className:"language-java"}),"public  net.vpc.app.nuts.NutsElementBuilder\n")),Object(r.b)("p",null,"Nuts Element builder that helps creating element instances."),Object(r.b)("h3",{id:"-instance-methods-3"},"\u2699 Instance Methods"),Object(r.b)("h4",{id:"-forarray"},"\u2699 forArray()"),Object(r.b)("p",null,"create array element builder (mutable)"),Object(r.b)("pre",null,Object(r.b)("code",Object(n.a)({parentName:"pre"},{className:"language-java"}),"NutsArrayElementBuilder forArray()\n")),Object(r.b)("h4",{id:"-forbooleanvalue"},"\u2699 forBoolean(value)"),Object(r.b)("p",null,"create primitive boolean element"),Object(r.b)("pre",null,Object(r.b)("code",Object(n.a)({parentName:"pre"},{className:"language-java"}),"NutsPrimitiveElement forBoolean(String value)\n")),Object(r.b)("p",null,"value"),Object(r.b)("h4",{id:"-forbooleanvalue-1"},"\u2699 forBoolean(value)"),Object(r.b)("p",null,"create primitive boolean element"),Object(r.b)("pre",null,Object(r.b)("code",Object(n.a)({parentName:"pre"},{className:"language-java"}),"NutsPrimitiveElement forBoolean(boolean value)\n")),Object(r.b)("p",null,"value"),Object(r.b)("h4",{id:"-fordatevalue"},"\u2699 forDate(value)"),Object(r.b)("p",null,"create primitive date element"),Object(r.b)("pre",null,Object(r.b)("code",Object(n.a)({parentName:"pre"},{className:"language-java"}),"NutsPrimitiveElement forDate(Date value)\n")),Object(r.b)("p",null,"value"),Object(r.b)("h4",{id:"-fordatevalue-1"},"\u2699 forDate(value)"),Object(r.b)("p",null,"create primitive date element"),Object(r.b)("pre",null,Object(r.b)("code",Object(n.a)({parentName:"pre"},{className:"language-java"}),"NutsPrimitiveElement forDate(Instant value)\n")),Object(r.b)("p",null,"value"),Object(r.b)("h4",{id:"-fordatevalue-2"},"\u2699 forDate(value)"),Object(r.b)("p",null,"create primitive date element"),Object(r.b)("pre",null,Object(r.b)("code",Object(n.a)({parentName:"pre"},{className:"language-java"}),"NutsPrimitiveElement forDate(String value)\n")),Object(r.b)("p",null,"value"),Object(r.b)("h4",{id:"-fornull"},"\u2699 forNull()"),Object(r.b)("p",null,"create primitive null element"),Object(r.b)("pre",null,Object(r.b)("code",Object(n.a)({parentName:"pre"},{className:"language-java"}),"NutsPrimitiveElement forNull()\n")),Object(r.b)("h4",{id:"-fornumbervalue"},"\u2699 forNumber(value)"),Object(r.b)("p",null,"create primitive number element"),Object(r.b)("pre",null,Object(r.b)("code",Object(n.a)({parentName:"pre"},{className:"language-java"}),"NutsPrimitiveElement forNumber(Number value)\n")),Object(r.b)("p",null,"value"),Object(r.b)("h4",{id:"-fornumbervalue-1"},"\u2699 forNumber(value)"),Object(r.b)("p",null,"create primitive number element"),Object(r.b)("pre",null,Object(r.b)("code",Object(n.a)({parentName:"pre"},{className:"language-java"}),"NutsPrimitiveElement forNumber(String value)\n")),Object(r.b)("p",null,"value"),Object(r.b)("h4",{id:"-forobject"},"\u2699 forObject()"),Object(r.b)("p",null,"create object element builder (mutable)"),Object(r.b)("pre",null,Object(r.b)("code",Object(n.a)({parentName:"pre"},{className:"language-java"}),"NutsObjectElementBuilder forObject()\n")),Object(r.b)("h4",{id:"-forstringvalue"},"\u2699 forString(value)"),Object(r.b)("p",null,"create primitive string element"),Object(r.b)("pre",null,Object(r.b)("code",Object(n.a)({parentName:"pre"},{className:"language-java"}),"NutsPrimitiveElement forString(String value)\n")),Object(r.b)("p",null,"value"),Object(r.b)("h2",{id:"-nutselementpath"},"\u2615 NutsElementPath"),Object(r.b)("pre",null,Object(r.b)("code",Object(n.a)({parentName:"pre"},{className:"language-java"}),"public  net.vpc.app.nuts.NutsElementPath\n")),Object(r.b)("p",null,"Element XPath like filter"),Object(r.b)("h3",{id:"-instance-methods-4"},"\u2699 Instance Methods"),Object(r.b)("h4",{id:"-filterelement"},"\u2699 filter(element)"),Object(r.b)("p",null,"filter element to a valid children list"),Object(r.b)("pre",null,Object(r.b)("code",Object(n.a)({parentName:"pre"},{className:"language-java"}),"List\\<NutsElement\\> filter(NutsElement element)\n")),Object(r.b)("p",null,"element to filter"),Object(r.b)("h4",{id:"-filterelements"},"\u2699 filter(elements)"),Object(r.b)("p",null,"filter elements to a valid children list"),Object(r.b)("pre",null,Object(r.b)("code",Object(n.a)({parentName:"pre"},{className:"language-java"}),"List\\<NutsElement\\> filter(List\\<NutsElement\\> elements)\n")),Object(r.b)("p",null,"elements to filter"),Object(r.b)("h2",{id:"-nutsobjectelement"},"\u2615 NutsObjectElement"),Object(r.b)("pre",null,Object(r.b)("code",Object(n.a)({parentName:"pre"},{className:"language-java"}),"public  net.vpc.app.nuts.NutsObjectElement\n")),Object(r.b)("p",null,"Object implementation of Nuts Element type.\nNuts Element types are generic JSON like parsable objects."),Object(r.b)("h3",{id:"-instance-methods-5"},"\u2699 Instance Methods"),Object(r.b)("h4",{id:"-children-2"},"\u2699 children()"),Object(r.b)("p",null,"object (key,value) attributes"),Object(r.b)("pre",null,Object(r.b)("code",Object(n.a)({parentName:"pre"},{className:"language-java"}),"Collection\\<NutsNamedElement\\> children()\n")),Object(r.b)("h4",{id:"-getname"},"\u2699 get(name)"),Object(r.b)("p",null,"return value for name or null.\nIf multiple values are available return any of them."),Object(r.b)("pre",null,Object(r.b)("code",Object(n.a)({parentName:"pre"},{className:"language-java"}),"NutsElement get(String name)\n")),Object(r.b)("p",null,"key name"),Object(r.b)("h4",{id:"-size-2"},"\u2699 size()"),Object(r.b)("p",null,"element count"),Object(r.b)("pre",null,Object(r.b)("code",Object(n.a)({parentName:"pre"},{className:"language-java"}),"int size()\n")),Object(r.b)("h2",{id:"-nutsprimitiveelement"},"\u2615 NutsPrimitiveElement"),Object(r.b)("pre",null,Object(r.b)("code",Object(n.a)({parentName:"pre"},{className:"language-java"}),"public  net.vpc.app.nuts.NutsPrimitiveElement\n")),Object(r.b)("p",null,"primitive values implementation of Nuts Element type. Nuts Element types are\ngeneric JSON like parsable objects."),Object(r.b)("h3",{id:"-instance-properties"},"\ud83c\udf9b Instance Properties"),Object(r.b)("h4",{id:"-boolean"},"\ud83d\udcc4\ud83c\udf9b boolean"),Object(r.b)("p",null,"value as any java Boolean. Best effort is applied to convert to this\ntype."),Object(r.b)("pre",null,Object(r.b)("code",Object(n.a)({parentName:"pre"},{className:"language-java"}),"[read-only]  boolean boolean\n boolean getBoolean()\n")),Object(r.b)("h4",{id:"-date"},"\ud83d\udcc4\ud83c\udf9b date"),Object(r.b)("p",null,"value as any java date. Best effort is applied to convert to this type."),Object(r.b)("pre",null,Object(r.b)("code",Object(n.a)({parentName:"pre"},{className:"language-java"}),"[read-only]  Instant date\n Instant getDate()\n")),Object(r.b)("h4",{id:"-double"},"\ud83d\udcc4\ud83c\udf9b double"),Object(r.b)("p",null,"true if the value is or can be converted to double"),Object(r.b)("pre",null,Object(r.b)("code",Object(n.a)({parentName:"pre"},{className:"language-java"}),"[read-only]  boolean double\n boolean isDouble()\n")),Object(r.b)("h4",{id:"-float"},"\ud83d\udcc4\ud83c\udf9b float"),Object(r.b)("p",null,"true if the value is or can be converted to float"),Object(r.b)("pre",null,Object(r.b)("code",Object(n.a)({parentName:"pre"},{className:"language-java"}),"[read-only]  boolean float\n boolean isFloat()\n")),Object(r.b)("h4",{id:"-int"},"\ud83d\udcc4\ud83c\udf9b int"),Object(r.b)("p",null,"true if the value is or can be converted to int."),Object(r.b)("pre",null,Object(r.b)("code",Object(n.a)({parentName:"pre"},{className:"language-java"}),"[read-only]  boolean int\n boolean isInt()\n")),Object(r.b)("h4",{id:"-long"},"\ud83d\udcc4\ud83c\udf9b long"),Object(r.b)("p",null,"true if the value is or can be converted to long."),Object(r.b)("pre",null,Object(r.b)("code",Object(n.a)({parentName:"pre"},{className:"language-java"}),"[read-only]  boolean long\n boolean isLong()\n")),Object(r.b)("h4",{id:"-null"},"\ud83d\udcc4\ud83c\udf9b null"),Object(r.b)("p",null,"true if the value is null (in which case, the type should be NULL)"),Object(r.b)("pre",null,Object(r.b)("code",Object(n.a)({parentName:"pre"},{className:"language-java"}),"[read-only]  boolean null\n boolean isNull()\n")),Object(r.b)("h4",{id:"-number"},"\ud83d\udcc4\ud83c\udf9b number"),Object(r.b)("p",null,"value as any java Number. Best effort is applied to convert to this type."),Object(r.b)("pre",null,Object(r.b)("code",Object(n.a)({parentName:"pre"},{className:"language-java"}),"[read-only]  Number number\n Number getNumber()\n")),Object(r.b)("h4",{id:"-string"},"\ud83d\udcc4\ud83c\udf9b string"),Object(r.b)("p",null,"value as any java string. Best effort is applied to convert to this type."),Object(r.b)("pre",null,Object(r.b)("code",Object(n.a)({parentName:"pre"},{className:"language-java"}),"[read-only]  String string\n String getString()\n")),Object(r.b)("h4",{id:"-value"},"\ud83d\udcc4\ud83c\udf9b value"),Object(r.b)("p",null,"value as any java Object"),Object(r.b)("pre",null,Object(r.b)("code",Object(n.a)({parentName:"pre"},{className:"language-java"}),"[read-only]  Object value\n Object getValue()\n")))}s.isMDXComponent=!0}}]);