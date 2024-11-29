This folder contains main/core nuts implementation

# nut-boot (lib)
    Nuts workspace bootstrapper Library. Responsible of downloading and creating/running nuts workspaces.
    Does no any dependency.

# nut-api (lib)
    Nuts Api for usage as a Core Library/Framework; Depends on nut-boot.
    Contains only core features of nuts. Does not for instance contain exotic implementation of collections or iterators etc...

# nut-lib (lib)
    Nuts Core Library for usage as a Library/Framework; This is the go-to if you want full fledged nuts as a library

# nut-runtime (lib)
    Nuts Implementation Library. Will be loaded dynamically by nuts-boot and linked to nuts-api.
    depends on

# nut (app)
    Nuts Application with minimum dependencies. Relies only on nut-boot. All required classes will be loaded on the fly.
    depends on nut-boot.
    will download and load nuts-api, nuts-lib and nuts-runtime

# nut-full (app)
    Nuts Application with all required dependencies statically linked. 
    No classes will be loaded (unless it is a community extension).
    Does no any dependency.
