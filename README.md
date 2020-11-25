# nuts
Network Updatable Things Services
<pre>
     __        __    
  /\ \ \ _  __/ /______
 /  \/ / / / / __/ ___/
/ /\  / /_/ / /_(__  )
\_\ \/\__,_/\__/____/    version 0.8.1.0
</pre>

website : [https://thevpc.github.io/nuts](https://thevpc.github.io/nuts)

**nuts** is a Package manager for Java (and other things). It stands for **Network Updatable Things Services** tool. Think of it as **npm** for javascript or **pip** for python. But it lots more... It is a simple tool  for managing remote artifacts, installing these  artifacts to the current machine and executing such  artifacts on need. Each managed artifact  is also called a **nuts** which  is a **Network Updatable Thing Service** . Nuts artifacts are  stored  into repositories. A  **repository**  may be local for  storing local Nuts or remote for accessing  remote artifacts (good examples  are  remote maven  repositories). 

One manages a set of repositories called a **workspace**. Managed **nuts**  (artifacts)  have descriptors that depict dependencies between them. This dependency is seamlessly handled by  **nuts**  (tool) to resolve and download on-need dependencies over the wire. 

**nuts** is a swiss army knife tool as it acts like (and supports) **maven** build tool to have an abstract view of the the  artifacts dependency and like  **npm**, **pip** or **zypper/apt-get**  package manager tools to  install and uninstall artifacts allowing multiple versions of the very same artifact to  be installed.

What makes **nuts** very helpful is that it simplifies application deployments by not including dependencies in the release bundle. All dependencies will be downloaded at installation time. They also will be shared among all application which reduces storage space as well. Offline deployment is still supported though. Besides, multiple versions of the same application can be installed simultaneously.

