---
title: How can I make my application "Nuts aware"
---


If by **```nuts```** aware you mean that you would download your application and run it using **```nuts```**, then you just need to create the application using maven and deploy your application to the public maven central.
Nothing really special is to be done from your side. You do not have to use plugins like 'maven-assembly-plugin' and 'maven-shade-plugin' to include your dependencies.
Or, you can also use NAF (**```nuts```** Application Framework) to make your application full featured "Nuts aware" application.
