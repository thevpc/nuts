bundles are statically included libraries as part of nuts-runtime.

nuts-runtime must have null to minimum dependencies and as such is should 
whenever possible use statically linked libraries.
In java the only way to do statically linked libraries is to copy the library and prefix it with 
nuts package (net.thevpc.nuts).

This package contains these statically libraries. Some of these libraries have been created for 
nuts itself (like ntalk and nanodb) and they are willing to be exported as a standalone library while
bundled here.

