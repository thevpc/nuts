cd ~/programs/nuts-bundles/
nuts-dev -ZyS --!switch

nuts-dev bundle -y nuts-app --lib=nsh,nuts-ssh,nuts-term,nuts-runtime

rm -Rf nuts-0.8.5/*
java -jar nuts-0.8.5-bundle.jar


#nuts-dev bundle -y noapi
#nuts-dev bundle -y nnet
#nuts-dev bundle -y kifkif
#nuts-dev bundle -y pnote
#nuts-dev bundle -y nsite
#nuts-dev bundle -y njob
#nuts-dev bundle -y net.thevpc.ndoc:ndoc-viewer
#nuts-dev bundle -y net.thevpc.ndb:ndb-cmd net.thevpc.ndb:ndb-full net.thevpc.ndb:ndb-import net.thevpc.ndb:ndb-export
#nuts-dev bundle -y net.thevpc.nops:nops --lib=com.cts.ops:cts-ops#1.0,com.cts.ops:cts-protos#1.0,com.cts.ops:icon-misa-ops#1.0,com.cts.ops:icon-veoni-ops#1.0,nsh,nuts-ssh,net.thevpc.nops:nops-desktop-lib,com.ops:my-ops
#
#
