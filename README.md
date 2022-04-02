# CrawlerJME3 (LPV)

This prototype, written in Java with a modified jMonkeyEngine3, features procedurally generated building floorplans.
The realtime global illumination is implemented with <b>Light Propagation Volumes</b>,
described in <a href="https://advances.realtimerendering.com/s2009/Light_Propagation_Volumes.pdf"> this 2009 paper from Crytek</a>.

The LPV is not cascaded, but simply envelopes each floor with a uniform grid.
For better performance each step of the LPV algorithm is spread out into multiple frames.

## A procedurally generated floorplan
![Procgen Floorplan](https://github.com/ArthurKnauer/CrawlerJME3/blob/main/floorplan.png?raw=true)

## LPV on
![LPV_on](https://github.com/ArthurKnauer/CrawlerJME3/blob/main/LPV_on.png?raw=true)

## LPV off
![LPV_off](https://github.com/ArthurKnauer/CrawlerJME3/blob/main/LPV_off.png?raw=true)
