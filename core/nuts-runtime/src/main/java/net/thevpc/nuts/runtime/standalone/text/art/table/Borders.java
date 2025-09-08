package net.thevpc.nuts.runtime.standalone.text.art.table;

import net.thevpc.nuts.runtime.standalone.text.art.region.NTextRegion;

class Borders {
    int startVerticalBorderSize = 1;
    int startHorizontalBorderSize = 1;

    NTextRegion topLeft; //╭
    NTextRegion topCenter; // ─
    NTextRegion topRight;

    NTextRegion middleLeft;
    NTextRegion middleRight;

    NTextRegion bottomLeft;
    NTextRegion bottomCenter;
    NTextRegion bottomRight;
}
